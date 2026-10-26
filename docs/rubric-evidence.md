# Final S6.1 rubric traceability

Audited October 26-27, 2026. Weights below are supplied by the user and total 100. Evidence supports assessment; no grade is guaranteed. All source paths are under `src/main/java/com/appointease/`, and tests under `src/test/java/com/appointease/`. [S6-AUDIT.md](S6-AUDIT.md) supplies per-pattern roles, integration, test methods and qualifications; [evaluation-criteria-draft.md](evaluation-criteria-draft.md) is the completed Moodle evidence table.

| Criterion | Weight | Concrete implementation and explanation | Automated evidence |
| --- | ---: | --- | --- |
| Creational | 15 | `domain/creation/AppointmentCreator.java` overrides create concrete ConsultationAppointment/ServiceAppointment; `domain/mode/AppointmentModeFactory.java` creates compatible meeting/instruction pairs; `domain/creation/AppointmentBuilder.java` validates a complete spec before construction. | `domain/CreationPatternsTest.java` (23): override invocation, both kind/mode combinations, mismatched creator/families, missing fields and duration/base-price guards. |
| Structural | 15 | `infrastructure/calendar/ExternalCalendarAdapter.java` translates typed events to legacy primitives/errors; `domain/addon/AppointmentServiceDecorator.java` and concrete wrappers recursively augment quotes; `application/BookingFacade.java` coordinates subsystems. | `domain/DecoratorTest.java` (14); `domain/CalendarObserverTest.java` (14); `domain/BookingIntegrationTest.java` (25), including real facade workflow and mapped post-commit errors. |
| Behavioral | 15 | `domain/scheduling/AvailabilityStrategy.java` varies preparation policy without bypassing mandatory rules; `application/event/AppointmentEventPublisher.java` notifies AppointmentListener subscribers; `domain/state/AppointmentState.java` polymorphically governs lifecycle. | `domain/SchedulingTest.java` (41), `domain/AppointmentStateTest.java` (26), observer/integration tests: policy substitution, state/operation matrix, isolated listener failure and ordering. |
| Integrated application | 25 | `Main.java` wires one repository/dispatcher/calendar/observer graph. `presentation/ConsoleApplication.java` routes all 11 operations through facade. `infrastructure/seed/DemoSeed.java` replays valid historical booking/confirmation. F01-F11 are reachable. | `ConsoleApplicationTest.java` (26), `MainSmokeTest.java` (1), BookingIntegrationTest.allNinePatternsIntegratedThroughCompleteLifecycle; packaged scripts exercise booking through completion/history. |
| Code quality | 5 | Immutable `domain/appointment/Appointment.java`, validated spec, typed IDs/Money, shared AppointmentRules/ReservationRules; constructor injection and no mutable globals. | `domain/DomainModelTest.java` (61), `domain/AppointmentTest.java` (13), `ArchitectureTest.java` static-field check. Broad repository port and compact formatting are disclosed tradeoffs. |
| Architecture/package structure | 5 | Application ports in `application/port/`; concrete implementations in infrastructure; domain independent; Main sole composition root. | ArchitectureTest compiled dependency check uses JDK jdeps. Layer directions and all 19 production packages pass the S6.1 acyclicity check; no package exclusions. |
| Testability/business logic | 10 | Injected Clock, explicit zone/DST rules, half-open occupancy, atomic commit/reschedule and immutable ordered event log in `infrastructure/memory/InMemoryAppointmentRepository.java`. EUR uses BigDecimal scale 2 HALF_UP. | `domain/TimeAndMoneyTest.java` (18); scheduling/integration controlled races, safe failure tests and fixed-clock console tests. Full final suite: 267, zero failures/errors/skips. |
| Documentation/justification | 10 | README; requirements, architecture, pattern matrix/audit; six UML views with rendered outputs; walkthrough and evaluation draft; scripts. | PlantUML validation/render exit 0 after fixing lifecycle syntax; source/diagram review and fresh-package verification recorded below. |

