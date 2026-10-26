package com.appointease.application.port;
public final class CalendarSyncException extends RuntimeException {
    public enum Reason { INVALID_TIMESTAMP, CONFLICT, UNAVAILABLE }
    private final Reason reason;
    public CalendarSyncException(Reason reason, Throwable cause) { super(reason.toString(), cause); this.reason = reason; }
    public Reason reason() { return reason; }
}
