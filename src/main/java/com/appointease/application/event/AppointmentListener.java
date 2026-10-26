package com.appointease.application.event;
import com.appointease.domain.event.AppointmentEvent;
@FunctionalInterface
public interface AppointmentListener { void onAppointmentEvent(AppointmentEvent event); }