Requirements coverage: F01-F02 browse/advisory availability and F11 input/EOF -> ConsoleApplicationTest; F03-F04 creation/options -> CreationPatternsTest/DecoratorTest; F05-F08 atomic lifecycle/history -> SchedulingTest/BookingIntegrationTest; F09-F10 events/adapter -> CalendarObserverTest/BookingIntegrationTest. B01-B10 map to the value/creation/scheduling/state/money tests above; B11 to TimeAndMoneyTest and adapter DST test. N01/N03-N08 have build, race, clock, failure, pattern, document and offline-runtime evidence. S6.1 resolves the original N02 package-cycle qualification.

## Historical S6 command evidence (S6.1 results below)

| Check | Actual result |
| --- | --- |
| java -version | Exit 0; Temurin OpenJDK 21.0.12.1+1-LTS |
| mvn -version | Exit 0; Maven 3.9.16 on Java 21 |
| Baseline mvn clean verify | Exit 0, October 26 23:55:02 +05:00; 262 tests, zero failures/errors/skips |
| Final source mvn clean verify | Exit 0, October 27 00:05:37 +05:00; 264 tests, zero failures/errors/skips; 76 production and 13 test source files |
| scripts/demo.ps1 | Workflow, empty EOF and partial EOF child processes all exit 0; four bookings plus rejection/adjacency/buffer/reschedule/cancellation/reuse/completion/history assertions pass |
| scripts/verify-console.ps1 | Script exit 0; smoke, invalid input recovery, empty EOF, mid-booking EOF, UTC and Asia/Karachi completion children all exit 0 |
| git diff --check | Exit 0; repository files are untracked, so a separate whole-submission whitespace scan is also required and recorded in history |
| PlantUML initial validation/render | Exit 1 check / 200 render; lifecycle sequence invalid note syntax, then fixed |
| PlantUML final validation/SVG/PNG | Exit 0 for all six; visually reviewed; layout corrected to prevent overview PNG clipping |

No production Java or dependency changes in S6. ArchitectureTest adds two invocations to the retained 262. Local target reports/transcripts are reproducible and excluded from the ZIP. Packaging and final handoff evidence appears in SESSION_HISTORY.md and PROJECT_MEMORY.md.

Fresh-extraction verification: `.submission-check/first`, October 27 at 00:13:28 +05:00, `mvn clean verify` exit 0, 264 tests, zero failures/errors/skips. Extracted demo and standalone console QA scripts both exit 0; all nine child processes exit 0. The 125-entry ZIP is rebuilt after documentation-only handoff updates and every archive entry is compared byte-for-byte by SHA-256 against the workspace source. Production/test Java, POM and scripts are unchanged from this verified extraction. No claim of a fresh Maven cache or another operating system is made.

## Historical S0-S5 records (superseded, retained as evidence)

Historical status at the end of S5: S0-S5 complete (9/9 patterns, interactive console and demo). The historical tables below retain prior-slice evidence; the S4 section supersedes their planned S4 entries. At that historical stage no marking weights had been supplied; this maps the stated academic objectives without inventing a scoring rubric. Historical S0 verification is preserved below.

