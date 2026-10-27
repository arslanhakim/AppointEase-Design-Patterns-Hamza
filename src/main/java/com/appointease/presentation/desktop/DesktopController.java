package com.appointease.presentation.desktop;

import com.appointease.application.BookingFacade;
import com.appointease.application.BookingRequest;
import com.appointease.application.BookingResult;
import com.appointease.application.event.DeliveryFailure;
import com.appointease.domain.addon.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.domain.mode.*;
import com.appointease.domain.model.*;
import com.appointease.domain.scheduling.*;
import com.appointease.domain.time.DomainTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/** UI orchestration only: rules, quotes and atomic writes belong to the existing domain/facade. */
public final class DesktopController {
    public enum Page { OVERVIEW, APPOINTMENTS, NEW_APPOINTMENT, CUSTOMERS, PROVIDERS, SERVICES, SCHEDULE, ACTIVITY }
    public enum Action { CONFIRM, CANCEL, COMPLETE, RESCHEDULE }
    public record Draft(Customer customer, ServiceProvider provider, Service service, LocalDate day,
            String time, AppointmentMode mode, boolean priority, boolean reminder, boolean buffered) { }
    public record PreparedBooking(String commandId, BookingRequest request, Draft draft, BookingEnhancements quote) { }
    public record PreparedMove(String commandId, AppointmentId id, Instant start, AvailabilityStrategy strategy) { }
    public record Activity(String channel, AppointmentEvent event) { }
    private final BookingFacade facade;
    private final DomainTime time;
    private final Supplier<List<AppointmentEvent>> notifications;
    private final Supplier<List<AppointmentEvent>> audit;
    private final List<DeliveryFailure> warnings = new ArrayList<>();
    private Page page = Page.OVERVIEW;
    private long revision;

