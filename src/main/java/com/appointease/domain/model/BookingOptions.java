package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** Fee snapshots only. Add-on behavior is supplied by domain.addon decorators. */
public record BookingOptions(Map<String, Money> additionalFees) {
    public BookingOptions {
        Validation.required(additionalFees, "additional fees");
        Map<String, Money> copy = new HashMap<>();
        additionalFees.forEach((code, fee) -> {
            String normalized = Validation.text(code, "add-on code");
            if (copy.putIfAbsent(normalized, Validation.required(fee, "add-on fee")) != null) {
                throw new DomainValidationException("Duplicate add-on: " + normalized);
            }
        });
        additionalFees = Map.copyOf(copy);
    }

    public Money totalAdditionalFee() {
        return additionalFees.values().stream().reduce(new Money(BigDecimal.ZERO), Money::plus);
    }
}
