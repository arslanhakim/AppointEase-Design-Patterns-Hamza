package com.appointease.infrastructure.calendar;
import java.time.ZoneId;
import java.util.*;
/** Deterministic offline legacy API. Blocked keys simulate external calendar conflicts. */
public final class SimulatedExternalCalendarClient {
    public record Entry(String operation, long startMillis, long endMillis, String zone, long revision) { }
    private final Map<String, Entry> entries = new HashMap<>();
    private final Set<String> applied = new HashSet<>();
    private final Set<String> blockedKeys;
    private final int failureCode;
    public SimulatedExternalCalendarClient() { this(Set.of(), 0); }
    public SimulatedExternalCalendarClient(Set<String> blockedKeys, int failureCode) {
        this.blockedKeys = Set.copyOf(blockedKeys); this.failureCode = failureCode;
    }
    public synchronized void apply(String operationCode, String key, long startEpochMillis,
            long endEpochMillis, String zoneName, String revisionToken, String eventKey) {
        if (applied.contains(eventKey)) return;
        if (failureCode != 0) throw new LegacyCalendarException(failureCode);
        long revision;
        try {
            revision = Long.parseLong(revisionToken.substring(1));
            if (!revisionToken.startsWith("r") || revision < 1 || key.isBlank() || eventKey.isBlank()
                    || startEpochMillis >= endEpochMillis || !Set.of("PUT", "ACK", "MOVE", "DELETE", "DONE").contains(operationCode))
                throw new IllegalArgumentException();
            ZoneId.of(zoneName);
        } catch (RuntimeException ex) { throw new LegacyCalendarException(400); }
        if (blockedKeys.contains(key) && !operationCode.equals("DELETE")) throw new LegacyCalendarException(409);
        Entry prior = entries.get(key);
        if (prior != null && revision <= prior.revision()) throw new LegacyCalendarException(409);
        // Retain a tombstone so stale revisions cannot resurrect a cancellation.
        entries.put(key, new Entry(operationCode, startEpochMillis, endEpochMillis, zoneName, revision));
        applied.add(eventKey);
    }
    public synchronized Map<String, Entry> entries() { return Map.copyOf(entries); }
    public synchronized int appliedCount() { return applied.size(); }
}
