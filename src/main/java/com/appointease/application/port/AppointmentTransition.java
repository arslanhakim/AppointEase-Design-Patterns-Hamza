package com.appointease.application.port;

import com.appointease.domain.appointment.Appointment;
import com.appointease.domain.time.DomainTime;

/** Finite commands; State still owns eligibility and transition behavior. */
public enum AppointmentTransition {
    CONFIRM { public Appointment apply(Appointment value, DomainTime time) { return value.confirm(time); } },
    CANCEL { public Appointment apply(Appointment value, DomainTime time) { return value.cancel(time); } },
    COMPLETE { public Appointment apply(Appointment value, DomainTime time) { return value.complete(time); } };
    public abstract Appointment apply(Appointment value, DomainTime time);
}
