package com.appointease.domain;

import com.appointease.application.port.*;
import com.appointease.domain.addon.*;
import com.appointease.domain.creation.*;
import com.appointease.domain.exception.*;
import com.appointease.domain.mode.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import com.appointease.infrastructure.memory.InMemoryAppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class SchedulingTest {
    private final AvailabilityStrategy standard = new StandardAvailabilityStrategy();
    private final AvailabilityStrategy buffered = new PreparationBufferAvailabilityStrategy();
    private final TestClock clock = new TestClock(START.minus(Duration.ofDays(1)));
    private final ServiceProvider first = provider();
    private final ServiceProvider second = new ServiceProvider(new ProviderId("second"), "Second",
            first.category(), first.serviceIds(), first.modes(), first.workingHours(), first.preparationBuffer());
    private final AppointmentRepository repo = repository(List.of(first, second), List.of(service()));

    private AppointmentRepository repository(List<ServiceProvider> providers, List<Service> services) {
        return new InMemoryAppointmentRepository(List.of(customer()), providers, services, clock, ZoneId.of("Europe/Vilnius"));
    }
    private Appointment candidate(String id, Instant start) { return candidate(id, start, first); }
    private Appointment candidate(String id, Instant start, ServiceProvider provider) {
        return appointment(new AppointmentId(id), customer(), provider, service(), start, AppointmentMode.ONLINE,
                options(), time(START.minus(Duration.ofDays(2))));
    }
    private Appointment book(String id, Instant start) { return repo.reserve(candidate(id, start), standard); }

    @Test void strategyChangesAcceptanceForSameSchedule() {
        book("one", START);
        var adjacent = candidate("two", END);
        var before = repo.reservations();
        assertThrows(SlotUnavailableException.class, () -> repo.reserve(adjacent, buffered));
        assertEquals(before, repo.reservations());
        assertSame(adjacent, repo.reserve(adjacent, standard));
    }
    @ParameterizedTest @ValueSource(ints = {-30, 30})
    void standardAcceptsAdjacencyOnBothSides(int minutes) {
        book("one", START);
        book("two", START.plus(Duration.ofMinutes(minutes)));
        assertEquals(2, repo.reservations().size());
    }
    @ParameterizedTest @ValueSource(ints = {-29, -1, 0, 1, 29})
    void exactOverlapBoundariesRejectAndLeaveContentsUnchanged(int minutes) {
        book("one", START);
        var before = repo.reservations();
        assertThrows(SlotUnavailableException.class,
                () -> book("two", START.plus(Duration.ofMinutes(minutes))));
        assertEquals(before, repo.reservations());
    }
    @Test void priorBufferSnapshotCannotBeBypassedByStandardPolicy() {
        repo.reserve(candidate("one", START), buffered);
        assertThrows(SlotUnavailableException.class, () -> book("two", START.minus(Duration.ofMinutes(30))));
        book("three", START.minus(Duration.ofMinutes(45)));
        assertEquals(Duration.ofMinutes(15), repo.reservations().get(1).preparationBuffer());
    }
    @Test void exactBufferGapAccepted() {
        book("one", START);
        repo.reserve(candidate("two", END.plus(Duration.ofMinutes(15))), buffered);
        assertEquals(2, repo.reservations().size());
    }
    @Test void configurableBufferIsPolymorphicAndSnapshotIsImmutable() {
        var policyValue = new AtomicReference<>(Duration.ofMinutes(20));
        repo.reserve(candidate("one", START), provider -> policyValue.get());
        policyValue.set(Duration.ZERO);
        assertEquals(Duration.ofMinutes(20), repo.reservations().getFirst().preparationBuffer());
        assertThrows(SlotUnavailableException.class, () -> book("two", START.minus(Duration.ofMinutes(30))));
        book("three", END);
        assertThrows(SlotUnavailableException.class, () -> repo.reserve(candidate("four", END.plusSeconds(1800)),
                new PreparationBufferAvailabilityStrategy(Duration.ofMinutes(5))));
    }
    @Test void standardAcceptsOpeningAndClosingBoundaries() {
        book("open", START.minusSeconds(3600)); // 09:00 local
        book("close", START.plusSeconds(6 * 3600 + 1800)); // 16:30 local, ends 17:00
        assertEquals(2, repo.reservations().size());
    }
    @Test void bufferMustFitOperatingHoursButMayBeginBeforeNow() {
        Instant opening = START.minusSeconds(3600);
        assertThrows(DomainValidationException.class, () -> repo.reserve(candidate("open", opening), buffered));
        clock.set(opening.plusSeconds(10 * 60));
        repo.reserve(candidate("fits", opening.plusSeconds(15 * 60)), buffered);
        assertEquals(opening, repo.reservations().getFirst().effectiveSlot().start());
    }
    @ParameterizedTest @ValueSource(ints = {-61, 391})
    void outsideWorkingHoursRejectedBeforeReservation(int minutes) {
        assertThrows(DomainValidationException.class, () -> book("invalid", START.plusSeconds(minutes * 60L)));
        assertTrue(repo.reservations().isEmpty());
    }
    @ParameterizedTest @ValueSource(ints = {0, 1})
    void agedCandidateIsRevalidatedInsideTransaction(int secondsAfter) {
        var value = candidate("aged", START);
        clock.set(START.plusSeconds(secondsAfter));
        assertThrows(DomainValidationException.class, () -> repo.reserve(value, standard));
        assertTrue(repo.reservations().isEmpty());
    }
    @Test void permissiveStrategyCannotBypassOverlapOrExpiredTime() {
        book("one", START);
        AvailabilityStrategy permissive = ignored -> Duration.ZERO;
        assertThrows(SlotUnavailableException.class, () -> repo.reserve(candidate("two", START), permissive));
        clock.set(END);
        assertThrows(DomainValidationException.class, () -> repo.reserve(candidate("three", END), permissive));
        assertEquals(1, repo.reservations().size());
    }
    @Test void invalidStrategyResultsAndExceptionsDoNotMutateStore() {
        var value = candidate("one", START);
        assertThrows(DomainValidationException.class, () -> repo.reserve(value, p -> Duration.ofSeconds(-1)));
        assertThrows(DomainValidationException.class, () -> repo.reserve(value, p -> null));
        assertThrows(DomainValidationException.class, () -> repo.reserve(value, p -> Duration.ofSeconds(Long.MAX_VALUE)));
        assertThrows(IllegalStateException.class, () -> repo.reserve(value, p -> { throw new IllegalStateException(); }));
        assertThrows(DomainValidationException.class, () -> new PreparationBufferAvailabilityStrategy(Duration.ofSeconds(-1)));
        assertTrue(repo.reservations().isEmpty());
        repo.reserve(value, standard); // lock released after every failure
    }
    @Test void strategyCannotPerformReentrantWrite() {
        var error = assertThrows(RepositoryException.class, () -> repo.reserve(candidate("one", START), p -> {
            book("nested", START);
            return Duration.ZERO;
        }));
        assertEquals(RepositoryException.Reason.REENTRANT_WRITE, error.reason());
        assertTrue(repo.reservations().isEmpty());
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void containingAndContainedIntervalsConflict(boolean longFirst) {
        var longerService = new Service(service().id(), "Long", Duration.ofMinutes(60), service().baseFee(), service().kind());
        var longer = appointment(new AppointmentId("long"), customer(), first, longerService, START.minusSeconds(900),
                AppointmentMode.ONLINE, options(), time(BEFORE.minusSeconds(3600)));
        var shorter = candidate("short", START);
        var existing = new Reservation(longFirst ? longer : shorter, Duration.ZERO);
        assertThrows(SlotUnavailableException.class, () -> ReservationRules.validate(longFirst ? shorter : longer,
                standard, List.of(existing), time(BEFORE.minusSeconds(3600))));
    }
    @Test void catalogIsDefensiveAndRejectsMissingCustomerOrChangedService() {
        var providers = new ArrayList<>(List.of(first));
        var store = new InMemoryAppointmentRepository(List.of(customer()), providers, List.of(service()), clock,
                ZoneId.of("Europe/Vilnius"));
        providers.clear();
        store.reserve(candidate("one", START), standard);
        var missingCustomer = new InMemoryAppointmentRepository(List.of(), List.of(first), List.of(service()), clock,
                ZoneId.of("Europe/Vilnius"));
        assertThrows(RepositoryException.class, () -> missingCustomer.reserve(candidate("two", END), standard));
        var changed = new Service(service().id(), "Changed", Duration.ofMinutes(60), service().baseFee(), service().kind());
        assertThrows(RepositoryException.class, () -> repository(List.of(first), List.of(changed))
                .reserve(candidate("three", END), standard));
    }
    @Test void transactionReadsInjectedClockExactlyOnce() {
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        Clock advancing = new Clock() {
            @Override public ZoneId getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(ZoneId zone) { return this; }
            @Override public Instant instant() { return BEFORE.plusSeconds(calls.getAndIncrement() * 120L); }
        };
        var store = new InMemoryAppointmentRepository(List.of(customer()), List.of(first), List.of(service()), advancing,
                ZoneId.of("Europe/Vilnius"));
        store.reserve(candidate("one", START), standard);
        assertEquals(1, calls.get());
        assertThrows(DomainValidationException.class, () -> store.transition(new AppointmentId("one"), AppointmentTransition.CONFIRM));
        assertEquals(2, calls.get());
        assertEquals(AppointmentStatus.PENDING, store.find(new AppointmentId("one")).orElseThrow().status());
    }
    @Test void catalogRejectsMissingProviderAndSpoofedProviderOrService() {
        var empty = repository(List.of(), List.of(service()));
        assertEquals(RepositoryException.Reason.CATALOG_MISMATCH,
                assertThrows(RepositoryException.class, () -> empty.reserve(candidate("one", START), standard)).reason());
        var changed = new ServiceProvider(first.id(), "Spoof", first.category(), first.serviceIds(), first.modes(),
                first.workingHours(), Duration.ZERO);
        assertThrows(RepositoryException.class, () -> repo.reserve(candidate("two", START, changed), standard));
        assertThrows(RepositoryException.class, () -> repository(List.of(first), List.of())
                .reserve(candidate("three", START), standard));
        assertThrows(RepositoryException.class, () -> repository(List.of(first, first), List.of(service())));
        assertTrue(repo.reservations().isEmpty());
    }
    @Test void unsupportedServiceAndIncorrectDurationRemainMandatory() {
        var unsupported = new ServiceProvider(first.id(), first.name(), first.category(), Set.of(new ServiceId("other")),
                first.modes(), first.workingHours(), Duration.ZERO);
        assertThrows(DomainValidationException.class, () -> candidate("one", START, unsupported));
        assertThrows(DomainValidationException.class, () -> new AppointmentBuilder().id(new AppointmentId("two"))
                .customer(customer()).provider(first).service(service()).mode(new OnlineModeFactory("room"))
                .slot(new TimeSlot(START, END.plusSeconds(1))).build(new ConsultationAppointmentCreator(), time(BEFORE)));
    }
    @Test void wrongBusinessZoneRejected() {
        var utc = new InMemoryAppointmentRepository(List.of(customer()), List.of(first), List.of(service()), clock, ZoneOffset.UTC);
        assertThrows(DomainValidationException.class, () -> utc.reserve(candidate("one", START), standard));
        assertTrue(utc.reservations().isEmpty());
    }
    @Test void duplicateAndMissingIdsAreExplicitAndNonDestructive() {
        var one = book("one", START);
        var before = repo.reservations();
        assertEquals(RepositoryException.Reason.DUPLICATE_ID,
                assertThrows(RepositoryException.class, () -> book("one", END)).reason());
        var missing = new AppointmentId("missing");
        assertTrue(repo.find(missing).isEmpty());
        assertEquals(RepositoryException.Reason.NOT_FOUND,
                assertThrows(RepositoryException.class, () -> repo.reschedule(missing, END, standard)).reason());
        assertThrows(RepositoryException.class, () -> repo.transition(missing, AppointmentTransition.CANCEL));
        assertEquals(before, repo.reservations());
        assertSame(one, repo.find(one.id()).orElseThrow());
    }
    @Test void cancelledSlotsReleaseCapacityAndRetainHistory() {
        var one = repo.reserve(candidate("one", START), buffered);
        repo.transition(one.id(), AppointmentTransition.CANCEL);
        repo.reserve(candidate("two", START), buffered);
        assertEquals(AppointmentStatus.CANCELLED, repo.history(customer().id()).getFirst().status());
        assertEquals(2, repo.history(customer().id()).size());
    }
    @Test void confirmedSlotsBlockAndCompletedHistoryRetainsEffectiveOccupancy() {
        var one = repo.reserve(candidate("one", START), buffered);
        repo.transition(one.id(), AppointmentTransition.CONFIRM);
        assertThrows(SlotUnavailableException.class, () -> book("two", START));
        clock.set(END);
        var complete = repo.transition(one.id(), AppointmentTransition.COMPLETE);
        var stored = repo.reservations().getFirst();
        assertEquals(AppointmentStatus.COMPLETED, complete.status());
        assertTrue(stored.blocksAvailability());
        assertEquals(START.minusSeconds(900), stored.effectiveSlot().start());
        // Earlier fixed validation time isolates occupancy from the independent future-time guard.
        assertThrows(SlotUnavailableException.class, () -> ReservationRules.validate(candidate("two", START),
                standard, List.of(stored), time(BEFORE)));
        assertThrows(InvalidTransitionException.class, () -> repo.reschedule(one.id(), END.plusSeconds(3600), standard));
        assertThrows(InvalidTransitionException.class, () -> repo.transition(one.id(), AppointmentTransition.CANCEL));
        assertEquals(List.of(stored), repo.reservations());
    }
    @Test void importsMustBePendingAndFailedTransitionsPreserveState() {
        var pending = candidate("one", START);
        assertThrows(DomainValidationException.class, () -> repo.reserve(pending.confirm(time(BEFORE)), standard));
        repo.reserve(pending, buffered);
        var before = repo.reservations();
        assertThrows(InvalidTransitionException.class, () -> repo.transition(pending.id(), AppointmentTransition.COMPLETE));
        assertEquals(before, repo.reservations());
        clock.set(START);
        assertThrows(DomainValidationException.class, () -> repo.transition(pending.id(), AppointmentTransition.CANCEL));
        assertEquals(before, repo.reservations());
    }
    @Test void failedReschedulePreservesOriginalSlotAndPolicy() {
        var one = repo.reserve(candidate("one", START), buffered);
        book("two", END.plusSeconds(3600));
        var before = repo.reservations();
        assertThrows(SlotUnavailableException.class, () -> repo.reschedule(one.id(), END.plusSeconds(3600), standard));
        assertThrows(DomainValidationException.class, () -> repo.reschedule(one.id(), START.plusSeconds(8 * 3600), standard));
        assertThrows(DomainValidationException.class, () -> repo.reschedule(one.id(), clock.instant(), standard));
        assertEquals(before, repo.reservations());
        assertThrows(SlotUnavailableException.class, () -> book("three", START));
    }
    @ParameterizedTest @ValueSource(booleans = {false, true})
    void reschedulePreservesAllPatternsAndState(boolean serviceKind) {
        var selected = serviceKind ? new Service(service().id(), "Service", service().duration(), service().baseFee(),
                AppointmentKind.SERVICE) : service();
        var store = repository(List.of(first), List.of(selected));
        AppointmentCreator creator = serviceKind ? new ServiceAppointmentCreator() : new ConsultationAppointmentCreator();
        AppointmentModeFactory mode = serviceKind ? new InPersonModeFactory("Office") : new OnlineModeFactory("Room");
        var original = new AppointmentBuilder().id(new AppointmentId("one")).customer(customer()).provider(first)
                .service(selected).start(START).mode(mode).appointmentService(new ReminderServiceDecorator(
                        new PriorityBookingDecorator(new BaseAppointmentService()))).build(creator, time(BEFORE));
        store.reserve(original, buffered);
        var confirmed = store.transition(original.id(), AppointmentTransition.CONFIRM);
        var moved = store.reschedule(original.id(), END, standard);
        assertEquals(original.getClass(), moved.getClass());
        assertEquals(original.id(), moved.id());
        assertEquals(AppointmentStatus.CONFIRMED, moved.status());
        assertSame(original.customer(), moved.customer());
        assertSame(original.provider(), moved.provider());
        assertSame(original.service(), moved.service());
        assertSame(original.modeDetails(), moved.modeDetails());
        assertSame(original.enhancements(), moved.enhancements());
        assertEquals(original.totalFee(), moved.totalFee());
        assertEquals(original.businessZone(), moved.businessZone());
        assertEquals(new TimeSlot(END, END.plus(selected.duration())), moved.slot());
        assertEquals(START, confirmed.slot().start());
        assertEquals(AppointmentStatus.PENDING, original.status());
        assertEquals(Duration.ZERO, store.reservations().getFirst().preparationBuffer());
        var sameSlot = store.reschedule(moved.id(), END, standard);
        assertEquals(moved.slot(), sameSlot.slot()); // self-slot is eligible
        assertEquals(moved.status(), sameSlot.status());
    }
    @Test void historyIsImmutableAndSortedByStartThenId() {
        book("late", END);
        book("z", START);
        repo.reserve(candidate("a", START, second), standard);
        var history = repo.history(customer().id());
        assertEquals(List.of("a", "z", "late"), history.stream().map(a -> a.id().value()).toList());
        assertThrows(UnsupportedOperationException.class, history::clear);
        assertThrows(UnsupportedOperationException.class, () -> repo.reservations().clear());
        repo.transition(new AppointmentId("z"), AppointmentTransition.CANCEL);
        assertEquals(AppointmentStatus.PENDING, history.get(1).status());
        assertTrue(repo.history(new CustomerId("unknown")).isEmpty());
    }
    @Test void simultaneousSameSlotHasExactlyOneWinner() throws Exception {
        var results = race(() -> book("one", START), () -> book("two", START));
        assertOneSuccess(results);
        assertEquals(1, repo.reservations().size());
    }
    @Test void simultaneousDifferentProvidersBothSucceed() throws Exception {
        var results = race(() -> book("one", START), () -> repo.reserve(candidate("two", START, second), standard));
        assertEquals(2, results.stream().filter(Appointment.class::isInstance).count());
        assertEquals(2, repo.reservations().size());
    }
    @Test void simultaneousDuplicateIdsHaveExactlyOneWinner() throws Exception {
        var results = race(() -> book("same", START), () -> book("same", END));
        assertEquals(1, results.stream().filter(Appointment.class::isInstance).count());
        var error = results.stream().filter(RepositoryException.class::isInstance).map(RepositoryException.class::cast).findFirst().orElseThrow();
        assertEquals(RepositoryException.Reason.DUPLICATE_ID, error.reason());
        assertEquals(1, repo.reservations().size());
    }
    @Test void bookingRacingRescheduleCannotDoubleBook() throws Exception {
        var one = book("one", START);
        Instant target = END.plusSeconds(3600);
        var results = race(() -> repo.reschedule(one.id(), target, standard), () -> book("two", target));
        assertOneSuccess(results);
        var current = repo.find(one.id()).orElseThrow();
        if (repo.find(new AppointmentId("two")).isPresent()) assertSame(one, current);
        else assertEquals(target, current.slot().start());
        assertEquals(1, repo.reservations().stream().filter(r -> r.appointment().slot().start().equals(target)).count());
    }
    @Test void simultaneousReschedulesHaveOneWinnerAndRetainLoserOriginal() throws Exception {
        var one = book("one", START);
        var two = book("two", END);
        Instant target = END.plusSeconds(3600);
        assertOneSuccess(race(() -> repo.reschedule(one.id(), target, standard),
                () -> repo.reschedule(two.id(), target, standard)));
        assertEquals(2, repo.reservations().size());
        long unchanged = List.of(one, two).stream().filter(a -> repo.find(a.id()).orElseThrow() == a).count();
        assertEquals(1, unchanged);
    }
    @Test void concurrentConfirmationAndRescheduleDoNotLoseEitherUpdate() throws Exception {
        var one = book("one", START);
        var results = race(() -> repo.transition(one.id(), AppointmentTransition.CONFIRM),
                () -> repo.reschedule(one.id(), END, standard));
        assertEquals(2, results.stream().filter(Appointment.class::isInstance).count());
        var current = repo.find(one.id()).orElseThrow();
        assertEquals(AppointmentStatus.CONFIRMED, current.status());
        assertEquals(END, current.slot().start());
    }
    private static void assertOneSuccess(List<Object> results) {
        assertEquals(1, results.stream().filter(Appointment.class::isInstance).count());
        assertEquals(1, results.stream().filter(SlotUnavailableException.class::isInstance).count());
    }
    private static List<Object> race(Supplier<Appointment> left, Supplier<Appointment> right) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Supplier<Appointment> action : List.of(left, right)) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) throw new AssertionError("Start gate timed out");
                    try { return action.get(); } catch (RuntimeException ex) { return ex; }
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS), "Workers did not reach start gate");
            start.countDown();
            return List.of(futures.get(0).get(10, TimeUnit.SECONDS), futures.get(1).get(10, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate");
        }
    }
    /** Only the tests advance time; production receives the standard Clock contract. */
    private static final class TestClock extends Clock {
        private final AtomicReference<Instant> current;
        TestClock(Instant instant) { current = new AtomicReference<>(instant); }
        void set(Instant instant) { current.set(instant); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        @Override public Instant instant() { return current.get(); }
    }
}
