package com.appointease.domain.scheduling;

import com.appointease.domain.model.ServiceProvider;
import java.time.Duration;

public final class StandardAvailabilityStrategy implements AvailabilityStrategy {
    @Override public Duration preparationBuffer(ServiceProvider provider) { return Duration.ZERO; }
}
