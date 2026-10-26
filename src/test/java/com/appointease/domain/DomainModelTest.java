package com.appointease.domain;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {
    @Test
    void validModelsHaveTypedNormalizedIdsDerivedDurationAndFeeSnapshots() {
        assertEquals("customer-1", new CustomerId(" customer-1 ").value());
        assertNotEquals(new CustomerId("same"), new ProviderId("same"));
        var appointment = appointment(new AppointmentId("a"), customer(), provider(), service(), START,
                AppointmentMode.IN_PERSON, new BookingOptions(Map.of("priority", new Money(new BigDecimal("10")))), time(BEFORE));
        assertEquals(END, appointment.slot().end());
        assertEquals(AppointmentStatus.PENDING, appointment.status());
        assertEquals(new BigDecimal("50.00"), appointment.totalFee().amount());
        assertEquals("EUR", appointment.totalFee().currency().getCurrencyCode());
        assertTrue(provider().supports(service(), AppointmentMode.ONLINE));
        assertEquals(AppointmentKind.CONSULTATION, service().kind());
        assertEquals("Demo Customer", customer().name());
    }

    static Stream<Executable> invalidModels() {
        Service s = service();
        return Stream.of(
                () -> new CustomerId(null), () -> new CustomerId(" "),
                () -> new ProviderId(null), () -> new ProviderId("\t"),
                () -> new ServiceId(null), () -> new ServiceId(""),
                () -> new AppointmentId(null), () -> new AppointmentId(" "),
                () -> new Customer(null, "Name"), () -> new Customer(customer().id(), null),
                () -> new Customer(customer().id(), " "),
                () -> new Service(null, "Name", s.duration(), s.baseFee(), s.kind()),
                () -> new Service(s.id(), null, s.duration(), s.baseFee(), s.kind()),
                () -> new Service(s.id(), " ", s.duration(), s.baseFee(), s.kind()),
                () -> new Service(s.id(), "Name", null, s.baseFee(), s.kind()),
                () -> new Service(s.id(), "Name", Duration.ZERO, s.baseFee(), s.kind()),
                () -> new Service(s.id(), "Name", Duration.ofNanos(-1), s.baseFee(), s.kind()),
                () -> new Service(s.id(), "Name", s.duration(), null, s.kind()),
                () -> new Service(s.id(), "Name", s.duration(), s.baseFee(), null),
                () -> new Money(null), () -> new Money(new BigDecimal("-0.001")),
                () -> new TimeSlot(null, END), () -> new TimeSlot(START, null),
                () -> new TimeSlot(START, START), () -> new TimeSlot(END, START),
                () -> TimeSlot.startingAt(null, Duration.ofMinutes(1)),
                () -> TimeSlot.startingAt(START, null),
                () -> TimeSlot.startingAt(START, Duration.ZERO),
                () -> TimeSlot.startingAt(START, Duration.ofSeconds(-1)),
                () -> TimeSlot.startingAt(Instant.MAX, Duration.ofSeconds(1)),
                () -> new WorkingPeriod(null, LocalTime.NOON),
                () -> new WorkingPeriod(LocalTime.NOON, null),
                () -> new WorkingPeriod(LocalTime.NOON, LocalTime.NOON),
                () -> new WorkingPeriod(LocalTime.of(17, 0), LocalTime.of(9, 0)),
                () -> new BookingOptions(null),
                () -> new BookingOptions(Map.of(" ", new Money(BigDecimal.ZERO))),
                () -> new BookingOptions(Collections.singletonMap(null, new Money(BigDecimal.ZERO))),
                () -> new BookingOptions(Collections.singletonMap("reminder", null)),
                () -> new BookingOptions(Map.of("reminder", new Money(BigDecimal.ZERO),
                        " reminder ", new Money(BigDecimal.ONE)))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModels")
    void invalidModelConstructionFailsCleanly(Executable construction) {
        assertThrows(DomainValidationException.class, construction);
    }

    @Test
    void providerCollectionsAndNestedHoursAreDefensivelyCopied() {
        Set<ServiceId> services = new HashSet<>(Set.of(service().id()));
        Set<AppointmentMode> modes = new HashSet<>(Set.of(AppointmentMode.ONLINE));
        List<WorkingPeriod> periods = new ArrayList<>(hours().get(DayOfWeek.TUESDAY));
        Map<DayOfWeek, List<WorkingPeriod>> hours = new HashMap<>(Map.of(DayOfWeek.TUESDAY, periods));
        ServiceProvider provider = new ServiceProvider(new ProviderId("p"), "Provider", ProviderCategory.TUTORING,
                services, modes, hours, Duration.ZERO);
        services.clear(); modes.clear(); periods.clear(); hours.clear();
        assertEquals(Set.of(service().id()), provider.serviceIds());
        assertEquals(Set.of(AppointmentMode.ONLINE), provider.modes());
        assertEquals(1, provider.workingHours().get(DayOfWeek.TUESDAY).size());
        assertThrows(UnsupportedOperationException.class, () -> provider.serviceIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> provider.modes().clear());
        assertThrows(UnsupportedOperationException.class, () -> provider.workingHours().clear());
        assertThrows(UnsupportedOperationException.class, () -> provider.workingHours().get(DayOfWeek.TUESDAY).clear());
    }

    static Stream<Executable> invalidProviders() {
        var p = provider();
        return Stream.of(
                () -> new ServiceProvider(null, p.name(), p.category(), p.serviceIds(), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), " ", p.category(), p.serviceIds(), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), null, p.category(), p.serviceIds(), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), null, p.serviceIds(), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), null, p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), Set.of(), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), Collections.singleton(null), p.modes(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), null, p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), Set.of(), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), Collections.singleton(null), p.workingHours(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), null, p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Map.of(), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Map.of(DayOfWeek.TUESDAY, List.of()), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Collections.singletonMap(null, List.of()), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Collections.singletonMap(DayOfWeek.TUESDAY, null), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Map.of(DayOfWeek.TUESDAY, Arrays.asList((WorkingPeriod) null)), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), Map.of(DayOfWeek.TUESDAY, List.of(
                        new WorkingPeriod(LocalTime.of(9, 0), LocalTime.NOON),
                        new WorkingPeriod(LocalTime.of(11, 0), LocalTime.of(17, 0)))), p.preparationBuffer()),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), p.workingHours(), null),
                () -> new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(), p.modes(), p.workingHours(), Duration.ofSeconds(-1))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidProviders")
    void invalidProviderConstructionFailsCleanly(Executable construction) {
        assertThrows(DomainValidationException.class, construction);
    }

    @Test
    void optionsAreImmutableSnapshots() {
        Map<String, Money> fees = new HashMap<>(Map.of("reminder", new Money(new BigDecimal("2"))));
        BookingOptions options = new BookingOptions(fees);
        fees.clear();
        assertEquals(new Money(new BigDecimal("2")), options.totalAdditionalFee());
        assertThrows(UnsupportedOperationException.class, () -> options.additionalFees().clear());
    }
}