| Objective / requirements | Current evidence | Planned evidence |
| --- | --- | --- |
| Java 21 / Maven / Jupiter (N01) | pom.xml, Main.java, MainSmokeTest.java | Per-slice clean build records |
| Nine integrated GoF patterns (N06) | State implemented in domain/state and Appointment; AppointmentStateTest provides actual behavior evidence. S2 creation/mode/addon packages implement four more patterns, verified by CreationPatternsTest and DecoratorTest. S3 Strategy and atomic storage are verified by SchedulingTest. | S4 facade/observer/adapter tests and source links |
| Domain model and SOLID (B01–B10, N02) | domain/model values/aggregate, domain/time/DomainTime, domain/exception errors; DomainModelTest, AppointmentTest, TimeAndMoneyTest; reviewed inward-only imports | S6 automated import check |
| Business functionality (F01–F11) | S1/S2 creation/lifecycle and S3 atomic F05-F08 foundation; SchedulingTest | S4–S5 implementation, integrated tests and console transcript |
| Atomicity/time correctness (B03–B11, N03–N04) | S1 actual interval/DST/money/time-boundary tests and immutable transition results; S3 repository lock/atomic reservation and SchedulingTest controlled races/original-reservation preservation | S4 event ordering |
| UML/design rationale (N07) | overview.puml shows actual S1-S3 model; scheduling-sequence.puml shows S3 atomic commands; creation-sequence.puml shows S2 assembly; lifecycle-sequence.puml actual State behavior; booking-sequence.puml explicitly future; alternatives in pattern matrix | Rendered diagrams and final source reconciliation in S6 |
| Notifications/audit/calendar (F09–F10, N05) | Ordered delivery/failure/translation specification | S4 success-only events, failing observer/calendar integration evidence |
| Maintainability and limits (N08) | AGENTS.md, README, pinned tools; no runtime dependencies | Final review and offline console run |

## S1 verification and actual evidence (October 26, 2026)

| Executed check | Actual result |
| --- | --- |
| `java -version` / `mvn -version` | Exit 0; Temurin JDK 21.0.12.1; Maven 3.9.16 on that JDK |
| First `mvn clean verify` | Exit 0, BUILD SUCCESS; 118 tests before the final business-zone guard |
| Final `mvn clean verify` | Exit 0, BUILD SUCCESS; release 21; 119 tests, 0 failures, 0 errors, 0 skipped |
| `java -jar target/appointease-0.1.0-SNAPSHOT.jar` | Actual exit 0; `Welcome to AppointEase!` |
| PowerShell source import review | All production domain imports target java.* or com.appointease.domain.*; no application/infrastructure/presentation imports |
| Production domain source review | No switch statements, direct Instant.now/LocalDateTime.now, Clock.system* or systemDefault calls |

| Test class (under src/test/java/com/appointease/) | Actual passed invocations | Evidence |
| --- | --- | --- |
| domain/DomainModelTest.java | 61 | Valid values, invalid/null/blank data, duration, money negativity, deep immutable collections, normalized add-on duplicate rejection |
| domain/TimeAndMoneyTest.java | 18 | Canonical EUR precision/addition, half-open symmetric overlaps/adjacency, DST gap/overlap and elapsed durations, configurable zone, invalid working boundary |
| domain/AppointmentTest.java | 13 | Required aggregate data, derived end, future start, provider compatibility, working-hour limits/breaks; explicitly no global collision enforcement |
| domain/AppointmentStateTest.java | 26 | All 16 state/operation pairs, valid/invalid transitions, terminal rejection, original/new-start and completion time boundaries, complete unchanged snapshots, zone preservation |
| MainSmokeTest.java | 1 | Separate JVM welcome output and exit status |

Parameterized invocations count as separate tests in the Surefire total. All business clocks are fixed; no tests sleep or call a real service. Detailed generated reports are in target/surefire-reports/ and intentionally Git-ignored.

Created production files: domain/model/{Appointment,AppointmentId,AppointmentKind,AppointmentMode,AppointmentStatus,BookingOptions,Customer,CustomerId,Money,ProviderCategory,ProviderId,Service,ServiceId,ServiceProvider,TimeSlot,Validation,WorkingPeriod}.java; domain/state/{AppointmentState,PendingState,ConfirmedState,CompletedState,CancelledState}.java; domain/time/DomainTime.java; domain/exception/{DomainValidationException,InvalidTransitionException}.java. Test additions are the four domain test classes above and DomainFixtures.java.

Updated AGENTS.md, README.md, requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, this evidence file, overview.puml and booking-sequence.puml; added lifecycle-sequence.puml. Maven/dependency versions, Main and existing smoke test are unchanged. No commits were made. Existing baseline files were already untracked when S1 began and remain untracked.

