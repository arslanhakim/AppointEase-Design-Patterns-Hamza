package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

/** Half-open elapsed-time interval [start, end). */
public record TimeSlot(Instant start, Instant end) {
    public TimeSlot {
        Validation.required(start, "start");
        Validation.required(end, "end");
        if (!start.isBefore(end)) {
            throw new DomainValidationException("Start must precede end");
        }
    }

    public static TimeSlot startingAt(Instant start, Duration duration) {
        Validation.required(start, "start");
        if (Validation.required(duration, "duration").isNegative() || duration.isZero()) {
            throw new DomainValidationException("Duration must be positive");
        }
        try {
            return new TimeSlot(start, start.plus(duration));
        } catch (DateTimeException | ArithmeticException ex) {
            throw new DomainValidationException("Appointment end is outside the supported instant range");
        }
    }

    public boolean overlaps(TimeSlot other) {
        Validation.required(other, "other slot");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
