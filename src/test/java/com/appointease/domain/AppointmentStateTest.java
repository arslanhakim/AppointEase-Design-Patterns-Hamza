package com.appointease.domain;

import com.appointease.domain.exception.DomainValidationException;
import com.appointease.domain.exception.InvalidTransitionException;
import com.appointease.domain.model.*;
import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.time.DomainTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.Instant;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static com.appointease.domain.DomainFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentStateTest {
    enum Operation { CONFIRM, CANCEL, COMPLETE, RESCHEDULE }

    static Appointment inState(AppointmentStatus status) {
        Appointment pending = pending();
        return switch (status) {
            case PENDING -> pending;
            case CONFIRMED -> pending.confirm(time(BEFORE));
            case COMPLETED -> pending.confirm(time(BEFORE)).complete(time(END));
            case CANCELLED -> pending.cancel(time(BEFORE));
        };
    }

    static Stream<Arguments> stateOperationPairs() {
        return Stream.of(AppointmentStatus.values()).flatMap(status -> Stream.of(Operation.values())
                .map(operation -> Arguments.of(status, operation)));
    }

    static Object apply(Appointment appointment, Operation operation, DomainTime time) {
        return switch (operation) {
            case CONFIRM -> appointment.confirm(time);
            case CANCEL -> appointment.cancel(time);
            case COMPLETE -> appointment.complete(time);
            case RESCHEDULE -> appointment.validateReschedule(START.plusSeconds(3600), time);
        };
    }

    static boolean allowed(AppointmentStatus status, Operation operation) {
        return (status == AppointmentStatus.PENDING && (operation == Operation.CONFIRM || operation == Operation.CANCEL
                || operation == Operation.RESCHEDULE))
                || (status == AppointmentStatus.CONFIRMED && operation != Operation.CONFIRM);
    }

    /** Snapshot every observable field to detect accidental mutation on any path. */
    record Snapshot(AppointmentId id, Customer customer, ServiceProvider provider, Service service,
                    TimeSlot slot, AppointmentMode mode, BookingOptions options, ZoneId zone, Money total, AppointmentStatus status) {
        static Snapshot of(Appointment a) {
            return new Snapshot(a.id(), a.customer(), a.provider(), a.service(), a.slot(), a.mode(), a.options(), a.businessZone(), a.totalFee(), a.status());
        }
    }

    @ParameterizedTest
    @MethodSource("stateOperationPairs")
    void everyStateOperationPairIsEnforcedPolymorphically(AppointmentStatus status, Operation operation) {
        Appointment appointment = inState(status);
        Snapshot before = Snapshot.of(appointment);
        DomainTime time = time(operation == Operation.COMPLETE ? END : BEFORE);
        if (!allowed(status, operation)) {
            assertThrows(InvalidTransitionException.class, () -> apply(appointment, operation, time));
        } else {
            Object result = apply(appointment, operation, time);
            if (result instanceof Appointment updated) {
                AppointmentStatus expected = switch (operation) {
                    case CONFIRM -> AppointmentStatus.CONFIRMED;
                    case CANCEL -> AppointmentStatus.CANCELLED;
                    case COMPLETE -> AppointmentStatus.COMPLETED;
                    case RESCHEDULE -> throw new AssertionError("Reschedule returns only a proposal");
                };
                assertEquals(expected, updated.status());
                assertEquals(before.id(), updated.id());
                assertEquals(before.slot(), updated.slot());
                assertEquals(new Snapshot(before.id(), before.customer(), before.provider(), before.service(),
                        before.slot(), before.mode(), before.options(), before.zone(), before.total(), expected), Snapshot.of(updated));
            } else {
                assertEquals(new TimeSlot(START.plusSeconds(3600), END.plusSeconds(3600)), result);
            }
        }
        assertEquals(before, Snapshot.of(appointment));
    }

    static Stream<Arguments> futureOnlyOperations() {
        return Stream.of(Arguments.of(AppointmentStatus.PENDING, Operation.CONFIRM),
                Arguments.of(AppointmentStatus.PENDING, Operation.CANCEL),
                Arguments.of(AppointmentStatus.CONFIRMED, Operation.CANCEL),
                Arguments.of(AppointmentStatus.PENDING, Operation.RESCHEDULE),
                Arguments.of(AppointmentStatus.CONFIRMED, Operation.RESCHEDULE));
    }

    @ParameterizedTest
    @MethodSource("futureOnlyOperations")
    void operationsRequireOriginalStartStrictlyAfterNow(AppointmentStatus status, Operation operation) {
        Appointment appointment = inState(status);
        Snapshot original = Snapshot.of(appointment);
        assertDoesNotThrow(() -> apply(appointment, operation, time(START.minusNanos(1))));
        for (Instant now : new Instant[]{START, START.plusNanos(1), END}) {
            assertThrows(DomainValidationException.class, () -> apply(appointment, operation, time(now)));
            assertEquals(original, Snapshot.of(appointment));
        }
    }

    @Test
    void completionRequiresEndToHaveBeenReached() {
        Appointment confirmed = inState(AppointmentStatus.CONFIRMED);
        Snapshot before = Snapshot.of(confirmed);
        for (Instant now : new Instant[]{BEFORE, START, END.minusNanos(1)}) {
            assertThrows(DomainValidationException.class, () -> confirmed.complete(time(now)));
            assertEquals(before, Snapshot.of(confirmed));
        }
        assertEquals(AppointmentStatus.COMPLETED, confirmed.complete(time(END)).status());
        assertEquals(AppointmentStatus.COMPLETED, confirmed.complete(time(END.plusNanos(1))).status());
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"PENDING", "CONFIRMED"})
    void invalidRescheduleProposalLeavesAppointmentUnchanged(AppointmentStatus status) {
        Appointment appointment = inState(status);
        Snapshot before = Snapshot.of(appointment);
        for (Instant newStart : new Instant[]{BEFORE.minusNanos(1), BEFORE, START.plusSeconds(10 * 3600)}) {
            assertThrows(DomainValidationException.class, () -> appointment.validateReschedule(newStart, time(BEFORE)));
            assertEquals(before, Snapshot.of(appointment));
        }
        assertThrows(DomainValidationException.class, () -> appointment.validateReschedule(null, time(BEFORE)));
        assertEquals(before, Snapshot.of(appointment));
    }

    @Test
    void missingInjectedTimeFailsWithoutMutation() {
        Appointment appointment = pending();
        Snapshot before = Snapshot.of(appointment);
        for (Operation operation : Operation.values()) {
            assertThrows(DomainValidationException.class, () -> apply(appointment, operation, null));
            assertEquals(before, Snapshot.of(appointment));
        }
    }

    @Test
    void reschedulingCannotSilentlyChangeTheBusinessZone() {
        Appointment appointment = pending();
        Snapshot original = Snapshot.of(appointment);
        DomainTime otherZone = new DomainTime("UTC", Clock.fixed(BEFORE, ZoneOffset.UTC));
        assertThrows(DomainValidationException.class,
                () -> appointment.validateReschedule(START.plusSeconds(3600), otherZone));
        assertEquals(original, Snapshot.of(appointment));
    }
}
