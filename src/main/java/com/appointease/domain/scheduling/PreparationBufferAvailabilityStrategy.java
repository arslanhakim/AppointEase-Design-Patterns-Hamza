package com.appointease.domain.scheduling;

import com.appointease.domain.model.ServiceProvider;
import com.appointease.domain.model.Validation;
import com.appointease.domain.exception.DomainValidationException;
import java.time.Duration;

/** Uses provider configuration by default, or an explicit immutable policy override. */
public final class PreparationBufferAvailabilityStrategy implements AvailabilityStrategy {
    private final Duration buffer;
    public PreparationBufferAvailabilityStrategy() { buffer = null; }
    public PreparationBufferAvailabilityStrategy(Duration buffer) {
        this.buffer = Validation.required(buffer, "buffer");
        if (buffer.isNegative()) throw new DomainValidationException("Buffer must not be negative");
    }
    @Override public Duration preparationBuffer(ServiceProvider provider) {
        return buffer == null ? provider.preparationBuffer() : buffer;
    }
}
