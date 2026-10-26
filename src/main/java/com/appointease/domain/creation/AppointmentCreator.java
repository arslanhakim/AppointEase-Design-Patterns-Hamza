package com.appointease.domain.creation;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
import com.appointease.domain.exception.DomainValidationException;
/** Template with shared kind guard and polymorphic product construction. */
public abstract class AppointmentCreator {
    private final AppointmentKind kind;
    protected AppointmentCreator(AppointmentKind kind) { this.kind = Validation.required(kind, "kind"); }
    public final Appointment create(ValidatedAppointmentSpec spec) {
        if (Validation.required(spec, "spec").service().kind() != kind) {
            throw new DomainValidationException("Creator does not support service kind");
        }
        return createAppointment(spec);
    }
    protected abstract Appointment createAppointment(ValidatedAppointmentSpec spec);
}
