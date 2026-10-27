package com.appointease.application.port;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.scheduling.AvailabilityStrategy;
import com.appointease.domain.scheduling.Reservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Atomic commands only: no public save or check-then-write path. Reads are immutable snapshots. */
public interface AppointmentRepository {
    List<Customer> customers();
    Customer createCustomer(Customer value);
    Customer updateCustomer(Customer expected, Customer replacement);
    ServiceProvider createProvider(ServiceProvider value);
    ServiceProvider updateProvider(ServiceProvider expected, ServiceProvider replacement);
    Service createService(Service value);
    Service updateService(Service expected, Service replacement);
    List<ServiceProvider> providers();
    List<Service> services();
    Optional<Customer> customer(CustomerId id);
    /** Append-only commit log, ordered globally; immutable snapshots. */
    List<com.appointease.domain.event.AppointmentEvent> events();
    Optional<Appointment> find(AppointmentId id);
    /** All statuses, sorted by start then ID; this is current appointment history, not an event log. */
    List<Appointment> history(CustomerId customerId);
    List<Reservation> reservations();
    Appointment reserve(Appointment candidate, AvailabilityStrategy strategy);
    /** Replaces the policy snapshot on success; excludes only this appointment from conflict checks. */
    Appointment reschedule(AppointmentId id, Instant newStart, AvailabilityStrategy strategy);
    Appointment transition(AppointmentId id, AppointmentTransition command);
}
