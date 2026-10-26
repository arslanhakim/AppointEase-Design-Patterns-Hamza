package com.appointease.domain.appointment;
import com.appointease.domain.model.*;
import com.appointease.domain.mode.AppointmentModeDetails;
import com.appointease.domain.addon.BookingEnhancements;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.state.AppointmentState;
import com.appointease.domain.state.PendingState;
import com.appointease.domain.time.DomainTime;
import java.time.Instant;
import java.time.ZoneId;
/** Immutable sealed lifecycle aggregate; construction does not establish a reservation. */
public abstract sealed class Appointment permits ConsultationAppointment, ServiceAppointment {
    private final ValidatedAppointmentSpec spec;
    private final AppointmentState state;
    Appointment(ValidatedAppointmentSpec spec, AppointmentKind kind) {
        this.spec = Validation.required(spec, "spec");
        if (spec.service().kind() != kind) throw new DomainValidationException("Appointment kind mismatch");
        state = new PendingState();
    }
    Appointment(Appointment original, AppointmentState next) {
        spec = original.spec;
        state = Validation.required(next, "state");
    }
    protected abstract Appointment withState(AppointmentState next);
    protected abstract Appointment withSpec(ValidatedAppointmentSpec next);
    Appointment(Appointment original, ValidatedAppointmentSpec next) {
        spec = Validation.required(next, "spec");
        state = original.state;
    }
    /** Detached replacement only; the repository must atomically validate and reserve it. */
    public Appointment reschedule(Instant newStart, DomainTime time) {
        TimeSlot proposed = validateReschedule(newStart, time);
        return withSpec(new ValidatedAppointmentSpec(id(), customer(), provider(), service(), proposed,
                modeDetails(), enhancements(), time));
    }
    public AppointmentId id() { return spec.id(); }
    public Customer customer() { return spec.customer(); }
    public ServiceProvider provider() { return spec.provider(); }
    public Service service() { return spec.service(); }
    public TimeSlot slot() { return spec.slot(); }
    public AppointmentMode mode() { return spec.modeDetails().meeting().mode(); }
    public AppointmentModeDetails modeDetails() { return spec.modeDetails(); }
    public BookingEnhancements enhancements() { return spec.enhancements(); }
    public BookingOptions options() { return enhancements().options(); }
    public ZoneId businessZone() { return spec.businessZone(); }
    public AppointmentStatus status() { return state.status(); }
    public Money totalFee() { return enhancements().totalFee(); }
    public Appointment confirm(DomainTime time) {
        return withState(state.confirm(slot(), Validation.required(time, "time").now()));
    }
    public Appointment cancel(DomainTime time) {
        return withState(state.cancel(slot(), Validation.required(time, "time").now()));
    }
    public Appointment complete(DomainTime time) {
        return withState(state.complete(slot(), Validation.required(time, "time").now()));
    }
    /** Proposal only; no mutable slot or reservation. */
    public TimeSlot validateReschedule(Instant newStart, DomainTime time) {
        Validation.required(time, "time");
        Instant now = time.now();
        state.requireRescheduling(slot(), now);
        if (!businessZone().equals(time.zone())) {
            throw new DomainValidationException("Rescheduling must use the appointment business zone");
        }
        TimeSlot proposed = TimeSlot.startingAt(newStart, service().duration());
        AppointmentRules.validateSlot(provider(), proposed, time, now);
        return proposed;
    }
}
