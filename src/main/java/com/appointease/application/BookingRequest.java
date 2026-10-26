package com.appointease.application;
import com.appointease.domain.model.*;
import com.appointease.domain.mode.AppointmentModeFactory;
import com.appointease.domain.addon.AppointmentService;
import com.appointease.domain.scheduling.AvailabilityStrategy;
import java.time.Instant;
/** Retain this immutable request for command-ID retries; collaborators must be immutable/pure. */
public record BookingRequest(AppointmentId id, CustomerId customer, ProviderId provider, ServiceId service,
        Instant start, AppointmentModeFactory mode, AppointmentService extras, AvailabilityStrategy strategy) { }
