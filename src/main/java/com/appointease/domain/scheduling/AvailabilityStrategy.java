package com.appointease.domain.scheduling;

import com.appointease.domain.model.ServiceProvider;
import java.time.Duration;

/** Additional preparation policy. Hard rules cannot be replaced by a strategy. */
@FunctionalInterface
public interface AvailabilityStrategy {
    Duration preparationBuffer(ServiceProvider provider);
}
