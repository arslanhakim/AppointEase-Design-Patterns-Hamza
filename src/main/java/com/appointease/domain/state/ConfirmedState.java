package com.appointease.domain.state;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.model.AppointmentStatus;
import com.appointease.domain.model.TimeSlot;
import java.time.Instant;

public final class ConfirmedState implements AppointmentState {
    @Override public AppointmentStatus status() { return AppointmentStatus.CONFIRMED; }

    @Override public AppointmentState cancel(TimeSlot slot, Instant now) {
        AppointmentState.requireBeforeStart(slot, now);
        return new CancelledState();
    }

    @Override public AppointmentState complete(TimeSlot slot, Instant now) {
        if (slot == null || now == null || now.isBefore(slot.end())) {
            throw new DomainValidationException("Completion requires appointment end to have been reached");
        }
        return new CompletedState();
    }

    @Override public void requireRescheduling(TimeSlot slot, Instant now) {
        AppointmentState.requireBeforeStart(slot, now);
    }
}
