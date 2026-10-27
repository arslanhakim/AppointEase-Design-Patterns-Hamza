package com.appointease.presentation.desktop;

import com.appointease.Main;
import com.appointease.domain.model.AppointmentId;
import com.appointease.domain.model.AppointmentStatus;
import com.appointease.domain.scheduling.StandardAvailabilityStrategy;
import com.appointease.infrastructure.seed.DesktopDemoSeed;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

class OverviewModelTest {
    @Test void overviewReadsRealFacadeSnapshotsAndObserverBackedFixtures() {
        var context = Main.createContext(Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneId.of("UTC")), ZoneId.of("Europe/Vilnius"));
        var day = DesktopDemoSeed.populate(context.facade(), context.time());
        var model = new OverviewModel(context.facade(), context.customers(), context.time());
        var snapshot = model.load(day);
        assertEquals(4, snapshot.appointments().size());
        assertEquals(3, snapshot.confirmed());
        assertEquals(1, snapshot.pending());
        assertEquals(4, snapshot.providers());
        assertEquals("Dana Green", snapshot.appointments().getFirst().provider().name());
        assertEquals(9, context.audit().get().size());
        assertEquals(context.audit().get(), context.notifications().get());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.appointments().clear());
        assertEquals(9, context.audit().get().size(), "Loading the view must not emit events");
    }

    @Test void refreshReflectsCancellationAndRescheduleWithoutChangingEarlierSnapshot() {
        var context = Main.createContext(Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneId.of("UTC")), ZoneId.of("Europe/Vilnius"));
        var day = DesktopDemoSeed.populate(context.facade(), context.time());
        var model = new OverviewModel(context.facade(), context.customers(), context.time());
        var original = model.load(day);
        var first = new AppointmentId("desktop-demo-001");
        context.facade().cancel("cancel", first);
        context.facade().reschedule("move", new AppointmentId("desktop-demo-003"),
                context.time().resolve(day.plusDays(1).atTime(11, 0)).toInstant(), new StandardAvailabilityStrategy());
        var updated = model.load(day);
        assertEquals(3, updated.appointments().size(), "Cancelled records remain visible; moved record leaves the day");
        assertEquals(2, updated.confirmed());
        assertEquals(0, updated.pending());
        assertEquals(2, updated.providers());
        assertEquals(AppointmentStatus.CANCELLED, updated.appointments().getFirst().status());
        assertEquals(AppointmentStatus.CONFIRMED, original.appointments().getFirst().status());
        assertEquals(1, model.load(day.plusDays(1)).pending());
    }

    @Test void emptyDayHasZeroCountsAndNoWrites() {
        var context = Main.createContext(Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneId.of("UTC")), ZoneId.of("Europe/Vilnius"));
        var model = new OverviewModel(context.facade(), context.customers(), context.time());
        var snapshot = model.load(java.time.LocalDate.of(2026, 10, 27));
        assertTrue(snapshot.appointments().isEmpty());
        assertEquals(0, snapshot.confirmed());
        assertEquals(0, snapshot.pending());
        assertEquals(0, snapshot.providers());
        assertEquals(2, context.audit().get().size());
    }

    @Test void desktopSeedSkipsWeekendAndUsesConfiguredZoneRatherThanClockZone() {
        var context = Main.createContext(Clock.fixed(Instant.parse("2026-10-30T20:00:00Z"), ZoneId.of("UTC")), ZoneId.of("Asia/Karachi"));
        var day = DesktopDemoSeed.populate(context.facade(), context.time());
        assertEquals(java.time.LocalDate.of(2026, 11, 2), day);
        var snapshot = new OverviewModel(context.facade(), context.customers(), context.time()).load(day);
        assertEquals(Instant.parse("2026-11-02T04:00:00Z"), snapshot.appointments().getFirst().slot().start());
        assertEquals(4, snapshot.appointments().size());
    }

    @Test void consoleCompositionRemainsIndependentFromDesktopSeed() {
        var clock = Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneId.of("UTC"));
        var desktop = Main.createContext(clock, ZoneId.of("Europe/Vilnius"));
        DesktopDemoSeed.populate(desktop.facade(), desktop.time());
        var console = Main.createContext(clock, ZoneId.of("Europe/Vilnius"));
        assertTrue(console.facade().details(new AppointmentId("desktop-demo-001")).isEmpty());
        assertEquals(2, console.audit().get().size());
    }
}