S1 completion status: **1/9 GoF patterns implemented (State)**. No repositories, atomic booking/rescheduling, events/observers, calendars, notifications, feature menus or other GoF patterns were implemented. The application still only prints its welcome message. AppointmentEvent was intentionally deferred to S4; no unused event shell was created.

Assumptions retained: confirmation/cancellation before start; completion at/after end; service duration is elapsed time; local-time gaps reject and overlaps choose the earlier instant; same-day non-overlapping provider working periods; one business zone per appointment; EUR scale 2 HALF_UP. Appointment construction validates intrinsic data/working hours but does not establish global availability or catalog existence. S2 must replace the direct validated construction seam with the planned abstract product hierarchy/creator/spec pipeline while preserving immutable State behavior.

No S1 build blocker remains. PlantUML is still source-reviewed only, with no renderer available. Git continues to warn about the inaccessible user-level ignore file. S2 is not authorized and must not start automatically.

## Historical S0 verification record

Initial inspection on October 26, 2026: repository contained only .git, no tracked files or project conflicts. Git emitted a warning that the user-level ignore file could not be read; no project data was overwritten. Java reported Temurin OpenJDK 21.0.12.1+1-LTS; Maven reported 3.9.16 using that JDK.

| Executed check | Actual result |
| --- | --- |
| `java -version` | Exit 0, Temurin OpenJDK 21.0.12.1+1-LTS |
| `mvn -version` | Exit 0, Maven 3.9.16 on Java 21 |
| `mvn clean verify` (initial sandbox attempt) | Exit 1: Maven Central download denied (`Permission denied: getsockopt`), before compilation |
| `mvn clean verify` (approved network retry) | Exit 0, BUILD SUCCESS; compiler release 21; runnable jar produced |
| Surefire report `target/surefire-reports/com.appointease.MainSmokeTest.txt` | Jupiter: 1 test, 0 failures, 0 errors, 0 skipped |
| `java -jar target/appointease-0.1.0-SNAPSHOT.jar` | Actual exit 0; printed `Welcome to AppointEase!` |
| `git diff --check` | Exit 0; new files remain untracked, so this is not full new-file whitespace validation |
| `Get-Command plantuml -ErrorAction SilentlyContinue` | No renderer found; diagrams reviewed as source, not rendered/renderer-validated |

Git inspection also tried `git -c core.excludesFile=NUL status --short`, which failed because Git could not use NUL as an exclude file. Ordinary `git status --short` succeeded with the existing user-ignore warning. No Git configuration was changed and no commit was created.

## S0 acceptance assessment

1. Clean Maven verification succeeded in the actual environment after approved dependency downloads.
2. JUnit discovered and passed the process smoke test.
3. Runnable jar welcome and actual exit 0 were verified.
4. Java release 21 is configured and compiler output confirmed it.
5. All nine planned patterns have distinct responsibilities, alternatives and tests in the matrix.
6. Preliminary class and sequence source diagrams match the documented layers/workflow; rendering remains unverified.
7. Domain dependency rules are documented; no domain implementation or outward dependency exists in S0.
8. Requirements and architecture define booking rules, atomicity, transitions and DST assumptions.
9. No booking logic, pattern classes, repositories or feature menus were implemented.
10. S1 boundaries, APIs, ownership and future acceptance gates are documented; policy assumptions below are explicit.

Before S1, review the project policies: confirmation/cancellation only before start, completion at/after end, EUR pricing, earlier-instant overlap resolution, a single business zone, and nondurable event delivery. None is an environmental blocker. Do not expand S1 beyond domain values/lifecycle/time rules without authorization.

All planned evidence remains unimplemented until an authorized slice supplies actual classes/tests. Diagrams describe the intended system, not the current welcome-only program.

## S2 verification and actual evidence (October 26, 2026)

