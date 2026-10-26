package com.appointease.domain.scheduling;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
import com.appointease.domain.appointment.AppointmentRules;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.time.DomainTime;
import java.util.List;

/** Stateless validation against an immutable snapshot supplied inside the repository transaction. */
public final class ReservationRules {
    private ReservationRules() { }
    public static Reservation validate(Appointment candidate, AvailabilityStrategy strategy,
            List<Reservation> schedule, DomainTime time) {
        Validation.required(candidate, "candidate");
        Validation.required(strategy, "strategy");
        schedule = List.copyOf(schedule);
        if (!candidate.businessZone().equals(time.zone())) {
            throw new DomainValidationException("Reservation must use the repository business zone");
        }
        // Reuse all intrinsic validation, including exact duration, supported mode/service and current time.
        new ValidatedAppointmentSpec(candidate.id(), candidate.customer(), candidate.provider(), candidate.service(),
                candidate.slot(), candidate.modeDetails(), candidate.enhancements(), time);
        requireFree(candidate, candidate.slot(), schedule);
        Reservation result = new Reservation(candidate, strategy.preparationBuffer(candidate.provider()));
        AppointmentRules.validateWorkingHours(candidate.provider(), result.effectiveSlot(), time);
        requireFree(candidate, result.effectiveSlot(), schedule);
        return result;
    }
    private static void requireFree(Appointment candidate, TimeSlot slot, List<Reservation> schedule) {
        for (Reservation existing : schedule) {
            if (existing.blocksAvailability()
                    && existing.appointment().provider().id().equals(candidate.provider().id())
                    && slot.overlaps(existing.effectiveSlot())) {
                throw new SlotUnavailableException();
            }
        }
    }
}
