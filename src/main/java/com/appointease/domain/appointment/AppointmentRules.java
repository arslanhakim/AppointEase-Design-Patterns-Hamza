package com.appointease.domain.appointment;
import com.appointease.domain.model.*;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.time.DomainTime;
import java.time.Instant;
import java.util.List;
/** Shared intrinsic slot rules; no global reservation. */
public final class AppointmentRules {
    private AppointmentRules() { }
    public static void validateSlot(ServiceProvider provider, TimeSlot candidate, DomainTime time, Instant now) {
        if (!candidate.start().isAfter(now)) {
            throw new DomainValidationException("Appointment start must be strictly in the future");
        }
        validateWorkingHours(provider, candidate, time);
    }
    /** Working-hour containment also applies to preparation, which need not start after now. */
    public static void validateWorkingHours(ServiceProvider provider, TimeSlot candidate, DomainTime time) {
        var date = candidate.start().atZone(time.zone()).toLocalDate();
        List<WorkingPeriod> periods = provider.workingHours().getOrDefault(date.getDayOfWeek(), List.of());
        // Resolve every dated boundary explicitly, so DST gaps cannot normalize silently.
        List<TimeSlot> datedHours = periods.stream().map(period -> new TimeSlot(
                time.resolve(date.atTime(period.start())).toInstant(),
                time.resolve(date.atTime(period.end())).toInstant())).toList();
        boolean fits = datedHours.stream().anyMatch(hours -> !candidate.start().isBefore(hours.start())
                && !candidate.end().isAfter(hours.end()));
        if (!fits) {
            throw new DomainValidationException("Appointment must fit one provider working interval");
        }
    }
}
