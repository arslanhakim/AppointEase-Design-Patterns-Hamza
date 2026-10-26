package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.time.LocalTime;

/** One same-day working interval; overnight hours are outside the first release. */
public record WorkingPeriod(LocalTime start, LocalTime end) {
    public WorkingPeriod {
        Validation.required(start, "working start");
        Validation.required(end, "working end");
        if (!start.isBefore(end)) {
            throw new DomainValidationException("Working start must precede end on the same day");
        }
    }
}
