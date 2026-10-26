package com.appointease.domain.addon;
import com.appointease.domain.model.*;
import java.math.BigDecimal;
public final class ReminderServiceDecorator extends AppointmentServiceDecorator {
    private final Money fee;
    public ReminderServiceDecorator(AppointmentService delegate) { this(delegate, new Money(new BigDecimal("2.00"))); }
    public ReminderServiceDecorator(AppointmentService delegate, Money fee) {
        super(delegate);
        this.fee = Validation.required(fee, "fee");
    }
    @Override public BookingEnhancements quote(Service service) {
        return super.quote(service).with("reminder", fee, "Reminder requested for this appointment; delivery deferred to event processing.");
    }
}
