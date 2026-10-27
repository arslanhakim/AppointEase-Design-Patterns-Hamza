package com.appointease.application;
import com.appointease.application.port.*;
import com.appointease.domain.creation.AppointmentCreator;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import java.time.Instant;
import java.util.*;

public final class BookingFacade {
    private final AppointmentRepository repository;
    private final CommittedEventDispatcher dispatcher;
    private final AppointmentAssembler assembler;
    private final DomainTime time;
    public BookingFacade(CommittedEventDispatcher dispatcher, Map<AppointmentKind, AppointmentCreator> creators, DomainTime time) {
        this.dispatcher = Objects.requireNonNull(dispatcher); this.repository = dispatcher.repository();
        this.time = Objects.requireNonNull(time); this.assembler = new AppointmentAssembler(repository, creators, time);
    }
    public List<ServiceProvider> providers() { return repository.providers(); }
    public List<Service> services() { return repository.services(); }
    public List<Customer> customers() { return repository.customers(); }
    public Optional<Customer> customer(CustomerId id) { return repository.customer(id); }
    public Customer createCustomer(String name) {
        return repository.createCustomer(new Customer(new CustomerId(UUID.randomUUID().toString()), name));
    }
    public Customer updateCustomer(Customer expected, String name) {
        return repository.updateCustomer(expected, new Customer(expected.id(), name));
    }
    public ServiceProvider createProvider(ServiceProvider value) { return repository.createProvider(value); }
    public ServiceProvider updateProvider(ServiceProvider expected, ServiceProvider value) { return repository.updateProvider(expected, value); }
    public Service createService(Service value) { return repository.createService(value); }
    public Service updateService(Service expected, Service value) { return repository.updateService(expected, value); }
    /** Advisory local availability; external mirror conflicts are reported after commit. Invalid input still throws. */
    public boolean available(BookingRequest request) {
        var candidate = assembler.assemble(request);
        try { ReservationRules.validate(candidate, request.strategy(), repository.reservations(), time); return true; }
        catch (SlotUnavailableException ex) { return false; }
    }
    public BookingResult book(String commandId, BookingRequest request) {
        return dispatcher.execute(commandId, List.of("book", request),
                () -> repository.reserve(assembler.assemble(request), request.strategy()));
    }
    /** A desktop review is an agreement to these exact catalog values, never an implicit new quote. */
    public BookingResult bookReviewed(String commandId, BookingRequest request, Customer customer,
            ServiceProvider provider, Service service) {
        return dispatcher.execute(commandId, List.of("reviewed-book", request, customer, provider, service), () -> {
            var candidate = assembler.assemble(request);
            if (!candidate.customer().equals(customer) || !candidate.provider().equals(provider) || !candidate.service().equals(service))
                throw new com.appointease.domain.exception.DomainValidationException("Catalog details changed. Return to edit and review the appointment again.");
            return repository.reserve(candidate, request.strategy());
        });
    }
    public BookingResult confirm(String commandId, AppointmentId id) { return transition(commandId, id, AppointmentTransition.CONFIRM); }
    public BookingResult cancel(String commandId, AppointmentId id) { return transition(commandId, id, AppointmentTransition.CANCEL); }
    public BookingResult complete(String commandId, AppointmentId id) { return transition(commandId, id, AppointmentTransition.COMPLETE); }
    private BookingResult transition(String key, AppointmentId id, AppointmentTransition command) {
        return dispatcher.execute(key, List.of(command, id), () -> repository.transition(id, command));
    }
    public BookingResult reschedule(String key, AppointmentId id, Instant start, AvailabilityStrategy strategy) {
        return dispatcher.execute(key, List.of("reschedule", id, start, strategy), () -> repository.reschedule(id, start, strategy));
    }
    public Optional<Appointment> details(AppointmentId id) { return repository.find(id); }
    public List<Appointment> history(CustomerId id) { return repository.history(id); }
    public List<AppointmentEvent> events(AppointmentId id) {
        return repository.events().stream().filter(e -> e.after().id().equals(id)).toList();
    }
}
