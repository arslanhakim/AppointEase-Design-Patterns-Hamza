package com.appointease.domain.appointment;
import com.appointease.domain.model.*;
import com.appointease.domain.state.AppointmentState;
public final class ConsultationAppointment extends Appointment {
    public ConsultationAppointment(ValidatedAppointmentSpec spec) { super(spec, AppointmentKind.CONSULTATION); }
    private ConsultationAppointment(ConsultationAppointment original, AppointmentState next) { super(original, next); }
    @Override protected Appointment withState(AppointmentState next) { return new ConsultationAppointment(this, next); }
    private ConsultationAppointment(ConsultationAppointment original, ValidatedAppointmentSpec next) { super(original, next); }
    @Override protected Appointment withSpec(ValidatedAppointmentSpec next) { return new ConsultationAppointment(this, next); }
}
