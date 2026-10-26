package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;

public final class Validation {
    private Validation() { }

    public static <T> T required(T value, String field) {
        if (value == null) {
            throw new DomainValidationException(field + " is required");
        }
        return value;
    }

    public static String text(String value, String field) {
        if (required(value, field).isBlank()) {
            throw new DomainValidationException(field + " must not be blank");
        }
        return value.strip();
    }
}
