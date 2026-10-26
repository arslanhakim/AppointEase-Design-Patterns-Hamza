package com.appointease.infrastructure.seed;

import com.appointease.application.*;
import com.appointease.domain.addon.BaseAppointmentService;
import com.appointease.domain.mode.InPersonModeFactory;
import com.appointease.domain.model.*;
import com.appointease.domain.scheduling.StandardAvailabilityStrategy;
import com.appointease.domain.time.DomainTime;
import java.time.*;
import java.util.Objects;

/** Replays a real historical booking/confirmation before handing over to the runtime clock.
 * Setup completes before the console is exposed. It never imports arbitrary lifecycle states.
 */
public final class DemoSeed {
    private final SetupClock clock;
    private final DomainTime time;
    private final LocalDate previousDay;

    public DemoSeed(Clock runtime, ZoneId zone) {
        var day = runtime.instant().atZone(zone).toLocalDate().minusDays(1);
        while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            day = day.minusDays(1);
        }
        previousDay = day;
        clock = new SetupClock(runtime, day.minusDays(1).atStartOfDay(zone).toInstant());
        time = new DomainTime(zone.getId(), clock);
    }
    public Clock clock() { return clock; }
    public DomainTime time() { return time; }

    public void populate(BookingFacade facade) {
        try {
            var id = new AppointmentId("seed-ready-001");
            facade.book("seed-book", new BookingRequest(id, new CustomerId("customer-demo-001"),
                    new ProviderId("provider-health-001"), new ServiceId("service-health-001"),
                    time.resolve(previousDay.atTime(10, 0)).toInstant(), new InPersonModeFactory("Demo Centre, room 1"),
                    new BaseAppointmentService(), new StandardAvailabilityStrategy()));
            facade.confirm("seed-confirm", id);
        } finally {
            clock.finish();
        }
    }

    private static final class SetupClock extends Clock {
        private final Clock runtime;
        private Instant bootstrap;
        private SetupClock(Clock runtime, Instant bootstrap) {
            this.runtime = Objects.requireNonNull(runtime);
            this.bootstrap = bootstrap;
        }
        private void finish() { bootstrap = null; }
        public ZoneId getZone() { return runtime.getZone(); }
        public Clock withZone(ZoneId zone) {
            return bootstrap == null ? runtime.withZone(zone) : Clock.fixed(bootstrap, zone);
        }
        public Instant instant() { return bootstrap == null ? runtime.instant() : bootstrap; }
    }
}
