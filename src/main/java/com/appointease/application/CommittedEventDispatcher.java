package com.appointease.application;
import com.appointease.application.event.AppointmentEventPublisher;
import com.appointease.application.event.DeliveryFailure;
import com.appointease.application.port.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import java.util.*;
import java.util.function.Supplier;

/** One dispatcher per repository/application lifetime, shared by all facades.
 * Serializes delivery and facade command retry receipts, never holds the repository lock during delivery.
 * Listeners must consume snapshots only, not issue commands or wait for command threads.
 */
public final class CommittedEventDispatcher {
    private record Receipt(Object command, BookingResult result) { }
    private final AppointmentRepository repository;
    private final AppointmentEventPublisher publisher;
    private final CalendarPort calendar;
    private final Map<String, Receipt> receipts = new HashMap<>();
    private final List<DeliveryFailure> failures = new ArrayList<>();
    private int cursor;
    private boolean active;
    public CommittedEventDispatcher(AppointmentRepository repository, AppointmentEventPublisher publisher, CalendarPort calendar) {
        this.repository = Objects.requireNonNull(repository);
        this.publisher = Objects.requireNonNull(publisher);
        this.calendar = Objects.requireNonNull(calendar);
    }
    public AppointmentRepository repository() { return repository; }
    public synchronized BookingResult execute(String commandId, Object command, Supplier<Appointment> operation) {
        commandId = Validation.text(commandId, "command ID");
        Objects.requireNonNull(command);
        if (active) throw new RepositoryException(RepositoryException.Reason.REENTRANT_WRITE, "Listener commands forbidden");
        Receipt prior = receipts.get(commandId);
        if (prior != null) {
            if (!prior.command().equals(command)) throw new IllegalArgumentException("Command ID reused for different input");
            return prior.result();
        }
        active = true;
        try {
            Appointment appointment = operation.get();
            var event = repository.events().stream().filter(e -> e.after() == appointment).findFirst().orElseThrow();
            drain();
            BookingResult result = new BookingResult(appointment, event.revision(), failures.stream()
                    .filter(f -> f.eventId().equals(event.id())).toList());
            receipts.put(commandId, new Receipt(command, result));
            return result;
        } finally { active = false; }
    }
    public synchronized void dispatchPending() {
        if (active) throw new IllegalStateException("Reentrant dispatch forbidden");
        active = true;
        try { drain(); } finally { active = false; }
    }
    private void drain() {
        var committed = repository.events();
        while (cursor < committed.size()) {
            var event = committed.get(cursor++);
            failures.addAll(publisher.publish(event));
            try { calendar.synchronize(event); }
            catch (RuntimeException ex) { failures.add(new DeliveryFailure(event.id(), "calendar", ex)); }
        }
    }
    public synchronized List<DeliveryFailure> failures() { return List.copyOf(failures); }
}
