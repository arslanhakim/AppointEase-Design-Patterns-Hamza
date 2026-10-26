package com.appointease.domain.creation;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ServiceAppointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
public final class ServiceAppointmentCreator extends AppointmentCreator {
    public ServiceAppointmentCreator() { super(AppointmentKind.SERVICE); }
    @Override protected Appointment createAppointment(ValidatedAppointmentSpec spec) {
        return new ServiceAppointment(spec);
    }
}
