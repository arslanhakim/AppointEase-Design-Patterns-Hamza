package com.appointease.domain.appointment;
import com.appointease.domain.model.*;
import com.appointease.domain.mode.AppointmentModeDetails;
import com.appointease.domain.addon.BookingEnhancements;
import com.appointease.domain.time.DomainTime;
import com.appointease.domain.exception.DomainValidationException;
import java.time.ZoneId;
/** Immutable validated capability; every constructor call enforces the complete intrinsic contract. */
public final class ValidatedAppointmentSpec {
    private final AppointmentId id;
    private final Customer customer;
    private final ServiceProvider provider;
    private final Service service;
    private final TimeSlot slot;
    private final AppointmentModeDetails modeDetails;
    private final BookingEnhancements enhancements;
    private final ZoneId businessZone;
    public ValidatedAppointmentSpec(AppointmentId id, Customer customer, ServiceProvider provider, Service service,
            TimeSlot slot, AppointmentModeDetails modeDetails, BookingEnhancements enhancements, DomainTime time) {
        this.id = Validation.required(id, "id");
        this.customer = Validation.required(customer, "customer");
        this.provider = Validation.required(provider, "provider");
        this.service = Validation.required(service, "service");
        this.slot = Validation.required(slot, "slot");
        this.modeDetails = Validation.required(modeDetails, "modeDetails");
        this.enhancements = Validation.required(enhancements, "enhancements");
        this.businessZone = Validation.required(time, "time").zone();
        if (!provider.supports(service, modeDetails.meeting().mode())) {
            throw new DomainValidationException("Provider does not support the service and mode");
        }
        if (!slot.equals(TimeSlot.startingAt(slot.start(), service.duration()))) {
            throw new DomainValidationException("Slot must match service duration");
        }
        if (!enhancements.baseFee().equals(service.baseFee())) {
            throw new DomainValidationException("Quote must preserve the service base fee");
        }
        AppointmentRules.validateSlot(provider, slot, time, time.now());
    }
    public AppointmentId id() { return id; }
    public Customer customer() { return customer; }
    public ServiceProvider provider() { return provider; }
    public Service service() { return service; }
    public TimeSlot slot() { return slot; }
    public AppointmentModeDetails modeDetails() { return modeDetails; }
    public BookingEnhancements enhancements() { return enhancements; }
    public ZoneId businessZone() { return businessZone; }
}
