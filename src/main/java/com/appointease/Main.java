package com.appointease;

import com.appointease.application.*;
import com.appointease.application.event.*;
import com.appointease.domain.creation.*;
import com.appointease.domain.model.*;
import com.appointease.infrastructure.calendar.*;
import com.appointease.infrastructure.memory.InMemoryAppointmentRepository;
import com.appointease.infrastructure.notification.*;
import com.appointease.infrastructure.seed.DemoCatalog;
import com.appointease.infrastructure.seed.DemoSeed;
import com.appointease.presentation.ConsoleApplication;
import java.io.*;
import java.time.*;
import java.util.Map;

/** Sole composition root; demo setup uses the same facade as interactive commands. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            create(System.in, System.out, Clock.systemUTC(),
                    ZoneId.of(System.getProperty("appointease.zone", "Europe/Vilnius"))).run();
        } catch (DateTimeException ex) {
            System.err.println("Configuration error: invalid appointease.zone. " + ex.getMessage());
        }
    }

    public static ConsoleApplication create(InputStream input, OutputStream output, Clock clock, ZoneId zone) {
        var catalog = DemoCatalog.create();
        var seed = new DemoSeed(clock, zone);
        var time = seed.time();
        var repository = new InMemoryAppointmentRepository(catalog.customers(), catalog.providers(), catalog.services(), seed.clock(), zone);
        var publisher = new AppointmentEventPublisher();
        var notifications = new NotificationListener();
        var audit = new AuditListener();
        publisher.subscribe(notifications);
        publisher.subscribe(audit);
        var dispatcher = new CommittedEventDispatcher(repository, publisher,
                new ExternalCalendarAdapter(new SimulatedExternalCalendarClient()));
        var facade = new BookingFacade(dispatcher, Map.of(AppointmentKind.CONSULTATION, new ConsultationAppointmentCreator(),
                AppointmentKind.SERVICE, new ServiceAppointmentCreator()), time);
        seed.populate(facade);
        return new ConsoleApplication(input, output, facade, catalog.customers(), time,
                notifications::notifications, audit::history);
    }
}
