package com.appointease.domain.addon;
import com.appointease.domain.model.*;
import java.math.BigDecimal;
public final class PriorityBookingDecorator extends AppointmentServiceDecorator {
    private final Money fee;
    public PriorityBookingDecorator(AppointmentService delegate) { this(delegate, new Money(new BigDecimal("10.00"))); }
    public PriorityBookingDecorator(AppointmentService delegate, Money fee) {
        super(delegate);
        this.fee = Validation.required(fee, "fee");
    }
    @Override public BookingEnhancements quote(Service service) {
        return super.quote(service).with("priority", fee, "Priority handling requested; all scheduling constraints still apply.");
    }
}
