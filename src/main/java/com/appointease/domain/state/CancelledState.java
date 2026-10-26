package com.appointease.domain.state;

import com.appointease.domain.model.AppointmentStatus;

/** Terminal; all operations inherit rejection. */
public final class CancelledState implements AppointmentState {
    @Override public AppointmentStatus status() { return AppointmentStatus.CANCELLED; }
}
