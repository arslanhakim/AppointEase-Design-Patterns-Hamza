package com.appointease.domain.creation;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
import com.appointease.domain.mode.*;
import com.appointease.domain.addon.*;
import com.appointease.domain.time.DomainTime;
import java.time.Instant;
/** Mutable assembly only; each build validates a detached snapshot. Not thread-safe. */
public final class AppointmentBuilder {
    private AppointmentId id;
    private Customer customer;
    private ServiceProvider provider;
    private Service service;
    private Instant start;
    private TimeSlot slot;
    private AppointmentModeDetails mode;
    private AppointmentService appointmentService = new BaseAppointmentService();
    public AppointmentBuilder id(AppointmentId value) { id = value; return this; }
    public AppointmentBuilder customer(Customer value) { customer = value; return this; }
    public AppointmentBuilder provider(ServiceProvider value) { provider = value; return this; }
    public AppointmentBuilder service(Service value) { service = value; return this; }
    /** End derives from duration. Last start/slot setter wins. */
    public AppointmentBuilder start(Instant value) { start = value; slot = null; return this; }
    public AppointmentBuilder slot(TimeSlot value) { slot = value; start = null; return this; }
    public AppointmentBuilder mode(AppointmentModeFactory factory) {
        Validation.required(factory, "mode factory");
        return mode(factory.createMeetingDetails(), factory.createParticipationInstructions());
    }
    public AppointmentBuilder mode(MeetingDetails details, ParticipationInstructions instructions) {
        mode = new AppointmentModeDetails(details, instructions); return this;
    }
    public AppointmentBuilder appointmentService(AppointmentService value) {
        appointmentService = Validation.required(value, "appointment service"); return this;
    }
    public Appointment build(AppointmentCreator creator, DomainTime time) {
        Validation.required(creator, "creator");
        Validation.required(service, "service");
        TimeSlot derived = slot == null ? TimeSlot.startingAt(start, service.duration()) : slot;
        var spec = new ValidatedAppointmentSpec(id, customer, provider, service, derived, mode,
                appointmentService.quote(service), time);
        return creator.create(spec);
    }
}
