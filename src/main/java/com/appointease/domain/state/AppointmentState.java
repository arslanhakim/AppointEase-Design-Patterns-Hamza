package com.appointease.domain.state;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.exception.InvalidTransitionException;
import com.appointease.domain.model.AppointmentStatus;
import com.appointease.domain.model.TimeSlot;
import java.time.Instant;

/** Stateless polymorphic lifecycle decisions; never mutates the aggregate. */
public sealed interface AppointmentState permits PendingState, ConfirmedState, CompletedState, CancelledState {
    AppointmentStatus status();

    default AppointmentState confirm(TimeSlot slot, Instant now) { throw rejected("confirm"); }
    default AppointmentState cancel(TimeSlot slot, Instant now) { throw rejected("cancel"); }
    default AppointmentState complete(TimeSlot slot, Instant now) { throw rejected("complete"); }
    default void requireRescheduling(TimeSlot slot, Instant now) { throw rejected("reschedule"); }

    private InvalidTransitionException rejected(String operation) {
        return new InvalidTransitionException("Cannot " + operation + " appointment in " + status());
    }

    static void requireBeforeStart(TimeSlot slot, Instant now) {
        if (slot == null || now == null || !now.isBefore(slot.start())) {
            throw new DomainValidationException("Operation requires a future appointment start");
        }
    }
}
