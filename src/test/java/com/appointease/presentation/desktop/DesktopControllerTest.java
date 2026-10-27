package com.appointease.presentation.desktop;

import com.appointease.Main;
import com.appointease.ApplicationContext;
import com.appointease.domain.model.*;
import com.appointease.domain.scheduling.SlotUnavailableException;
import com.appointease.domain.exception.DomainValidationException;
import com.appointease.infrastructure.seed.DesktopDemoSeed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class DesktopControllerTest {
    private final ApplicationContext context = Main.createContext(
            Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneOffset.UTC), ZoneId.of("Europe/Vilnius"));
    private final LocalDate day = DesktopDemoSeed.populate(context.facade(), context.time());
    private final DesktopController controller = new DesktopController(context.facade(), context.customers(), context.time(), context.notifications(), context.audit());
    private DesktopController.Draft draft(String at) {
        var provider = controller.providers().stream().filter(p -> p.id().value().contains("health")).findFirst().orElseThrow();
        return new DesktopController.Draft(controller.customers().getFirst(), provider, controller.services(provider).getFirst(),
                day, at, AppointmentMode.ONLINE, true, true, false);
    }

    @Test void reviewIsReadOnlyAndBookingUsesRealQuoteAndStableRetryIdentity() {
        int events = context.audit().get().size();
        var prepared = controller.prepare(draft("14:00"));
        assertEquals(events, context.audit().get().size());
        assertEquals("52.00", prepared.quote().totalFee().amount().toPlainString());
        var result = controller.book(prepared);
        assertEquals(AppointmentStatus.PENDING, result.appointment().status());
        assertEquals(AppointmentMode.ONLINE, result.appointment().mode());
        assertEquals(prepared.quote(), result.appointment().enhancements());
        assertEquals(result, controller.book(prepared));
        assertEquals(events + 1, context.audit().get().size());
        assertEquals(result.appointment(), controller.details(result.appointment().id()));
    }
    @ParameterizedTest @ValueSource(strings = {"", "9:00", "25:00", "10:60", "abc", "10:00:30"})
    void invalidTimeProducesNoWrites(String input) {
        int count = context.audit().get().size();
        assertThrows(IllegalArgumentException.class, () -> controller.prepare(draft(input)));
        assertEquals(count, context.audit().get().size());
        assertEquals(0, controller.revision());
    }
    @Test void missingCustomerAndUnsupportedModeAreRejectedWithoutWrites() {
        var d = draft("14:00");
        assertThrows(IllegalArgumentException.class, () -> controller.prepare(new DesktopController.Draft(null, d.provider(), d.service(), day, d.time(), d.mode(), false, false, false)));
        var salon = controller.providers().stream().filter(p -> p.id().value().contains("salon")).findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> controller.prepare(new DesktopController.Draft(d.customer(), salon,
                controller.services(salon).getFirst(), day, "14:00", AppointmentMode.ONLINE, false, false, false)));
        assertEquals(9, context.audit().get().size());
    }
    @Test void conflictAtReviewAndConflictAfterReviewBothReject() {
        assertThrows(SlotUnavailableException.class, () -> controller.prepare(draft("09:00")));
        var first = controller.prepare(draft("14:00"));
        var second = controller.prepare(draft("14:00"));
        controller.book(first);
        assertThrows(SlotUnavailableException.class, () -> controller.book(second));
        assertEquals(10, context.audit().get().size());
    }
    @Test void failedReschedulePreservesOriginalAndSuccessfulMoveRefreshesProjections() {
        var id = new AppointmentId("desktop-demo-001");
        var original = controller.details(id);
        controller.book(controller.prepare(draft("14:00")));
        var rejected = controller.prepareMove(id, day, "14:00", false);
        assertThrows(SlotUnavailableException.class, () -> controller.reschedule(rejected));
        assertSame(original, controller.details(id));
        var move = controller.prepareMove(id, day.plusDays(1), "10:00", true);
        var moved = controller.reschedule(move);
        assertEquals(moved, controller.reschedule(move));
        assertEquals(original.totalFee(), moved.appointment().totalFee());
        assertEquals(original.status(), moved.appointment().status());
        assertEquals(1, controller.appointments("", null, null, day.plusDays(1)).size());
        assertEquals(1, new OverviewModel(context.facade(), context.customers(), context.time()).load(day.plusDays(1)).appointments().size());
        assertEquals(3, controller.history(id).size());
    }
    @Test void cancellationReleasesCapacityAndRefreshesHistoryAndActivity() {
        var id = new AppointmentId("desktop-demo-001");
        controller.act(DesktopController.Action.CANCEL, "cancel", id);
        assertEquals(AppointmentStatus.CANCELLED, controller.details(id).status());
        assertEquals(1, controller.appointments("", AppointmentStatus.CANCELLED, null, null).size());
        assertNotNull(controller.prepare(draft("09:00")));
        assertEquals(20, controller.activity().size());
        assertEquals(com.appointease.domain.event.AppointmentEvent.Type.CANCELLED, controller.activity().getFirst().event().type());
        assertEquals(1, controller.revision());
    }
    @Test void confirmationAndCompletionUseDomainStateAndTimeRules() {
        var pending = new AppointmentId("desktop-demo-003");
        assertTrue(controller.can(DesktopController.Action.CONFIRM, controller.details(pending)));
        controller.act(DesktopController.Action.CONFIRM, "confirm", pending);
        assertFalse(controller.can(DesktopController.Action.CONFIRM, controller.details(pending)));
        assertFalse(controller.can(DesktopController.Action.COMPLETE, controller.details(pending)));
        assertThrows(DomainValidationException.class, () -> controller.act(DesktopController.Action.COMPLETE, "early", pending));
        var elapsed = new AppointmentId("seed-ready-001");
        assertTrue(controller.can(DesktopController.Action.COMPLETE, controller.details(elapsed)));
        controller.act(DesktopController.Action.COMPLETE, "complete", elapsed);
        assertEquals(AppointmentStatus.COMPLETED, controller.details(elapsed).status());
        for (var action : DesktopController.Action.values()) assertFalse(controller.can(action, controller.details(elapsed)));
    }
    @Test void filtersCombineCaseInsensitiveSearchStatusProviderAndDate() {
        var a = controller.details(new AppointmentId("desktop-demo-001"));
        assertEquals(1, controller.appointments("DANA", AppointmentStatus.CONFIRMED, a.provider().id(), day).size());
        assertEquals(0, controller.appointments("DANA", AppointmentStatus.PENDING, a.provider().id(), day).size());
        assertEquals(1, controller.appointments("desktop-demo-003", null, null, null).size());
        assertEquals(5, controller.appointments("", null, null, null).size());
        assertThrows(UnsupportedOperationException.class, () -> controller.appointments("", null, null, null).clear());
    }
    @Test void allDestinationsNavigateWithoutMutations() {
        for (var page : DesktopController.Page.values()) {
            controller.navigate(page);
            assertEquals(page, controller.page());
        }
        assertEquals(9, context.audit().get().size());
        assertEquals(0, controller.revision());
    }
    @Test void dateResolutionRejectsGapAndShowsEarlierOverlapInstant() {
        assertThrows(IllegalArgumentException.class, () -> controller.resolve(LocalDate.of(2026, 3, 29), "03:30"));
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), controller.resolve(LocalDate.of(2026, 10, 25), "03:30").toInstant());
    }
    @Test void providerAvailabilityAndEmptyActivityReadsRemainReadOnly() {
        var empty = new DesktopController(context.facade(), context.customers(), context.time(), java.util.List::of, java.util.List::of);
        assertTrue(empty.activity().isEmpty());
        assertNotNull(controller.prepare(draft("14:00")));
        assertEquals(9, context.audit().get().size());
        var broken = new DesktopController(context.facade(), context.customers(), context.time(), () -> { throw new IllegalStateException("Observer unavailable"); }, java.util.List::of);
        assertThrows(IllegalStateException.class, broken::activity);
    }

    @Test void deliveryWarningIsCommittedSuccessAndRetriesDoNotDuplicateWarningsOrReservations() {
        var clock = Clock.fixed(Instant.parse("2026-10-27T06:00:00Z"), ZoneOffset.UTC);
        var catalog = com.appointease.infrastructure.seed.DemoCatalog.create();
        var time = new com.appointease.domain.time.DomainTime(clock);
        var repository = new com.appointease.infrastructure.memory.InMemoryAppointmentRepository(
                catalog.customers(), catalog.providers(), catalog.services(), clock, time.zone());
        var dispatcher = new com.appointease.application.CommittedEventDispatcher(repository,
                new com.appointease.application.event.AppointmentEventPublisher(), event -> { throw new IllegalStateException("Simulated delivery failure"); });
        var facade = new com.appointease.application.BookingFacade(dispatcher, java.util.Map.of(
                AppointmentKind.CONSULTATION, new com.appointease.domain.creation.ConsultationAppointmentCreator(),
                AppointmentKind.SERVICE, new com.appointease.domain.creation.ServiceAppointmentCreator()), time);
        var warned = new DesktopController(facade, catalog.customers(), time, java.util.List::of, java.util.List::of);
        var prepared = warned.prepare(draft("14:00"));
        var result = warned.book(prepared);
        assertEquals(AppointmentStatus.PENDING, warned.details(result.appointment().id()).status());
        assertEquals(1, result.warnings().size());
        assertEquals(1, warned.warnings().size());
        assertEquals(result, warned.book(prepared));
        assertEquals(1, warned.warnings().size());
        assertEquals(1, repository.events().size());
    }
}
