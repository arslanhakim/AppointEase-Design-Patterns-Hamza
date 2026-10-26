package com.appointease.presentation;

import com.appointease.application.BookingFacade;
import com.appointease.application.BookingRequest;
import com.appointease.application.BookingResult;
import com.appointease.application.port.RepositoryException;
import com.appointease.domain.addon.*;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.exception.InvalidTransitionException;
import com.appointease.domain.mode.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;

/** Console orchestration and rendering. All booking rules remain behind BookingFacade. */
public final class ConsoleApplication {
    private final PrintWriter out;
    private final ConsoleInput input;
    private final BookingFacade facade;
    private final List<Customer> customers;
    private final DomainTime time;
    private final Supplier<List<AppointmentEvent>> notifications;
    private final Supplier<List<AppointmentEvent>> audit;
    private final List<AppointmentId> aliases = new ArrayList<>();
    private final DateTimeFormatter dates = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm XXX");

    public ConsoleApplication(InputStream input, OutputStream output, BookingFacade facade, List<Customer> customers,
            DomainTime time, Supplier<List<AppointmentEvent>> notifications, Supplier<List<AppointmentEvent>> audit) {
        out = new PrintWriter(output, true, StandardCharsets.UTF_8);
        this.input = new ConsoleInput(input, out);
        this.facade = Objects.requireNonNull(facade);
        this.customers = List.copyOf(customers);
        this.time = Objects.requireNonNull(time);
        this.notifications = Objects.requireNonNull(notifications);
        this.audit = Objects.requireNonNull(audit);
    }

    public void run() {
        out.println("Welcome to AppointEase!");
        out.println("Offline demonstration | Zone: " + time.zone() + " | Now: " + format(time.now()));
        out.println("Hours: Mon-Fri 09:00-17:00. Data lasts for this session only.");
        out.println("Complete seed-ready-001 to demonstrate completion of an elapsed confirmed appointment.");
        var suggested = time.now().atZone(time.zone()).toLocalDate().plusDays(1);
        while (suggested.getDayOfWeek() == DayOfWeek.SATURDAY || suggested.getDayOfWeek() == DayOfWeek.SUNDAY) {
            suggested = suggested.plusDays(1);
        }
        out.println("Suggested booking date: " + suggested + " at 10:00.");
        try {
            boolean running = true;
            while (running) {
                menu();
                try {
                    switch (input.read("Choose an option")) {
                        case "1" -> providers();
                        case "2" -> services(facade.services());
                        case "3" -> booking(false);
                        case "4" -> booking(true);
                        case "5" -> appointments();
                        case "6" -> { var id = appointmentId(); detail(facade.details(id).orElseThrow(
                                () -> new IllegalArgumentException("Appointment not found."))); events("Appointment history", facade.events(id)); }
                        case "7" -> result(facade.confirm(command(), appointmentId()));
                        case "8" -> reschedule();
                        case "9" -> result(facade.cancel(command(), appointmentId()));
                        case "10" -> result(facade.complete(command(), appointmentId()));
                        case "11" -> { events("Notifications (simulated)", notifications.get()); events("Audit history", audit.get()); }
                        case "0" -> running = false;
                        default -> out.println("Invalid menu option. Choose 0-11.");
                    }
                } catch (IllegalArgumentException | DateTimeException | InvalidTransitionException
                        | RepositoryException | SlotUnavailableException ex) {
                    out.println("Unable to complete request: " + ex.getMessage());
                    out.println("Return to the menu and try again.");
                }
            }
        } catch (ConsoleInput.EndOfInput ex) {
            out.println("\nEnd of input; unfinished interaction discarded.");
        } catch (UncheckedIOException ex) {
            out.println("\nInput error; session closed: " + ex.getCause().getMessage());
        } finally {
            out.println("Goodbye.");
            out.flush();
        }
    }

