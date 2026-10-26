package com.appointease.application.port;
import com.appointease.domain.event.AppointmentEvent;
/** Post-commit mirror only. External conflicts warn; local repository owns capacity. */
public interface CalendarPort { void synchronize(AppointmentEvent event); }
