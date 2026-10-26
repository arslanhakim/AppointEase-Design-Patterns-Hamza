package com.appointease.domain.exception;

/** The current lifecycle state does not permit the requested operation. */
public final class InvalidTransitionException extends IllegalStateException {
    public InvalidTransitionException(String message) {
        super(message);
    }
}