    private void menu() {
        out.println("""

                === AppointEase ===
                 1. View service providers
                 2. View available services
                 3. Check appointment availability
                 4. Book a new appointment
                 5. View appointments
                 6. View appointment details
                 7. Confirm an appointment
                 8. Reschedule an appointment
                 9. Cancel an appointment
                10. Complete an appointment
                11. View notification and audit history
                 0. Exit""");
    }

    private void providers() {
        out.printf("%-24s %-18s %-20s %s%n", "PROVIDER ID", "NAME", "CATEGORY", "MODES / SERVICES");
        for (var p : facade.providers()) {
            out.printf("%-24s %-18s %-20s %s / %s%n", p.id().value(), p.name(), p.category(),
                    p.modes().stream().sorted().toList(), p.serviceIds().stream().map(ServiceId::value).sorted().toList());
            p.workingHours().entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(e -> out.println("  " + e.getKey() + " " + e.getValue()));
            out.println("  Preparation policy: " + p.preparationBuffer().toMinutes() + " minutes before start.");
        }
    }
    private void services(List<Service> services) {
        out.printf("%-23s %-26s %-14s %6s %12s%n", "SERVICE ID", "SERVICE", "TYPE", "MIN", "BASE FEE");
        for (var s : services) out.printf("%-23s %-26s %-14s %6d %12s%n",
                s.id().value(), s.name(), s.kind(), s.duration().toMinutes(), money(s.baseFee()));
    }
    private <T> T choose(String title, List<T> values, Function<T, String> id, Function<T, String> label) {
        out.println(title + " (enter row number or full ID)");
        for (int i = 0; i < values.size(); i++) out.printf("%2d. %-24s %s%n", i + 1, id.apply(values.get(i)), label.apply(values.get(i)));
        String answer = input.read(title);
        for (int i = 0; i < values.size(); i++) {
            if (answer.equals(Integer.toString(i + 1)) || answer.equals(id.apply(values.get(i)))) return values.get(i);
        }
        throw new IllegalArgumentException("Unknown " + title.toLowerCase(Locale.ROOT) + ".");
    }
    private Customer customer() {
        return choose("Customer", customers, c -> c.id().value(), Customer::name);
    }
    private void booking(boolean commit) {
        var customer = customer();
        var provider = choose("Provider", facade.providers(), p -> p.id().value(), p -> p.name() + " " + p.modes());
        var kind = choose("Appointment type", List.of(AppointmentKind.values()), Enum::name, Enum::name);
        var offered = facade.services().stream().filter(s -> provider.serviceIds().contains(s.id()) && s.kind() == kind).toList();
        if (offered.isEmpty()) throw new IllegalArgumentException("This provider offers no services of that type.");
        services(offered);
        var service = choose("Service", offered, s -> s.id().value(), Service::name);
        var start = start();
        var mode = choose("Mode", List.of(AppointmentMode.values()), Enum::name, Enum::name);
        var id = new AppointmentId(UUID.randomUUID().toString());
        AppointmentModeFactory family = mode == AppointmentMode.ONLINE ? new OnlineModeFactory(id.value())
                : new InPersonModeFactory("Demo Centre, " + provider.name() + " reception");
        AppointmentService extras = new BaseAppointmentService();
        if (input.yes("Priority service (+EUR 10.00)")) extras = new PriorityBookingDecorator(extras);
        if (input.yes("Reminder service (+EUR 2.00)")) extras = new ReminderServiceDecorator(extras);
        var policy = policy();
        var request = new BookingRequest(id, customer.id(), provider.id(), service.id(), start, family, extras, policy);
        boolean available = facade.available(request);
        out.println(available ? "Slot available (advisory until booking commits)." : "Provider slot is unavailable.");
        if (!commit || !available) return;
        out.println("Review: " + customer.name() + " | " + provider.name() + " | " + service.name() + " | " + kind);
        out.println(format(start) + " -> " + format(start.plus(service.duration())) + " | " + mode);
        quote(extras.quote(service));
        if (!input.yes("Confirm booking")) { out.println("Booking discarded."); return; }
        var result = facade.book(command(), request);
        aliases.add(result.appointment().id());
        out.println("Booked successfully. Session shortcut: @" + aliases.size());
        result(result);
    }
    private Instant start() {
        var resolved = time.resolve(input.dateTime());
        out.println("Resolved start: " + dates.format(resolved) + " [" + time.zone() + "]");
        out.println("DST gaps reject; repeated local times use the earlier instant.");
        return resolved.toInstant();
    }
    private AvailabilityStrategy policy() {
        return switch (input.read("Availability policy [1 standard / 2 preparation buffer]")) {
            case "1" -> new StandardAvailabilityStrategy();
            case "2" -> new PreparationBufferAvailabilityStrategy();
            default -> throw new IllegalArgumentException("Choose policy 1 or 2.");
        };
    }
    private AppointmentId appointmentId() {
        String value = input.read("Appointment ID (or @1, @2 for bookings this session)");
        if (value.startsWith("@")) {
            try {
                int index = Integer.parseInt(value.substring(1)) - 1;
                if (index >= 0 && index < aliases.size()) return aliases.get(index);
            } catch (NumberFormatException ignored) { }
            throw new IllegalArgumentException("Unknown session shortcut.");
        }
        return new AppointmentId(value);
    }
    private void reschedule() {
        var id = appointmentId();
        var start = start();
        var policy = policy();
        if (input.yes("Confirm reschedule")) result(facade.reschedule(command(), id, start, policy));
        else out.println("Reschedule discarded.");
    }
    private void appointments() {
        var list = facade.history(customer().id());
        out.printf("%-36s %-23s %-18s %-26s %-12s %s%n", "APPOINTMENT ID", "START", "PROVIDER", "SERVICE", "STATUS", "TOTAL");
        for (var a : list) out.printf("%-36s %-23s %-18s %-26s %-12s %s%n",
                a.id().value(), format(a.slot().start()), a.provider().name(), a.service().name(), a.status(), money(a.totalFee()));
        if (list.isEmpty()) out.println("No appointments.");
    }
    private void result(BookingResult result) {
        out.println("Saved successfully. Revision " + result.revision());
        detail(result.appointment());
        result.warnings().forEach(w -> out.println("Delivery warning (booking remains saved): " + w));
    }
    private void detail(Appointment a) {
        out.println("Appointment ID: " + a.id().value());
        out.println("Customer: " + a.customer().name() + " | Provider: " + a.provider().name());
        out.println("Service: " + a.service().name() + " | Type: " + a.service().kind());
        out.println("Time: " + format(a.slot().start()) + " -> " + format(a.slot().end()) + " [" + a.businessZone() + "]");
        out.println("Mode: " + a.mode() + " | Status: " + a.status());
        out.println(a.modeDetails().meeting().text());
        out.println(a.modeDetails().instructions().text());
        quote(a.enhancements());
    }
    private void quote(BookingEnhancements quote) {
        out.println("Base fee: " + money(quote.baseFee()));
        if (quote.options().additionalFees().isEmpty()) out.println("Optional services: none");
        quote.options().additionalFees().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> out.println("  " + e.getKey() + ": " + money(e.getValue())));
        out.println("Total fee: " + money(quote.totalFee()));
        quote.instructions().forEach(out::println);
    }
    private void events(String title, List<AppointmentEvent> events) {
        out.println(title);
        out.printf("%-5s %-23s %-36s %-13s %-12s %s%n", "REV", "OCCURRED", "APPOINTMENT ID", "EVENT", "STATUS", "SLOT");
        for (var e : events) out.printf("%-5d %-23s %-36s %-13s %-12s %s%n", e.revision(), format(e.occurredAt()),
                e.after().id().value(), e.type(), e.after().status(), format(e.after().slot().start()));
        if (events.isEmpty()) out.println("No records.");
    }
    private String format(Instant instant) { return dates.format(instant.atZone(time.zone())); }
    private static String money(Money money) { return "EUR " + money.amount().toPlainString(); }
    private static String command() { return UUID.randomUUID().toString(); }
}
