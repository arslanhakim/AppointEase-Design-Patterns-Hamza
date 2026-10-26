package com.appointease.infrastructure.calendar;
import com.appointease.application.port.*;
import com.appointease.domain.event.AppointmentEvent;
import java.time.Instant;
import java.util.Objects;
public final class ExternalCalendarAdapter implements CalendarPort {
    private final SimulatedExternalCalendarClient client;
    public ExternalCalendarAdapter(SimulatedExternalCalendarClient client) { this.client = Objects.requireNonNull(client); }
    @Override public void synchronize(AppointmentEvent event) {
        if (event == null) throw new CalendarSyncException(CalendarSyncException.Reason.INVALID_TIMESTAMP,
                new IllegalArgumentException("event required"));
        try {
            var a = event.after();
            String operation = switch (event.type()) {
                case RESERVED -> "PUT"; case CONFIRMED -> "ACK"; case RESCHEDULED -> "MOVE";
                case CANCELLED -> "DELETE"; case COMPLETED -> "DONE";
            };
            client.apply(operation, a.id().value(), millis(a.slot().start()), millis(a.slot().end()),
                    a.businessZone().getId(), "r" + event.revision(), event.id());
        } catch (LegacyCalendarException ex) {
            throw new CalendarSyncException(switch (ex.code()) {
                case 400 -> CalendarSyncException.Reason.INVALID_TIMESTAMP;
                case 409 -> CalendarSyncException.Reason.CONFLICT;
                default -> CalendarSyncException.Reason.UNAVAILABLE;
            }, ex);
        } catch (ArithmeticException | IllegalArgumentException ex) {
            throw new CalendarSyncException(CalendarSyncException.Reason.INVALID_TIMESTAMP, ex);
        }
    }
    public static long millis(Instant instant) {
        if (instant.getNano() % 1_000_000 != 0) throw new IllegalArgumentException("Submillisecond timestamp");
        return instant.toEpochMilli();
    }
}
