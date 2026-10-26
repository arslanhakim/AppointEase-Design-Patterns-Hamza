package com.appointease.application.port;

public final class RepositoryException extends RuntimeException {
    public enum Reason { NOT_FOUND, DUPLICATE_ID, CATALOG_MISMATCH, REENTRANT_WRITE }
    private final Reason reason;
    public RepositoryException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
