# AppointEase

Java 21 / Maven academic appointment booking console. **S0-S6.1 complete; all nine GoF patterns integrated and independently audited.** See docs/S6-AUDIT.md for findings and qualifications. No runtime network or external dependencies.

## Build and run

Install JDK 21 and Maven 3.9.x, set JAVA_HOME, and put both on PATH:

```powershell
java -version
mvn -version
mvn clean verify
java -jar target/appointease-0.1.0-SNAPSHOT.jar
```

Maven may download build/test dependencies. The application itself runs offline. Change the business timezone with:

```powershell
java '-Dappointease.zone=UTC' -jar target/appointease-0.1.0-SNAPSHOT.jar
```

Default: Europe/Vilnius. Dates are YYYY-MM-DD and times HH:mm in that zone; the selected UTC offset is displayed. DST gaps reject and repeated times use the earlier instant. All data disappears on exit.

## Console use

The main menu provides providers (1), services (2), advisory availability (3), booking (4), customer appointments (5), details/event history (6), confirm (7), reschedule (8), cancel (9), complete (10), notifications/audit (11), and exit (0).

For booking, choose a customer and provider by row number or full ID, choose consultation/service type, then a compatible service, date/time, online/in-person mode, optional priority/reminder services and standard/buffered availability. Review the provider, slot and EUR fee breakdown before answering y to save. Availability is advisory; the facade rechecks atomically on booking. The selected service determines duration and type. Salon services require in-person mode.

Copy the displayed appointment UUID for later commands, or use @1, @2, etc. for successful bookings in this session. Option 5 lists current snapshots by customer; option 6 includes the immutable event history. Rescheduling asks for final confirmation. Invalid input reports an error and returns to the main menu; re-enter that operation. EOF closes the session and discards an unfinished interaction. No ANSI colors or terminal libraries are used.

## Demo catalog

Three fictional customers: customer-demo-001 (Alex Morgan), customer-demo-002 (Sam Taylor), customer-demo-003 (Robin Lee).

| Provider ID | Name/category | Service ID | Duration | Base fee | Modes |
| --- | --- | --- | --- | --- | --- |
| provider-health-001 | Dana Green / healthcare | service-health-001 | 30 min | EUR 40.00 | Both |
| provider-tutor-001 | Jamie White / tutoring | service-tutor-001 | 60 min | EUR 25.00 | Both |
| provider-consult-001 | Casey Brown / consulting | service-consult-001 | 45 min | EUR 60.00 | Both |
| provider-salon-001 | Riley Stone / personal services | service-salon-001 | 30 min | EUR 20.00 | In person |

All providers work Monday-Friday 09:00-17:00. Buffered scheduling reserves 15 minutes before the appointment; standard scheduling permits exact adjacency. Priority adds EUR 10.00; reminder adds EUR 2.00. Neither bypasses scheduling rules. The first three services are consultations; salon is a general service.

Startup seeds one confirmed appointment, seed-ready-001, for the previous weekday at 10:00 with Dana Green and Alex Morgan. It is booked and confirmed through the real facade under a startup-only historical clock, then the clock permanently delegates to the injected runtime clock. Option 10 can complete it immediately. Seed events are visible in notification/audit history. No active reservations overlap and there are no fixed production appointment dates.

New bookings start pending; confirm/cancel/reschedule before their start. Complete only after a confirmed appointment ends. A newly booked future appointment cannot be completed immediately. Startup prints a suitable next-weekday date.

## Repeatable packaged demonstration

After building, run from PowerShell:

```powershell
./scripts/demo.ps1
```

The script selects the next weekday in Vilnius, launches the packaged JAR, and verifies four bookings, overlap rejection, adjacency, failed buffered rescheduling, successful rescheduling, cancellation/reuse, both modes, add-ons, completion, history, explicit exit and EOF (including mid-booking). Transcripts are generated under target/demo-workflow.txt, target/demo-eof.txt and target/demo-partial-eof.txt. Rebuilding removes target; rerun the script to regenerate them.

