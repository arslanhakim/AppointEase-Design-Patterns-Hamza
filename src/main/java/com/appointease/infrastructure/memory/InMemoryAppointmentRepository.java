package com.appointease.infrastructure.memory;

import com.appointease.application.port.*;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.AppointmentRules;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import com.appointease.domain.exception.DomainValidationException;
import java.time.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/** One instance-owned lock protects the entire read/validate/replace transaction and every read. */
public final class InMemoryAppointmentRepository implements AppointmentRepository {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<AppointmentId, Reservation> entries = new HashMap<>();
    private final List<AppointmentEvent> events = new ArrayList<>();
    private final Map<AppointmentId, Long> revisions = new HashMap<>();
    private final String eventNamespace = UUID.randomUUID().toString();
    private final Map<CustomerId, Customer> customers;
    private final Map<ProviderId, ServiceProvider> providers;
    private final Map<ServiceId, Service> services;
    private final Clock clock;
    private final ZoneId zone;

    public InMemoryAppointmentRepository(Collection<Customer> customers, Collection<ServiceProvider> providers,
            Collection<Service> services, Clock clock, ZoneId zone) {
        this.customers = catalog(customers, Customer::id);
        this.providers = catalog(providers, ServiceProvider::id);
        this.services = catalog(services, Service::id);
        this.clock = Validation.required(clock, "clock");
        this.zone = Validation.required(zone, "zone");
    }
    private static <K, V> Map<K, V> catalog(Collection<V> values, Function<V, K> key) {
        Map<K, V> copy = new HashMap<>();
        for (V value : Validation.required(values, "catalog")) {
            Validation.required(value, "catalog value");
            if (copy.putIfAbsent(key.apply(value), value) != null) {
                throw new RepositoryException(RepositoryException.Reason.DUPLICATE_ID, "Duplicate catalog ID");
            }
        }
        return copy;
    }
    @Override public Optional<Appointment> find(AppointmentId id) {
        return read(() -> Optional.ofNullable(entries.get(Validation.required(id, "id"))).map(Reservation::appointment));
    }
    @Override public List<ServiceProvider> providers() {
        return read(() -> providers.values().stream().sorted(Comparator.comparing(p -> p.id().value())).toList());
    }
    @Override public List<Service> services() {
        return read(() -> services.values().stream().sorted(Comparator.comparing(s -> s.id().value())).toList());
    }
    @Override public Optional<Customer> customer(CustomerId id) { return read(() -> Optional.ofNullable(customers.get(id))); }
    @Override public List<Customer> customers() {
        return read(() -> customers.values().stream().sorted(Comparator.comparing(c -> c.id().value())).toList());
    }
    private <K, V> V insert(Map<K, V> catalog, K id, V value) {
        if (catalog.containsKey(id)) throw new RepositoryException(RepositoryException.Reason.DUPLICATE_ID, "Catalog ID already exists");
        catalog.put(id, value);
        return value;
    }
    private <K, V> void expect(Map<K, V> catalog, K id, V expected, K replacementId) {
        if (!id.equals(replacementId)) throw new DomainValidationException("Catalog IDs cannot change");
        if (!catalog.containsKey(id)) throw new RepositoryException(RepositoryException.Reason.NOT_FOUND, "Catalog entry not found");
        if (!catalog.get(id).equals(expected)) throw new RepositoryException(RepositoryException.Reason.CATALOG_MISMATCH,
                "This record changed. Reopen the editor and try again.");
    }
    @Override public Customer createCustomer(Customer value) {
        return write(() -> insert(customers, value.id(), value));
    }
    @Override public Customer updateCustomer(Customer expected, Customer value) {
        return write(() -> { expect(customers, expected.id(), expected, value.id()); customers.put(value.id(), value); return value; });
    }
    @Override public Service createService(Service value) {
        return write(() -> insert(services, value.id(), value));
    }
    @Override public Service updateService(Service expected, Service value) {
        return write(() -> { expect(services, expected.id(), expected, value.id()); services.put(value.id(), value); return value; });
    }
    private void providerReferences(ServiceProvider value) {
        if (!services.keySet().containsAll(value.serviceIds())) throw new DomainValidationException("Every supported service must exist in the catalog");
    }
    @Override public ServiceProvider createProvider(ServiceProvider value) {
        return write(() -> { providerReferences(value); return insert(providers, value.id(), value); });
    }
    @Override public ServiceProvider updateProvider(ServiceProvider expected, ServiceProvider value) {
        return write(() -> {
            expect(providers, expected.id(), expected, value.id());
            providerReferences(value);
            DomainTime time = transactionTime();
            for (Reservation r : entries.values()) {
                var a = r.appointment();
                if (a.provider().id().equals(value.id()) && (a.status() == AppointmentStatus.PENDING || a.status() == AppointmentStatus.CONFIRMED)) {
                    if (!value.supports(a.service(), a.mode())) throw new DomainValidationException(
                            "Provider must remain enabled and support the service and mode of active appointment " + a.id().value());
                    try { AppointmentRules.validateWorkingHours(value, r.effectiveSlot(), time); }
                    catch (DomainValidationException ex) { throw new DomainValidationException(
                            "Working hours must cover active appointment " + a.id().value() + " and its reserved preparation buffer"); }
                }
            }
            providers.put(value.id(), value);
            return value;
        });
    }
    @Override public List<AppointmentEvent> events() { return read(() -> List.copyOf(events)); }
    private void commit(Reservation reservation, Appointment before, AppointmentEvent.Type type, DomainTime time) {
        Appointment after = reservation.appointment();
        long revision = revisions.getOrDefault(after.id(), 0L) + 1;
        long sequence = events.size() + 1L;
        AppointmentEvent event = new AppointmentEvent(eventNamespace + ":" + sequence, sequence,
                revision, type, time.now(), before, after);
        entries.put(after.id(), reservation);
        revisions.put(after.id(), revision);
        events.add(event);
    }
    @Override public List<Appointment> history(CustomerId customerId) {
        Validation.required(customerId, "customer ID");
        return read(() -> ordered().stream().map(Reservation::appointment)
                .filter(a -> a.customer().id().equals(customerId)).toList());
    }
    @Override public List<Reservation> reservations() { return read(this::ordered); }
    private List<Reservation> ordered() {
        return entries.values().stream().sorted(Comparator
                .comparing((Reservation r) -> r.appointment().slot().start())
                .thenComparing(r -> r.appointment().id().value())).toList();
    }
    @Override public Appointment reserve(Appointment candidate, AvailabilityStrategy strategy) {
        return write(() -> {
            Validation.required(candidate, "candidate");
            if (entries.containsKey(candidate.id())) {
                throw new RepositoryException(RepositoryException.Reason.DUPLICATE_ID, "Appointment ID already exists");
            }
            if (candidate.status() != AppointmentStatus.PENDING) {
                throw new DomainValidationException("New reservations must be pending");
            }
            requireCatalog(candidate);
            DomainTime time = transactionTime();
            Reservation reservation = ReservationRules.validate(candidate, strategy, ordered(), time);
            commit(reservation, null, AppointmentEvent.Type.RESERVED, time);
            return candidate;
        });
    }
    @Override public Appointment reschedule(AppointmentId id, Instant newStart, AvailabilityStrategy strategy) {
        return write(() -> {
            Reservation original = required(id);
            DomainTime time = transactionTime();
            Appointment replacement = original.appointment().reschedule(newStart, time);
            List<Reservation> others = ordered().stream().filter(r -> !r.appointment().id().equals(id)).toList();
            ServiceProvider current = providers.get(replacement.provider().id());
            // Use current scheduling configuration while retaining the agreed appointment snapshots.
            Reservation reservation = ReservationRules.validate(replacement, strategy, others, time, current);
            commit(reservation, original.appointment(), AppointmentEvent.Type.RESCHEDULED, time);
            return replacement;
        });
    }
    @Override public Appointment transition(AppointmentId id, AppointmentTransition command) {
        return write(() -> {
            Reservation original = required(id);
            DomainTime time = transactionTime();
            Appointment replacement = Validation.required(command, "command").apply(original.appointment(), time);
            Reservation reservation = new Reservation(replacement, original.preparationBuffer());
            commit(reservation, original.appointment(), switch (command) {
                case CONFIRM -> AppointmentEvent.Type.CONFIRMED;
                case CANCEL -> AppointmentEvent.Type.CANCELLED;
                case COMPLETE -> AppointmentEvent.Type.COMPLETED;
            }, time);
            return replacement;
        });
    }
    private Reservation required(AppointmentId id) {
        Reservation result = entries.get(Validation.required(id, "id"));
        if (result == null) throw new RepositoryException(RepositoryException.Reason.NOT_FOUND, "Appointment not found");
        return result;
    }
    private void requireCatalog(Appointment a) {
        if (!a.customer().equals(customers.get(a.customer().id()))
                || !a.provider().equals(providers.get(a.provider().id()))
                || !a.service().equals(services.get(a.service().id()))) {
            throw new RepositoryException(RepositoryException.Reason.CATALOG_MISMATCH,
                    "Customer, provider and service must match the repository catalog");
        }
    }
    private DomainTime transactionTime() {
        return new DomainTime(zone.getId(), Clock.fixed(clock.instant(), zone));
    }
    private <T> T read(Supplier<T> operation) {
        lock.lock();
        try { return operation.get(); } finally { lock.unlock(); }
    }
    private <T> T write(Supplier<T> operation) {
        // A user-supplied strategy may call back into the repository; nested writes would invalidate its snapshot.
        if (lock.isHeldByCurrentThread()) {
            throw new RepositoryException(RepositoryException.Reason.REENTRANT_WRITE, "Nested repository writes are forbidden");
        }
        return read(operation);
    }
}
