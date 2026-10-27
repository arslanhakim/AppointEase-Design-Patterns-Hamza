package com.appointease.presentation.desktop;

import com.appointease.application.BookingFacade;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.AppointmentStatus;
import com.appointease.domain.model.Customer;
import com.appointease.domain.time.DomainTime;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Read-only projection. Status grouping is display logic, never permission to mutate. */
public final class OverviewModel {
    private final BookingFacade facade;
    private final DomainTime time;

    public OverviewModel(BookingFacade facade, List<Customer> customers, DomainTime time) {
        this.facade = Objects.requireNonNull(facade);
        this.time = Objects.requireNonNull(time);
    }

    public DomainTime time() { return time; }

    public Snapshot load(LocalDate day) {
        var appointments = facade.customers().stream().flatMap(c -> facade.history(c.id()).stream())
                .filter(a -> a.slot().start().atZone(time.zone()).toLocalDate().equals(day))
                .sorted(Comparator.comparing((Appointment a) -> a.slot().start()).thenComparing(a -> a.id().value()))
                .toList();
        long confirmed = appointments.stream().filter(a -> a.status() == AppointmentStatus.CONFIRMED).count();
        long pending = appointments.stream().filter(a -> a.status() == AppointmentStatus.PENDING).count();
        long providers = appointments.stream().filter(a -> a.status() != AppointmentStatus.CANCELLED)
                .map(a -> a.provider().id()).distinct().count();
        return new Snapshot(day, appointments, confirmed, pending, providers);
    }

    public record Snapshot(LocalDate day, List<Appointment> appointments, long confirmed, long pending, long providers) {
        public Snapshot { appointments = List.copyOf(appointments); }
    }
}
