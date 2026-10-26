package com.appointease.application.event;
import com.appointease.domain.event.AppointmentEvent;
import java.util.*;
/** Registration order, snapshot subscriptions, one attempt per event ID. No automatic retry. */
public final class AppointmentEventPublisher {
    private final List<AppointmentListener> listeners = new ArrayList<>();
    private final Set<String> attempted = new HashSet<>();
    private boolean delivering;
    public synchronized void subscribe(AppointmentListener listener) {
        Objects.requireNonNull(listener);
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public synchronized void unsubscribe(AppointmentListener listener) { listeners.remove(listener); }
    public synchronized List<DeliveryFailure> publish(AppointmentEvent event) {
        Objects.requireNonNull(event);
        if (delivering) throw new IllegalStateException("Reentrant publication forbidden");
        if (!attempted.add(event.id())) return List.of();
        List<DeliveryFailure> failures = new ArrayList<>();
        delivering = true;
        try {
            for (AppointmentListener listener : List.copyOf(listeners)) {
                try { listener.onAppointmentEvent(event); }
                catch (RuntimeException ex) { failures.add(new DeliveryFailure(event.id(), listener.getClass().getName(), ex)); }
            }
        } finally { delivering = false; }
        return List.copyOf(failures);
    }
}
