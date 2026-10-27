package com.appointease.infrastructure.seed;

import com.appointease.application.BookingFacade;
import com.appointease.application.BookingRequest;
import com.appointease.domain.addon.BaseAppointmentService;
import com.appointease.domain.addon.ReminderServiceDecorator;
import com.appointease.domain.mode.InPersonModeFactory;
import com.appointease.domain.mode.OnlineModeFactory;
import com.appointease.domain.model.AppointmentId;
import com.appointease.domain.model.CustomerId;
import com.appointease.domain.model.ProviderId;
import com.appointease.domain.model.ServiceId;
import com.appointease.domain.scheduling.StandardAvailabilityStrategy;
import com.appointease.domain.time.DomainTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** Fictional desktop fixtures, created exclusively through real application commands. */
public final class DesktopDemoSeed {
    private DesktopDemoSeed() { }

    public static LocalDate populate(BookingFacade facade, DomainTime time) {
        LocalDate day = time.now().atZone(time.zone()).toLocalDate().plusDays(1);
        while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            day = day.plusDays(1);
        }
        String[] codes = {"health", "tutor", "consult", "salon"};
        LocalTime[] starts = {LocalTime.of(9, 0), LocalTime.of(10, 30), LocalTime.of(13, 0), LocalTime.of(15, 0)};
        for (int i = 0; i < codes.length; i++) {
            var id = new AppointmentId("desktop-demo-00" + (i + 1));
            facade.book("desktop-book-" + i, new BookingRequest(id,
                    new CustomerId("customer-demo-00" + (i % 3 + 1)),
                    new ProviderId("provider-" + codes[i] + "-001"),
                    new ServiceId("service-" + codes[i] + "-001"),
                    time.resolve(day.atTime(starts[i])).toInstant(),
                    i == 1 || i == 2 ? new OnlineModeFactory("Demo session " + (i + 1))
                            : new InPersonModeFactory("Demo Centre, room " + (i + 1)),
                    new ReminderServiceDecorator(new BaseAppointmentService()), new StandardAvailabilityStrategy()));
            if (i != 2) facade.confirm("desktop-confirm-" + i, id);
        }
        return day;
    }
}
