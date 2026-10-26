package com.appointease.domain.exception;

/** Invalid domain data or a violated time-dependent business rule. */
public class DomainValidationException extends IllegalArgumentException {
    public DomainValidationException(String message) {
        super(message);
    }
}
