package com.appointease.domain.creation;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ConsultationAppointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
public final class ConsultationAppointmentCreator extends AppointmentCreator {
    public ConsultationAppointmentCreator() { super(AppointmentKind.CONSULTATION); }
    @Override protected Appointment createAppointment(ValidatedAppointmentSpec spec) {
        return new ConsultationAppointment(spec);
    }
}
