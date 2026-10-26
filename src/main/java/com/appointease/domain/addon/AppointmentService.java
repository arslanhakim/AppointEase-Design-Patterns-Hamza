package com.appointease.domain.addon;
import com.appointease.domain.model.Service;
/** Quoting never mutates an appointment or establishes a reservation. */
public interface AppointmentService {
    BookingEnhancements quote(Service service);
}
