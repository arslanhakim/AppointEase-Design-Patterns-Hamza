package com.appointease.infrastructure.notification;
import com.appointease.application.event.AppointmentListener;
import com.appointease.domain.event.AppointmentEvent;
import java.util.*;
/** Simulated notifications: immutable event snapshots, no email or SMS. */
public final class NotificationListener implements AppointmentListener {
    private final List<AppointmentEvent> notifications = new ArrayList<>();
    public synchronized void onAppointmentEvent(AppointmentEvent event) { notifications.add(event); }
    public synchronized List<AppointmentEvent> notifications() { return List.copyOf(notifications); }
}