This entry supersedes historical S1 statements that S2 was not authorized. S2 was explicitly requested, implemented and verified. **5/9 patterns complete:** Factory Method, Abstract Factory, Builder, Decorator and State.

| Evidence | Actual result |
| --- | --- |
| Java / Maven versions | Both exit 0; Temurin 21.0.12.1+1-LTS / Maven 3.9.16 |
| Initial migration mvn clean verify | Exit 0, 119 original tests pass |
| Final mvn clean verify (17:42:36 +05:00) | Exit 0, BUILD SUCCESS, 156 tests, 0 failures/errors/skips |
| Packaged welcome jar | Exit 0, exact welcome message |
| CreationPatternsTest | 23 passing invocations; kinds/families, real override dispatch, builder errors, incompatible data, subtype/lifecycle preservation |
| DecoratorTest | 14 passing invocations; combinations/orders, delegation, precision, duplicates, snapshots, required inputs, unchanged intrinsic rules |
| Original suite | DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, MainSmokeTest 1; all retained |
| Domain source review | Domain/JDK imports only; no production switches/system-clock calls; no outer implementations introduced |
| UML | Actual overview/lifecycle/creation source reconciled; integrated booking diagram remains future; rendering unverified |

Source evidence: domain/creation/{AppointmentCreator,ConsultationAppointmentCreator,ServiceAppointmentCreator,AppointmentBuilder}; domain/mode/{AppointmentModeFactory,OnlineModeFactory,InPersonModeFactory,MeetingDetails,ParticipationInstructions,AppointmentModeDetails}; domain/addon/{AppointmentService,BaseAppointmentService,AppointmentServiceDecorator,PriorityBookingDecorator,ReminderServiceDecorator,BookingEnhancements}; domain/model/{ValidatedAppointmentSpec,AppointmentRules,Appointment,ConsultationAppointment,ServiceAppointment}. All names refer to Java files.

F03/F04 domain construction/composition now has executable evidence; catalog resolution, persistence, user-facing booking and display remain future. Public spec construction validates fully; creator and subtype enforce kind. S1 assertions were preserved using migrated construction fixtures. Main and POM unchanged. Full file inventory, design refinements and verification chronology are in SESSION_HISTORY.md.

No build blockers. Source review is not a permanent automated architecture test. PlantUML rendering remains deferred. Git files were already untracked, so diff --check alone does not validate the changed inventory. No commits made. S3 requires explicit authorization.

## S3 verification and actual evidence (October 26, 2026)

S3 was explicitly authorized and completed. Six patterns implemented; Adapter, Facade and Observer remain planned S4.

| Evidence | Actual result |
| --- | --- |
| java -version | Exit 0; Temurin OpenJDK 21.0.12.1+1-LTS |
| mvn -version | Exit 0; Maven 3.9.16, Java 21 |
| Initial mvn clean verify (19:43:03 +05:00) | Exit 0, BUILD SUCCESS, 193 tests, 0 failures/errors/skips |
| Final mvn clean verify (19:49:42 +05:00) | Exit 0, BUILD SUCCESS, 197 tests, 0 failures/errors/skips |
| java -jar target/appointease-0.1.0-SNAPSHOT.jar | Exit 0; printed Welcome to AppointEase! |
| SchedulingTest | 41 invocations including six coordinated race scenarios; fixed inputs/clocks, explicitly advanced test clock for lifecycle boundaries; no sleep |
| Retained suite | 156 tests: DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, CreationPatternsTest 23, DecoratorTest 14, MainSmokeTest 1 |
| Source/transaction review | Domain depends on JDK/domain only; application has no infrastructure imports; all storage reads/writes use instance lock; no public save or mutable indexes; no S4 classes |
| UML | Updated class/lifecycle/future booking diagrams; new implemented scheduling sequence; source reviewed, rendering unverified |

