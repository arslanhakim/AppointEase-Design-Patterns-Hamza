package com.appointease.domain;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.time.DomainTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class TimeAndMoneyTest {
    @ParameterizedTest
    @CsvSource({"0, 0.00", "1, 1.00", "1.004, 1.00", "1.005, 1.01", "99.999, 100.00"})
    void moneyNormalizesHalfUp(String input, String expected) {
        assertEquals(new BigDecimal(expected), new Money(new BigDecimal(input)).amount());
    }

    @Test
    void moneyHasCanonicalEqualityAndExactAddition() {
        assertEquals(new Money(new BigDecimal("1.0")), new Money(new BigDecimal("1.00")));
        assertEquals(new Money(new BigDecimal("0.30")),
                new Money(new BigDecimal("0.10")).plus(new Money(new BigDecimal("0.20"))));
        assertThrows(DomainValidationException.class, () -> new Money(BigDecimal.ZERO).plus(null));
    }

    @ParameterizedTest
    @CsvSource({"-10, 10, true", "0, 30, true", "5, 10, true", "-5, 35, true",
            "20, 40, true", "-30, 0, false", "30, 60, false", "31, 60, false"})
    void halfOpenOverlapIsSymmetric(long startMinutes, long endMinutes, boolean expected) {
        TimeSlot a = new TimeSlot(START, END);
        TimeSlot b = new TimeSlot(START.plusSeconds(startMinutes * 60), START.plusSeconds(endMinutes * 60));
        assertEquals(expected, a.overlaps(b));
        assertEquals(expected, b.overlaps(a));
        assertThrows(DomainValidationException.class, () -> a.overlaps(null));
    }

    @Test
    void vilniusRejectsGapAndChoosesEarlierOverlapInstant() {
        DomainTime time = time(BEFORE);
        assertThrows(DomainValidationException.class, () -> time.resolve(LocalDateTime.parse("2026-03-29T03:30")));
        var ambiguous = time.resolve(LocalDateTime.parse("2026-10-25T03:30"));
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), ambiguous.toInstant());
        assertEquals(ZoneOffset.ofHours(3), ambiguous.getOffset());
        assertEquals(Instant.parse("2026-10-27T08:00:00Z"), time.resolve(LocalDateTime.parse("2026-10-27T10:00")).toInstant());
    }

    @Test
    void zoneIsConfigurableAndDoesNotFollowClockZone() {
        Clock clock = Clock.fixed(BEFORE, ZoneId.of("Asia/Karachi"));
        assertEquals(ZoneId.of("Europe/Vilnius"), new DomainTime(clock).zone());
        DomainTime utc = new DomainTime("UTC", clock);
        assertEquals(BEFORE, utc.now());
        assertEquals(Instant.parse("2026-03-29T03:30:00Z"), utc.resolve(LocalDateTime.parse("2026-03-29T03:30")).toInstant());
        assertThrows(DomainValidationException.class, () -> new DomainTime("unknown/zone", clock));
        assertThrows(DomainValidationException.class, () -> new DomainTime(null, clock));
        assertThrows(DomainValidationException.class, () -> new DomainTime("UTC", null));
        assertThrows(DomainValidationException.class, () -> utc.resolve(null));
    }

    @Test
    void serviceDurationIsElapsedTimeAcrossBothDstChanges() {
        for (String local : List.of("2026-03-29T02:30", "2026-10-25T03:30")) {
            DomainTime time = time(Instant.parse("2026-01-01T00:00:00Z"));
            Service service = new Service(new ServiceId("dst"), "Long appointment", Duration.ofHours(2),
                    new Money(BigDecimal.ZERO), AppointmentKind.SERVICE);
            ServiceProvider provider = new ServiceProvider(new ProviderId("p"), "Provider", ProviderCategory.PERSONAL_SERVICES,
                    Set.of(service.id()), Set.of(AppointmentMode.IN_PERSON), Map.of(DayOfWeek.SUNDAY,
                    List.of(new WorkingPeriod(LocalTime.MIDNIGHT, LocalTime.of(23, 59)))), Duration.ZERO);
            var start = time.resolve(LocalDateTime.parse(local)).toInstant();
            Appointment appointment = appointment(new AppointmentId(local), customer(), provider, service,
                    start, AppointmentMode.IN_PERSON, options(), time);
            assertEquals(Duration.ofHours(2), Duration.between(appointment.slot().start(), appointment.slot().end()));
        }
    }

    @Test
    void nonexistentDatedWorkingBoundaryIsRejected() {
        DomainTime time = time(Instant.parse("2026-01-01T00:00:00Z"));
        ServiceProvider p = new ServiceProvider(new ProviderId("p"), "Provider", ProviderCategory.CONSULTING,
                Set.of(service().id()), Set.of(AppointmentMode.ONLINE), Map.of(DayOfWeek.SUNDAY,
                List.of(new WorkingPeriod(LocalTime.of(3, 30), LocalTime.of(8, 0)))), Duration.ZERO);
        assertThrows(DomainValidationException.class, () -> appointment(new AppointmentId("a"), customer(), p,
                service(), time.resolve(LocalDateTime.parse("2026-03-29T05:00")).toInstant(),
                AppointmentMode.ONLINE, options(), time));
    }
}
