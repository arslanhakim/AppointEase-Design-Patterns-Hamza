package com.appointease.domain;

import com.appointease.domain.creation.*;
import com.appointease.domain.mode.*;
import com.appointease.domain.addon.*;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.appointment.ConsultationAppointment;
import com.appointease.domain.appointment.ServiceAppointment;
import com.appointease.domain.appointment.ValidatedAppointmentSpec;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class CreationPatternsTest {
    static AppointmentBuilder builder() {
        return new AppointmentBuilder().id(new AppointmentId("s2")).customer(customer()).provider(provider())
                .service(service()).start(START).mode(new OnlineModeFactory("session-1"));
    }
    static Stream<Arguments> kindsAndModes() {
        return Stream.of(AppointmentKind.values()).flatMap(kind -> Stream.of(AppointmentMode.values())
                .map(mode -> Arguments.of(kind, mode)));
    }
    @ParameterizedTest @MethodSource("kindsAndModes")
    void bothCreatorsAndFamiliesPreserveLifecycleAndImmutableSnapshots(AppointmentKind kind, AppointmentMode mode) {
        var service = new Service(service().id(), "Kind variant", service().duration(), service().baseFee(), kind);
        AppointmentCreator creator = kind == AppointmentKind.CONSULTATION
                ? new ConsultationAppointmentCreator() : new ServiceAppointmentCreator();
        AppointmentModeFactory family = mode == AppointmentMode.ONLINE
                ? new OnlineModeFactory("meeting-1") : new InPersonModeFactory("Room 12");
        var chain = new ReminderServiceDecorator(new PriorityBookingDecorator(new BaseAppointmentService()));
        var builder = builder().service(service).mode(family).appointmentService(chain);
        Appointment original = builder.build(creator, time(BEFORE));
        assertEquals(kind == AppointmentKind.CONSULTATION ? ConsultationAppointment.class : ServiceAppointment.class,
                original.getClass());
        assertEquals(AppointmentStatus.PENDING, original.status());
        assertEquals(mode, original.mode());
        assertEquals(mode, original.modeDetails().instructions().mode());
        assertSame(original.modeDetails().meeting().familyIdentity(), original.modeDetails().instructions().familyIdentity());
        assertEquals(new BigDecimal("52.00"), original.totalFee().amount());
        var confirmed = original.confirm(time(BEFORE));
        var completed = confirmed.complete(time(END));
        var cancelled = original.cancel(time(BEFORE));
        var cancelledConfirmed = confirmed.cancel(time(BEFORE));
        for (var result : List.of(confirmed, completed, cancelled, cancelledConfirmed)) {
            assertEquals(original.getClass(), result.getClass());
            assertEquals(original.id(), result.id());
            assertSame(original.customer(), result.customer());
            assertSame(original.provider(), result.provider());
            assertSame(original.service(), result.service());
            assertSame(original.slot(), result.slot());
            assertSame(original.modeDetails(), result.modeDetails());
            assertSame(original.enhancements(), result.enhancements());
            assertEquals(original.businessZone(), result.businessZone());
        }
        assertEquals(AppointmentStatus.COMPLETED, completed.status());
        assertEquals(AppointmentStatus.CANCELLED, cancelled.status());
        assertThrows(InvalidTransitionException.class, () -> completed.cancel(time(BEFORE)));
        assertThrows(InvalidTransitionException.class, () -> cancelled.confirm(time(BEFORE)));
        assertEquals(AppointmentStatus.PENDING, original.status());
        builder.id(new AppointmentId("later")).start(START.plusSeconds(3600));
        assertEquals(new AppointmentId("s2"), original.id());
        assertEquals(START, original.slot().start());
    }
    @Test void abstractCreatorOverrideIsActuallyInvokedWithValidatedData() {
        var captured = new ArrayList<ValidatedAppointmentSpec>();
        AppointmentCreator creator = new AppointmentCreator(AppointmentKind.CONSULTATION) {
            @Override protected Appointment createAppointment(ValidatedAppointmentSpec spec) {
                captured.add(spec);
                return new ConsultationAppointment(spec);
            }
        };
        Appointment result = builder().build(creator, time(BEFORE));
        assertEquals(1, captured.size());
        assertEquals(new TimeSlot(START, END), captured.getFirst().slot());
        assertEquals(result.id(), captured.getFirst().id());
        assertThrows(DomainValidationException.class, () -> builder().customer(null).build(creator, time(BEFORE)));
        assertEquals(1, captured.size(), "Invalid input must never reach the factory method");
        assertThrows(DomainValidationException.class, () -> new ServiceAppointment(captured.getFirst()));
    }
    @ParameterizedTest @EnumSource(AppointmentKind.class)
    void incompatibleCreatorIsRejected(AppointmentKind kind) {
        Service variant = new Service(service().id(), "Variant", service().duration(), service().baseFee(), kind);
        AppointmentCreator wrong = kind == AppointmentKind.CONSULTATION
                ? new ServiceAppointmentCreator() : new ConsultationAppointmentCreator();
        assertThrows(DomainValidationException.class, () -> builder().service(variant).build(wrong, time(BEFORE)));
    }
    static Stream<Consumer<AppointmentBuilder>> missing() {
        return Stream.of(b -> b.id(null), b -> b.customer(null), b -> b.provider(null),
                b -> b.service(null), b -> b.start(null), b -> b.slot(null));
    }
    @ParameterizedTest @MethodSource("missing")
    void requiredDataCannotBeOmitted(Consumer<AppointmentBuilder> remove) {
        var b = builder(); remove.accept(b);
        assertThrows(DomainValidationException.class, () -> b.build(new ConsultationAppointmentCreator(), time(BEFORE)));
    }
    @Test void missingCollaboratorsAndModeFailCleanly() {
        assertThrows(DomainValidationException.class, () -> new AppointmentBuilder().id(new AppointmentId("a"))
                .customer(customer()).provider(provider()).service(service()).start(START)
                .build(new ConsultationAppointmentCreator(), time(BEFORE)));
        assertThrows(DomainValidationException.class, () -> builder().build(null, time(BEFORE)));
        assertThrows(DomainValidationException.class, () -> builder().build(new ConsultationAppointmentCreator(), null));
        assertThrows(DomainValidationException.class, () -> builder().mode(null));
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(null));
        var family = new OnlineModeFactory("room");
        assertThrows(DomainValidationException.class, () -> builder().mode(null, family.createParticipationInstructions()));
        assertThrows(DomainValidationException.class, () -> builder().mode(family.createMeetingDetails(), null));
        assertThrows(DomainValidationException.class, () -> new ConsultationAppointmentCreator().create(null));
        assertThrows(DomainValidationException.class, () -> new ConsultationAppointment(null));
        assertThrows(DomainValidationException.class, () -> new ServiceAppointment(null));
    }
    @Test void rejectsMixedModesAndDifferentSessionsEvenWithIdenticalLabels() {
        var online = new OnlineModeFactory("same");
        var otherOnline = new OnlineModeFactory("same");
        var physical = new InPersonModeFactory("same");
        var otherPhysical = new InPersonModeFactory("same");
        for (var pair : List.of(new AppointmentModeFactory[]{online, physical},
                new AppointmentModeFactory[]{physical, online}, new AppointmentModeFactory[]{online, otherOnline},
                new AppointmentModeFactory[]{physical, otherPhysical})) {
            assertThrows(DomainValidationException.class, () ->
                    builder().mode(pair[0].createMeetingDetails(), pair[1].createParticipationInstructions()));
        }
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void factoriesRequireRealDetails(String text) {
        assertThrows(DomainValidationException.class, () -> new OnlineModeFactory(text));
        assertThrows(DomainValidationException.class, () -> new InPersonModeFactory(text));
    }
    @Test void productBehaviorUsesMatchingSessionOrLocation() {
        AppointmentModeFactory online = new OnlineModeFactory(" abc ");
        assertEquals("Simulated meeting: abc", online.createMeetingDetails().text());
        assertEquals("Join simulated meeting abc", online.createParticipationInstructions().text());
        AppointmentModeFactory physical = new InPersonModeFactory(" Office ");
        assertEquals("Location: Office", physical.createMeetingDetails().text());
        assertEquals("Check in at reception: Office", physical.createParticipationInstructions().text());
    }
    @Test void explicitSlotMustMatchDurationAndLastSlotSetterWins() {
        var creator = new ConsultationAppointmentCreator();
        assertThrows(DomainValidationException.class,
                () -> builder().slot(new TimeSlot(START, END.plusSeconds(1))).build(creator, time(BEFORE)));
        assertEquals(new TimeSlot(START, END), builder().slot(new TimeSlot(START, END)).build(creator, time(BEFORE)).slot());
        assertEquals(START, builder().slot(new TimeSlot(START, END.plusSeconds(1))).start(START)
                .build(creator, time(BEFORE)).slot().start());
    }
    @Test void quoteCannotReplaceBasePriceOrReturnMissingData() {
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(s ->
                new BookingEnhancements(new Money(BigDecimal.ZERO), options(), List.of()))
                .build(new ConsultationAppointmentCreator(), time(BEFORE)));
        assertThrows(DomainValidationException.class, () -> builder().appointmentService(s -> null)
                .build(new ConsultationAppointmentCreator(), time(BEFORE)));
    }
    @Test void publicSpecConstructorCannotBypassValidation() {
        var factory = new OnlineModeFactory("room");
        var mode = new AppointmentModeDetails(factory.createMeetingDetails(), factory.createParticipationInstructions());
        var quote = new BaseAppointmentService().quote(service());
        assertThrows(DomainValidationException.class, () -> new ValidatedAppointmentSpec(new AppointmentId("a"),
                customer(), provider(), service(), new TimeSlot(START, END.plusSeconds(1)), mode, quote, time(BEFORE)));
        assertThrows(DomainValidationException.class, () -> new ValidatedAppointmentSpec(new AppointmentId("a"),
                customer(), provider(), service(), new TimeSlot(START, END), mode, quote, time(START)));
    }
}
