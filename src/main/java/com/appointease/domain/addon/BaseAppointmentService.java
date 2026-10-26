package com.appointease.domain.addon;
import com.appointease.domain.model.*;
import java.util.List;
import java.util.Map;
public final class BaseAppointmentService implements AppointmentService {
    private final BookingOptions options;
    public BaseAppointmentService() { this(new BookingOptions(Map.of())); }
    /** Imports a fee snapshot without inventing notification behavior. */
    public BaseAppointmentService(BookingOptions options) { this.options = Validation.required(options, "options"); }
    @Override public BookingEnhancements quote(Service service) {
        return new BookingEnhancements(Validation.required(service, "service").baseFee(), options, List.of());
    }
}
