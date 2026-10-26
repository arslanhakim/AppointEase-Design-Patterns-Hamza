package com.appointease.application;
import com.appointease.application.port.*;
import com.appointease.domain.creation.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.time.DomainTime;
import java.util.*;
/** Catalog resolution and construction pipeline kept separate from lifecycle orchestration. */
public final class AppointmentAssembler {
    private final AppointmentRepository repository;
    private final Map<AppointmentKind, AppointmentCreator> creators;
    private final DomainTime time;
    public AppointmentAssembler(AppointmentRepository repository, Map<AppointmentKind, AppointmentCreator> creators, DomainTime time) {
        this.repository = Objects.requireNonNull(repository); this.creators = Map.copyOf(creators);
        this.time = Objects.requireNonNull(time);
    }
    public Appointment assemble(BookingRequest request) {
        Objects.requireNonNull(request);
        var customer = repository.customer(request.customer()).orElseThrow(AppointmentAssembler::missing);
        var provider = repository.providers().stream().filter(p -> p.id().equals(request.provider())).findFirst().orElseThrow(AppointmentAssembler::missing);
        var service = repository.services().stream().filter(s -> s.id().equals(request.service())).findFirst().orElseThrow(AppointmentAssembler::missing);
        return new AppointmentBuilder().id(request.id()).customer(customer).provider(provider).service(service)
                .start(request.start()).mode(request.mode()).appointmentService(request.extras())
                .build(creators.get(service.kind()), time);
    }
    private static RepositoryException missing() {
        return new RepositoryException(RepositoryException.Reason.NOT_FOUND, "Catalog entry not found");
    }
}
