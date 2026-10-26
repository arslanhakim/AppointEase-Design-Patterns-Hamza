package com.appointease.domain.scheduling;

/** A valid candidate conflicts with an existing effective reservation. */
public final class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException() { super("Provider slot is unavailable"); }
}