Executable Strategy evidence: domain/scheduling/AvailabilityStrategy, StandardAvailabilityStrategy and PreparationBufferAvailabilityStrategy. ReservationRules enforces intrinsic checks and conflicts before applying polymorphic buffer output, then validates effective occupancy and hours. Reservation stores immutable policy snapshots. Application contracts are in application/port; infrastructure/memory/InMemoryAppointmentRepository implements atomic reserve/reschedule/transition and immutable sorted lookup/history.

SchedulingTest covers both strategies, configurable/retained buffers, working-hour boundaries, adjacency/partial/identical/contains/contained overlaps, aged candidates, one clock capture per transaction, invalid policies, reentrant write rejection, catalog integrity, invalid service/duration/zone, duplicate/missing IDs, cancelled reuse, confirmed/completed occupancy, immutable ordered history, failed reserve/reschedule/transition preservation, and both subtype/mode/add-on/State integrations. Races cover conflicting reservation, separate providers, duplicate IDs, booking versus rescheduling, two reschedules, and confirmation versus rescheduling. Executors always shut down; winner order is not assumed.

Limitations: process-local storage and immutable catalogs, single lock with linear conflict scans, no historical import or event log. Custom strategies execute under the lock and must not block waiting for repository commands. Completed occupancy is checked with an earlier fixed validation time in a domain test to isolate it from the separate future-time rule; normal runtime cannot create past bookings. No scalability/durability claim. Permanent automated architecture checks and UML rendering remain deferred. Git user-ignore warning persists; files were already untracked, no commits made.

## S4 verification and actual evidence (October 26, 2026)

S4 explicitly authorized; all nine patterns implemented. Main/POM and all 197 existing test invocations retained unchanged. No S5 or commits.

| Check | Actual result |
| --- | --- |
| java -version | Exit 0; Temurin 21.0.12.1+1-LTS |
| mvn -version | Exit 0; Maven 3.9.16, Java 21 |
| Initial mvn clean verify after production integration | Exit 0 at 20:25:38 +05:00; 197 tests passed |
| Expanded-suite intermediate mvn clean verify | Exit 1 at 20:28:20; 233 tests, 1 failure, 0 errors/skips; duplicate-ID test incorrectly reused a command ID with new collaborators; corrected test to use a fresh command ID |
| Final mvn clean verify | Exit 0 at 20:29:46 +05:00; BUILD SUCCESS; 236 tests, 0 failures/errors/skips |
| java -jar target/appointease-0.1.0-SNAPSHOT.jar | Exit 0; exact Welcome to AppointEase! |
| BookingIntegrationTest / CalendarObserverTest | 25 / 14 passing invocations; 39 new tests |
| Source dependency scan | 0 forbidden domain/application imports; no network or Thread.sleep added |
| Git / UML | git diff --check exit 0 (untracked files require separate scan); diagrams updated/source-reviewed, rendering not verified |

F01-F08 application access is demonstrated through BookingFacade and AppointmentAssembler; deterministic seeded catalog/UI remains S5. F09 is implemented by domain/event/AppointmentEvent, the atomic repository commit helper and application/event publisher/dispatcher. F10 is implemented by application CalendarPort and infrastructure/calendar/ExternalCalendarAdapter plus incompatible simulated client. CalendarObserverTest verifies real format translation and retained error causes. BookingIntegrationTest.allNinePatternsIntegratedThroughCompleteLifecycle demonstrates all nine patterns, completion and preservation of identity/family/add-ons/fees. Other tests demonstrate both service mode families, cancellation, warning-only conflicts, retries, rejection integrity and five controlled facade concurrency scenarios.

Source review confirms latest-state transitions/reschedules and replacement/revision/event recording share the store lock. Listener/calendar callbacks run after unlock; a cross-thread observer test checks lock release. Shared dispatcher serializes facade commands and global delivery; rejected commands record no event. Per-command retry receipts, publisher event IDs and fake-calendar event keys suppress duplicates within their current instances. No distributed exactly-once, durability, fatal-error recovery, automatic retry or throughput claim. Logs and receipt sets grow in memory. Single dispatcher per repository is a required composition rule, not a global singleton.

