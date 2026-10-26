package com.appointease.domain;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {
    static Stream<Executable> invalidAppointments() {
        var id = new AppointmentId("a");
        return Stream.of(
                () -> appointment(null, customer(), provider(), service(), START, AppointmentMode.ONLINE, options(), time(BEFORE)),
                () -> appointment(id, null, provider(), service(), START, AppointmentMode.ONLINE, options(), time(BEFORE)),
                () -> appointment(id, customer(), null, service(), START, AppointmentMode.ONLINE, options(), time(BEFORE)),
                () -> appointment(id, customer(), provider(), null, START, AppointmentMode.ONLINE, options(), time(BEFORE)),
                () -> appointment(id, customer(), provider(), service(), null, AppointmentMode.ONLINE, options(), time(BEFORE)),
                () -> appointment(id, customer(), provider(), service(), START, null, options(), time(BEFORE)),
                () -> appointment(id, customer(), provider(), service(), START, AppointmentMode.ONLINE, null, time(BEFORE)),
                () -> appointment(id, customer(), provider(), service(), START, AppointmentMode.ONLINE, options(), null),
                () -> appointment(id, customer(), provider(), service(), START, AppointmentMode.ONLINE, options(), time(START)),
                () -> appointment(id, customer(), provider(), service(), START, AppointmentMode.ONLINE, options(), time(START.plusNanos(1)))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAppointments")
    void appointmentRequiresValidCompleteFutureData(Executable construction) {
        assertThrows(DomainValidationException.class, construction);
    }

    @Test
    void rejectsUnsupportedServiceAndMode() {
        var p = provider();
        var restricted = new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(),
                Set.of(AppointmentMode.IN_PERSON), p.workingHours(), p.preparationBuffer());
        assertFalse(restricted.supports(service(), AppointmentMode.ONLINE));
        assertThrows(DomainValidationException.class, () -> appointment(new AppointmentId("a"), customer(),
                restricted, service(), START, AppointmentMode.ONLINE, options(), time(BEFORE)));
        var other = new Service(new ServiceId("other"), "Other", service().duration(), service().baseFee(), service().kind());
        assertFalse(p.supports(other, AppointmentMode.IN_PERSON));
        assertThrows(DomainValidationException.class, () -> appointment(new AppointmentId("a"), customer(),
                p, other, START, AppointmentMode.IN_PERSON, options(), time(BEFORE)));
    }

    @Test
    void workingHoursAllowExactBoundariesButNotCrossingBreaksOrClosedDays() {
        var p = provider();
        var split = new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(),
                Map.of(DayOfWeek.TUESDAY, List.of(
                        new WorkingPeriod(LocalTime.of(9, 0), LocalTime.NOON),
                        new WorkingPeriod(LocalTime.of(13, 0), LocalTime.of(17, 0)))), Duration.ZERO);
        var time = time(Instant.parse("2026-10-26T00:00:00Z"));
        for (String local : List.of("2026-10-27T09:00", "2026-10-27T11:30", "2026-10-27T16:30")) {
            assertDoesNotThrow(() -> appointment(new AppointmentId(local), customer(), split, service(),
                    time.resolve(LocalDateTime.parse(local)).toInstant(), AppointmentMode.ONLINE, options(), time));
        }
        for (String local : List.of("2026-10-27T08:59", "2026-10-27T11:45", "2026-10-27T12:00",
                "2026-10-27T16:31", "2026-10-28T10:00")) {
            assertThrows(DomainValidationException.class, () -> appointment(new AppointmentId(local), customer(), split,
                    service(), time.resolve(LocalDateTime.parse(local)).toInstant(), AppointmentMode.ONLINE, options(), time));
        }
    }

    @Test
    void singleAppointmentDoesNotPretendToPreventGlobalCollisions() {
        Appointment first = pending();
        Appointment second = pending();
        assertEquals(first.id(), second.id());
        assertTrue(first.slot().overlaps(second.slot()));
        // Only the later repository slice can decide whether either candidate may be reserved.
    }
}
