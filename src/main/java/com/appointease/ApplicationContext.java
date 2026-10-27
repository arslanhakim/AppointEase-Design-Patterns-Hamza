package com.appointease;

import com.appointease.application.BookingFacade;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.model.Customer;
import com.appointease.domain.time.DomainTime;
import java.util.List;
import java.util.function.Supplier;

/** Instance-owned presentation dependencies. No storage implementation escapes composition. */
public record ApplicationContext(BookingFacade facade, List<Customer> customers, DomainTime time,
        Supplier<List<AppointmentEvent>> notifications, Supplier<List<AppointmentEvent>> audit) {
    public ApplicationContext {
        customers = List.copyOf(customers);
    }
}
