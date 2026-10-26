package com.appointease.domain.appointment;
import com.appointease.domain.model.*;
import com.appointease.domain.state.AppointmentState;
public final class ServiceAppointment extends Appointment {
    public ServiceAppointment(ValidatedAppointmentSpec spec) { super(spec, AppointmentKind.SERVICE); }
    private ServiceAppointment(ServiceAppointment original, AppointmentState next) { super(original, next); }
    @Override protected Appointment withState(AppointmentState next) { return new ServiceAppointment(this, next); }
    private ServiceAppointment(ServiceAppointment original, ValidatedAppointmentSpec next) { super(original, next); }
    @Override protected Appointment withSpec(ValidatedAppointmentSpec next) { return new ServiceAppointment(this, next); }
}
