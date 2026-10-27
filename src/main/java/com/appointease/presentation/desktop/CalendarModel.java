package com.appointease.presentation.desktop;

import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.model.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** Pure calendar projection. No availability, booking or lifecycle decisions. */
final class CalendarModel {
    enum View { DAY, WEEK, MONTH }
    record Range(LocalDate start, LocalDate endExclusive) {
        List<LocalDate> days() { return start.datesUntil(endExclusive).toList(); }
    }
    record Placement(Appointment appointment, int lane, int lanes) { }
    private CalendarModel() { }

    /** A transition day needs its own axis even when the chosen planning origin falls in a gap. */
    static boolean hasOffsetChange(LocalDate day, ZoneId zone) {
        return !day.atStartOfDay(zone).getOffset().equals(day.plusDays(1).atStartOfDay(zone).getOffset());
    }

    static Range range(LocalDate selected, View view) {
        if (view == View.DAY) return new Range(selected, selected.plusDays(1));
        if (view == View.WEEK) {
            var monday = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new Range(monday, monday.plusDays(7));
        }
        var first = selected.withDayOfMonth(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var end = selected.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        return new Range(first, end);
    }
    static LocalDate navigate(LocalDate selected, View view, int direction) {
        return switch (view) {
            case DAY -> selected.plusDays(direction);
            case WEEK -> selected.plusWeeks(direction);
            case MONTH -> selected.plusMonths(direction);
        };
    }
    /** Facade histories are projected into a half-open range in the configured business zone. */
    static Map<LocalDate, List<Appointment>> group(List<Appointment> appointments, Range range,
                                                ZoneId zone, ProviderId provider, boolean cancelled) {
        var result = new LinkedHashMap<LocalDate, List<Appointment>>();
        range.days().forEach(day -> result.put(day, new ArrayList<>()));
        var start = range.start().atStartOfDay(zone).toInstant();
        var end = range.endExclusive().atStartOfDay(zone).toInstant();
        appointments.stream().filter(a -> !a.slot().start().isBefore(start) && a.slot().start().isBefore(end))
                .filter(a -> provider == null || a.provider().id().equals(provider))
                .filter(a -> cancelled || a.status() != AppointmentStatus.CANCELLED)
                .sorted(Comparator.comparing((Appointment a) -> a.slot().start()).thenComparing(a -> a.id().value()))
                .forEach(a -> result.get(a.slot().start().atZone(zone).toLocalDate()).add(a));
        result.replaceAll((day, entries) -> List.copyOf(entries));
        return Collections.unmodifiableMap(result);
    }
    /** Interval partitioning: half-open adjacency reuses a lane; connected clusters share width. */
    static List<Placement> layout(List<Appointment> appointments) {
        var result = new ArrayList<Placement>();
        var cluster = new ArrayList<Placement>();
        var ends = new ArrayList<Instant>();
        Instant clusterEnd = Instant.MIN;
        for (var a : appointments.stream().sorted(Comparator.comparing((Appointment a) -> a.slot().start())
                .thenComparing(a -> a.id().value())).toList()) {
            if (!a.slot().start().isBefore(clusterEnd)) {
                finish(result, cluster, ends.size()); cluster.clear(); ends.clear();
            }
            int lane = 0;
            while (lane < ends.size() && ends.get(lane).isAfter(a.slot().start())) lane++;
            if (lane == ends.size()) ends.add(a.slot().end()); else ends.set(lane, a.slot().end());
            if (a.slot().end().isAfter(clusterEnd)) clusterEnd = a.slot().end();
            cluster.add(new Placement(a, lane, 0));
        }
        finish(result, cluster, ends.size());
        return List.copyOf(result);
    }
    private static void finish(List<Placement> result, List<Placement> cluster, int lanes) {
        cluster.forEach(p -> result.add(new Placement(p.appointment(), p.lane(), lanes)));
    }
}
