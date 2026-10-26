package com.appointease.domain;

import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.time.DomainTime;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DomainFixtures {
    static final Instant START = Instant.parse("2026-10-27T08:00:00Z");
    static final Instant END = START.plus(Duration.ofMinutes(30));
    static final Instant BEFORE = START.minusSeconds(60);

    private DomainFixtures() { }

    static DomainTime time(Instant instant) {
        return new DomainTime(Clock.fixed(instant, ZoneOffset.UTC));
    }

    static Customer customer() { return new Customer(new CustomerId("customer-demo-001"), "Demo Customer"); }

    static Service service() {
        return new Service(new ServiceId("service-demo-001"), "Consultation", Duration.ofMinutes(30),
                new Money(new BigDecimal("40.00")), AppointmentKind.CONSULTATION);
    }

    static Map<DayOfWeek, List<WorkingPeriod>> hours() {
        return Stream.of(DayOfWeek.values()).collect(Collectors.toMap(day -> day,
                day -> List.of(new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(17, 0)))));
    }

    static ServiceProvider provider() {
        return new ServiceProvider(new ProviderId("provider-demo-001"), "Demo Provider", ProviderCategory.CONSULTING,
                Set.of(service().id()), Set.of(AppointmentMode.ONLINE, AppointmentMode.IN_PERSON),
                hours(), Duration.ofMinutes(15));
    }

    static BookingOptions options() { return new BookingOptions(Map.of()); }

    static Appointment pending() {
        return appointment(new AppointmentId("appointment-demo-001"), customer(), provider(), service(),
                START, AppointmentMode.ONLINE, options(), time(BEFORE));
    }
    static Appointment appointment(AppointmentId id, Customer customer, ServiceProvider provider, Service service,
            Instant start, AppointmentMode mode, BookingOptions options, DomainTime time) {
        Validation.required(mode, "mode");
        Validation.required(service, "service");
        com.appointease.domain.mode.AppointmentModeFactory family = mode == AppointmentMode.ONLINE
                ? new com.appointease.domain.mode.OnlineModeFactory("test-room")
                : new com.appointease.domain.mode.InPersonModeFactory("Test office");
        com.appointease.domain.creation.AppointmentCreator creator = service.kind() == AppointmentKind.CONSULTATION
                ? new com.appointease.domain.creation.ConsultationAppointmentCreator()
                : new com.appointease.domain.creation.ServiceAppointmentCreator();
        return new com.appointease.domain.creation.AppointmentBuilder().id(id).customer(customer).provider(provider)
                .service(service).start(start).mode(family)
                .appointmentService(new com.appointease.domain.addon.BaseAppointmentService(options)).build(creator, time);
    }
}
