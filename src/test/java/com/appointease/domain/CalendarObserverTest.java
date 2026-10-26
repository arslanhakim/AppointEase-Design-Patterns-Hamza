package com.appointease.domain;
import com.appointease.application.event.*;
import com.appointease.application.port.*;
import com.appointease.domain.event.AppointmentEvent;
import com.appointease.infrastructure.calendar.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class CalendarObserverTest {
    private AppointmentEvent event() {
        return new AppointmentEvent("event-1", 1, 1, AppointmentEvent.Type.RESERVED, BEFORE, null, pending());
    }
    @Test void adapterTranslatesOperationIdentityRevisionZoneAndEpochTime() {
        var client = new SimulatedExternalCalendarClient(); var adapter = new ExternalCalendarAdapter(client);
        var event = event(); adapter.synchronize(event); adapter.synchronize(event);
        var entry = client.entries().get(event.after().id().value());
        assertEquals("PUT", entry.operation()); assertEquals(START.toEpochMilli(), entry.startMillis());
        assertEquals(END.toEpochMilli(), entry.endMillis()); assertEquals("Europe/Vilnius", entry.zone());
        assertEquals(1, entry.revision()); assertEquals(1, client.appliedCount());
        assertEquals(10, Instant.ofEpochMilli(entry.startMillis()).atZone(ZoneId.of(entry.zone())).getHour());
    }
    @Test void exactNegativeEpochConversionAndOverflowRejection() {
        assertEquals(-1, ExternalCalendarAdapter.millis(Instant.parse("1969-12-31T23:59:59.999Z")));
        assertThrows(ArithmeticException.class, () -> ExternalCalendarAdapter.millis(Instant.MAX.minusNanos(999999)));
        assertThrows(IllegalArgumentException.class, () -> ExternalCalendarAdapter.millis(START.plusNanos(1)));
    }
    @Test void nullEventMapsToTypedApplicationError() {
        var ex = assertThrows(CalendarSyncException.class, () -> new ExternalCalendarAdapter(new SimulatedExternalCalendarClient()).synchronize(null));
        assertEquals(CalendarSyncException.Reason.INVALID_TIMESTAMP, ex.reason()); assertNotNull(ex.getCause());
    }
    @ParameterizedTest @ValueSource(ints = {400, 409, 503, 999})
    void legacyFailureCodesMapAndRetainCause(int code) {
        var adapter = new ExternalCalendarAdapter(new SimulatedExternalCalendarClient(Set.of(), code));
        var ex = assertThrows(CalendarSyncException.class, () -> adapter.synchronize(event()));
        assertEquals(code == 400 ? CalendarSyncException.Reason.INVALID_TIMESTAMP : code == 409
                ? CalendarSyncException.Reason.CONFLICT : CalendarSyncException.Reason.UNAVAILABLE, ex.reason());
        assertEquals(code, ((LegacyCalendarException) ex.getCause()).code());
    }
    @Test void fakeRejectsMalformedLegacyDataAndDoesNotRecordIt() {
        var client = new SimulatedExternalCalendarClient();
        assertEquals(400, assertThrows(LegacyCalendarException.class,
                () -> client.apply("PUT", "key", 1, 0, "bad-zone", "r1", "event")).code());
        assertEquals(400, assertThrows(LegacyCalendarException.class,
                () -> client.apply("PUT", "key", 0, 1, "bad-zone", "r1", "event")).code());
        assertTrue(client.entries().isEmpty()); assertEquals(0, client.appliedCount());
    }
    @Test void cancellationTombstoneRejectsStaleRevisions() {
        var client = new SimulatedExternalCalendarClient();
        client.apply("DELETE", "key", 0, 1, "UTC", "r2", "cancel");
        assertEquals(409, assertThrows(LegacyCalendarException.class,
                () -> client.apply("PUT", "key", 0, 1, "UTC", "r1", "reserve")).code());
        assertEquals("DELETE", client.entries().get("key").operation());
    }
    @Test void subscriptionsDeliverInOrderAndReplayIsSuppressed() {
        var publisher = new AppointmentEventPublisher(); var seen = new ArrayList<String>();
        AppointmentListener first = e -> seen.add("first");
        AppointmentListener second = e -> seen.add("second");
        publisher.subscribe(first); publisher.subscribe(first); publisher.subscribe(second);
        publisher.publish(event()); publisher.publish(event());
        assertEquals(List.of("first", "second"), seen);
        publisher.unsubscribe(first);
        var old = event(); var next = new AppointmentEvent("event-2", 2, 2, AppointmentEvent.Type.CONFIRMED,
                BEFORE, old.after(), old.after().confirm(time(BEFORE)));
        publisher.publish(next); assertEquals(List.of("first", "second", "second"), seen);
    }
    @Test void subscriptionChangesOnlyAffectNextEvent() {
        var publisher = new AppointmentEventPublisher(); var seen = new ArrayList<String>();
        AppointmentListener later = e -> seen.add("later");
        publisher.subscribe(e -> { seen.add("first"); publisher.subscribe(later); });
        publisher.publish(event()); assertEquals(List.of("first"), seen);
    }
    @Test void reentrantPublicationIsIsolatedAndDoesNotInterleaveListeners() {
        var publisher = new AppointmentEventPublisher(); var seen = new ArrayList<String>();
        publisher.subscribe(e -> publisher.publish(e));
        publisher.subscribe(e -> seen.add(e.id()));
        assertEquals(1, publisher.publish(event()).size()); assertEquals(List.of("event-1"), seen);
    }
    @Test void adapterPreservesDstOverlapInstantAndExplicitZone() {
        Instant start = time(BEFORE).resolve(LocalDateTime.parse("2026-10-25T03:30:00")).toInstant();
        long translated = ExternalCalendarAdapter.millis(start);
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), Instant.ofEpochMilli(translated));
        assertEquals(ZoneOffset.ofHours(3), Instant.ofEpochMilli(translated).atZone(ZoneId.of("Europe/Vilnius")).getOffset());
    }
    @Test void eachFailingListenerIsIsolatedAndAttemptedOnce() {
        var publisher = new AppointmentEventPublisher(); var seen = new ArrayList<String>();
        publisher.subscribe(e -> { seen.add("bad"); throw new IllegalStateException("failure"); });
        publisher.subscribe(e -> seen.add("good"));
        assertEquals(1, publisher.publish(event()).size()); assertTrue(publisher.publish(event()).isEmpty());
        assertEquals(List.of("bad", "good"), seen);
    }
}
