package com.appointease.domain.time;

import com.appointease.domain.exception.DomainValidationException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;

/** Explicit business zone, independent of the injected clock's zone. */
public final class DomainTime {
    private final ZoneId zone;
    private final Clock clock;

    public DomainTime(Clock clock) { this("Europe/Vilnius", clock); }

    public DomainTime(String zoneId, Clock clock) {
        if (clock == null || zoneId == null) {
            throw new DomainValidationException("Zone and clock are required");
        }
        try {
            this.zone = ZoneId.of(zoneId);
        } catch (DateTimeException ex) {
            throw new DomainValidationException("Invalid business zone: " + zoneId);
        }
        this.clock = clock;
    }

    public ZoneId zone() { return zone; }
    public Instant now() { return clock.instant(); }

    /** Gaps are rejected; overlap resolution selects the earlier instant explicitly. */
    public ZonedDateTime resolve(LocalDateTime local) {
        if (local == null) {
            throw new DomainValidationException("Local time is required");
        }
        var offsets = zone.getRules().getValidOffsets(local);
        return offsets.stream().map(offset -> ZonedDateTime.ofStrict(local, offset, zone))
                .min(Comparator.comparing(ZonedDateTime::toInstant))
                .orElseThrow(() -> new DomainValidationException("Nonexistent local time: " + local + " in " + zone));
    }
}