## S5 verification and actual evidence (October 26, 2026)

S5 explicitly authorized; all nine existing patterns preserved, no domain/application changes. Main is now the real composition root; presentation/{ConsoleInput,ConsoleApplication} and infrastructure/seed/{DemoCatalog,DemoSeed} supply the console and demo. ConsoleApplicationTest exercises the real composition. F01-F11 are reachable through menus 1-11; 0 and EOF exit. No runtime dependencies added.

| Check | Actual result |
| --- | --- |
| java -version / mvn -version | Exit 0; Temurin 21.0.12.1+1-LTS / Maven 3.9.16 |
| Initial sandbox baseline mvn clean verify | Exit 1 before compilation: dependency access denied; not a passing check |
| Approved baseline retry | Exit 0 at 23:26:39 +05:00; all 236 S4 tests pass |
| Expanded console suite | Exit 0 at 23:30:05; 262 tests, zero failures/errors/skips |
| Final mvn clean verify | Exit 0 at 23:31:46; BUILD SUCCESS; 262 tests, zero failures/errors/skips; XML totals independently confirmed |
| scripts/demo.ps1 | Initial timezone-name lookup failure before Java; fixed Windows fallback; rerun exit 0 |
| Packaged JAR workflow | Exit 0; four interactive bookings, overlap rejection, adjacency, failed buffered reschedule preserving original, successful buffered reschedule, cancellation/reuse, both modes/types, EUR fees, completion and observer/history output |
| Packaged JAR EOF / partial EOF | Both exit 0, goodbye output; unfinished booking discarded |
| Dependency source review | No domain imports of outer layers or application imports of infrastructure/presentation; rg no-match exit 1 indicates no findings |

Retained counts: DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, CreationPatternsTest 23, DecoratorTest 14, SchedulingTest 41, BookingIntegrationTest 25, CalendarObserverTest 14. MainSmokeTest remains 1 invocation with stdin closed and interactive EOF assertions. ConsoleApplicationTest adds 26. Total 262; 76 production / 12 test source files.

Actual scripted run used next weekday 2026-10-27: online health 10:00 with both add-ons (EUR 52.00); same-slot rejection; adjacent 10:30 success; buffered reschedule of adjacent booking rejected; first appointment confirmed, moved to 11:30 with buffer, cancelled, and its slot rebooked; in-person salon booking (EUR 20.00); seed-ready-001 completed. Notification and audit histories include all successful events and no rejected command events. Full generated transcripts reside in target/demo-*.txt; scripts/demo.ps1 regenerates them using the next weekday.

No known failing tested workflows. Limitations: process-local state/unbounded logs, exact-slot queries only, restart operation after invalid input, narrow-terminal wrapping, simulated event notifications rather than scheduled reminder delivery. UML sources updated but not rendered. No commits, external services, payments, authentication or S6 work. S6 QA/submission requires explicit authorization.


## S6.1 current verification

Original 264 tests retained; three architecture tests added, for 267 tests with zero failures/errors/skips. Package graph: 19 packages, zero cycles. Domain/application/infrastructure/presentation directions and composition-root boundary pass. Original four package cycles and exact migration decisions are in S6-AUDIT.md. Source/extraction builds, packaged demo/EOF/console QA, UML rendering and archive evidence are recorded in the S6.1 SESSION_HISTORY.md entry; prior S6 results above remain historical evidence.


S6.1 external candidate extraction: <external-temp>/AppointEase-S6.1/candidate; clean verify October 27 at 01:05:21 +05:00, exit 0, 267 tests, zero failures/errors/skips. Extracted demo/console QA exit 0, nine Java child exits 0. Final handoff changes are repackaged and the final archive repeats the same checks; final identity and outcomes are emitted externally without rewriting archived documents. See SESSION_HISTORY.md.
