package com.appointease.domain.addon;
import com.appointease.domain.model.*;
public abstract class AppointmentServiceDecorator implements AppointmentService {
    private final AppointmentService delegate;
    protected AppointmentServiceDecorator(AppointmentService delegate) {
        this.delegate = Validation.required(delegate, "delegate");
    }
    @Override public BookingEnhancements quote(Service service) { return delegate.quote(service); }
}