    public DesktopController(BookingFacade facade, List<Customer> customers, DomainTime time,
            Supplier<List<AppointmentEvent>> notifications, Supplier<List<AppointmentEvent>> audit) {
        this.facade = Objects.requireNonNull(facade);
        this.time = Objects.requireNonNull(time);
        this.notifications = Objects.requireNonNull(notifications);
        this.audit = Objects.requireNonNull(audit);
    }
    public DomainTime time() { return time; }
    public List<Customer> customers() { return facade.customers(); }
    public Customer saveCustomer(Customer expected, String name) {
        Customer saved = expected == null ? facade.createCustomer(name) : facade.updateCustomer(expected, name);
        revision++;
        return saved;
    }
    public List<Service> services() { return facade.services(); }
    public Service saveService(Service expected, Service value) {
        var saved = expected == null ? facade.createService(value) : facade.updateService(expected, value);
        revision++;
        return saved;
    }
    public ServiceProvider saveProvider(ServiceProvider expected, ServiceProvider value) {
        var saved = expected == null ? facade.createProvider(value) : facade.updateProvider(expected, value);
        revision++;
        return saved;
    }
    public List<Appointment> customerAppointments(CustomerId id) { return facade.history(id); }
    public List<ServiceProvider> bookableProviders() { return providers().stream().filter(ServiceProvider::enabled).toList(); }
    public List<ServiceProvider> providers() { return facade.providers(); }
    public List<Service> services(ServiceProvider provider) {
        return facade.services().stream().filter(s -> provider != null && provider.serviceIds().contains(s.id())).toList();
    }
    public Page page() { return page; }
    public void navigate(Page page) { this.page = Objects.requireNonNull(page); }
    public long revision() { return revision; }
    public Appointment details(AppointmentId id) {
        return facade.details(id).orElseThrow(() -> new IllegalArgumentException("Appointment no longer exists."));
    }
    public List<AppointmentEvent> history(AppointmentId id) { return facade.events(id); }
    public List<Appointment> appointments(String search, AppointmentStatus status, ProviderId provider, LocalDate day) {
        String query = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
        return customers().stream().flatMap(c -> facade.history(c.id()).stream())
                .filter(a -> status == null || a.status() == status)
                .filter(a -> provider == null || a.provider().id().equals(provider))
                .filter(a -> day == null || a.slot().start().atZone(time.zone()).toLocalDate().equals(day))
                .filter(a -> (a.id().value() + " " + a.customer().name() + " " + a.provider().name() + " "
                        + a.service().name()).toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing((Appointment a) -> a.slot().start()).thenComparing(a -> a.id().value())).toList();
    }
    public BookingEnhancements quote(Service service, boolean priority, boolean reminder) {
        return extras(priority, reminder).quote(required(service, "Choose a service."));
    }
    private AppointmentService extras(boolean priority, boolean reminder) {
        AppointmentService extras = new BaseAppointmentService();
        if (priority) extras = new PriorityBookingDecorator(extras);
        if (reminder) extras = new ReminderServiceDecorator(extras);
        return extras;
    }
    public ZonedDateTime resolve(LocalDate day, String input) {
        required(day, "Choose a date.");
        if (input == null || !input.strip().matches("[0-9]{2}:[0-9]{2}"))
            throw new IllegalArgumentException("Enter a time as HH:mm, for example 10:30.");
        try { return time.resolve(LocalDateTime.of(day, LocalTime.parse(input.strip()))); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("Enter a valid time between 00:00 and 23:59."); }
    }
    public PreparedBooking prepare(Draft draft) {
        required(draft.customer(), "Choose a customer.");
        required(draft.provider(), "Choose a provider.");
        required(draft.service(), "Choose a service.");
        required(draft.mode(), "Choose an appointment mode.");
        var id = new AppointmentId(UUID.randomUUID().toString());
        var extra = extras(draft.priority(), draft.reminder());
        AppointmentModeFactory mode = draft.mode() == AppointmentMode.ONLINE
                ? new OnlineModeFactory(id.value()) : new InPersonModeFactory(draft.provider().name() + " reception");
        var request = new BookingRequest(id, draft.customer().id(), draft.provider().id(), draft.service().id(),
                resolve(draft.day(), draft.time()).toInstant(), mode, extra, strategy(draft.buffered()));
        // Validation/availability is advisory. book() always revalidates atomically.
        if (!facade.available(request)) throw new SlotUnavailableException();
        return new PreparedBooking(UUID.randomUUID().toString(), request, draft, extra.quote(draft.service()));
    }
    public BookingResult book(PreparedBooking prepared) {
        var d = prepared.draft();
        return committed(facade.bookReviewed(prepared.commandId(), prepared.request(), d.customer(), d.provider(), d.service()));
    }
    public PreparedMove prepareMove(AppointmentId id, LocalDate day, String input, boolean buffered) {
        return new PreparedMove(UUID.randomUUID().toString(), id, resolve(day, input).toInstant(), strategy(buffered));
    }
    public BookingResult reschedule(PreparedMove move) {
        return committed(facade.reschedule(move.commandId(), move.id(), move.start(), move.strategy()));
    }
    public BookingResult act(Action action, String commandId, AppointmentId id) {
        return committed(switch (action) {
            case CONFIRM -> facade.confirm(commandId, id);
            case CANCEL -> facade.cancel(commandId, id);
            case COMPLETE -> facade.complete(commandId, id);
            case RESCHEDULE -> throw new IllegalArgumentException("Choose a new date and time.");
        });
    }
    /** Ask immutable domain snapshots about actions; never encode a second lifecycle state table. */
    public boolean can(Action action, Appointment a) {
        try {
            switch (action) {
                case CONFIRM -> a.confirm(time);
                case CANCEL -> a.cancel(time);
                case COMPLETE -> a.complete(time);
                case RESCHEDULE -> a.validateReschedule(a.slot().start(), time);
            }
            return true;
        } catch (IllegalArgumentException | IllegalStateException ex) { return false; }
    }
    private AvailabilityStrategy strategy(boolean buffered) {
        return buffered ? new PreparationBufferAvailabilityStrategy() : new StandardAvailabilityStrategy();
    }
    private BookingResult committed(BookingResult result) {
        revision++;
        result.warnings().stream().filter(w -> !warnings.contains(w)).forEach(warnings::add);
        return result;
    }
    public List<DeliveryFailure> warnings() { return List.copyOf(warnings); }
    public List<Activity> activity() {
        var result = new ArrayList<Activity>();
        notifications.get().forEach(e -> result.add(new Activity("Notification (simulated)", e)));
        audit.get().forEach(e -> result.add(new Activity("Audit", e)));
        result.sort(Comparator.comparingLong((Activity a) -> a.event().sequence()).reversed().thenComparing(Activity::channel));
        return List.copyOf(result);
    }
    public String format(Instant instant) { return instant.atZone(time.zone()).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm XXX", Locale.ENGLISH)); }
    public static String money(Money money) { return "EUR " + money.amount().toPlainString(); }
    public static String title(Enum<?> value) {
        String text = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
    private static <T> T required(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }
}
