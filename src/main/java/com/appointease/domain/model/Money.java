package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/** EUR only; validate negativity before rounding, normalize each amount HALF_UP. */
public record Money(BigDecimal amount) {
    public Money {
        if (Validation.required(amount, "amount").signum() < 0) {
            throw new DomainValidationException("Fees must not be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Currency currency() { return Currency.getInstance("EUR"); }

    public Money plus(Money other) {
        return new Money(amount.add(Validation.required(other, "money").amount));
    }
}
