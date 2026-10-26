package com.appointease.domain.state;

import com.appointease.domain.model.AppointmentStatus;
import com.appointease.domain.model.TimeSlot;
import java.time.Instant;

public final class PendingState implements AppointmentState {
    @Override public AppointmentStatus status() { return AppointmentStatus.PENDING; }

    @Override public AppointmentState confirm(TimeSlot slot, Instant now) {
        AppointmentState.requireBeforeStart(slot, now);
        return new ConfirmedState();
    }

    @Override public AppointmentState cancel(TimeSlot slot, Instant now) {
        AppointmentState.requireBeforeStart(slot, now);
        return new CancelledState();
    }

    @Override public void requireRescheduling(TimeSlot slot, Instant now) {
        AppointmentState.requireBeforeStart(slot, now);
    }
}
