package com.appointease.domain.scheduling;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.exception.DomainValidationException;
import java.time.DateTimeException;
import java.time.Duration;

/** Immutable stored policy snapshot; never retains a caller's strategy object. */
public record Reservation(Appointment appointment, Duration preparationBuffer) {
    public Reservation {
        Validation.required(appointment, "appointment");
        if (Validation.required(preparationBuffer, "buffer").isNegative()) {
            throw new DomainValidationException("Buffer must not be negative");
        }
        effectiveSlot(appointment, preparationBuffer);
    }
    public boolean blocksAvailability() { return appointment.status() != AppointmentStatus.CANCELLED; }
    public TimeSlot effectiveSlot() { return effectiveSlot(appointment, preparationBuffer); }
    private static TimeSlot effectiveSlot(Appointment appointment, Duration buffer) {
        try {
            return new TimeSlot(appointment.slot().start().minus(buffer), appointment.slot().end());
        } catch (DateTimeException | ArithmeticException ex) {
            throw new DomainValidationException("Preparation interval is outside the supported instant range");
        }
    }
}