Final S6.1 verification: **267 tests, 0 failures/errors/skips; Maven BUILD SUCCESS, exit 0.** All original S6 tests (264) remain; three architecture checks were added. The packaged workflow and both EOF runs exited 0. All 235 existing domain/integration assertions remain unchanged (package imports migrated); the existing process smoke test now closes stdin and checks the interactive application's welcome/menu exit behavior. ConsoleApplicationTest adds 26 deterministic invocations using Main.create with injected streams and a fixed Clock.

## Design and limitations

Pattern packages are under `src/main/java/com/appointease/`:

| Pattern | Implementation location |
| --- | --- |
| Factory Method, Builder | `domain/creation/` (products and validated specification in `domain/appointment/`) |
| Abstract Factory | `domain/mode/` |
| Decorator | `domain/addon/` |
| State | `domain/state/`, with the immutable appointment as context |
| Strategy | `domain/scheduling/` |
| Facade | `application/BookingFacade.java` |
| Observer | `application/event/`, with listeners in `infrastructure/notification/` |
| Adapter | `infrastructure/calendar/`, implementing `application/port/CalendarPort.java` |

Main is the sole composition root; ConsoleApplication calls BookingFacade, never the repository. Existing State, Strategy, Factory Method, Abstract Factory, Builder, Decorator, Adapter, Observer and Facade implementations are preserved. Simulated notifications are event records, not scheduled messages; the calendar is an offline mirror. Persistence, retries and event logs are process-local. No authentication, payments, database or real integrations.

UI limits: exact-slot availability queries (no automatic slot search), no editing a partially entered form, wide tables may wrap on narrow terminals, and English prompts only. No known failing tested workflow. All six UML sources pass renderer validation; SVG and PNG outputs are included. ArchitectureTest checks compiled layer dependencies, every production package for cycles, declared imports, the cycle detector and mutable static fields. All four original package cycles are resolved; see the audit for class relocations and reproducible analysis.

- [Requirements](docs/requirements.md), [architecture](docs/architecture.md), [pattern matrix](docs/design-pattern-matrix.md)
- [Implementation plan](docs/implementation-plan.md), [testing strategy](docs/testing-strategy.md), [rubric evidence](docs/rubric-evidence.md)
- [UML overview](docs/uml/overview.puml), [console sequence](docs/uml/console-sequence.puml)
- [Project memory](PROJECT_MEMORY.md), [session history](docs/SESSION_HISTORY.md)

Pinned tooling: Jupiter 5.13.4; Maven clean 3.4.1, resources 3.3.1, compiler 3.14.1, Surefire 3.5.4, jar 3.4.2. No dependencies or plugin versions changed in S5.

## Submission materials

- [Independent S6 audit](docs/S6-AUDIT.md), [100-point rubric evidence](docs/rubric-evidence.md), [Moodle evaluation draft and checklist](docs/evaluation-criteria-draft.md).
- [Academic demonstration walkthrough](docs/demonstration-walkthrough.md); run `./scripts/verify-console.ps1` after building for standalone process checks.
- [Rendered overview](docs/uml/overview.svg) and [booking sequence](docs/uml/booking-sequence.svg); all six diagrams include PUML, SVG and PNG. Use SVG zoom for the dense overview.
- Run `./scripts/package-submission.ps1` to create `AppointEase_DesignPatterns_Submission.zip` in the project root. It includes source, tests, POM, documentation, diagrams and scripts, verifies every entry against source bytes, and excludes build outputs, Git/IDE files and local audit tooling.

Extract the ZIP into an empty directory, then run `mvn clean verify`, `./scripts/demo.ps1` and `./scripts/verify-console.ps1` there. Build dependencies may need downloading on another machine; runtime remains offline. S6.1 does not upload to Moodle or make commits.
