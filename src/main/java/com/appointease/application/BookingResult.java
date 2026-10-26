package com.appointease.application;
import com.appointease.domain.appointment.Appointment;
import com.appointease.application.event.DeliveryFailure;
import java.util.List;
public record BookingResult(Appointment appointment, long revision, List<DeliveryFailure> warnings) {
    public BookingResult { warnings = List.copyOf(warnings); }
}
