package com.appointease.presentation.desktop;

import com.appointease.Main;
import com.appointease.ApplicationContext;
import com.appointease.application.BookingRequest;
import com.appointease.domain.addon.BaseAppointmentService;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.mode.OnlineModeFactory;
import com.appointease.domain.model.*;
import com.appointease.domain.scheduling.StandardAvailabilityStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CalendarModelTest {
    @ParameterizedTest @CsvSource({"2026-03-29,true", "2026-10-25,true", "2026-03-30,false", "2026-10-26,false"})
    void transitionDaysKeepTheirOwnAxisEvenWhenPlanningHoursStartAfterTheChange(String date, boolean changed) {
        var day = LocalDate.parse(date); var zone = ZoneId.of("Europe/Vilnius");
        assertEquals(changed, CalendarModel.hasOffsetChange(day, zone));
        assertFalse(CalendarModel.hasOffsetChange(day, ZoneOffset.UTC));
        // Office hours alone cannot detect the transition or a normalized planning origin.
        assertEquals(day.atTime(9, 0).atZone(zone).getOffset(), day.atTime(17, 0).atZone(zone).getOffset());
        if (day.equals(LocalDate.of(2026, 3, 29))) assertEquals(4, day.atTime(3, 0).atZone(zone).getHour());
    }
    @ParameterizedTest @CsvSource({
            "2026-10-26,2026-10-26", "2026-11-01,2026-10-26",
            "2027-01-01,2026-12-28", "2028-02-29,2028-02-28"})
    void weekAlwaysContainsSevenDaysFromMonday(String selected, String monday) {
        var range = CalendarModel.range(LocalDate.parse(selected), CalendarModel.View.WEEK);
        assertEquals(LocalDate.parse(monday), range.start());
        assertEquals(7, range.days().size());
        assertEquals(DayOfWeek.SUNDAY, range.days().getLast().getDayOfWeek());
        assertTrue(range.days().contains(LocalDate.parse(selected)));
    }
    @ParameterizedTest @CsvSource({
            "2026-10-15,2026-09-28,2026-11-02,35",
            "2026-02-01,2026-01-26,2026-03-02,35",
            "2027-02-15,2027-02-01,2027-03-01,28",
            "2028-02-29,2028-01-31,2028-03-06,35",
            "2026-03-31,2026-02-23,2026-04-06,42",
            "2026-12-31,2026-11-30,2027-01-04,35"})
    void monthIncludesExactlyTheBoundaryWeeks(String selected, String start, String end, int days) {
        var range = CalendarModel.range(LocalDate.parse(selected), CalendarModel.View.MONTH);
        assertEquals(LocalDate.parse(start), range.start());
        assertEquals(LocalDate.parse(end), range.endExclusive());
        assertEquals(days, range.days().size());
    }
    @ParameterizedTest @CsvSource({
            "DAY,2026-12-31,1,2027-01-01", "WEEK,2026-12-30,1,2027-01-06",
            "WEEK,2027-01-01,-1,2026-12-25", "MONTH,2026-01-31,1,2026-02-28",
            "MONTH,2028-03-31,-1,2028-02-29", "MONTH,2026-12-15,1,2027-01-15"})
    void navigationPreservesDateWherePossibleAndClampsMonthEnds(String view, String date, int direction, String expected) {
        assertEquals(LocalDate.parse(expected), CalendarModel.navigate(LocalDate.parse(date), CalendarModel.View.valueOf(view), direction));
    }
    @Test void selectingDateDoesNotNormalizeItToTheWeekOrMonthStart() {
        var selected = LocalDate.of(2026, 10, 29);
        for (var view : CalendarModel.View.values()) assertTrue(CalendarModel.range(selected, view).days().contains(selected));
        assertEquals(selected, CalendarModel.range(selected, CalendarModel.View.DAY).start());
    }
    @Test void groupingUsesBusinessDateAndHalfOpenMidnightBoundaries() {
        var context = context("Asia/Karachi");
        var day = LocalDate.of(2026, 10, 28);
        var before = book(context, "before", day.minusDays(1).atTime(23, 0), "health");
        var start = book(context, "start", day.atStartOfDay(), "health");
        var late = book(context, "late", day.atTime(23, 0), "health");
        var end = book(context, "end", day.plusDays(1).atStartOfDay(), "health");
        assertEquals(day.minusDays(1), start.slot().start().atZone(ZoneOffset.UTC).toLocalDate());
        var grouped = CalendarModel.group(List.of(end, late, before, start), CalendarModel.range(day, CalendarModel.View.DAY),
                context.time().zone(), null, false);
        assertEquals(List.of(start, late), grouped.get(day));
        assertThrows(UnsupportedOperationException.class, () -> grouped.get(day).clear());
    }
    @Test void providerAndCancelledFiltersApplyEquallyToAllViewsIncludingBoundaryDates() {
        var context = context("Europe/Vilnius");
        var day = LocalDate.of(2026, 10, 28);
        var active = book(context, "active", day.atTime(9, 0), "health");
        var cancelled = book(context, "cancelled", day.atTime(10, 0), "health");
        cancelled = context.facade().cancel("cancel", cancelled.id()).appointment();
        var other = book(context, "other", day.atTime(9, 0), "tutor");
        for (var view : CalendarModel.View.values()) {
            var range = CalendarModel.range(day, view);
            var hidden = CalendarModel.group(List.of(active, cancelled, other), range, context.time().zone(), active.provider().id(), false);
            assertEquals(List.of(active), hidden.get(day));
            var shown = CalendarModel.group(List.of(active, cancelled, other), range, context.time().zone(), active.provider().id(), true);
            assertEquals(List.of(active, cancelled), shown.get(day));
            assertEquals(range.days().size(), shown.size());
        }
    }
    @Test void overlapChainsReuseAdjacentLanesAndSeparateClustersGetFullWidth() {
        var context = context("Europe/Vilnius"); var day = LocalDate.of(2026, 10, 28);
        var a = book(context, "a", day.atTime(9, 0), "health");
        var b = book(context, "b", day.atTime(9, 15), "tutor");
        var c = book(context, "c", day.atTime(9, 30), "health");
        var d = book(context, "d", day.atTime(11, 0), "health");
        var layout = CalendarModel.layout(List.of(d, c, b, a));
        assertEquals(List.of(a, b, c, d), layout.stream().map(CalendarModel.Placement::appointment).toList());
        assertEquals(List.of(0, 1, 0, 0), layout.stream().map(CalendarModel.Placement::lane).toList());
        assertEquals(List.of(2, 2, 2, 1), layout.stream().map(CalendarModel.Placement::lanes).toList());
        for (var first : layout) for (var second : layout)
            if (!first.equals(second) && first.appointment().slot().overlaps(second.appointment().slot()))
                assertNotEquals(first.lane(), second.lane());
    }
    @Test void cancelledAndReplacementAtSameTimeHaveSeparateClickLanes() {
        var context = context("Europe/Vilnius"); var day = LocalDate.of(2026, 10, 28);
        var a = book(context, "a", day.atTime(9, 0), "health");
        a = context.facade().cancel("cancel-a", a.id()).appointment();
        var b = book(context, "b", day.atTime(9, 0), "health");
        var layout = CalendarModel.layout(List.of(a, b));
        assertEquals(2, layout.size()); assertEquals(2, layout.getFirst().lanes());
        assertNotEquals(layout.getFirst().lane(), layout.getLast().lane());
    }
    @ParameterizedTest @CsvSource({"2026-03-29,23", "2026-10-25,25"})
    void dstDayBoundariesUseActualInstants(String date, int elapsedHours) {
        var context = context("Europe/Vilnius"); var day = LocalDate.parse(date);
        var a = book(context, "early", day.atTime(0, 0), "health");
        var b = book(context, "late", day.atTime(23, 0), "health");
        var range = CalendarModel.range(day, CalendarModel.View.DAY);
        assertEquals(elapsedHours, Duration.between(range.start().atStartOfDay(context.time().zone()),
                range.endExclusive().atStartOfDay(context.time().zone())).toHours());
        assertEquals(List.of(a, b), CalendarModel.group(List.of(a, b), range, context.time().zone(), null, false).get(day));
        assertEquals(30, Duration.between(a.slot().start(), a.slot().end()).toMinutes());
    }
    private ApplicationContext context(String zone) {
        var context = Main.createContext(Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC), ZoneId.of(zone));
        var hours = new EnumMap<DayOfWeek, List<WorkingPeriod>>(DayOfWeek.class);
        for (var day : DayOfWeek.values()) hours.put(day, List.of(new WorkingPeriod(LocalTime.MIN, LocalTime.of(23, 59))));
        for (var provider : context.facade().providers()) context.facade().updateProvider(provider,
                new ServiceProvider(provider.id(), provider.name(), provider.category(), provider.serviceIds(),
                        provider.modes(), hours, Duration.ZERO));
        return context;
    }
    private Appointment book(ApplicationContext context, String id, LocalDateTime date, String provider) {
        return context.facade().book("book-" + id, new BookingRequest(new AppointmentId(id), new CustomerId("customer-demo-001"),
                new ProviderId("provider-" + provider + "-001"), new ServiceId("service-" + provider + "-001"),
                context.time().resolve(date).toInstant(), new OnlineModeFactory("Calendar test"),
                new BaseAppointmentService(), new StandardAvailabilityStrategy())).appointment();
    }
}
