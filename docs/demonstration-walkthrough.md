# Academic demonstration walkthrough

Allow about 7 minutes. Use Java 21 and Maven 3.9.x. Build first with `mvn clean verify`, then run `java -jar target/appointease-0.1.0-SNAPSHOT.jar`. The application needs no runtime network. Use the future weekday suggested at startup; dates are in Europe/Vilnius unless configured otherwise.

1. Show providers (1) and services (2): four business categories, supported modes, working hours, durations and EUR fees.
2. Book (4): customer 1, provider `provider-health-001`, type 1, service 1, suggested date at 10:00, mode 1, priority y, reminder y, policy 1, confirm y. Show the PENDING appointment, `@1` shortcut, resolved offset and EUR 52.00 total. Explain Builder, Factory Method, Abstract Factory and Decorator in the creation sequence.
3. Try the identical slot: unavailable. Book 10:30 with the same duration and standard policy: exact adjacency succeeds. Attempt to reschedule `@2` to the same 10:30 using buffer policy 2: rejection leaves its original slot intact. Explain Strategy and atomic repository validation.
4. Confirm `@1` (7), reschedule it (8) to 11:30 with buffer policy 2, then cancel it (9). Book its released slot again. Explain State, immutable replacement and success-only events.
5. Book an in-person salon service using provider `provider-salon-001`, type 2, service 1, mode 2 and no extras: EUR 20.00. This demonstrates the other product subtype and family.
6. Complete (10) `seed-ready-001`. It was legitimately booked and confirmed for the previous weekday; do not attempt to complete a newly booked future appointment. Show details (6), customer appointments (5) and notification/audit history (11). Explain Observer and calendar Adapter through the booking sequence.
7. Enter an invalid menu choice, show recovery, then exit (0). Mention graceful EOF, local-only storage, simulated calendar/notifications, and the S6.1 automated package-cycle check (zero cycles).

For exact repeatable input and assertions, run `./scripts/demo.ps1`; it performs this workflow plus two EOF processes and writes transcripts to target. Run `./scripts/verify-console.ps1` for separate invalid-input/EOF/timezone process checks. Tests and scripts do not contact external services.

For oral defense: begin at ConsoleApplication -> BookingFacade -> AppointmentAssembler -> Builder/creator -> repository/Strategy -> publisher/listeners -> Adapter; later lifecycle operations delegate to State. Open [S6-AUDIT.md](S6-AUDIT.md) for concrete roles, test methods and qualifications. Builder is a fluent concrete variant, Strategy controls preparation duration, and the two appointment products intentionally share most business rules.
