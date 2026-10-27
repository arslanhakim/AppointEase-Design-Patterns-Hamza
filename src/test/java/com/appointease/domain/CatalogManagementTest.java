package com.appointease.domain;

import com.appointease.Main;
import com.appointease.application.*;
import com.appointease.application.port.RepositoryException;
import com.appointease.domain.addon.BaseAppointmentService;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.mode.*;
import com.appointease.domain.model.*;
import com.appointease.domain.scheduling.*;
import com.appointease.infrastructure.memory.InMemoryAppointmentRepository;
import com.appointease.infrastructure.seed.DemoCatalog;
import com.appointease.presentation.desktop.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogManagementTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneOffset.UTC);
    private final BookingFacade facade = Main.createContext(clock, ZoneOffset.UTC).facade();
    private final Instant start = Instant.parse("2026-10-28T11:00:00Z");
    private Service service(String id, String name, int minutes, String fee) {
        return new Service(new ServiceId(id), name, Duration.ofMinutes(minutes), new Money(new BigDecimal(fee)),
                AppointmentKind.CONSULTATION, ProviderCategory.HEALTHCARE);
    }
    private ServiceProvider provider(String id, Service s) {
        return new ServiceProvider(new ProviderId(id), "New provider", ProviderCategory.HEALTHCARE, Set.of(s.id()),
                Set.of(AppointmentMode.values()), Map.of(DayOfWeek.WEDNESDAY, List.of(new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(17, 0)))), Duration.ofMinutes(15));
    }
    private ServiceProvider change(ServiceProvider p, String name, Set<ServiceId> services, Set<AppointmentMode> modes,
            Map<DayOfWeek, List<WorkingPeriod>> hours, long buffer, boolean enabled) {
        return new ServiceProvider(p.id(), name, p.category(), services, modes, hours, Duration.ofMinutes(buffer), enabled);
    }
    private BookingRequest request(String id, Customer c, ServiceProvider p, Service s, Instant at) {
        return new BookingRequest(new AppointmentId(id), c.id(), p.id(), s.id(), at, new OnlineModeFactory(id),
                new BaseAppointmentService(), new PreparationBufferAvailabilityStrategy());
    }
    @Test void customerCreateEditLookupAndImmutableLists() {
        var customer = facade.createCustomer("  Renée Khan  ");
        assertEquals("Renée Khan", customer.name());
        assertEquals(customer, facade.customer(customer.id()).orElseThrow());
        var before = facade.customers();
        var updated = facade.updateCustomer(customer, "Renée Ali");
        assertEquals(customer.id(), updated.id()); assertEquals("Renée Khan", customer.name());
        assertTrue(before.contains(customer)); assertFalse(before.contains(updated));
        assertThrows(UnsupportedOperationException.class, () -> before.clear());
        assertEquals(updated, facade.customer(customer.id()).orElseThrow());
    }
    @ParameterizedTest @ValueSource(strings = {"", " ", "\t\n"})
    void customerValidationRejectsBlankNames(String name) {
        int count = facade.customers().size();
        assertThrows(DomainValidationException.class, () -> facade.createCustomer(name));
        assertEquals(count, facade.customers().size());
    }
    @Test void customerInvalidEditAndStaleEditLeaveSavedValue() {
        var c = facade.createCustomer("First");
        assertThrows(DomainValidationException.class, () -> facade.updateCustomer(c, " "));
        var updated = facade.updateCustomer(c, "Second");
        assertThrows(RepositoryException.class, () -> facade.updateCustomer(c, "Lost update"));
        assertEquals(updated, facade.customer(c.id()).orElseThrow());
    }
    @Test void serviceCreateEditNormalizesMoneyAndRetainsId() {
        var s = facade.createService(service("new-service", "New service", 35, "25.125"));
        assertEquals(new BigDecimal("25.13"), s.baseFee().amount());
        var changed = new Service(s.id(), "Updated", Duration.ofMinutes(45), new Money(new BigDecimal("0")), AppointmentKind.SERVICE, ProviderCategory.TUTORING);
        facade.updateService(s, changed);
        assertTrue(facade.services().contains(changed)); assertFalse(facade.services().contains(s));
        assertEquals(2, changed.baseFee().amount().scale());
    }
    @ParameterizedTest @ValueSource(strings = {"-0.001", "-1", "-100.99"})
    void negativePriceRejectedBeforeRounding(String fee) {
        int count = facade.services().size();
        assertThrows(DomainValidationException.class, () -> facade.createService(service("bad", "Invalid", 30, fee)));
        assertEquals(count, facade.services().size());
    }
    @Test void invalidDurationNameAndDuplicateServiceRejected() {
        assertThrows(DomainValidationException.class, () -> service("a", "", 30, "1"));
        assertThrows(DomainValidationException.class, () -> service("a", "A", 0, "1"));
        var s = facade.createService(service("a", "A", 30, "1"));
        assertThrows(RepositoryException.class, () -> facade.createService(s));
        assertThrows(DomainValidationException.class, () -> facade.updateService(s, service("b", "B", 30, "1")));
    }
    @Test void providerCreateEditWorkingHoursModesServicesAndDisable() {
        var s = facade.createService(service("s", "Service", 30, "10"));
        var p = facade.createProvider(provider("p", s));
        var changed = change(p, "Edited", p.serviceIds(), Set.of(AppointmentMode.IN_PERSON),
                Map.of(DayOfWeek.FRIDAY, List.of(new WorkingPeriod(LocalTime.NOON, LocalTime.of(18, 0)))), 25, false);
        facade.updateProvider(p, changed);
        assertTrue(facade.providers().contains(changed)); assertFalse(changed.supports(s, AppointmentMode.IN_PERSON));
        var enabled = change(changed, changed.name(), changed.serviceIds(), changed.modes(), changed.workingHours(), 25, true);
        facade.updateProvider(changed, enabled); assertTrue(enabled.supports(s, AppointmentMode.IN_PERSON));
        assertThrows(RepositoryException.class, () -> facade.updateProvider(p, changed));
    }
    @Test void providerRejectsMissingServicesAndInvalidHoursWithoutMutation() {
        int count = facade.providers().size();
        var s = service("missing", "Missing", 30, "10");
        assertThrows(DomainValidationException.class, () -> facade.createProvider(provider("p", s)));
        assertEquals(count, facade.providers().size());
        var p = facade.providers().getFirst();
        assertThrows(DomainValidationException.class, () -> change(p, "X", Set.of(), p.modes(), p.workingHours(), 0, true));
        assertThrows(DomainValidationException.class, () -> change(p, "X", p.serviceIds(), Set.of(), p.workingHours(), 0, true));
        assertThrows(DomainValidationException.class, () -> change(p, "X", p.serviceIds(), p.modes(), Map.of(), 0, true));
        assertThrows(DomainValidationException.class, () -> change(p, "X", p.serviceIds(), p.modes(), p.workingHours(), -1, true));
    }
    @Test void bookNewRecordsThenCatalogEditsPreserveEveryAgreedSnapshot() {
        var c = facade.createCustomer("New customer");
        var s = facade.createService(service("s", "Original", 30, "25.125"));
        var p = facade.createProvider(provider("p", s));
        var a = facade.book("book", request("a", c, p, s, start)).appointment();
        var event = facade.events(a.id()).getFirst();
        facade.updateCustomer(c, "Renamed customer");
        var updated = facade.updateService(s, service("s", "New price and duration", 60, "99.99"));
        var newProvider = facade.updateProvider(p, change(p, "Renamed provider", p.serviceIds(), p.modes(), p.workingHours(), 30, true));
        assertSame(a, facade.details(a.id()).orElseThrow()); assertSame(event, facade.events(a.id()).getFirst());
        assertEquals(s, a.service()); assertEquals(c, a.customer()); assertEquals(p, a.provider());
        assertEquals(Duration.ofMinutes(30), Duration.between(a.slot().start(), a.slot().end())); assertEquals(new BigDecimal("25.13"), a.totalFee().amount());
        var next = facade.book("next", request("b", c, newProvider, updated, start.plusSeconds(7200))).appointment();
        assertEquals(updated, next.service()); assertEquals(Duration.ofMinutes(60), Duration.between(next.slot().start(), next.slot().end()));
        var moved = facade.reschedule("move", a.id(), start.plusSeconds(3600), new StandardAvailabilityStrategy()).appointment();
        assertEquals(s, moved.service()); assertEquals(a.totalFee(), moved.totalFee()); assertEquals(a.modeDetails(), moved.modeDetails());
    }
    @Test void providerCannotInvalidateActiveServiceModeHoursOrEnabledState() {
        var c = facade.createCustomer("C"); var s = facade.createService(service("s", "S", 30, "1"));
        var other = facade.createService(service("other", "Other", 30, "1"));
        var p = facade.createProvider(provider("p", s));
        var a = facade.book("book", request("a", c, p, s, start)).appointment();
        var invalid = List.of(change(p, "X", Set.of(other.id()), p.modes(), p.workingHours(), 15, true),
                change(p, "X", p.serviceIds(), Set.of(AppointmentMode.IN_PERSON), p.workingHours(), 15, true),
                change(p, "X", p.serviceIds(), p.modes(), p.workingHours(), 15, false),
                change(p, "X", p.serviceIds(), p.modes(), Map.of(DayOfWeek.WEDNESDAY, List.of(new WorkingPeriod(LocalTime.of(11, 0), LocalTime.of(17, 0)))), 15, true));
        for (var value : invalid) {
            assertThrows(DomainValidationException.class, () -> facade.updateProvider(p, value));
            assertTrue(facade.providers().contains(p)); assertSame(a, facade.details(a.id()).orElseThrow());
            assertEquals(1, facade.events(a.id()).size());
        }
        facade.cancel("cancel", a.id());
        assertFalse(facade.updateProvider(p, invalid.get(2)).enabled());
    }
    @Test void currentHoursAlsoProtectReschedulesOfOldSnapshots() {
        var c = facade.createCustomer("C"); var s = facade.createService(service("s", "S", 30, "1")); var p = facade.createProvider(provider("p", s));
        var a = facade.book("book", request("a", c, p, s, start)).appointment();
        facade.updateProvider(p, change(p, "P", p.serviceIds(), p.modes(), Map.of(DayOfWeek.WEDNESDAY,
                List.of(new WorkingPeriod(LocalTime.of(10, 0), LocalTime.of(12, 0)))), 15, true));
        assertThrows(DomainValidationException.class, () -> facade.reschedule("move", a.id(), start.plusSeconds(7200), new StandardAvailabilityStrategy()));
        assertSame(a, facade.details(a.id()).orElseThrow());
    }
    @Test void desktopImmediatelySeesNewCustomersAndOverviewIncludesTheirBookings() {
        var context = Main.createContext(clock, ZoneOffset.UTC);
        var controller = new DesktopController(context.facade(), context.customers(), context.time(), context.notifications(), context.audit());
        var c = controller.saveCustomer(null, "Immediate"); assertTrue(controller.customers().contains(c));
        var p = controller.providers().getFirst(); var s = controller.services(p).getFirst();
        var draft = new DesktopController.Draft(c, p, s, LocalDate.of(2026, 10, 28), "14:00", AppointmentMode.ONLINE, false, false, false);
        var a = controller.book(controller.prepare(draft)).appointment();
        assertTrue(controller.appointments("Immediate", null, null, null).contains(a));
        assertTrue(new OverviewModel(context.facade(), context.customers(), context.time()).load(draft.day()).appointments().contains(a));
    }
    @Test void reviewedBookingRejectsChangedCatalogWithoutRepricing() {
        var c = facade.createCustomer("C"); var s = facade.createService(service("s", "S", 30, "1")); var p = facade.createProvider(provider("p", s));
        var r = request("a", c, p, s, start);
        facade.updateService(s, service("s", "Changed", 60, "100"));
        assertThrows(DomainValidationException.class, () -> facade.bookReviewed("review", r, c, p, s));
        assertTrue(facade.details(r.id()).isEmpty()); assertTrue(facade.events(r.id()).isEmpty());
    }
    @Test void concurrentCreatesAreUniqueAndReadsAreImmutable() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            var jobs = new ArrayList<Callable<Customer>>();
            for (int i = 0; i < 80; i++) { int n = i; jobs.add(() -> facade.createCustomer("Customer " + n)); }
            var results = executor.invokeAll(jobs);
            var ids = new HashSet<CustomerId>(); for (var result : results) ids.add(result.get().id());
            assertEquals(80, ids.size()); assertEquals(83, facade.customers().size());
        }
    }
    @Test void concurrentEditsHaveOneWinnerAndNoLostUpdate() throws Exception {
        var c = facade.createCustomer("Original"); var gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> edit = () -> { gate.await(); try { facade.updateCustomer(c, Thread.currentThread().getName()); return true; }
                catch (RepositoryException ex) { return false; } };
            var a = executor.submit(edit); var b = executor.submit(edit); gate.countDown();
            assertNotEquals(a.get(), b.get()); assertNotEquals(c, facade.customer(c.id()).orElseThrow());
        }
    }
    @Test void staleCandidateCannotReserveAfterCatalogReplacement() {
        var demo = DemoCatalog.create();
        var repo = new InMemoryAppointmentRepository(demo.customers(), demo.providers(), demo.services(), clock, ZoneOffset.UTC);
        var s = demo.services().getFirst(); var p = demo.providers().getFirst(); var c = demo.customers().getFirst();
        var assembler = new AppointmentAssembler(repo, Map.of(AppointmentKind.CONSULTATION, new com.appointease.domain.creation.ConsultationAppointmentCreator()),
                new com.appointease.domain.time.DomainTime(ZoneOffset.UTC.getId(), clock));
        var candidate = assembler.assemble(request("a", c, p, s, start));
        repo.updateService(s, new Service(s.id(), "Changed", s.duration(), s.baseFee(), s.kind()));
        assertThrows(RepositoryException.class, () -> repo.reserve(candidate, new StandardAvailabilityStrategy()));
        assertTrue(repo.reservations().isEmpty()); assertTrue(repo.events().isEmpty());
    }
    @Test void disabledProviderRejectsBookingAndMissingEditDoesNotInsert() {
        var c = facade.createCustomer("C"); var s = facade.createService(service("s", "S", 30, "1"));
        var p = provider("p", s);
        var disabled = facade.createProvider(change(p, "P", p.serviceIds(), p.modes(), p.workingHours(), 0, false));
        assertThrows(DomainValidationException.class, () -> facade.book("book", request("a", c, disabled, s, start)));
        assertTrue(facade.details(new AppointmentId("a")).isEmpty());
        var missing = new Customer(new CustomerId("missing"), "Missing");
        assertThrows(RepositoryException.class, () -> facade.updateCustomer(missing, "Replacement"));
        assertTrue(facade.customer(missing.id()).isEmpty());
    }
    @Test void bufferEditPreservesReservedBufferAndRescheduleUsesCurrentStrategyExactlyOnce() {
        var demo = DemoCatalog.create();
        var repo = new InMemoryAppointmentRepository(demo.customers(), demo.providers(), demo.services(), clock, ZoneOffset.UTC);
        var s = demo.services().getFirst(); var p = demo.providers().getFirst(); var c = demo.customers().getFirst();
        var assembler = new AppointmentAssembler(repo, Map.of(AppointmentKind.CONSULTATION, new com.appointease.domain.creation.ConsultationAppointmentCreator()),
                new com.appointease.domain.time.DomainTime(ZoneOffset.UTC.getId(), clock));
        var a = repo.reserve(assembler.assemble(request("a", c, p, s, start)), new PreparationBufferAvailabilityStrategy());
        repo.updateProvider(p, change(p, "Edited", p.serviceIds(), p.modes(), p.workingHours(), 45, true));
        assertEquals(Duration.ofMinutes(15), repo.reservations().getFirst().preparationBuffer());
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        repo.reschedule(a.id(), start.plusSeconds(3600), current -> { calls.incrementAndGet(); return current.preparationBuffer(); });
        assertEquals(1, calls.get()); assertEquals(Duration.ofMinutes(45), repo.reservations().getFirst().preparationBuffer());
    }
    @Test void simultaneousProviderDisableAndReservationCannotBothCommit() throws Exception {
        for (int iteration = 0; iteration < 20; iteration++) {
            var demo = DemoCatalog.create();
            var repo = new InMemoryAppointmentRepository(demo.customers(), demo.providers(), demo.services(), clock, ZoneOffset.UTC);
            var s = demo.services().getFirst(); var p = demo.providers().getFirst(); var c = demo.customers().getFirst();
            var assembler = new AppointmentAssembler(repo, Map.of(AppointmentKind.CONSULTATION, new com.appointease.domain.creation.ConsultationAppointmentCreator()),
                    new com.appointease.domain.time.DomainTime(ZoneOffset.UTC.getId(), clock));
            var candidate = assembler.assemble(request("a", c, p, s, start));
            var disabled = change(p, "Disabled", p.serviceIds(), p.modes(), p.workingHours(), 0, false);
            var gate = new CountDownLatch(1);
            try (var executor = Executors.newFixedThreadPool(2)) {
                var reserve = executor.submit(() -> { gate.await(); try { repo.reserve(candidate, new StandardAvailabilityStrategy()); return true; }
                    catch (RepositoryException ex) { return false; } });
                var edit = executor.submit(() -> { gate.await(); try { repo.updateProvider(p, disabled); return true; }
                    catch (DomainValidationException ex) { return false; } });
                gate.countDown();
                boolean reserved = reserve.get(5, TimeUnit.SECONDS); boolean edited = edit.get(5, TimeUnit.SECONDS);
                assertNotEquals(reserved, edited);
                assertEquals(reserved ? 1 : 0, repo.events().size());
                assertEquals(reserved, repo.providers().stream().filter(v -> v.id().equals(p.id())).findFirst().orElseThrow().enabled());
            }
        }
    }
}
