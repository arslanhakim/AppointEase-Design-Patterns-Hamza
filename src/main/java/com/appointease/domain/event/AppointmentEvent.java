package com.appointease.domain.event;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import java.time.Instant;
import java.util.Objects;

/** Immutable snapshots; sequence is global to one repository, revision is per appointment. */
public record AppointmentEvent(String id, long sequence, long revision, Type type,
        Instant occurredAt, Appointment before, Appointment after) {
    public enum Type { RESERVED, CONFIRMED, RESCHEDULED, CANCELLED, COMPLETED }
    public AppointmentEvent {
        id = Validation.text(id, "event ID");
        Objects.requireNonNull(type);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(after);
        if (sequence < 1 || revision < 1) throw new IllegalArgumentException("Positive event counters required");
        if ((type == Type.RESERVED) != (before == null)) throw new IllegalArgumentException("Invalid before snapshot");
        if (before != null && !before.id().equals(after.id())) throw new IllegalArgumentException("Identity changed");
    }
}
