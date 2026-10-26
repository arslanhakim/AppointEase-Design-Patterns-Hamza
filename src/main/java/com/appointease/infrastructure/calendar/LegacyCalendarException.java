package com.appointease.infrastructure.calendar;
public final class LegacyCalendarException extends RuntimeException {
    private final int code;
    public LegacyCalendarException(int code) { super("Legacy calendar error " + code); this.code = code; }
    public int code() { return code; }
}
