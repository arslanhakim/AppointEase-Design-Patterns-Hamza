package com.appointease.infrastructure.notification;
import com.appointease.application.event.AppointmentListener;
import com.appointease.domain.event.AppointmentEvent;
import java.util.*;
public final class AuditListener implements AppointmentListener {
    private final List<AppointmentEvent> history = new ArrayList<>();
    public synchronized void onAppointmentEvent(AppointmentEvent event) { history.add(event); }
    public synchronized List<AppointmentEvent> history() { return List.copyOf(history); }
}
