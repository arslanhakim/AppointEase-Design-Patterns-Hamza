package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.time.Duration;

public record Service(ServiceId id, String name, Duration duration, Money baseFee, AppointmentKind kind,
        ProviderCategory category) {
    public Service(ServiceId id, String name, Duration duration, Money baseFee, AppointmentKind kind) {
        this(id, name, duration, baseFee, kind, ProviderCategory.CONSULTING);
    }
    public Service {
        Validation.required(id, "service ID");
        name = Validation.text(name, "service name");
        if (Validation.required(duration, "duration").isNegative() || duration.isZero()) {
            throw new DomainValidationException("Service duration must be positive");
        }
        Validation.required(baseFee, "base fee");
        Validation.required(kind, "appointment kind");
        Validation.required(category, "service category");
    }
}
