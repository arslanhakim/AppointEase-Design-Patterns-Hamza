package com.appointease.domain;

import com.appointease.domain.addon.*;
import com.appointease.domain.creation.ConsultationAppointmentCreator;
import com.appointease.domain.model.*;
import com.appointease.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static com.appointease.domain.DomainFixtures.*;
import static com.appointease.domain.CreationPatternsTest.builder;
import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {
    @ParameterizedTest @CsvSource({"false,false,40.00,0", "true,false,50.00,1",
            "false,true,42.00,1", "true,true,52.00,2"})
    void baseAndOptionalCombinations(boolean priority, boolean reminder, String total, int count) {
        AppointmentService component = new BaseAppointmentService();
        if (priority) component = new PriorityBookingDecorator(component);
        if (reminder) component = new ReminderServiceDecorator(component);
        var quote = component.quote(service());
        assertEquals(new BigDecimal(total), quote.totalFee().amount());
        assertEquals(count, quote.options().additionalFees().size());
        assertEquals(count, quote.instructions().size());
        assertEquals(service().baseFee(), quote.baseFee());
        assertEquals("EUR", quote.totalFee().currency().getCurrencyCode());
    }

    @Test void delegationOccursOnceAndOrderPreservesBaseBehavior() {
        var calls = new AtomicInteger();
        AppointmentService base = service -> {
            calls.incrementAndGet();
            return new BookingEnhancements(service.baseFee(), options(), List.of("Base instructions"));
        };
        AppointmentService first = new ReminderServiceDecorator(new PriorityBookingDecorator(base));
        AppointmentService reverse = new PriorityBookingDecorator(new ReminderServiceDecorator(base));
        var a = first.quote(service());
        var b = reverse.quote(service());
        assertEquals(2, calls.get());
        assertEquals(a.totalFee(), b.totalFee());
        assertEquals(a.options(), b.options());
        assertEquals("Base instructions", a.instructions().getFirst());
        assertEquals("Base instructions", b.instructions().getFirst());
        assertTrue(a.instructions().get(1).startsWith("Priority"));
        assertTrue(a.instructions().get(2).startsWith("Reminder"));
        assertEquals(a.instructions().get(1), b.instructions().get(2));
        assertEquals(a.instructions().get(2), b.instructions().get(1));
    }

    @ParameterizedTest @CsvSource({"0.105,0.205,40.32", "1.004,1.005,42.01", "0,0,40.00", "99.999,0.001,140.00"})
    void roundsEachFeeBeforeAddition(String priority, String reminder, String expected) {
        AppointmentService component = new ReminderServiceDecorator(
                new PriorityBookingDecorator(new BaseAppointmentService(), new Money(new BigDecimal(priority))),
                new Money(new BigDecimal(reminder)));
        var quote = component.quote(service());
        assertEquals(new BigDecimal(expected), quote.totalFee().amount());
        assertEquals(2, quote.totalFee().amount().scale());
        assertEquals(2, quote.options().additionalFees().get("priority").amount().scale());
    }

    @Test void duplicateAddOnsRejectedEvenWhenSeparatedByAnotherWrapper() {
        var base = new BaseAppointmentService();
        assertThrows(DomainValidationException.class, () ->
                new PriorityBookingDecorator(new ReminderServiceDecorator(new PriorityBookingDecorator(base))).quote(service()));
        assertThrows(DomainValidationException.class, () ->
                new ReminderServiceDecorator(new PriorityBookingDecorator(new ReminderServiceDecorator(base))).quote(service()));
        var snapshot = new BaseAppointmentService(new BookingOptions(Map.of("priority", new Money(BigDecimal.ONE))));
        assertThrows(DomainValidationException.class, () -> new PriorityBookingDecorator(snapshot).quote(service()));
    }

    @Test void invalidFeeAndMissingDelegateAreRejected() {
        var base = new BaseAppointmentService();
        assertThrows(DomainValidationException.class, () -> new PriorityBookingDecorator(null));
        assertThrows(DomainValidationException.class, () -> new ReminderServiceDecorator(null));
        assertThrows(DomainValidationException.class, () -> new PriorityBookingDecorator(base, null));
        assertThrows(DomainValidationException.class, () -> new ReminderServiceDecorator(base, null));
        assertThrows(DomainValidationException.class, () ->
                new PriorityBookingDecorator(base, new Money(new BigDecimal("-0.001"))));
        assertThrows(DomainValidationException.class, () -> base.quote(null));
    }

    @Test void quotesAreImmutableDetachedAndDoNotChangeExistingAppointments() {
        var original = pending().confirm(time(BEFORE));
        var before = AppointmentStateTest.Snapshot.of(original);
        var messages = new ArrayList<>(List.of("Base"));
        var quote = new BookingEnhancements(service().baseFee(), options(), messages);
        messages.clear();
        var decorated = new ReminderServiceDecorator(new PriorityBookingDecorator(s -> quote)).quote(original.service());
        assertEquals(List.of("Base"), quote.instructions());
        assertEquals(3, decorated.instructions().size());
        assertTrue(quote.options().additionalFees().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> decorated.instructions().clear());
        assertThrows(UnsupportedOperationException.class, () -> decorated.options().additionalFees().clear());
        assertEquals(before, AppointmentStateTest.Snapshot.of(original));
        assertEquals(AppointmentStatus.CONFIRMED, original.status());
    }

    @Test void priorityCannotBypassFutureWorkingHoursOrProviderCompatibility() {
        var component = new PriorityBookingDecorator(new BaseAppointmentService());
        var creator = new ConsultationAppointmentCreator();
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(component).build(creator, time(START)));
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(component)
                .start(START.plusSeconds(12 * 3600)).build(creator, time(BEFORE)));
        var p = provider();
        var restricted = new ServiceProvider(p.id(), p.name(), p.category(), p.serviceIds(),
                Set.of(AppointmentMode.IN_PERSON), p.workingHours(), p.preparationBuffer());
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(component)
                .provider(restricted).build(creator, time(BEFORE)));
        var unsupported = new Service(new ServiceId("other"), "Other", service().duration(),
                service().baseFee(), service().kind());
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(component)
                .service(unsupported).build(creator, time(BEFORE)));
    }

    @Test void builderRevalidatesTimeOnEveryBuildAndSnapshotsEachQuote() {
        var b = builder().appointmentService(new ReminderServiceDecorator(new BaseAppointmentService()));
        var creator = new ConsultationAppointmentCreator();
        var first = b.build(creator, time(BEFORE));
        assertThrows(DomainValidationException.class, () -> b.build(creator, time(START)));
        b.appointmentService(new BaseAppointmentService());
        var second = b.build(creator, time(BEFORE));
        assertEquals(new BigDecimal("42.00"), first.totalFee().amount());
        assertEquals(new BigDecimal("40.00"), second.totalFee().amount());
        assertEquals(1, first.enhancements().instructions().size());
    }
}
