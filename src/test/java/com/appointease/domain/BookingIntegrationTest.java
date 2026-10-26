package com.appointease.domain;

import com.appointease.application.*;
import com.appointease.application.event.*;
import com.appointease.application.port.*;
import com.appointease.domain.addon.*;
import com.appointease.domain.creation.*;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.exception.*;
import com.appointease.domain.mode.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.ConsultationAppointment;
import com.appointease.domain.appointment.ServiceAppointment;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import com.appointease.infrastructure.calendar.*;
import com.appointease.infrastructure.memory.InMemoryAppointmentRepository;
import com.appointease.infrastructure.notification.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingIntegrationTest {
    private final AtomicReference<Instant> now = new AtomicReference<>(START.minusSeconds(86400));
    private final Clock clock = new Clock() {
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        public Instant instant() { return now.get(); }
    };
    private final DomainTime time = new DomainTime(clock);
    private final AvailabilityStrategy standard = new StandardAvailabilityStrategy();
    private final AppointmentRepository repo = new InMemoryAppointmentRepository(List.of(customer()), List.of(provider()),
            List.of(service()), clock, time.zone());
    private final AppointmentEventPublisher publisher = new AppointmentEventPublisher();
    private final NotificationListener notifications = new NotificationListener();
    private final AuditListener audit = new AuditListener();
    private final SimulatedExternalCalendarClient client = new SimulatedExternalCalendarClient();
    private final CommittedEventDispatcher dispatcher = new CommittedEventDispatcher(repo, publisher, new ExternalCalendarAdapter(client));
    private final BookingFacade facade = facade(dispatcher);
    private BookingFacade facade(CommittedEventDispatcher delivery) {
        return new BookingFacade(delivery, Map.of(AppointmentKind.CONSULTATION, new ConsultationAppointmentCreator(),
                AppointmentKind.SERVICE, new ServiceAppointmentCreator()), time);
    }
    private BookingRequest request(String id, Instant start) {
        return new BookingRequest(new AppointmentId(id), customer().id(), provider().id(), service().id(), start,
                new OnlineModeFactory("room"), new ReminderServiceDecorator(new PriorityBookingDecorator(new BaseAppointmentService())), standard);
    }
    private BookingResult book(String id) { return facade.book("book-" + id, request(id, START)); }

    @Test void allNinePatternsIntegratedThroughCompleteLifecycle() {
        publisher.subscribe(notifications); publisher.subscribe(audit);
        var request = request("nine", START);
        assertTrue(facade.available(request));
        assertEquals(List.of(provider()), facade.providers()); assertEquals(List.of(service()), facade.services());
        var booked = facade.book("book", request);
        var a = booked.appointment();
        assertInstanceOf(ConsultationAppointment.class, a); // Factory Method product
        assertEquals(AppointmentMode.ONLINE, a.mode()); // Abstract Factory family, validated by Builder
        assertEquals(new Money(new java.math.BigDecimal("52")), a.totalFee()); // Decorator
        assertEquals(2, a.options().additionalFees().size());
        assertEquals("PUT", client.entries().get("nine").operation()); // Adapter
        assertFalse(facade.available(request("overlap", START))); // Strategy + hard rules
        facade.confirm("confirm", a.id()); // State
        var moved = facade.reschedule("move", a.id(), START.plusSeconds(3600), new PreparationBufferAvailabilityStrategy());
        assertEquals(AppointmentStatus.CONFIRMED, moved.appointment().status());
        assertEquals(Duration.ofMinutes(15), repo.reservations().getFirst().preparationBuffer());
        now.set(moved.appointment().slot().end());
        var complete = facade.complete("complete", a.id());
        assertEquals(AppointmentStatus.COMPLETED, complete.appointment().status());
        assertEquals(a.id(), complete.appointment().id());
        assertSame(a.modeDetails(), complete.appointment().modeDetails());
        assertSame(a.enhancements(), complete.appointment().enhancements());
        assertInstanceOf(ConsultationAppointment.class, complete.appointment());
        assertEquals(List.of(AppointmentEvent.Type.RESERVED, AppointmentEvent.Type.CONFIRMED,
                AppointmentEvent.Type.RESCHEDULED, AppointmentEvent.Type.COMPLETED), repo.events().stream().map(AppointmentEvent::type).toList());
        assertEquals(repo.events(), notifications.notifications()); assertEquals(repo.events(), audit.history()); // Observer
        assertEquals(List.of(1L, 2L, 3L, 4L), repo.events().stream().map(AppointmentEvent::revision).toList());
        assertEquals("DONE", client.entries().get("nine").operation());
        assertEquals(4, client.appliedCount());
        assertEquals(List.of(complete.appointment()), facade.history(customer().id()));
        assertEquals(complete.appointment(), facade.details(a.id()).orElseThrow());
        for (int i = 1; i < repo.events().size(); i++) assertSame(repo.events().get(i - 1).after(), repo.events().get(i).before());
    }
    @Test void cancellationReleasesCapacityAndRetainsEventHistory() {
        var a = book("one").appointment(); facade.cancel("cancel", a.id());
        book("two");
        assertEquals(AppointmentStatus.CANCELLED, facade.details(a.id()).orElseThrow().status());
        assertEquals(2, facade.events(a.id()).size());
        assertEquals("DELETE", client.entries().get("one").operation());
        assertEquals(2, facade.history(customer().id()).size());
    }
    @Test void observerFailureIsolatedAndWarningsVisible() {
        publisher.subscribe(e -> { throw new IllegalStateException("simulated notification failure"); });
        publisher.subscribe(audit);
        var result = book("one");
        assertEquals(1, result.warnings().size()); assertEquals(1, audit.history().size());
        assertTrue(facade.details(result.appointment().id()).isPresent()); assertEquals(1, client.appliedCount());
    }
    @Test void failedCommandsDoNotRecordOrDeliverEvents() {
        book("one"); var events = repo.events();
        assertThrows(SlotUnavailableException.class, () -> book("two"));
        assertThrows(RepositoryException.class, () -> facade.book("duplicate-id", request("one", START)));
        assertThrows(InvalidTransitionException.class, () -> facade.complete("early", new AppointmentId("one")));
        assertThrows(RepositoryException.class, () -> facade.confirm("missing", new AppointmentId("missing")));
        assertEquals(events, repo.events()); assertEquals(1, client.appliedCount());
    }
    @Test void failedReschedulePreservesOriginalRevisionAndSlot() {
        var a = book("one").appointment();
        facade.book("two", request("two", START.plusSeconds(3600)));
        assertThrows(SlotUnavailableException.class, () -> facade.reschedule("move", a.id(), START.plusSeconds(3600), standard));
        assertSame(a, facade.details(a.id()).orElseThrow()); assertEquals(1, facade.events(a.id()).size());
    }
    @Test void commandRetriesAndDeliveryReplayDoNotDuplicateEvents() {
        var req = request("one", START);
        var first = facade.book("book", req);
        assertSame(first, facade.book("book", req));
        var moved = facade.reschedule("move", req.id(), START.plusSeconds(3600), standard);
        assertSame(moved, facade.reschedule("move", req.id(), START.plusSeconds(3600), standard));
        var confirmed = facade.confirm("confirm", req.id());
        assertSame(confirmed, facade.confirm("confirm", req.id()));
        assertSame(first, facade.book("book", req)); // Original receipt, not current state
        assertThrows(IllegalArgumentException.class, () -> facade.cancel("confirm", req.id()));
        dispatcher.dispatchPending();
        repo.events().forEach(e -> { publisher.publish(e); new ExternalCalendarAdapter(client).synchronize(e); });
        assertEquals(3, repo.events().size()); assertEquals(3, client.appliedCount());
    }
    @ParameterizedTest @ValueSource(ints = {400, 409, 503, 599})
    void calendarFailureIsWarningAfterCommit(int code) {
        var bad = new SimulatedExternalCalendarClient(Set.of(), code);
        var f = facade(new CommittedEventDispatcher(repo, publisher, new ExternalCalendarAdapter(bad)));
        var result = f.book("one", request("one", START));
        assertEquals(1, result.warnings().size());
        var error = assertInstanceOf(CalendarSyncException.class, result.warnings().getFirst().cause());
        assertEquals(code == 400 ? CalendarSyncException.Reason.INVALID_TIMESTAMP : code == 409
                ? CalendarSyncException.Reason.CONFLICT : CalendarSyncException.Reason.UNAVAILABLE, error.reason());
        assertEquals(code, assertInstanceOf(LegacyCalendarException.class, error.getCause()).code());
        assertTrue(f.details(result.appointment().id()).isPresent()); assertEquals(1, repo.events().size());
    }
    @Test void explicitExternalConflictPolicyKeepsLocalReservationAuthoritative() {
        var bad = new SimulatedExternalCalendarClient(Set.of("one"), 0);
        var f = facade(new CommittedEventDispatcher(repo, publisher, new ExternalCalendarAdapter(bad)));
        assertTrue(f.available(request("one", START)));
        assertEquals(CalendarSyncException.Reason.CONFLICT,
                ((CalendarSyncException) f.book("one", request("one", START)).warnings().getFirst().cause()).reason());
        assertFalse(f.available(request("two", START))); assertTrue(bad.entries().isEmpty());
    }
    @Test void listenersRunOutsideRepositoryMutationLock() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            publisher.subscribe(e -> {
                try { assertEquals(1, pool.submit(() -> repo.events().size()).get(3, TimeUnit.SECONDS)); }
                catch (Exception ex) { throw new AssertionError(ex); }
            });
            book("one");
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS)); }
    }
    @Test void listenerReentrantFacadeCommandRejectedWithoutExtraCommit() {
        publisher.subscribe(e -> facade.confirm("nested", e.after().id())); publisher.subscribe(audit);
        var result = book("one");
        assertEquals(RepositoryException.Reason.REENTRANT_WRITE,
                ((RepositoryException) result.warnings().getFirst().cause()).reason());
        assertEquals(1, repo.events().size()); assertEquals(1, audit.history().size());
    }
    @Test void concurrentConflictingBookingsHaveOneEventAndOneWinner() throws Exception {
        var results = race(() -> attemptBook("one"), () -> attemptBook("two"));
        assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
        assertEquals(1, repo.events().size()); assertEquals(1, client.appliedCount());
    }
    private boolean attemptBook(String id) {
        try { book(id); return true; } catch (SlotUnavailableException ex) { return false; }
    }
    @Test void concurrentConfirmAndReschedulePreserveBothUpdates() throws Exception {
        var a = book("one").appointment(); publisher.subscribe(audit);
        race(() -> facade.confirm("confirm", a.id()), () -> facade.reschedule("move", a.id(), START.plusSeconds(3600), standard));
        var current = facade.details(a.id()).orElseThrow();
        assertEquals(AppointmentStatus.CONFIRMED, current.status()); assertEquals(START.plusSeconds(3600), current.slot().start());
        assertEquals(List.of(1L, 2L, 3L), repo.events().stream().map(AppointmentEvent::sequence).toList());
        assertEquals(repo.events().subList(1, 3), audit.history());
    }
    @Test void concurrentRetriesAcrossFacadesReturnOneReceipt() throws Exception {
        var another = facade(dispatcher); var req = request("one", START);
        var results = race(() -> facade.book("same", req), () -> another.book("same", req));
        assertSame(results.get(0), results.get(1)); assertEquals(1, repo.events().size());
    }
    @Test void concurrentDisjointCommandsHaveGlobalDeliveryOrder() throws Exception {
        publisher.subscribe(audit);
        race(() -> facade.book("one", request("one", START)),
                () -> facade.book("two", request("two", START.plusSeconds(3600))));
        assertEquals(repo.events(), audit.history()); assertEquals(2, client.appliedCount());
        assertEquals(List.of(1L, 1L), repo.events().stream().map(AppointmentEvent::revision).toList());
    }
    @Test void directRepositoryCommitsAreDrainedInCommitOrder() {
        var assembler = new AppointmentAssembler(repo, Map.of(AppointmentKind.CONSULTATION, new ConsultationAppointmentCreator()), time);
        var a = repo.reserve(assembler.assemble(request("one", START)), standard);
        repo.transition(a.id(), AppointmentTransition.CONFIRM);
        publisher.subscribe(audit); dispatcher.dispatchPending(); dispatcher.dispatchPending();
        assertEquals(repo.events(), audit.history()); assertEquals(2, client.appliedCount());
    }
    @Test void readsAndEventSnapshotsAreImmutable() {
        var a = book("one").appointment(); facade.confirm("confirm", a.id());
        assertEquals(AppointmentStatus.PENDING, repo.events().getFirst().after().status());
        assertThrows(UnsupportedOperationException.class, () -> repo.events().clear());
        assertThrows(UnsupportedOperationException.class, () -> facade.providers().clear());
        assertEquals(now.get(), repo.events().getFirst().occurredAt());
        assertEquals(2, repo.events().stream().map(AppointmentEvent::id).distinct().count());
    }
    @ParameterizedTest @ValueSource(booleans = {true, false})
    void serviceSubtypeAndBothModeFamiliesSurviveFacadeLifecycle(boolean online) {
        Service service = new Service(new ServiceId("service"), "Service", Duration.ofMinutes(30),
                new Money(java.math.BigDecimal.TEN), AppointmentKind.SERVICE);
        var original = provider();
        var provider = new ServiceProvider(original.id(), original.name(), original.category(), Set.of(service.id()),
                original.modes(), original.workingHours(), original.preparationBuffer());
        var repository = new InMemoryAppointmentRepository(List.of(customer()), List.of(provider), List.of(service), clock, time.zone());
        var f = facade(new CommittedEventDispatcher(repository, publisher, new ExternalCalendarAdapter(client)));
        var req = new BookingRequest(new AppointmentId("service"), customer().id(), provider.id(), service.id(), START,
                online ? new OnlineModeFactory("room") : new InPersonModeFactory("office"),
                new ReminderServiceDecorator(new BaseAppointmentService()), standard);
        var a = f.book("book", req).appointment();
        f.confirm("confirm", a.id());
        var moved = f.reschedule("move", a.id(), START.plusSeconds(3600), standard).appointment();
        assertInstanceOf(ServiceAppointment.class, moved); assertSame(a.modeDetails(), moved.modeDetails());
        assertEquals(online ? AppointmentMode.ONLINE : AppointmentMode.IN_PERSON, moved.mode());
        assertSame(a.enhancements(), moved.enhancements()); assertEquals(a.totalFee(), moved.totalFee());
        f.cancel("cancel", a.id());
        assertEquals(4, f.events(a.id()).size());
    }
    @Test void invalidCatalogAndInvalidBookingProduceNoEvents() {
        var req = request("one", START);
        var missing = new BookingRequest(req.id(), new CustomerId("missing"), req.provider(), req.service(), req.start(), req.mode(), req.extras(), req.strategy());
        assertThrows(RepositoryException.class, () -> facade.book("missing", missing));
        assertThrows(DomainValidationException.class, () -> facade.book("past", request("past", now.get())));
        assertTrue(repo.events().isEmpty()); assertEquals(0, client.appliedCount());
    }
    @Test void submillisecondCalendarInputIsTypedWarningWithoutRollback() {
        var result = facade.book("nano", request("nano", START.plusNanos(1)));
        assertEquals(CalendarSyncException.Reason.INVALID_TIMESTAMP,
                ((CalendarSyncException) result.warnings().getFirst().cause()).reason());
        assertEquals(1, repo.events().size()); assertEquals(0, client.appliedCount());
    }
    @Test void concurrentCancelAndRescheduleUseLatestState() throws Exception {
        var a = book("one").appointment();
        race(() -> { facade.cancel("cancel", a.id()); return true; }, () -> {
            try { facade.reschedule("move", a.id(), START.plusSeconds(3600), standard); return true; }
            catch (InvalidTransitionException ex) { return false; }
        });
        assertEquals(AppointmentStatus.CANCELLED, facade.details(a.id()).orElseThrow().status());
        assertEquals(AppointmentEvent.Type.CANCELLED, repo.events().getLast().type());
        assertEquals(repo.events().size(), client.appliedCount());
    }
    @Test void confirmedCompletionBeforeEndAndTerminalChangesEmitNothing() {
        var a = book("one").appointment(); facade.confirm("confirm", a.id());
        assertThrows(DomainValidationException.class, () -> facade.complete("early", a.id()));
        assertEquals(2, repo.events().size());
        now.set(a.slot().end()); facade.complete("complete", a.id());
        assertThrows(InvalidTransitionException.class, () -> facade.cancel("cancel", a.id()));
        assertThrows(InvalidTransitionException.class, () -> facade.reschedule("move", a.id(), START.plusSeconds(3600), standard));
        assertEquals(3, repo.events().size()); assertEquals(3, client.appliedCount());
    }
    private static <T> List<T> race(Callable<T> first, Callable<T> second) throws Exception {
        var ready = new CountDownLatch(2); var go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (var task : List.of(first, second)) futures.add(pool.submit(() -> {
                ready.countDown(); if (!go.await(3, TimeUnit.SECONDS)) throw new AssertionError("start timeout"); return task.call();
            }));
            assertTrue(ready.await(3, TimeUnit.SECONDS)); go.countDown();
            return List.of(futures.get(0).get(5, TimeUnit.SECONDS), futures.get(1).get(5, TimeUnit.SECONDS));
        } finally { go.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS)); }
    }
}
