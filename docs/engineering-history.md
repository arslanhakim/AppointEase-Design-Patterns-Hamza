# AppointEase engineering history

Historical implementation decisions, verification results and known limitations. S0/S1 records were reconstructed from source and rubric evidence; later entries record the checks performed at each milestone. Historical filenames, package contents and release states describe their date, not the current tree. Historical review boundaries are archival records, not current development instructions. Current status: [project-status.md](project-status.md).

## 2026-10-28 — S8A-S8C final feature-branch publication checks

**Scope and authorization:** The user approved S8A-S8C functional/visual review and authorized one commit and a normal push to `origin/feature/javafx-ui`. Reviewed every pending tracked diff and untracked source/report before staging. Initial local HEAD and GitHub feature branch both were `3b8b63779fd2db0d619a766e777af8b473a15a17`. GitHub main was `b8417e91960c3e632c16b2a3e1b14763445c5382`. Earlier no-commit handoffs below are historical and superseded by this publication authorization.

**Release inventory:** 41 intended files. Java filenames below are relative to `src/main/java/com/appointease/`, except the test row.

| Area | Included files |
| --- | --- |
| Application | `application/BookingFacade.java`, `application/port/AppointmentRepository.java` |
| Domain | `domain/model/Service.java`, `domain/model/ServiceProvider.java`, `domain/scheduling/ReservationRules.java` |
| Infrastructure | `infrastructure/memory/InMemoryAppointmentRepository.java`, `infrastructure/seed/DemoCatalog.java` |
| Desktop | `presentation/desktop/ActivityView.java`, `AppointmentDetailsView.java`, `AppointmentsView.java`, `BookingView.java`, `CalendarKeyboard.java`, `CalendarModel.java`, `CatalogForms.java`, `CustomersView.java`, `DesktopController.java`, `DesktopShell.java`, `NavigationIcon.java`, `OverviewModel.java`, `ProvidersView.java`, `ScheduleView.java`, `ServicesView.java` |
| CSS | `src/main/resources/com/appointease/presentation/desktop/theme.css` |
| Tests | `src/test/java/com/appointease/domain/CatalogManagementTest.java`; `src/test/java/com/appointease/presentation/desktop/CalendarKeyboardTest.java`, `CalendarModelTest.java`, `DesktopS7BCheck.java`, `DesktopS8ACheck.java`, `DesktopS8BCheck.java`, `DesktopS8CCheck.java` |
| Scripts | `scripts/run-desktop.ps1`, `scripts/capture-ui.ps1` |
| Documentation | `README.md`, `docs/architecture.md`, `docs/design-pattern-matrix.md`, `docs/engineering-history.md`, `docs/project-status.md`, `docs/testing-strategy.md`, `docs/ui-review/S8A.md`, `S8B.md`, `S8C.md` |

**Excluded:** All `target/` outputs, `.audit-tools/` logs/helpers, `.submission-check/`, `docs/ui-review/screenshots/` and the original academic ZIP remain local and ignored. No temporary files, screenshots, generated binaries, credentials or AI-specific configuration are included. Reviewed changed text contains no credential-scan matches. Existing `.gitignore`, POM, console sources and original JUnit tests are unchanged. All nine GoF roles remain; Factory Method/Builder, Abstract Factory, State, Decorator, Observer and Adapter implementation packages have no delta. Facade and Strategy retain their existing roles while supporting catalog management and current provider configuration.

**Fresh checks on the final Java/test sources:**

| Command/check | Result |
| --- | --- |
| `mvn clean verify` | BUILD SUCCESS, exit 0 at **00:58:04 +05:00**; **351 tests, 0 failures/errors/skips**; independent sum of 17 Surefire XML suites agrees; all five architecture checks pass |
| `run-desktop.ps1 -VerifyS8A` | **40 assertions**, exit 0 |
| `run-desktop.ps1 -VerifyS8B` | **80 assertions**, exit 0 |
| `run-desktop.ps1 -VerifyS8C` | **70 assertions**, exit 0; 505 appointments / 2,024 Activity records |
| `run-desktop.ps1 -VerifyS7C` | **85 assertions**, exit 0 |
| `run-desktop.ps1 -VerifySidebar` | **106 assertions**, exit 0; motion enabled |
| `run-desktop.ps1 -VerifySidebar -ReduceMotion` | **106 assertions**, exit 0; reduced motion |
| `run-desktop.ps1 -Verify` | **13 assertions**, exit 0 |
| `scripts/demo.ps1` and `scripts/verify-console.ps1` | Both exit 0; all **nine packaged-JAR child processes** pass |
| `git diff --check` | Exit 0 |

Windows checks ran serially against the packaged JAR, without recapturing already approved screenshots. The first S8A run exposed an ambiguous fixture search: `Morgan` matches both newly created Morgan Rivera and seeded Alex Morgan, with random-ID ordering. Both history selections now search the created customer's exact ID; all original 40 assertions remain and the corrected run passes. Production code was not changed during publication. The initial Maven run passed 351 tests; rebuilding after this harness correction first hit sandbox dependency access denial, then passed with approved cache/network access. No failed attempt is counted as a pass.

**Measured limits:** S8C's 100-appointment Week refresh/CSS/layout measured median **144.43 ms**, maximum **223.88 ms** in this run. Operations and histories remain synchronous and process-local; dense Week rendering can briefly block input. Native Windows automation remains focus/timing-sensitive. No persistence, production-scale guarantee, non-Windows rendering verification or full assistive-technology certification is added.

**Publication boundary and protected artifacts:** Commit message: `Add catalog management and multi-view appointment scheduling`. Only the feature branch is authorized for a normal push; no merge, force-push, main update or academic repackaging. The ZIP remains **1,372,171 bytes**, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. This entry records completed pre-commit verification; the resulting commit SHA, local/tracking/GitHub equality and final protected-artifact comparison are reported after publication.

## 2026-10-28 — S8C: desktop usability and Activity handoff

**Scope:** Implemented the authorized S8C slice on `feature/javafx-ui` after S8B visual approval, preserving the existing uncommitted S8A/S8B work. Schedule retains mode/date/provider/cancelled visibility, Week horizontal position and shell vertical position across destinations and return from details. Re-entry obtains current facade snapshots and provider choices by ID. Details opened from Calendar names its Back to Schedule action and restores originating focus when available. Month adds date-only arrows, Home/End, Page Up/Down, a single Tab entry point and native Enter/Space activation. Compact Week exposes named scrolling controls only when needed.

Activity now uses a native virtualized selectable feed and selected immutable event details, 62/38 desktop columns and a compact stacked arrangement. Event type/time, classification, customer/provider, appointment reference, status and revision remain available. Refresh and resize preserve selection by event ID plus channel; empty/error states clear visual and accessible details. Field minimum heights prevent wrapped scheduled time from being compressed. No new animations, dependencies, backend APIs, business rules, threading or persistence architecture were introduced. The nine GoF implementations and console remain intact.

**Files:** Presentation changes in DesktopShell, ScheduleView, new CalendarKeyboard, AppointmentDetailsView, ActivityView and theme.css. New CalendarKeyboardTest and DesktopS8CCheck; S8C runner/capture switches. The retained S7C harness counts Activity ListView items with the original 20/10 thresholds instead of materialized row nodes. Retained S8B native input now uses separate press/release events and an explicitly foreground stage; all assertions remain. README, architecture, testing strategy, project status and [S8C report](ui-review/S8C.md) describe the current contract and evidence.

**Build:** Final `mvn clean verify` at **00:35:53 +05:00**, exit 0, **351 tests, zero failures/errors/skips**, independently checked against Surefire XML. All **339 S8B invocations** remain; 12 keyboard date cases were added. All five architecture checks pass. Production source count is 96; test source count is 24.

**Windows and console:** S8C passes **70 assertions**, exit 0, and covers state/scroll/focus restoration, confirmation/rescheduling/cancellation refresh, native date traversal and activation, conditional Week controls, Activity selection/filtering/desktop proportions/compact stacking, full detail heights, empty/failure/recovery accessibility and larger histories. Retained S8A **40**, S8B **80**, S7C **85**, sidebar **106** (motion enabled), Overview **13**; console demo and QA have **nine successful packaged-JAR child processes**. Final S8C assertion count, timing table and screenshot inventory are recorded in the linked report. These opt-in Windows assertions are separate from Maven's 351 tests.

**Performance and evidence:** The harness creates 500 additional bookings and confirmations through real facade commands, producing **505 appointments / 2,024 Activity records**, with 100 appointments in a busy week. Queries and refresh/CSS/layout are measured over 12 samples after three warmups on the JavaFX thread; render timing excludes OS composition and screenshot latency. Activity creates fewer than 40 cells for the full history. Queries are inexpensive; Week rendering is the dominant synchronous cost. There is no demonstrated need for background queries at this scale. Eleven real Windows PNGs cover both requested window sizes, selected Activity, compact stacked details, before/after navigation, Week scrolling, Month focus and details return; signature/decode checks and visual inspection are recorded in the S8C report. Captures remain local and ignored.

**Intermediate issues:** Sandboxed Maven dependency access failed before the approved successful retry. Early new-harness key input did not reach the scene despite reported focus; a real control click and separated native press/release resolved input delivery. Two retained S8B runs missed their initial native Week click before the timing/foreground correction; no assertion was weakened. A retained sidebar run and a later S8C capture lost Windows foreground focus; unchanged reruns passed. A retained S7C run missed native Tab traversal; its unchanged direct rerun passed all 85 assertions. Visual review caught compressed Activity service/time text; the final layout and regression cover it. A source review then caught accessible event/error metadata surviving empty/recovered visual states; it was cleared and the regression strengthened. Captures were repeated with the pointer away from tooltip targets.

**Limits and stop:** Navigation state lasts for the process only; scroll values are normalized and constrained by current content/viewport. Compact details and dense calendars scroll. Dense Week rendering can briefly block the UI; larger histories need separate profiling. Full assistive-technology certification, non-Windows graphics and real-window DST transitions were not verified; existing DST tests remain passing. No persistence, recurrence, drag/drop, automatic slot search or unrelated features. S8C stops for final review, uncommitted. Main remains `b8417e91960c3e632c16b2a3e1b14763445c5382`; the original academic ZIP remains 1,372,171 bytes with SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. No commit, push, merge or archive modification.

## 2026-10-26 — S0: architecture and project foundation

**Scope/completion:** Created the Java 21/Maven welcome application and design baseline. The workspace originally contained only Git metadata; no project conflicts were reported. No booking logic or patterns were implemented in S0.

**Files completed:**

- Root: pom.xml, README.md, AGENTS.md, .gitignore.
- Java: src/main/java/com/appointease/Main.java; src/test/java/com/appointease/MainSmokeTest.java.
- Documentation: docs/requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, rubric-evidence.md.
- UML: docs/uml/overview.puml and booking-sequence.puml.

**Significant decisions:** Java compiler release 21; Jupiter 5.13.4; plugins clean 3.4.1, resources 3.3.1, compiler 3.14.1, Surefire 3.5.4, jar 3.4.2. Domain inward dependency rules, one future composition root, constructor injection, injectable Clock, Europe/Vilnius default zone, explicit DST resolution, EUR rounding, half-open intervals. Planned atomic repository check/write, immutable replacement on success, completed occupancy retention, ordered post-commit events and isolated observer/calendar failures. All nine patterns received distinct responsibilities, alternatives and planned tests; S1–S6 acceptance gates were documented.

**Historical verification:** Temurin JDK 21.0.12.1 and Maven 3.9.16. Initial `mvn clean verify` failed before compilation because sandbox networking denied Maven Central access. Approved retry exited 0 with BUILD SUCCESS and 1 passing smoke test, no failures/errors/skips. Runnable jar printed `Welcome to AppointEase!`, actual exit 0.

**Outstanding at close:** All domain and booking functionality remained planned. PlantUML was unavailable, so diagrams were source-reviewed only. Git warned about an inaccessible user-level ignore file. Files remained untracked; no commit was made. S1 required explicit authorization.

## 2026-10-26 — S1: domain foundation and State

**Scope/completion:** S1 was explicitly authorized, implemented, tested and subsequently approved by the user. The console entry point remained welcome-only. Exactly one GoF pattern, State, was implemented.

**Files created:** Paths below are relative to src/main/java/com/appointease/.

- domain/model/: Appointment.java, AppointmentId.java, AppointmentKind.java, AppointmentMode.java, AppointmentStatus.java, BookingOptions.java, Customer.java, CustomerId.java, Money.java, ProviderCategory.java, ProviderId.java, Service.java, ServiceId.java, ServiceProvider.java, TimeSlot.java, Validation.java, WorkingPeriod.java.
- domain/state/: AppointmentState.java, PendingState.java, ConfirmedState.java, CompletedState.java, CancelledState.java.
- domain/time/DomainTime.java; domain/exception/DomainValidationException.java and InvalidTransitionException.java.
- Tests under src/test/java/com/appointease/domain/: DomainFixtures.java, DomainModelTest.java, TimeAndMoneyTest.java, AppointmentTest.java, AppointmentStateTest.java.
- docs/uml/lifecycle-sequence.puml.

**Files updated:** AGENTS.md, README.md; docs/requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, rubric-evidence.md; docs/uml/overview.puml and booking-sequence.puml. POM, Main and original smoke test were unchanged.

**Significant decisions:** Typed nonblank IDs; defensive immutable provider collections; positive service duration and service-derived appointment end; supported service/mode and working-hour checks; half-open overlap calculation; EUR scale 2/HALF_UP with negativity checked before rounding. DomainTime resolves gaps/overlaps explicitly and uses injected Clock. Appointment is a final immutable aggregate with fully validated direct construction pending S2. Lifecycle operations delegate to polymorphic states and return immutable replacements; no production status switch. Terminal states reject every operation. Confirmation/cancellation require future start; completion requires end reached. Rescheduling validates a proposal only, preserves the original, and rejects a changed business zone. Global uniqueness/reservation was deliberately deferred.

**Historical verification:** First clean build passed 118 tests. After adding the business-zone preservation guard/test, final `mvn clean verify` exited 0 with **119 passed, 0 failures, 0 errors, 0 skipped**. Counts: DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, MainSmokeTest 1. All 16 state/operation pairs and exact time boundaries were covered with fixed clocks. Jar welcome run exited 0. Source review found only JDK/domain imports, no production switches and no direct system-time calls.

**Pattern status at close:** State complete; Factory Method, Abstract Factory, Builder, Decorator planned S2; Strategy planned S3; Adapter, Facade, Observer planned S4. The remaining eight patterns were not implemented.

**Outstanding at close:** S2 must migrate direct construction to validated specs/creators/subtypes and paired mode families without weakening S1 invariants. No repository, strategy, atomic reservation/reschedule, event/revision mechanism, notification/calendar service, deterministic catalog or menu exists. AppointmentEvent is deferred to S4. PlantUML rendering and automated architecture checks remain pending. Existing files were untracked; no commits made. S2 was not authorized.

## 2026-10-26 — S1 baseline reverification

**Documentation:** Added historical development records. No Java, test, POM or S2 implementation changes.

**Review:** Read AGENTS.md, Git status, source inventory, Appointment, all State classes, DomainTime, Money, Main, State tests and POM alongside architecture/pattern/slice/evidence documents. Source supports one implemented State pattern and eight planned patterns. Git lists existing project files as untracked and no tracked paths. The fresh build below confirms the historical 119-test claim.

**Fresh verification (completed 17:17:47 +05:00):**

| Command/check | Actual result |
| --- | --- |
| `java -version` | Exit 0; Temurin OpenJDK 21.0.12.1+1-LTS |
| `mvn -version` | Exit 0; Maven 3.9.16 using Java 21 |
| `mvn clean verify` | Exit 0, BUILD SUCCESS; compiler release 21; 119 tests, 0 failures, 0 errors, 0 skipped |
| Surefire reports in target/surefire-reports/ | DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, MainSmokeTest 1; parameterized invocations included |
| `java -jar target/appointease-0.1.0-SNAPSHOT.jar` | Actual exit 0; printed `Welcome to AppointEase!` |

The fresh verification required no code changes and confirmed the S1 baseline. Test reports are generated artifacts and remain ignored by Git.

**Historical next milestone:** S2 construction fixtures and creational patterns were still planned at this point; S1 immutable State behavior was to be preserved.

**Outstanding:** No build/test blocker remains. PlantUML rendering remains unverified; the Git user-ignore warning persists. No commit authorized or made.

## 2026-10-26 - S2: creational patterns and Decorator

**Inspection:** Read AGENTS/memory/latest history, requirements, architecture, matrix, slice plan, testing/evidence/UML; inspected source, tests and Git. Existing project files were all untracked, and user-ignore permission warning remained. No existing files discarded.

**Production files added** (relative to src/main/java/com/appointease/domain/):
- creation/: AppointmentBuilder.java, AppointmentCreator.java, ConsultationAppointmentCreator.java, ServiceAppointmentCreator.java.
- mode/: AppointmentModeFactory.java, OnlineModeFactory.java, InPersonModeFactory.java, MeetingDetails.java, ParticipationInstructions.java, AppointmentModeDetails.java. Concrete immutable family records are nested in the two factories.
- addon/: AppointmentService.java, BaseAppointmentService.java, AppointmentServiceDecorator.java, PriorityBookingDecorator.java, ReminderServiceDecorator.java, BookingEnhancements.java.
- model/: ValidatedAppointmentSpec.java, AppointmentRules.java, ConsultationAppointment.java, ServiceAppointment.java.

**Production files changed:** model/Appointment.java now sealed abstract with subtype-preserving immutable lifecycle copies; model/Validation.java exposes shared validation helpers; model/BookingOptions.java comment updated. Main, POM, State classes, time and monetary behavior unchanged.

**Tests:** Added domain/CreationPatternsTest.java (23 invocations) and domain/DecoratorTest.java (14). Migrated construction calls in DomainFixtures.java, AppointmentTest.java, DomainModelTest.java and TimeAndMoneyTest.java; retained all original assertions and 119 test invocations. AppointmentStateTest behavior unchanged. Coverage includes both kinds x both modes, override dispatch and pre-factory rejection, required fields, incompatible kinds/families/session identities, exact slot duration, quote/base-fee validation, both decorator orders, duplicates, Money precision, invalid fees/delegates, preserved immutable appointment identity/slot/provider/lifecycle/subtype, and unchanged hard constraints.

**Decisions:** Fully validating public ValidatedAppointmentSpec in domain.model refines the planned restricted-constructor API and avoids model-to-creation orchestration dependency; subtype constructors accept validated specs and check kind. Shared AppointmentRules prevents duplicated creation/reschedule rules. Mode family identity belongs to each immutable factory instance; sealed product interfaces prevent mutable implementations. Builder accepts start or explicit slot (last setter wins), delegates quote(Service), validates then calls Factory Method. No separate BookingContext needed. BaseAppointmentService can import an existing fee snapshot without inventing behavior. Priority/reminder defaults EUR 10.00/2.00, with injectable Money; additive fees normalize individually, instructions follow wrapper order. Reminder/priority instructions are retained, not sent or executed as scheduling policy. Point-in-time specs are candidates, not reservations.

**Actual verification:**

| Command/check | Result |
| --- | --- |
| java -version | Exit 0; Temurin OpenJDK 21.0.12.1+1-LTS |
| mvn -version | Exit 0; Maven 3.9.16 using Java 21 |
| First mvn clean verify after construction migration | Exit 0, BUILD SUCCESS; all original 119 passed |
| Final mvn clean verify, completed 17:42:36 +05:00 | Exit 0, BUILD SUCCESS; 156 passed, 0 failures, 0 errors, 0 skipped |
| java -jar target/appointease-0.1.0-SNAPSHOT.jar | Exit 0; Welcome to AppointEase! |
| Domain source/import review | Only JDK/domain imports; no production construction/lifecycle switch, outer-layer dependency or system-time calls |
| Git review | Existing project remains untracked; diff --check exit 0 cannot cover untracked files; no staging/commit |

Surefire totals: AppointmentStateTest 26, AppointmentTest 13, CreationPatternsTest 23, DecoratorTest 14, DomainModelTest 61, TimeAndMoneyTest 18, MainSmokeTest 1. Compiler processed 46 production and 8 test sources. Reports remain ignored under target/surefire-reports/.

**Documentation:** Updated AGENTS.md, PROJECT_MEMORY.md, README.md, requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, rubric-evidence.md, overview.puml, lifecycle-sequence.puml, booking-sequence.puml; added creation-sequence.puml and this history entry. Historical S0/S1 results are retained.

**Status/boundary:** 5/9 implemented: Factory Method, Abstract Factory, Builder, Decorator, State. Strategy planned S3; Adapter/Facade/Observer planned S4. No repository, booking persistence, strategy, calendar, events, notification delivery or menu added.

**Blockers/debt:** No implementation/build failure outstanding. PlantUML rendering remains unverified; source diagrams reviewed. Automated architecture checks remain planned. Spec validation can age after creation; future atomic repository must revalidate current time and hard rules under lock. Instruction strings are snapshots, not a delivery protocol. Sealed family extension requires source changes. Inaccessible Git user-ignore warning persists.

**Proposed next slice (not authorized):** S3 Strategy plus unconditional hard scheduling rules, application repository contracts and thread-safe in-memory atomic commands. Test races, duplicates, adjacency/buffers, cancelled/completed occupancy and failure-preserving reschedule. Leave Facade/Observer/Adapter and event delivery to S4; menu to S5. Stop after S2.

Final handoff review: scanned all 45 domain source files for imports outside JDK/domain and production switches/system-clock calls; none found. Checked source/document trailing whitespace (0 findings) and UML delimiters; this does not constitute renderer validation. No production changes followed the successful 156-test verification.

## 2026-10-26 - S3: Strategy, scheduling and atomic reservations

**Initial inspection:** Read AGENTS.md, memory, latest history, requirements, architecture, pattern matrix, plan, testing/evidence and relevant UML. Inspected actual aggregate/spec/creator/state/time/interval/provider APIs, test fixtures, POM and Git. S2 source and its 156-test claim were confirmed by retained passing tests. All project files were already untracked; preserved them. Git user-ignore permission warning remains. Main/POM and all pre-existing tests were left unchanged.

**New production files** (relative to src/main/java/com/appointease/):
- domain/scheduling/AvailabilityStrategy.java, StandardAvailabilityStrategy.java, PreparationBufferAvailabilityStrategy.java, Reservation.java, ReservationRules.java, SlotUnavailableException.java.
- application/port/AppointmentRepository.java, AppointmentTransition.java, RepositoryException.java.
- infrastructure/memory/InMemoryAppointmentRepository.java.

**Changed production files:** domain/model/Appointment.java, ConsultationAppointment.java, ServiceAppointment.java (detached subtype/State-preserving reschedule replacement); AppointmentRules.java (extract shared working-hour containment without duplicating future checks).

**Tests:** Added src/test/java/com/appointease/domain/SchedulingTest.java, 41 invocations. Retained all 156 existing tests unchanged. New coverage includes both policies/configuration/immutable snapshots, boundary overlaps including containment, hours, stale time, single clock capture, catalogs/defensive copies, invalid service/duration/zone, duplicate/missing IDs, pending-only insertion, terminal history, immutable sorted reads, cancelled buffer release, failed commands, both creator kinds/families/decorators/State integrations and six latch-coordinated races. Race outcomes assert one conflicting winner, independent providers, duplicate ID protection, reschedule rollback, no lost confirm/reschedule update; bounded futures and guaranteed executor cleanup, no sleeps.

**Architecture choices:** One application repository port combines lookup/history/atomic commands without public save. Constructor snapshots customer/provider/service catalogs; reserve requires exact value matches so forged provider hours or service duration cannot bypass catalog authority. IDs remain caller-supplied and atomically unique, including cancelled records. Domain ReservationRules reuses fully validating S2 spec and shared working-hours checks. Strategy returns preparation Duration via polymorphism: standard zero, buffered provider configuration or explicit override. This refines the conceptual schedule-assessing strategy while keeping hard overlap/rule enforcement unconditional. Store immutable buffer snapshots, not strategy objects. Effective occupancy is [start-buffer,end), must fit hours; preparation may begin before now, appointment must be future. Completed blocks, cancelled releases.

One instance ReentrantLock protects all reads and full validation/replacement transactions. Capture Clock once under lock into fixed DomainTime. Build detached replacements and put only after success; no index synchronization issue because only one appointment map is maintained. Reschedule excludes only self and changes policy on successful explicit request; retains ID/subtype/State/family/add-ons/zone and original slot on failure. Finite transition commands delegate to existing State, preserving buffer. Nested writes from custom strategies are rejected to prevent reentrant snapshot invalidation. History is current appointment snapshots in start-then-ID order, not event history. No revision/event/queue/Facade/Observer/calendar implementation (Facade is planned S4).

**Commands/results:** java -version exit 0 (Temurin 21.0.12.1+1-LTS); mvn -version exit 0 (3.9.16). Initial mvn clean verify at 19:43:03 +05:00 exit 0, 193 tests, no failures/errors/skips. Review added four invocations for containment, defensive catalogs and single clock capture; final mvn clean verify at 19:49:42 +05:00 exit 0, BUILD SUCCESS, **197 tests, 0 failures/errors/skips**. Final class totals: SchedulingTest 41, DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, CreationPatternsTest 23, DecoratorTest 14, MainSmokeTest 1. Explicit java -jar target/appointease-0.1.0-SNAPSHOT.jar exit 0, exact welcome output. No Java/test changes after that build.

**Documentation completed:** AGENTS.md, PROJECT_MEMORY.md, README.md, docs/requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, rubric-evidence.md and this appended history. Updated docs/uml/overview.puml, lifecycle-sequence.puml, booking-sequence.puml; added scheduling-sequence.puml. The full booking sequence explicitly remains planned S4 orchestration; scheduling sequence describes implemented S3 behavior. No renderer validation claimed.

**Review/limitations:** Source review found inward domain/application imports; inspected all storage entrypoints and lock scope, no public save/check-write bypass, no mutable global state or static lock, no accidental changes to prior pattern implementations beyond immutable reschedule support. Tests demonstrate concurrency outcomes, not exhaustive interleaving exploration. Winner order unspecified, contents/history deterministically ordered. Whole-store lock and linear scan suit academic scale, not a throughput claim. Storage/catalogs are process-local; no dynamic catalog updates, historical import or durability. Custom strategies must be quick/pure and not wait on another repository command. Completed occupancy test uses an earlier fixed validation time to separate it from the independent future-only rule. PlantUML rendering and automated architecture tests remain deferred. No build blocker.

**Pattern status and next boundary:** 6/9 implemented: Factory Method, Abstract Factory, Builder, Decorator, State, Strategy. Proposed S4 (not authorized): BookingFacade; immutable events/revisions atomically committed with repository updates; ordered synchronous dispatcher, Observer listeners and isolated failure reporting; CalendarPort/Adapter to incompatible simulated client with precise timestamp/error mapping and idempotency. Preserve S3 atomic semantics. No console menus/seed composition until S5, no real external services, background workers or databases. Stop after S3.

Final handoff check: scanned 51 domain and 3 application source files for forbidden imports (0 findings), all source/docs for trailing whitespace (0 findings), and UML start/end delimiters (valid; not renderer validation). Git diff --check exited 0 but files remain untracked, so the separate source scan supplies whitespace evidence. Final Git status retained the existing untracked project structure and user-ignore warning. Documentation-only corrections followed the final passing build.

## 2026-10-26 - S4: Adapter, Facade, Observer and integrated booking

**Inspection:** Read AGENTS/memory/latest history, requirements/architecture/pattern matrix/plan/testing/evidence/UML; inspected actual Java creation/family/add-on/State/time/scheduling/repository APIs and tests. Existing repository had no event queue or revisions, confirming S3 handoff. All files were already untracked and preserved; Git user-ignore permission warning remains. No staging/commit.

**Production files added** (under src/main/java/com/appointease/):
- domain/event/AppointmentEvent.java.
- application/BookingFacade.java, AppointmentAssembler.java, BookingRequest.java, BookingResult.java.
- application/port/CalendarPort.java, CalendarSyncException.java.
- application/event/AppointmentListener.java, AppointmentEventPublisher.java, CommittedEventDispatcher.java, DeliveryFailure.java.
- infrastructure/calendar/ExternalCalendarAdapter.java, SimulatedExternalCalendarClient.java, LegacyCalendarException.java.
- infrastructure/notification/NotificationListener.java, AuditListener.java.

**Production files modified:** application/port/AppointmentRepository.java (catalog reads and immutable event log); infrastructure/memory/InMemoryAppointmentRepository.java (catalog browsing, revision map/global event log and atomic commit helper). Existing domain production classes, Main and POM unchanged. No repository recreation or discarded work.

**Tests added:** src/test/java/com/appointease/domain/BookingIntegrationTest.java (25 invocations) and CalendarObserverTest.java (14). All original 197 tests unchanged. Tests cover all-nine-pattern full lifecycle, catalog/availability, both service mode families, immutable subtype/family/fee preservation, cancellation/reuse, failures/no events, revision chains, exact time/zone/operation/revision translation, numeric failure mapping with causes, invalid legacy input, stale revision tombstones, subscription order/unsubscribe/snapshot semantics, deduplication/reentrancy, post-lock callbacks and warning isolation. Five new bounded latch/future races cover conflicting/disjoint bookings, confirm/reschedule, cancel/reschedule and same-command retries across shared facades. Six S3 races retained. No sleeps/network.

**Decisions and actual guarantees:** Existing S3 atomic commands retained and extended with one commit helper. Event/revision/map update uses the same repository lock and captured Clock instant. AppointmentEvent carries immutable before/after snapshots, per-appointment revision and global sequence, with stable UUID-namespace/sequence event ID. Failed operations append nothing. Append-only log plus dispatcher cursor refines planned destructive FIFO queue; no contradictory S3 queue existed. No caller may overwrite stored snapshots; commands read latest state. State remains polymorphic; finite command-to-event/legacy-code mapping switches do not implement lifecycle rules.

Facade delegates construction to AppointmentAssembler using existing Builder/creators/family factories/decorator quotes; repository always revalidates Strategy/hard rules atomically. Advisory available(request) is local only. One shared CommittedEventDispatcher per repository serializes facade operations and post-commit delivery. Direct repository writes can be drained explicitly or on a subsequent successful facade call. Publisher delivers subscriber snapshots in registration order, deduplicates IDs and isolates RuntimeExceptions; later listeners/calendar still run. Callbacks run outside repository lock. Reentrant facade/publication rejected; callbacks must not wait for command threads. Fatal JVM Errors are not recoverable delivery exceptions.

Successful caller-supplied command-ID receipts return the original result on retry, including original warning/revision snapshots. Reuse with different input rejects; requests compare collaborators by their existing equality, so retain same request/immutable collaborator instances for retries. Failed commands do not consume keys; new keys mean new operations. No distributed exactly-once or crash durability claimed.

Calendar policy: post-commit mirror only; external conflict/unavailability/invalid timestamps produce typed warnings and preserve local committed capacity. Adapter translates event types to primitive operation codes, exact epoch milliseconds, explicit zone, rN revision and stable event key. 400/409/503/unknown map to typed reasons with cause. Extra event-key legacy argument enables idempotency. Fake conflict keys/error codes are deterministic; successful event keys deduplicate, stale revisions reject, DELETE retains an inactive tombstone. No live calendar/notification calls.

**Commands and outcomes:** java -version exit 0 (Temurin 21.0.12.1+1-LTS); mvn -version exit 0 (3.9.16). Initial production-integration mvn clean verify at 20:25:38 +05:00 exit 0, original 197 passing. Expanded suite at 20:28:20 exit 1: 233 tests, 1 failure, 0 errors/skips. This was a test setup error: duplicate appointment-ID assertion accidentally reused a successful command ID with different collaborator instances, correctly triggering the input-reuse guard. Corrected it to a distinct command ID; added publication-reentrancy, DST conversion and terminal completion coverage. Final mvn clean verify at 20:29:46 exit 0, BUILD SUCCESS: **236 tests, 0 failures/errors/skips**. Surefire XML totals independently checked. Jar run exit 0, exact Welcome to AppointEase! Repeated standalone Java/Maven/jar checks also exited 0. One combined patch was rejected for duplicate target before mutation and reapplied successfully; no partial edits lost.

Final counts: DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, CreationPatternsTest 23, DecoratorTest 14, SchedulingTest 41, MainSmokeTest 1, BookingIntegrationTest 25, CalendarObserverTest 14. 72 production / 11 test sources compiled. No Java/test edits after final passing build.

**Handoff files:** Updated AGENTS.md, PROJECT_MEMORY.md, README.md; docs/requirements.md, architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, rubric-evidence.md and this appended history. Updated UML overview, booking sequence, lifecycle sequence and scheduling sequence. Architecture documents actual log/cursor, shared dispatcher, retry equality and calendar warning policies. Source dependency scan found 0 forbidden domain/application imports. Lock/callback paths reviewed. UML source only; no renderer validation. Historical verification retained rather than overwritten.

**Limits/readiness:** All nine patterns executable and S4 complete. No outstanding build failure. In-memory logs, deduplication/receipt/failure sets are unbounded; storage nondurable, immutable catalogs, whole-store locking/linear scans and synchronous serialized facade delivery suit academic scale. Multiple independent dispatchers per repository unsupported; composition must share one. Automatic delivery retry, external-availability veto, crash recovery and throughput guarantees are not implemented. Permanent architecture tests/UML rendering remain deferred. Git ignore warning persists. Recommended S5 only when authorized: deterministic seed/demo, Main composition, injectable console streams/parsing/rendering, scripted lifecycle/bad-input/EOF tests. No S5 implementation begun.

Final handoff check: all source/document files scanned separately because Git files remain untracked: 0 trailing-whitespace findings; all PlantUML files have one start/end pair (not rendering proof). Final Git status preserves existing untracked structure and user-ignore warning. Documentation-only handoff updates followed the final passing build; no production/test changes followed it.

## 2026-10-26 - S5: console application, seed data and executed demonstration

**Initial inspection:** Read AGENTS.md, PROJECT_MEMORY.md, latest history, architecture, pattern matrix, implementation plan, testing strategy and requirements. Inspected real facade/request/result/assembler APIs, domain models/modes/time/add-ons/exceptions, repository/event/observer/calendar wiring, fixtures/integration tests, Main/process smoke and POM. Fresh baseline verification confirmed all 236 S4 tests after a sandbox dependency-access failure and approved retry. Existing patterns were not inferred from reports alone.

**Files created:**

- src/main/java/com/appointease/presentation/ConsoleInput.java and ConsoleApplication.java.
- src/main/java/com/appointease/infrastructure/seed/DemoCatalog.java and DemoSeed.java.
- src/test/java/com/appointease/ConsoleApplicationTest.java.
- scripts/demo.ps1 and docs/uml/console-sequence.puml.

**Files modified:** Main.java; MainSmokeTest.java (interactive EOF expectations); README.md; AGENTS.md; PROJECT_MEMORY.md; docs/architecture.md, design-pattern-matrix.md, implementation-plan.md, testing-strategy.md, requirements.md, rubric-evidence.md, uml/overview.puml and this history. No domain/application/existing infrastructure adapter or POM changes. All 235 pre-existing domain/integration test invocations remain unchanged; original process smoke retained as one adapted invocation.

**Implementation:** All eleven requested operations and exit; numbered/full-ID catalog choices; type-filtered services; strict local dates/times resolved via DomainTime with displayed offset; online/in-person family selections; priority/reminder decorator quotes; standard/provider-buffer policies; final booking and reschedule confirmation; complete receipt/fees/status/mode instructions; UUID appointment IDs and @1 etc. session shortcuts; sorted customer snapshots, event details, notification/audit tables. Expected parsing/domain/repository/slot/lifecycle errors return to menu. EOF unwinds any prompt without saving partial work; streams injected and output flushed. No terminal libraries or ANSI reliance.

Main.create(InputStream, OutputStream, Clock, ZoneId) owns one real repository/dispatcher/publisher/calendar/observer composition; main only resolves configuration and runs it. Console talks to BookingFacade and injected read-only observer suppliers, never storage. Main wires creator map; request selections reuse existing factories/decorators/strategies and facade assembly. All nine pattern implementations remain intact.

DemoCatalog supplies three customers and four providers/services with stable namespaced IDs, categories healthcare/tutoring/consulting/personal services, durations 30/60/45/30 minutes, EUR 40/25/60/20, weekdays 09:00-17:00 and optional 15-minute preparation. Both modes except salon in-person. DemoSeed creates exactly one previous-weekday 10:00 confirmed appointment (seed-ready-001) using legitimate book/confirm facade commands under an instance-local startup clock, then permanently delegates to runtime Clock before exposing the console. This refines the old fixed-date/one-customer plan and supports immediate completion without relaxing State rules or importing arbitrary state. There are no overlapping seed reservations and no fixed production appointment dates.

**Commands and actual outcomes:**

- java -version: exit 0, Temurin OpenJDK 21.0.12.1+1-LTS. mvn -version: exit 0, Maven 3.9.16 with Java 21.
- Initial sandbox mvn clean verify: exit 1 at 23:25:52 +05:00, before compilation; Maven Central dependency access denied (getsockopt). Approved retry: exit 0 at 23:26:39, BUILD SUCCESS, original 236 tests, zero failures/errors/skips.
- First expanded mvn clean verify: exit 0 at 23:30:05, 262 tests, zero failures/errors/skips. After extracting historical setup from Main into DemoSeed, final mvn clean verify: exit 0 at 23:31:46, BUILD SUCCESS, **262 tests, 0 failures, 0 errors, 0 skipped**. Compiled 76 production and 12 test sources. Surefire XML independently summed to these totals. No Java/test edits after this final build.
- New ConsoleApplicationTest: 26 invocations through real Main composition, fixed Clock and byte-array streams; navigation, listings, workflow, both kinds/modes, fees, overlap/adjacency/buffer/reuse, confirmation/reschedule/cancel/complete, history, validation recovery, no-confirm/advisory writes, EOF at multiple stages, configured zone and independent compositions. No sleeps/external services.
- scripts/demo.ps1 initial run: exit 1 before Java because Windows PowerShell's timezone database did not recognize Europe/Vilnius. Added FLE Standard Time fallback; rerun exit 0. It actually invoked java -jar target/appointease-0.1.0-SNAPSHOT.jar for workflow, empty EOF and mid-booking EOF; **all three child exits 0** with expected evidence assertions. Runtime is offline.
- Script used 2026-10-27 (computed next weekday): online health 10:00 EUR 52.00 with both extras; duplicate-slot rejection; adjacent 10:30 booking; rejected buffered reschedule preserving 10:30; confirmed first booking, rescheduled to 11:30 buffered, cancelled and reused that slot; in-person salon EUR 20.00; completed elapsed seed; appointment, notification and audit histories. Four successful interactive bookings. Explicit exit plus both EOF paths show Goodbye. Full generated transcripts: target/demo-workflow.txt, target/demo-eof.txt, target/demo-partial-eof.txt.
- Domain/application forbidden-import rg scans: no findings (rg exit 1 means no matches, not a failed application check). No permanent architecture test added. Git remains untracked with existing ignore warning; no commits.

**Handoff/limits:** README contains build/run, timezone, complete catalog, menu use, lifecycle rules, shortcuts and repeatable demo instructions. Architecture and UML now show console/seed composition; UML source reviewed, not rendered. PROJECT_MEMORY is concise/current with links, actual outcomes and boundary. No known failing tested workflow or build blocker. Existing nondurable/unbounded process-local storage/logs and serialized delivery remain. Console supports exact-slot queries, not automatic slot discovery; invalid input restarts the operation; wide tables can wrap; English prompts only; notification records are simulated, not timed reminders. Exotic historical timezone changes and broad terminal compatibility are not comprehensively tested.

**Recommended S6 only after authorization:** final requirements/pattern traceability audit, permanent architecture checks, UML rendering, broader terminal/timezone QA, regenerated demo evidence and submission packaging review. No S6 implementation or final submission started.

## 2026-10-27 - S6: independent final audit and submission preparation

**Inspection and findings:** Read AGENTS.md, PROJECT_MEMORY.md, latest history, README, requirements, architecture, pattern matrix, testing strategy, rubric and implementation plan. Inspected actual source tree, creation/state/scheduling/assembler/facade/repository/dispatcher/observer/calendar/console/seed paths, tests and concurrency helpers, POM, demo and all six UML sources. Independently verified genuine behavioral participation of all nine patterns in the real lifecycle. No critical defect found. Qualifications: Builder is a fluent concrete variant without separate Director/abstract builder; product subtype differences are modest; Strategy varies preparation duration; calendar/notifications are simulated. No guaranteed mark.

Architecture: compiled dependencies confirm inward, acyclic layer directions and no forbidden domain/application dependencies or presentation repository access. Actual package cycles exist inside domain (model/state/mode/addon) and application/event. This is explicitly disclosed as a literal package-level N02 shortfall; avoiding it would require unjustified restructuring in a final audit. No cyclic constructor injection or mutable global state found. Reviewed one-lock atomic reserve/reschedule/transition/revision/event path, immutable snapshots, post-lock listener/calendar delivery, RuntimeException isolation and callback/retry limitations. Invalid timezone prints a configuration error but exits 0; documented low-severity CLI limitation, not silently called successful configuration.

**Created files:** src/test/java/com/appointease/ArchitectureTest.java (two tests, JDK jdeps plus static-field reflection); scripts/verify-console.ps1; scripts/package-submission.ps1; docs/S6-AUDIT.md; docs/demonstration-walkthrough.md; docs/evaluation-criteria-draft.md; SVG and PNG renders for all six UML sources; root AppointEase_DesignPatterns_Submission.zip (ignored local deliverable).

**Updated files:** AGENTS.md, PROJECT_MEMORY.md, README.md, .gitignore; docs/requirements.md, architecture.md, design-pattern-matrix.md, testing-strategy.md, rubric-evidence.md, implementation-plan.md, SESSION_HISTORY.md; docs/uml/overview.puml, booking-sequence.puml, lifecycle-sequence.puml. Fixed stale planned/current-boundary claims, added user-supplied 100-point traceability and evidence draft, corrected unsupported UUID-collision retry and EventId descriptions. Historical outcomes remain intact under explicit historical headings. No existing Java tests changed; no production Java changed.

**Actual verification:**

- java -version and mvn -version each exit 0: Temurin OpenJDK 21.0.12.1+1-LTS, Maven 3.9.16 on Java 21.
- Fresh baseline `mvn clean verify`: exit 0, October 26 23:55:02 +05:00, 262 tests, zero failures/errors/skips.
- Final source `mvn clean verify`: exit 0, October 27 00:05:37 +05:00, BUILD SUCCESS, **264 tests, zero failures/errors/skips**. 76 production and 13 test source files compiled. Surefire XML independently summed to 264/0/0/0. Retained counts: DomainModelTest 61, TimeAndMoneyTest 18, AppointmentTest 13, AppointmentStateTest 26, CreationPatternsTest 23, DecoratorTest 14, SchedulingTest 41, BookingIntegrationTest 25, CalendarObserverTest 14, ConsoleApplicationTest 26, MainSmokeTest 1; new ArchitectureTest 2.
- scripts/demo.ps1 executed packaged workflow and empty/mid-booking EOF: all three Java children exit 0, expected assertions pass. Four bookings, overlap rejection, adjacency, rejected buffered reschedule, successful buffered reschedule, cancellation/reuse, both modes/types, EUR fees, elapsed-seed completion and observer/history outputs. Script is also executed as a separate PowerShell process in the extraction verification below, exit 0.
- `powershell -NoProfile -File scripts/verify-console.ps1`: script exit 0; six Java children (smoke, invalid menu/customer with recovery to service listing, empty EOF, mid-booking EOF, UTC completion, Asia/Karachi completion) all exit 0. No partial booking saved. Transcripts under target/qa-*.txt, excluded from package.
- Quoted invalid-zone Java command prints expected error and exits 0, confirming diagnostic limitation. An initial unquoted PowerShell -D argument was parsed incorrectly and exited 1 with ClassNotFoundException; corrected quoting verified actual behavior. This was a command invocation error, not a product regression.
- git diff --check exit 0. Since files are untracked, separate scan of 113 submission text files found zero trailing-whitespace issues. Targeted credential/private-key scan found no matches in source/docs/scripts/POM/README; archive allowlist excludes local secret/tool/build directories. This is not a comprehensive security certification.

**UML:** No installed plantuml/dot command initially found. Standalone PlantUML 1.2024.8 download from Maven Central first failed in sandbox (exit 1); approved retry succeeded. Tool reports bundled/available Graphviz 2.44.1. Initial checkonly exit 1 and render exit 200 identified lifecycle-sequence.puml line 37: sequence note used invalid `note bottom of`. Changed to `note over`. Corrected stale no-console text and Builder quote ordering in booking sequence; actual Appointment spec field/indirect relationships and final status in overview. Visual inspection revealed initial overview PNG clipping at renderer size limit; disabled automatic dotted namespace nesting and bounded image dimensions, rerendered successfully. All six final sources pass syntax validation (exit 0), SVG/PNG rendering exits 0, all images visually inspected. SVG recommended for dense overview/wide booking sequence. Renderer and audit tooling are excluded from ZIP; application build does not depend on renderer.

**Packaging and fresh extraction:** scripts/package-submission.ps1 creates root `AppointEase_DesignPatterns_Submission.zip` from explicit roots/extensions and required files. 125 entries: 76 production Java files, 13 test Java files, 18 UML files (six each PUML/SVG/PNG), 10 documentation Markdown files, three PowerShell scripts, and five root files (POM, README, AGENTS, PROJECT_MEMORY, .gitignore). It verifies required entries, excludes target/.git/IDE/log/temp/local-tool/secret paths, checks entry count and compares every archived entry with its source SHA-256. No binary JAR or renderer is shipped; source builds the runnable JAR.

First extraction `.submission-check/first` was a new empty directory. `mvn clean verify` there exited 0 at 00:13:28 +05:00, 264 tests, zero failures/errors/skips. Extracted `powershell -NoProfile -File scripts/demo.ps1` and `powershell -NoProfile -File scripts/verify-console.ps1` both exited 0; all nine Java children exited 0 and assertions passed. Builds used this host's existing Maven cache; fresh extraction does not imply a clean cache, another JDK distribution or OS. Closing documentation changes are repackaged; production/test Java, POM and scripts remain unchanged from the verified extraction. Every final ZIP entry is checked against current source bytes.

**Known limitations and manual tasks:** Intra-layer package cycles/N02 qualification, concrete fluent Builder variant, invalid-zone exit code 0, unbounded in-memory receipts/logs, serialized synchronous delivery, no durable retry, simulated reminders/calendar, exact-slot queries, English/wide tables and non-exhaustive historical timezone/terminal coverage remain explicit. No failing supported test/build or unresolved critical defect. Before October 30, user must confirm Moodle template/size/deadline timezone, add student/course identifiers/declaration, upload ZIP and any required evaluation form, open/verify attachment, perform final submission and save receipt. The completed evidence draft is docs/evaluation-criteria-draft.md. No upload or commit was authorized/performed. Stop after S6.


## 2026-10-27 - S6.1: package dependency correction and release verification

**Baseline:** java -version and mvn -version exit 0 (Temurin 21.0.12.1, Maven 3.9.16). Sandbox mvn clean verify failed at 00:52:59 before compilation with getsockopt access denial (exit 1); approved retry completed 00:53:32 +05:00, exit 0, 264/0/0/0 tests/failures/errors/skips. Existing two architecture checks passed. jdeps class edges showed 18 packages, exactly four simple cycles in two cyclic components: domain model/state/mode/addon and application/event. Concrete class/import edges are recorded before refactoring in S6-AUDIT.md. These were real compile-time ownership cycles, not cyclic constructors or merely conceptual arrows.

**Decisions:** Five aggregate-related classes (Appointment, ConsultationAppointment, ServiceAppointment, ValidatedAppointmentSpec, AppointmentRules) moved domain.model -> domain.appointment, preserving sealed/package-private construction relationships and lower shared model values. Command/receipt coordinator CommittedEventDispatcher moved application.event -> application; publisher/listener/failure contracts stay in event. Six FQCNs change; no method contracts or business logic changes, no wrappers/artificial interfaces. Migration preserves all nine patterns and full immutable identity/lifecycle/factory/decorator/strategy/repository/adapter/observer/facade semantics.

**Architecture tests:** Retained compiled layer allowlist and mutable-static-field checks; added all-production exact-package cycle check, detector regression cases and source import guard. JDK jdeps -verbose:class -filter:none covers every compiled class, including nested classes. Sorted graph and active-path DFS check every connected component. Only within-package edges/JDK leaves omitted; no project exclusions. Synthetic direct/indirect/disconnected cycles fail and diamond/same-package cases pass. Reports target/architecture-jdeps.txt and architecture-packages.txt. Console changed to explicit facade/request/result imports after intermediate check rejected application wildcard; test was not weakened.

**Actual verification:** Intermediate mvn clean verify 00:56:44 exit 1, 267 tests, 1 failure (new source import guard), zero errors/skips. Corrected import, final workspace mvn clean verify 00:57:23 exit 0, BUILD SUCCESS, **267/0/0/0**, independently summed from Surefire XML. Original 264 retained; three added architecture invocations. 76 production / 13 test files. All eleven existing coordinated concurrency scenarios pass. Local comparison proves all 88 production/non-architecture test and fixture bodies unchanged after stripping package/import declarations. POM unchanged. Independent graph enumeration now finds 19 packages, zero cycles. git diff --check exit 0 (untracked files require supplementary whitespace review).

**Runtime:** powershell -NoProfile -File scripts/demo.ps1 and scripts/verify-console.ps1 exit 0; nine packaged Java child exits 0. Demonstration uses next Vilnius weekday 2026-10-27: four bookings, overlap/adjacency/buffer failure and preservation, successful reschedule, cancellation/reuse, confirmation/completion, both families/kinds/fees, notification/audit histories, explicit exit, empty and mid-operation EOF. Additional QA covers malformed input recovery, UTC/Karachi seed completion. Offline runtime unchanged.

**UML/docs:** Read all six sources. Overview relocates aggregate and dispatcher, moves relationships after declarations to avoid implicit old-package ownership, and updates boundary. Five sequence sources are unchanged; a trial ASCII title did not fix the SVG metadata decoder and was reverted. All six checkonly/SVG/PNG renders exit 0; updated overview visually reviewed without clipping. All six PNG embedded sources match PUML; six SVGs match independent fresh renders byte-for-byte. SVG metadata decoding was unreliable and is not claimed as verification. Early validation helper needed stdout/stderr UTF-8 handling; failed SVG decoding comparisons were not called passing. Documentation updates preserve original historical S6 results under explicit historical headings and document N02 resolution. Packaging script now requires handoff docs, relocated entrypoints and every UML source/render.

**Changed/current files:**

- src/main/java/com/appointease/application/AppointmentAssembler.java
- src/main/java/com/appointease/application/BookingFacade.java
- src/main/java/com/appointease/application/BookingResult.java
- src/main/java/com/appointease/application/CommittedEventDispatcher.java
- src/main/java/com/appointease/application/port/AppointmentRepository.java
- src/main/java/com/appointease/application/port/AppointmentTransition.java
- src/main/java/com/appointease/domain/appointment/Appointment.java
- src/main/java/com/appointease/domain/appointment/AppointmentRules.java
- src/main/java/com/appointease/domain/appointment/ConsultationAppointment.java
- src/main/java/com/appointease/domain/appointment/ServiceAppointment.java
- src/main/java/com/appointease/domain/appointment/ValidatedAppointmentSpec.java
- src/main/java/com/appointease/domain/creation/AppointmentBuilder.java
- src/main/java/com/appointease/domain/creation/AppointmentCreator.java
- src/main/java/com/appointease/domain/creation/ConsultationAppointmentCreator.java
- src/main/java/com/appointease/domain/creation/ServiceAppointmentCreator.java
- src/main/java/com/appointease/domain/event/AppointmentEvent.java
- src/main/java/com/appointease/domain/scheduling/Reservation.java
- src/main/java/com/appointease/domain/scheduling/ReservationRules.java
- src/main/java/com/appointease/infrastructure/memory/InMemoryAppointmentRepository.java
- src/main/java/com/appointease/presentation/ConsoleApplication.java
- src/test/java/com/appointease/ArchitectureTest.java
- src/test/java/com/appointease/domain/AppointmentStateTest.java
- src/test/java/com/appointease/domain/AppointmentTest.java
- src/test/java/com/appointease/domain/BookingIntegrationTest.java
- src/test/java/com/appointease/domain/CreationPatternsTest.java
- src/test/java/com/appointease/domain/DomainFixtures.java
- src/test/java/com/appointease/domain/SchedulingTest.java
- src/test/java/com/appointease/domain/TimeAndMoneyTest.java
- docs/architecture.md
- docs/demonstration-walkthrough.md
- docs/design-pattern-matrix.md
- docs/evaluation-criteria-draft.md
- docs/implementation-plan.md
- docs/requirements.md
- docs/rubric-evidence.md
- docs/S6-AUDIT.md
- docs/testing-strategy.md
- docs/uml/overview.png
- docs/uml/overview.puml
- docs/uml/overview.svg
- scripts/package-submission.ps1
- AGENTS.md
- PROJECT_MEMORY.md
- README.md

Old locations of the six relocated Java files were removed by move. Unchanged pattern classes, source bodies, POM, demo and console QA scripts retained. Local tooling/snapshots excluded from ZIP.

**Candidate release status:** Source and runtime verification complete. Handoff written before candidate ZIP creation. External-temp fresh extraction and final regeneration evidence will be appended below before the final archive is built. Do not treat this candidate as the completed final release until that gate passes.


**S6.1 extraction and finalized handoff:** Candidate ZIP created only after workspace verification and candidate memory/history were written: 125 entries, SHA-256 CF25FC23A0CCAA74B1EB46C13CEDDED0007C9B4337570C8FE43E565787153E8D (candidate identity only, not the final ZIP). Extracted into new external temporary directory <external-temp>/AppointEase-S6.1/candidate, outside the repository. There, mvn clean verify completed 2026-10-27 01:05:21 +05:00, exit 0, BUILD SUCCESS, **267 tests, 0 failures/errors/skips**, independently summed from XML. All five architecture checks pass. Every extracted payload file matches workspace bytes. The initial sandbox console-script launches exited 1 due to temporary-directory access denial, before Java; approved combined retry ran both extracted scripts with exit 0 and all nine Java children exit 0, full workflow/EOF/QA assertions passing. Build uses the same Windows/JDK/Maven cache, not a fresh-cache or second-OS certification.

Final untracked-file supplement: 113 submission text files, zero trailing whitespace; git diff --check exit 0. Inventory remains 125 (76 production Java, 13 test Java, 18 UML artifacts, 10 docs Markdown, 3 scripts, 5 root files). No target/.git/IDE/temp/audit tooling/secrets shipped; packaging compares all entries by SHA-256 and requires current handoff, relocated entrypoints and all six diagrams. No JAR shipped; extracted Maven build produces and runs it.

This finalized memory/history/audit evidence is written BEFORE regenerating the final ZIP. Only these closing Markdown files change after candidate verification; production/test Java, POM, scripts and UML remain byte-identical to the verified extraction. Final release protocol immediately regenerates the ZIP, verifies its inventory/entry hashes, extracts again into <external-temp>/AppointEase-S6.1/final, and repeats mvn clean verify plus demo/console QA. The exact final ZIP size/hash and repeated-check outcomes are reported externally in the final response and local .audit-tools/final-release.json, so recording the final archive identity does not change the archive it identifies. No pre-execution result of that final repeat is fabricated here. If that gate fails, completion must not be claimed; repair, update evidence, regenerate and recheck.

**Remaining limits/next boundary:** Four cycles resolved, no exclusions or unavoidable production package cycle remains. Nine patterns and all original behavioral/concurrency assertions preserved. Six FQCN migrations require downstream source imports/recompilation; no binary-compatibility facade is claimed. Existing academic/process-local/synchronous/simulated-integration/console/invalid-zone-exit limitations remain as documented. No blocker on verified payload. User must confirm Moodle requirements/deadline timezone, add identifiers/declaration, upload and verify final receipt; no upload or commit made. Stop after S6.1, await explicit authorization for additional work.


## 2026-10-27 - GitHub publication and final local verification

**Initial state:** Read AGENTS.md, PROJECT_MEMORY.md, latest history, README, relevant architecture/pattern/academic docs, pom.xml and ignore rules. All 125 intended project files were untracked; unborn master, no commits/remotes/tracked files. Existing ignored S6.1 ZIP and local audit artifacts preserved. GitHub CLI 2.89.0 installed. Sandboxed gh auth status falsely reported an invalid credential; authorized outside-sandbox check succeeded using the keyring account arslanhakim. No token was requested or exposed. Initial requested-repository lookup reported nonexistent; created arslanhakim/AppointEase-Design-Patterns-Hamza as PRIVATE with the exact requested description and verified metadata. No preexisting remote content was modified.

**Files prepared:** .gitignore expanded for Git metadata, target directories, IDE/editor state, local agent/audit/temp/extraction directories, logs, environment files, credentials/private keys and ZIPs. README retains JDK 21/Maven setup/build/run/demo instructions and adds a nine-pattern package map. AGENTS records narrow publication authorization. PROJECT_MEMORY and this history updated before commit. Historical host/user directory paths in PROJECT_MEMORY, S6-AUDIT, rubric-evidence and this history were replaced with repository-relative or external-temp references solely for privacy; original verification outcomes retained. No source/test/POM/UML/script behavior changed.

**Privacy/inventory:** Explicit allowlist is .gitignore, AGENTS.md, PROJECT_MEMORY.md, README.md, pom.xml, src/, docs/, scripts/ (125 files). Checked credential/private-key/cloud-key/credential-URL/assigned-secret/personal-email/personal-path signatures without printing sensitive values; zero findings. Ignore probes confirm Git/build/IDE/audit/extracted/env/credentials/key/ZIP exclusions. This targeted scan is not an exhaustive security certification. Commit identity uses repository-local authenticated account login and GitHub no-reply address, leaving global Git identity unchanged. No AI attribution/co-author metadata.

**Actual verification before commit:** java -version exit 0 (Temurin 21.0.12.1+1-LTS); mvn -version exit 0 (Maven 3.9.16). mvn clean verify completed 01:44:41 +05:00, exit 0, BUILD SUCCESS, **267 tests, 0 failures/errors/skips**. Five architecture checks pass; all nine patterns and original concurrency tests retained. powershell -NoProfile -File scripts/verify-console.ps1 exit 0; all six packaged-JAR processes exit 0 and assert welcome/goodbye, explicit exit, empty/mid-operation EOF, malformed-input recovery, UTC and Karachi completion. Business code unchanged.

**Artifact/boundary:** Existing verified S6.1 submission ZIP unchanged and excluded from Git; its archived docs precede this publication-specific privacy/handoff update. Do not confuse it with the new GitHub snapshot. No academic ZIP regeneration or Moodle upload requested. Stop after publishing/verifying; further development needs explicit authorization.

## 2026-10-27 - S7A JavaFX shell and Overview visual prototype

**Initial evidence:** Read AGENTS.md, PROJECT_MEMORY.md, recent history, requirements, architecture, implementation plan, pattern matrix, testing strategy, POM and actual facade/seed/domain/console/test APIs. Clean main tracked origin/main at b8417e91960c3e632c16b2a3e1b14763445c5382. Existing submission ZIP SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A, 1,372,171 bytes. Creating feature/javafx-ui was blocked by sandbox Git-metadata restrictions; approved retry succeeded before edits. Java version and Maven version commands exit 0: Temurin 21.0.12.1, Maven 3.9.16, Windows 11. Git user-ignore permission warning remains.

**Architecture/implementation:** Main.createContext exposes the unchanged instance-owned service graph as ApplicationContext; Main.create preserves its signature and console composition. DesktopMain owns the JavaFX lifecycle/root wiring and invokes the same composition, then DesktopDemoSeed creates four next-weekday fictional appointments through facade book/confirm (three confirmed, one pending; both modes, reminder decorator). Console historical seed remains unchanged. No facade/domain/application/existing adapter semantics or source bodies changed. No new business rules or real integrations.

DesktopShell declares Overview, Appointments, New Appointment, Providers, Schedule, Activity; only Overview is enabled, with no empty placeholder controllers/pages. OverviewModel is a read-only facade-history projection for the injected customer catalog. OverviewView renders selected-day counts, compact appointment rows, status labels, upcoming appointment, a proportional 09:00-17:00 agenda and week strip. Native date picker, previous/next, Today, Refresh and collapsible navigation work. Status counts are display groupings, not lifecycle permission checks. Cancelled records remain in rows but not agenda occupancy; reads make no writes/events. Compact layout stacks the agenda below the list at 850 logical pixels and scrolls.

**Design:** Ink #202A35 navigation/text, ivory #F5F3ED workspace, terracotta #C56B4E selected/pending accents, sage #7D927B confirmed accents. Local Georgia headings/numbers and Segoe UI data/controls; thin dividers, three-pixel radii, no content shadows/gradients/icons/animations. Darker text variants maintain readable contrast. Shared Ui helper and theme.css keep controls consistent. Native focus/Tab behavior retained. Real captures revealed sidebar clipping, generic button style precedence and a premature responsive breakpoint; corrected source and relaunched. Final density/date-format refinements preserve all four rows in the desktop viewport. At 125% Windows scaling lower summary/final agenda hours still require scrolling; compact agenda is below the list by design.

**Dependencies/runtime:** Pinned javafx-controls 21.0.13 (transitive base/graphics) and javafx-maven-plugin 0.0.8, checked against official OpenJFX setup/Gluon release notes. No FXML, Swing, icon, CSS or test framework dependencies added. Maven manifest still points to Main, so console java -jar works without JavaFX modules. scripts/run-desktop.ps1 uses the packaged JAR and cached Windows module JARs directly; no Maven/network at runtime, optional -MavenRepository. mvn javafx:run is the alternate development launch. No installer/bundled runtime.

**Test changes:** Original 267 test invocations retained; all existing behavioral/concurrency sources unchanged. ArchitectureTest keeps its five checks, all-class/package coverage and inward boundaries, explicitly allows JavaFX only in presentation.desktop/DesktopMain and names new composition-root types. Five OverviewModelTest cases cover real facade-backed data/events/counts, read-only loading, immutable snapshot/cancel/reschedule refresh, empty days, weekend/configured-zone seeding and isolated entry-point state. DesktopVisualCheck is a compiled opt-in executable outside Surefire discovery, not a skipped test; it calls actual DesktopMain, verifies window/data/date changes/empty state/refresh/navigation/compact resize/native Tab focus and closes its own stage. No S7B controller writes were implemented or claimed tested.

**Actual command outcomes and corrections:**

- First expanded production build ran original 267 tests successfully, exit 0 at 11:01:36 +05:00.
- Initial approved mvn -B compile javafx:run failed downloading plugin dependencies, exit 1 at 11:16:31, before GUI launch. Retry downloaded tooling and launched the actual app successfully. Closed/relaunched during visual iteration.
- A sandbox mvn -B clean verify failed at 11:46:40, exit 1, before compilation (dependency access denied/getsockopt). Approved mvn clean verify at 11:47:16 passed 272/0/0/0, exit 0.
- New optional visual harness initially called Scene.lookupAll; test compilation failed at 11:54:39, exit 1. Corrected to scene root lookupAll. Rerun at 11:55:36 passed 272/0/0/0, exit 0.
- After final visual corrections, **mvn clean verify finished 12:05:03 +05:00, exit 0, BUILD SUCCESS, 272 tests, zero failures/errors/skips**. Independently summed Surefire XML; compiled 83 production/15 test sources. No Java/CSS/POM changes after this final build.
- Final powershell -NoProfile -File scripts/run-desktop.ps1 -Verify: exit 0, all 13 real-window assertions pass. Actual packaged GUI launched separately through scripts/run-desktop.ps1, offline, and was left open for review. No graphical limitation was falsely reported as verified; this environment supported real Windows launch/capture. Other OSes/assistive technology remain unverified.
- Final powershell -NoProfile -File scripts/demo.ps1 and scripts/verify-console.ps1 both exit 0. All nine actual java -jar child processes exit 0, demonstrating booking/conflicts/adjacency/buffer preservation/confirm/reschedule/cancel/reuse/completion/history, normal exit, EOF/input recovery and UTC/Karachi behavior. Demo target day October 28, 2026. Transcripts remain under target.
- One close-window pipeline returned process-close True but shell exit 1 because an optional javaw process-name query was absent; later explicit java-only close exited 0. This was tooling enumeration, not a Java crash.
- git diff --check exit 0. Main and origin/main still match the initial SHA; protected business/console/test-source diff is empty. ZIP hash and byte size still match pre-edit evidence. No staging/commit/push performed.

**Real screenshot evidence:** Directory C:\dev\appointease\docs\ui-review\screenshots\. scripts/capture-ui.ps1 identifies the exact AppointEase window, resizes to 1440x900 or 1024x768, temporarily raises it, verifies visible-window ownership, captures raw DWM-bound screen pixels and restores normal ordering. No mockups, generated images, scaling, editing or enhancement. An intermediate foreground-denied capture included an obscuring editor; rejected and overwritten, then added the ownership check/topmost safeguard. No unrelated editor content remains in the final files. All three final PNGs were reopened/decoded, checked nonempty, checked against signature 89-50-4E-47-0D-0A-1A-0A and visually inspected:

- 01-overview-desktop.png: 1424x892, 69,178 bytes; complete window/navigation/header, four rows and daily agenda.
- 02-overview-compact.png: 1008x760, 50,804 bytes; compact window/navigation/header/scrolling Overview.
- 03-navigation.png: 1424x892, 69,167 bytes; complete window with expanded navigation. No other view is implemented, so no additional screen capture claimed.

Saved dimensions exclude Windows' invisible resize borders from the requested outer sizes. PNGs are local and ignored by Git (check-ignore verified), not staged/committed/uploaded externally. Final captures supersede earlier local iterations.

**Files:** Created ApplicationContext.java, DesktopMain.java, infrastructure/seed/DesktopDemoSeed.java, presentation/desktop/{DesktopShell,OverviewModel,OverviewView,Ui}.java, desktop theme.css, presentation/desktop/{OverviewModelTest,DesktopVisualCheck}.java, scripts/{run-desktop,capture-ui}.ps1 and docs/ui-review/S7A.md. Modified Main.java, ArchitectureTest.java, pom.xml, .gitignore, AGENTS.md, PROJECT_MEMORY.md, README.md, docs/{architecture,requirements,design-pattern-matrix,implementation-plan,testing-strategy,SESSION_HISTORY}.md. Full explicit inventory in ui-review/S7A.md. Three ignored screenshots are additional local artifacts. No existing file deleted. No UML or ZIP regenerated; historical UML describes the preserved console baseline, and the desktop extension is documented in the architecture addendum. Main memory condensed to current boundary/evidence with links to preserved historical outcomes in this log/S6 audit.

**Limits/next boundary:** Read-only Overview only. Remaining five views and all desktop booking/confirm/reschedule/cancel/complete flows await S7B approval. The demo-hours agenda is not availability, dense/large/custom schedules are unverified, updates are synchronous/manual, and standard process-local unbounded/simulated integration limits remain. Native font fallback and non-Windows launch are not certified. At compact size or 125% display scaling, scroll to inspect lower content; final branding/labels are readable with no horizontal clipping observed. S7B must reuse facade and DomainTime, handle typed validation and post-commit warnings distinctly, preserve retry request identity/command IDs, and test conflicts, validation, reschedule/cancel preservation through actual presentation controllers. No S7B work, commit, push or academic repackaging authorized. Stop and wait for visual approval.

## 2026-10-27 - S7B complete JavaFX desktop and functional review handoff

**Fresh-session evidence:** Read AGENTS.md, PROJECT_MEMORY.md, latest history, S7A review, requirements, architecture, implementation plan, pattern matrix and testing strategy. Inspected facade/request/result/time/appointment/mode/decorator/provider/seed/console APIs, actual desktop source/CSS/tests/scripts and Git state. Existing reports independently summed to 272/0/0/0; this was recorded as existing evidence, not a fresh initial test run. Branch already feature/javafx-ui with all S7A changes uncommitted/untracked; preserved them. Main/origin/main at b8417e91960c3e632c16b2a3e1b14763445c5382. ZIP size/hash recorded and rechecked at closure.

**Implemented:** All six destinations enabled. Overview gains prominent New Appointment, List/Schedule links and mouse/keyboard details. Appointments combines search/status/provider filters, all-date history, selection/Enter/double-click and details. Booking supports customer/provider/service/date/time/mode, priority/reminder decorators, preparation policy, advisory availability, quote/offset review, explicit reservation and saved Pending receipt. Back-to-edit preserves fields and visible validation. Details supports confirm/cancel/complete and reviewed reschedule with domain eligibility, state refresh and immutable event history. Providers shows service prices/durations/modes/hours/buffer, checks an exact time and prefills a provider booking. Schedule has date/provider navigation, selectable time-ordered status entries and empty states. Activity reads actual notification/audit supplier records, sequence ordering, channel filtering, timestamps, revisions, descriptions and saved delivery warnings with empty/error feedback.

**Architecture decisions:** Added JavaFX-independent DesktopController; DesktopMain injects Main.createContext's same facade/customer/time/observer dependencies. No S7B domain/application/console/existing infrastructure/POM modification. No repository access or rule duplication in presentation. Decorators compute quotes, existing factories/strategies provide mode/policy, DomainTime resolves input and displays UTC offsets. Detached immutable Appointment methods determine eligible actions; real commands revalidate atomically. PreparedBooking and PreparedMove retain keys/request/collaborator identity for retries. Tests verify original reservation preservation and successful committed warning semantics. Views refresh after mutations or when navigated to; no timers/background infrastructure added.

**Visual decisions:** Retained ink/ivory/terracotta/sage, Georgia/Segoe UI, fine borders, small radii, native controls and explicit focus; removed the prior focus glow. Darker terracotta primary button preserves text readability. Sidebar automatically collapses below 1000 logical pixels and can be toggled; choosing a compact destination releases content width. Replaced preview footer with local workspace/timezone. Form grids and toolbars wrap; table columns constrain to available width. New Appointment sits alongside the Overview heading so all four desktop rows remain visible. Improved 12-13px secondary text; consistent date formats, buttons, price/error/status presentation. No gradients, glass, decorative dashboard charts, animations or content shadows. The dedicated Schedule uses readable chronological rows rather than proportional block heights; the existing Overview mini-agenda remains proportional to demo hours.

**Files:** New production desktop classes: DesktopController, AppointmentsView, AppointmentDetailsView, BookingView, ProvidersView, ScheduleView, ActivityView. Modified DesktopMain, desktop DesktopShell/OverviewView/Ui and theme.css. Added DesktopControllerTest and opt-in DesktopS7BCheck; updated the retained DesktopVisualCheck's destination expectation only. Extended scripts/run-desktop.ps1 with -VerifyS7B/-Capture and scripts/capture-ui.ps1 with validated S7B filenames/output directory. Updated AGENTS, PROJECT_MEMORY, README, architecture, requirements, pattern matrix, implementation plan, testing strategy and this history; created docs/ui-review/S7B.md with full exact paths/inventory/limitations. No preexisting file discarded. The 13 ignored PNGs are separate local review artifacts. All S7A work remains preserved in the working tree; the slice-specific full inventory is in S7B.md.

**Actual verification and corrections:**

- java -version: exit 0, Temurin 21.0.12.1+1-LTS. mvn -version: exit 0, Maven 3.9.16 on Java 21/Windows 11.
- Initial mvn clean verify failed at 12:30:23 +05:00, exit 1 before compilation: the still-running S7A review window locked target/appointease-0.1.0-SNAPSHOT.jar. Sandboxed process metadata was incomplete and CIM access was denied; approved exact-title window closure returned True, exit 0. No unrelated process terminated.
- First expanded suite finished 12:31:51, exit 1: 288 invocations, one failure in a new test expecting InvalidTransitionException for early completion; actual existing contract raises DomainValidationException. Corrected only that expectation. Original 272 tests passed. Next clean verify at 12:34:46 exited 0 with 288/0/0/0.
- Initial real-window journey passed 38 assertions and generated the requested raw captures; console scripts passed. Visual review moved the Overview action into its heading, tightened form spacing, moved table actions above rows, unified date formats and hid empty feedback panels. Added committed-warning regression coverage; clean verify at 12:40:18 passed 289/0/0/0, exit 0.
- Source review found feedback was reparented out of the form on review/back-to-edit. Kept feedback at the stable view root and added a real-window invalid-input regression after returning to edit. Clean verify at 12:42:41 passed 289/0/0/0, exit 0; UI rerun passed 39 assertions. This was an implementation correction, not a hidden tested success.
- A documentation patch with an incorrect architecture heading was rejected before mutation, then reapplied with actual context. No Java/test change resulted.
- PNG review found the compact conflict capture had valid bytes but cut off part of the message. Rejected/overwrote it. The capture harness now waits for feedback layout, scrolls to the end and asserts the complete message is inside the viewport before capture. No image editing was used.
- **Final mvn clean verify finished 12:48:23 +05:00, October 27, exit 0, BUILD SUCCESS: 289 tests, 0 failures, 0 errors, 0 skipped.** Independent Surefire XML sum agrees. 90 production and 17 test sources compiled. Original 272 retained; 17 new controller test invocations. All five existing architecture checks pass with all-class/package coverage. No Java/CSS/POM/script behavior change followed this final build (closing documentation only).
- Final powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7B -Capture: exit 0, **40 actual-window assertions passed**. Includes creation/review/receipt/no duplicate submit, invalid input and back-to-edit validation, conflict error visibility, confirm, rejected then successful reschedule, cancellation, completion of elapsed seed, filtering/navigation, provider availability/prefill, schedule selection/empty dates, activity, refreshed state, six compact page widths and keyboard focus. Without -Capture the journey has 39 assertions. Final -Verify: exit 0, **13 retained Overview real-window assertions passed**. Both use packaged DesktopMain and cached JavaFX modules, with no Maven/network runtime; both close their windows. No app was left running.
- Final scripts/demo.ps1 and scripts/verify-console.ps1 each exit 0 after the final build. All **nine java -jar child processes exit 0**: workflow/conflicts/buffer/adjacency/confirm/reschedule/cancel/reuse/completion/history, normal exit, malformed-input recovery, EOF and UTC/Karachi. Demo day October 28, 2026. Transcripts in target/demo-*.txt and target/qa-*.txt.
- git diff --check exit 0; staged diff empty. Protected domain/application/existing infrastructure/console/original behavior tests have no tracked diff against main. Main/origin/main refs unchanged. Existing Git user-ignore permission warning persists. No staging, commit or push performed.

**Final screenshot evidence:** Exact directory `C:\dev\appointease\docs\ui-review\screenshots\S7B\`. All files are raw actual-window pixels, not generated mockups. Script validates unobscured ownership and PNG decoding; independent final pass checked all 13 files, nonzero sizes, PNG decoding and signature 89-50-4E-47-0D-0A-1A-0A. All screen layouts were visually inspected; the corrected final conflict capture was reopened and shows the full error. No external uploads; Git ignore confirmed. Requested 1440x900 desktop and 1024x768 compact outer windows give DWM frame captures below at 125% scaling. Exact full paths also in S7B.md.

| Filename | Saved pixels | Bytes |
| --- | --- | --- |
| 01-overview-desktop.png | 1424x892 | 70780 |
| 02-overview-compact.png | 1008x760 | 43009 |
| 03-appointments.png | 1424x892 | 62637 |
| 04-new-appointment.png | 1424x892 | 57293 |
| 05-providers.png | 1424x892 | 55489 |
| 06-schedule.png | 1424x892 | 57866 |
| 07-activity.png | 1424x892 | 64809 |
| 08-appointment-details.png | 1424x892 | 55494 |
| 09-booking-confirmation.png | 1424x892 | 44056 |
| 10-conflict-error.png | 1008x760 | 39805 |
| 11-appointments-compact.png | 1008x760 | 43811 |
| 12-new-appointment-compact.png | 1008x760 | 35492 |
| 13-booking-review.png | 1424x892 | 52218 |

**Protected artifact:** AppointEase_DesignPatterns_Submission.zip unchanged at 1,372,171 bytes; SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. No UML or submission regeneration. The ZIP remains the earlier academic console artifact, not S7B.

**Limitations/next boundary:** S7B is complete for the requested review scope; no known unresolved tested functional failure. Process-local/unbounded/synchronous/simulated-integration limitations remain. No automatic slot discovery, auto-refresh, week/month view, durable store or installer. Dedicated Schedule uses ordered day rows, and Overview retains a demo-hours mini-agenda. Compact/lower content scrolls vertically; supported-size checks found no horizontal overflow. Very large/custom catalogs, exhaustive keyboard/screen-reader certification and other OSes remain unverified. Native window title remains AppointEase | Overview; breadcrumb/selected navigation convey the current destination. Explicitly starting a new booking replaces the unfinished draft. Next recommended action is user visual and functional review using local captures/live app, then only explicitly authorized follow-up fixes or accessibility/scale/package work. **Stop after S7B. No commits, pushes, screenshot uploads, ZIP regeneration or later implementation.**

## October 27, 2026 — S7B continuation and final re-verification

**Authorization and reconciliation:** The user consolidated the fragmented S7B instructions and explicitly requested execution of all nine sections. Reread AGENTS.md, PROJECT_MEMORY.md, S7A review, latest history and architecture/pattern documentation; inspected source and Git. The complete functional S7B implementation was already present from this session and was preserved. Work remained on feature/javafx-ui. No later slice, commit, staging, push, screenshot upload or ZIP regeneration was performed.

**Final refinement:** DesktopShell exposes the current page title and binds the breadcrumb to it; DesktopMain binds the native window title to that property. Appointment details receives its own title. scripts/capture-ui.ps1 accepts the current AppointEase title while still requiring exactly one matching application window. DesktopS7BCheck adds seven real-window title assertions. This supersedes the static Overview-title limitation in the preceding entry. No domain/application/console behavior, CSS or dependency changed in this continuation. Updated PROJECT_MEMORY.md, docs/ui-review/S7B.md and docs/testing-strategy.md to the latest evidence.

**Executed verification:**

- java -version and mvn -version: exit 0, Temurin 21.0.12.1+1-LTS and Maven 3.9.16.
- mvn clean verify completed October 27, 2026 at **13:06:20 +05:00**, exit 0, BUILD SUCCESS: **289 tests, 0 failures, 0 errors, 0 skipped**. Surefire XML sum agrees. All original 272 preserved; 17 presentation controller invocations added in S7B. All five architecture checks pass. Compiled 90 production and 17 test sources.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7B -Capture: exit 0, **47 actual-window assertions passed**. Exercises booking/review/receipt, invalid input and back-to-edit feedback, conflicts, confirmation, rejected and successful rescheduling, cancellation, elapsed-seed completion, filters, provider availability/prefill, schedule/details/activity, mutation refresh, six compact widths, focus and current window titles. Capture mode additionally asserts full conflict-message visibility; the noncapture journey contains 46 assertions.
- powershell -NoProfile -File scripts/run-desktop.ps1 -Verify: exit 0, **13 retained Overview actual-window assertions passed**. Both harnesses launched packaged DesktopMain with local JavaFX modules and closed their windows. No application window remains running.
- scripts/demo.ps1 and scripts/verify-console.ps1: each exit 0 after the final build; **all nine java -jar child processes exit 0**. Includes full workflow/conflicts/lifecycle/history, malformed input, EOF and UTC/Karachi. Demo date October 28, 2026; transcripts remain in target.
- All 13 refreshed screenshot files passed nonempty, PNG signature and System.Drawing decode validation, exit 0. Images were reopened for visual inspection. These are raw actual JavaFX windows, with no generated/edited imagery or uploads. Requested outer windows 1440x900 and 1024x768 produce DWM captures 1424x892 and 1008x760 at 125% Windows scaling.

**Latest screenshot inventory:** Exact directory C:\dev\appointease\docs\ui-review\screenshots\S7B\. This inventory supersedes the byte counts from the preceding capture run; all full paths are also in docs/ui-review/S7B.md.

| Filename | Saved pixels | Bytes |
| --- | --- | --- |
| 01-overview-desktop.png | 1424x892 | 72936 |
| 02-overview-compact.png | 1008x760 | 44077 |
| 03-appointments.png | 1424x892 | 64919 |
| 04-new-appointment.png | 1424x892 | 59066 |
| 05-providers.png | 1424x892 | 57498 |
| 06-schedule.png | 1424x892 | 59937 |
| 07-activity.png | 1424x892 | 66398 |
| 08-appointment-details.png | 1424x892 | 57609 |
| 09-booking-confirmation.png | 1424x892 | 46232 |
| 10-conflict-error.png | 1008x760 | 41265 |
| 11-appointments-compact.png | 1008x760 | 45148 |
| 12-new-appointment-compact.png | 1008x760 | 37037 |
| 13-booking-review.png | 1424x892 | 54330 |

**Protection and handoff:** main and origin/main remain b8417e91960c3e632c16b2a3e1b14763445c5382. AppointEase_DesignPatterns_Submission.zip remains 1,372,171 bytes; SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. Protected domain/application/existing infrastructure/console/original behavior tests have no tracked diff against main. Final git diff --check passes; staged diff empty; local PNGs ignored. The preexisting user-level Git ignore permission warning persists. No technical blocker or known unresolved tested functional failure remains.

**Limits and next boundary:** All six views and requested booking/lifecycle interactions are implemented through the existing facade. Data/receipts/activity are process-local; integrations simulated; operations synchronous for the small demo catalog. Availability checks an exact slot; Schedule is an ordered day timeline, not a week/month calendar. Compact forms/lower content require vertical scrolling. Large datasets, exhaustive accessibility and non-Windows graphics remain unverified. User visual and functional review is next. **Stop after S7B; no further implementation or publication without explicit authorization.**

## October 27, 2026 - S7C final JavaFX visual polish

**Scope:** S7C refined Schedule, pricing, table status/selection, Activity, confirmation/details and responsive layouts on feature/javafx-ui. Existing S7A/S7B changes were uncommitted and preserved. No backend feature, rule, dependency, nine-pattern architecture, main or ZIP change was made.

**Implemented:** ScheduleView now lays out hour/half-hour lines and appointment blocks by actual elapsed start/duration (1.5 logical px/minute), with greedy reusable columns per connected overlap group. Default range 09:00-17:00 expands for outlying bookings. Cancelled entries hidden by default; Show cancelled, status legend/counts and dashed gray history retained alongside Pending terracotta, Confirmed sage and Completed blue-gray. Existing provider/date/list/details controls retained. Grid positions use Instants; UTC-offset labels are added for a displayed offset change, but DST-transition graphics were not independently certified.

BookingView groups optional services, itemizes existing quote fees and emphasizes the total; fields use three columns on desktop, two compact, one below 480 logical pixels. Compact insets reduced and field rearrangement occurs only when column count changes. Review/receipt/details share date/time/provider hierarchy with explicit offsets. Receipt uses a concise total, review retains the full quote/instructions, and eligible Confirm/Complete actions are primary with Cancel quiet. Ui adds shared status table cells, price/date helpers and a combo button cell preserving null-selection prompts. Activity shows type and timestamp before description, booking reference and channel/revision; event UUID remains in tooltip. Table selection gets a restrained sage fill and left border. Existing palette/fonts/flat controls preserved.

**Exact S7C source/tooling changes:** src/main/java/com/appointease/presentation/desktop/{ActivityView,AppointmentDetailsView,AppointmentsView,BookingView,DesktopShell,ScheduleView,Ui}.java; src/main/resources/com/appointease/presentation/desktop/theme.css; src/test/java/com/appointease/presentation/desktop/DesktopS7BCheck.java; scripts/run-desktop.ps1; scripts/capture-ui.ps1. Harness gains -VerifyS7C / --s7c and the capture script gains a validated S7B/S7C destination. Old S7B capture files remain untouched. Documentation: AGENTS.md, PROJECT_MEMORY.md, README.md, docs/implementation-plan.md, docs/testing-strategy.md, new docs/ui-review/S7C.md and this appended history. No new production class or dependency. Before/after SHA-256 inventory of src/scripts confirms all other existing files unchanged, including DesktopController, Main/DesktopMain, domain/application/infrastructure/console, all JUnit test sources and console QA scripts. POM/UML unchanged in S7C.

**Intermediate outcomes and fixes:**
- Initial sandbox mvn clean verify failed before compilation at 13:28:08 +05:00 because dependency resolution was denied (getsockopt). Approved retry passed at 13:29:33: 289/0/0/0. This blocked check is not reported as passed.
- Initial Windows capture journey passed 52 assertions, but visual inspection found hour labels autosized before CSS had zero dimensions and the expanded price summary displaced desktop form actions. Corrected axis sizing in layoutChildren and responsive field columns; shortened receipt and review spacing.
- Next Maven verification passed at 13:38:27: 289/0/0/0. Expanded no-capture Windows checks passed geometry/overlap but failed the keyboard traversal assertion. The grid's focused-button toFront handler changed child traversal order; removed it and allowed normal ScrollPane keyboard traversal. Added visible-desktop-action and real EUR 52.00 total checks.

**Final actual verification after all code/CSS/script changes:**
- java -version and mvn -version: exit 0, Temurin 21.0.12.1 and Maven 3.9.16 on Windows 11.
- mvn clean verify: **exit 0, BUILD SUCCESS, 13:46:19 +05:00 October 27, 2026; 289 tests, 0 failures, 0 errors, 0 skipped**. Independent Surefire XML sum agrees. 90 production and 17 test sources compiled; all five architecture checks pass. All 289 S7B Surefire tests preserved; harness assertions are additional opt-in checks, not skipped JUnit tests.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C: **exit 0, 77 actual-window assertions**. Complete existing booking/lifecycle/conflict/back-edit/filter flow retained; S7B always-visible-cancelled expectation replaced by requested default-hidden/show behavior. New geometry checks cover 90-minute offsets, 30/45/60-minute proportions, a quarter-hour start, overlapping providers, lane reuse and visible axis labels. Keyboard Tab traverses blocks, scrolls the later focused block into view, and Enter opens details. Completed historical styling and both window sizes checked. Fixture overlaps created through the real booking form only; no seed changes.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C -Capture: **exit 0, 78 assertions**, adding complete conflict-feedback visibility; 27 actual-window PNGs. Capture journey completed around 13:49 +05:00. Retained -Verify: **exit 0, 13 Overview assertions**. Both harnesses use the final packaged DesktopMain and cached JavaFX modules offline and close their windows.
- scripts/demo.ps1 and scripts/verify-console.ps1: **both exit 0 after final build; all nine packaged java -jar child processes exit 0**, covering complete lifecycle/conflict/EOF workflow, malformed recovery, UTC and Karachi. Demonstration date October 28. Transcripts in target/demo-*.txt and target/qa-*.txt.
- No production/test/CSS/script changes after final build. Closing changes are documentation only.

**Screenshots:** 27 raw local PNGs in C:\dev\appointease\docs\ui-review\screenshots\S7C\. Exact full paths, requested sizes, decoded sizes and final bytes: [S7C review](ui-review/S7C.md). All independently checked nonempty with PNG signature 89-50-4E-47-0D-0A-1A-0A and successful PNG decoding. Changed views captured at both sizes, including selected table, booking form/pricing/review/receipt/details, Activity and Schedule active/cancelled/overlap/completed states. Additional Overview/Providers/conflict captures retained. At 125% display scaling, 1440x900/1024x768 outer windows produce 1424x892/1008x760 DWM images excluding invisible resize margins. Final rendered layouts inspected; no image generation/editing or uploads. Ignored local audit inventories: .audit-tools/S7C/before-hashes.csv and screenshots.csv.

**Protection:** main and origin/main remain b8417e91960c3e632c16b2a3e1b14763445c5382. Academic ZIP remains 1,372,171 bytes, SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. No staging/commit/merge/push or external action. Existing Git user-ignore permission warning persists.

**Limits/next boundary:** Compact forms, histories and later grid hours scroll vertically; supported-size checks found no horizontal overflow. High overlap density may ellipsize labels; full text remains in tooltip/details. Very dense/custom catalogs, unusually short services, exhaustive assistive-technology support, DST-transition UI rendering and non-Windows graphics remain unverified. Existing domain DST coverage passes. Cancelled visibility resets on re-entering Schedule. Data/receipts/logs remain process-local/unbounded; integrations simulated, operations synchronous, availability exact-slot only. No known unresolved tested functional failure. User review is next. **Stop after S7C; no later work or publication without explicit authorization.**

**Final handoff check:** git diff --check exit 0; separate changed untracked source/tooling/report trailing-whitespace check exit 0. Staged diff empty; S7C PNG ignore confirmed. All 27 exact screenshot paths verified present in S7C.md. Final source/script modification times precede the packaged JAR; only closing documentation changed afterward. Harness windows are closed. No later slice started.

## October 27, 2026 - Approved S7C feature-branch publication handoff

**Inspection/reconciliation:** Read AGENTS.md, PROJECT_MEMORY.md, latest history, S7A/S7B/S7C review evidence and relevant architecture/requirements/pattern documentation. Inspected actual tracked diffs, untracked inventory, shared composition/controller and desktop source/tests/scripts. Active branch feature/javafx-ui, initially at b8417e91960c3e632c16b2a3e1b14763445c5382 with an empty index. Reviewed 37 changed/new files: 13 tracked modifications and 24 additions. These are the authorized JavaFX dependency/tooling, Main shared composition, ApplicationContext/DesktopMain/desktop seed, presentation views/controller/CSS, focused tests/harnesses, desktop runner/capture scripts, ignore rule and S7A-S7C/publication documentation. No unrelated payload found. Protected domain/application/existing infrastructure/console/original behavior tests and console QA scripts have no tracked diff against main. No production/test/script changes during publication preparation; only current handoff/documentation authorization was reconciled. Architecture's current header and S7C addendum now distinguish the duration-scaled grid from the historical S7B rows.

**Repository/preflight:** Existing origin is https://github.com/arslanhakim/AppointEase-Design-Patterns-Hamza.git. GitHub metadata verifies PRIVATE, default branch main and the expected academic project description. Initial git ls-remote --heads origin main feature/javafx-ui found only main at b8417e91960c3e632c16b2a3e1b14763445c5382; the feature branch does not yet exist. Local main/origin/main match that SHA. Repository-local commit identity remains the existing authenticated account login and GitHub no-reply address; global identity unchanged. No credential printed or requested. Existing sandbox user-ignore warning persists.

**Payload/privacy:** Full intended tree has 149 files: 143 text files and six preexisting academic UML PNGs. Targeted scan found zero private keys, cloud/service-token signatures, credential URLs, assigned secrets, personal profile paths or forbidden artifact paths. This is a targeted scan, not an exhaustive security certification. Existing CSS and UML are appropriate assets. Excluded/preserved locally: all 43 UI PNGs (3 S7A, 13 S7B, 27 S7C), target/ build output and QA transcripts, .audit-tools/ local helpers/inventories, .submission-check/ extraction, and AppointEase_DesignPatterns_Submission.zip. Existing ignore rules cover credentials, environment files, IDE/temp/log files and archives; no ignored file will be force-added. Screenshot ignore probes passed for all three slices. No screenshot was regenerated, removed or uploaded. Local release scanner is .audit-tools/review-feature-payload.ps1 and is excluded.

**Fresh executed QA before staging/commit:**
- java -version and mvn -version: exit 0, Temurin 21.0.12.1 and Maven 3.9.16.
- mvn clean verify: **exit 0, BUILD SUCCESS, October 27, 2026 at 15:06:32 +05:00; 289 tests, 0 failures, 0 errors, 0 skipped**. Independent Surefire XML sum agrees. All five architecture checks pass; 90 production and 17 test sources compiled. Original tests preserved.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C: **exit 0, 77 actual-window assertions**, including packaged DesktopMain launch, real booking/review/receipt/validation/conflicts/lifecycle, provider/filter/date navigation, proportional grid/overlap/status, keyboard traversal/scroll/Enter and six views at 1440x900/1024x768. No capture mode, so the historical extra capture-visibility assertion is not counted here.
- powershell -NoProfile -File scripts/run-desktop.ps1 -Verify: **exit 0, 13 retained Overview assertions**. Both harnesses close their windows and use cached local JavaFX modules offline.
- scripts/demo.ps1 and scripts/verify-console.ps1: **both exit 0; all nine packaged java -jar child processes exit 0**, covering workflow/lifecycle/conflict/history, malformed input, EOF and UTC/Karachi. Demo date October 28. Transcripts remain in target and are excluded.

**Protected artifact:** Academic ZIP remains 1,372,171 bytes with SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. Main is preserved at the original SHA. No archive regeneration, source feature, merge or new dependency occurred in this publication pass.

**Pre-commit handoff and final gate:** This memory/history entry is deliberately written BEFORE the commit. It does not fabricate its own eventual commit SHA or a future push result. Next execute the explicit 37-file allowlist, inspect the exact staged names/content/whitespace, rescan the index, commit with `Add polished JavaFX appointment management interface`, and push only feature/javafx-ui without force. Compare local HEAD, local tracking ref, advertised remote feature SHA and GitHub branch API; also reconfirm remote/local main and ZIP unchanged. Report final commit/push/SHA evidence externally in the publication response and excluded local evidence, without creating an extra commit just to record its own identity. If a gate fails, do not claim publication success.

**Boundary/limits:** S7C visual approval is complete. Existing documented process-local/simulated/synchronous, compact vertical scrolling, dense-label, accessibility/DST-graphics/non-Windows limitations remain. Fresh tests reveal no failing verified workflow. After verified feature publication, stop. No main merge, ZIP update, additional feature or subsequent publication without new authorization.

## October 27, 2026 - Sidebar toggle refinement, local only

**Publication reconciliation:** The preceding entry was deliberately written before publication. That authorized publication subsequently completed: commit `400b1b7ec325c5fc5654e1ef0d400306fc9563f7`, message `Add polished JavaFX appointment management interface`, push exit 0. At 15:29 +05:00, the advertised remote feature SHA and GitHub branch API matched local HEAD; main remained `b8417e91960c3e632c16b2a3e1b14763445c5382`, and the ZIP hash/size were unchanged. Evidence remains in ignored .audit-tools/feature-publication-result.json. The latest sidebar request started from that clean published feature branch, not the older pre-commit snapshot.

**Scope:** Focused sidebar refinement on feature/javafx-ui, with actual Windows checks and before/after screenshots. DesktopShell, DesktopMain, views/CSS, runner/capture scripts and tests were inspected. No business logic, main or academic ZIP change was made.

**Implemented files and decisions:**
- DesktopShell.java retains the sidebar and all six native navigation buttons in expanded 214-pixel or narrow 64-pixel modes. Expanded buttons show icon/text; rail buttons show icons and retain tooltips/accessibility. Current destination has a terracotta edge; focus is visibly distinct. Compact destination selection restores the rail. Toolbar Menu text is replaced by a native 36-pixel icon button with collapse/expand glyphs, matching accessible text/help and tooltip. No page is recreated when toggling, preserving draft/review/selection/lifecycle confirmation and scroll state.
- New NavigationIcon.java contains local SVGPath stroke geometry for six navigation icons and the two toggle states. theme.css uses the existing palette and styling architecture, flat hover/selected/focus treatments, and shadow-free navigation tooltips. No emoji, icon font or external dependency.
- A 140 ms glyph-opacity FadeTransition with spline (0.23, 1, 0.32, 1) acknowledges pointer activation; interrupted feedback uses current opacity. Layout width changes only once, without animation. Keyboard/reduced-motion activation is immediate. scripts/run-desktop.ps1 samples the Windows client-area animation preference at startup through the built-in PresentationFramework assembly, supports explicit -ReduceMotion and defaults to reduced motion if detection fails. Direct launches also default to no motion. No system preference is changed.
- New opt-in DesktopSidebarCheck.java exercises the packaged application with real Robot input and native controls. DesktopVisualCheck.java/DesktopS7BCheck.java only replace the previous fully-hidden-sidebar assumptions with the requested rail behavior. Existing Surefire tests unchanged. scripts/capture-ui.ps1 adds the sidebar-toggle capture destination. AGENTS.md, README.md, PROJECT_MEMORY.md, docs/testing-strategy.md and docs/ui-review/sidebar-toggle.md reflect the current boundary and results.

**Intermediate failures and corrections:** Initial Robot checks failed to activate the toggle because the test window was not reliably in front; merely separating mouse move/press/release was insufficient. Explicit window positioning and visibility fixed the noncapture run. The first capture run exposed the capture helper releasing its native topmost flag; the harness now restores its foreground/topmost state after capture and waits for native input delivery. These corrections are confined to test setup. An early Maven output was lost across context, so verification was rerun with a local log instead of inferring a successful exit. First successful capture inspection found the default JavaFX tooltip shadow; a scoped navigation-tooltip CSS rule removed it. All final checks below follow those corrections. No assertion was removed or failure bypassed.

**Final executed verification:**
- java -version and mvn -version: exit 0, Temurin 21.0.12.1 and Maven 3.9.16.
- mvn clean verify: **exit 0, BUILD SUCCESS, October 27, 2026 at 15:52:03 +05:00; 289 tests, zero failures/errors/skips**. Independent Surefire XML sum agrees; all five architecture checks pass. 91 production and 18 test sources compiled. Existing 289 JUnit tests preserved unchanged. Local build transcript: .audit-tools/sidebar-maven.log.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -Capture: **exit 0, 86 actual-window assertions**, reducedMotion=false. Covers collapse/expand glyph/label, actual pointer activation, eight repeated toggles, settled opacity, same content instance, focus, Space/Enter, six rail destinations, actual tooltip display, keyboard rail navigation, preserved table search/selection, pending lifecycle confirmation, draft customer/time/optional service, reviewed booking and Activity scroll. All six views checked in both modes at physical 1440x900 and 1024x768, including button widths and all navigation bounds. Seven after captures saved.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -ReduceMotion: **exit 0, 86 assertions**, reducedMotion=true, including immediate pointer activation. No OS setting was changed.
- Retained -VerifyS7C: **exit 0, 77 actual-window assertions**; full booking/review/receipt/conflict/lifecycle/provider/filter/date/grid/status/keyboard/both-size coverage. Retained -Verify: **exit 0, 13 Overview assertions**. All GUI runs use the packaged app and cached JavaFX modules offline; windows close afterward.
- scripts/demo.ps1 and scripts/verify-console.ps1 after final build: **both exit 0; all nine packaged JAR child processes exit 0**, including workflow, errors, EOF and UTC/Karachi. Transcripts remain in target.

**Screenshots:** **11 raw local PNGs** in `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\`. Four before captures used the unchanged published JAR before production rebuild, via ignored .audit-tools/SidebarBefore.java and capture-sidebar-before.ps1; seven after captures used the final JAR. Exact paths, pixels and bytes are in [sidebar review](ui-review/sidebar-toggle.md). All nonempty, valid PNG signatures and successful PNG decode. Actual expanded/rail/tooltip/draft views visually inspected. At 125% Windows scaling, requested outer 1440x900/1024x768 yield 1424x892/1008x760 DWM images. No image generation, editing or upload. Prior S7A-S7C captures preserved; all screenshots/build/audit files remain ignored.

**Protection and boundary:** HEAD and origin/feature/javafx-ui remain `400b1b7ec325c5fc5654e1ef0d400306fc9563f7`; main and origin/main remain `b8417e91960c3e632c16b2a3e1b14763445c5382`. Academic ZIP remains 1,372,171 bytes, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. No domain/application/infrastructure/console/Main/DesktopMain/controller/dependency or existing JUnit-source changes. No staging, commit, push, merge or archive regeneration in this refinement. Final closure changes are documentation only. Stop here for user review; do not publish or start another feature without new authorization.

**Known limits:** Compact/lower content scrolls vertically; supported-size checks found no horizontal overflow or clipped controls. Normal content reflow occurs once when available width changes. The animation preference is read only at startup; changes require restart. Motion feel can be reviewed in the live runner. Exhaustive screen-reader certification and non-Windows graphics remain unverified, and previous process-local/simulated/synchronous/data-volume limits remain. No unresolved failure in the final executed checks. Existing sandbox user-ignore warning remains unrelated.

**Final closure checks:** git diff --check and new-file whitespace checks exit 0; all 11 exact screenshot paths are present in the review inventory. Index is empty. Source and script modification times precede the final packaged JAR. No AppointEase window remains. Branch, protected refs and ZIP hash/size reconfirmed unchanged. Only the 14 intended source/tooling/documentation files are modified or new; ignored screenshots and local helpers remain excluded.

## October 27, 2026 - Final sidebar toolbar icon correction

**Scope:** Sidebar icon correction used a recognizable panel-left-close/open toolbar icon, approximately 18-20px with thin vector strokes, preserving hover/focus/accessibility. Regression verification included four real screenshots. Prior unstaged work on feature/javafx-ui was preserved. Initial source/script hashes are stored locally in ignored .audit-tools/sidebar-icon-final-before.csv.

**Correction:** NavigationIcon.java replaces the cramped chevron with a full directional arrow beside a narrower left panel: leftward for expanded/Collapse sidebar, rightward for collapsed/Expand sidebar. Rounded vector path bounds plus the existing 1.5px stroke occupy 19.5px. theme.css adds one toggle-scoped Ink stroke rule for legibility. The 22px holder, 36px native button, transparent rest, subtle hover, sage focus, accessible names/tooltips, motion and approved sidebar behavior are unchanged. No dependency, navigation-icon, DesktopShell, form, controller or business-rule change.

**Capture/check tooling:** run-desktop.ps1 adds -CaptureIconFinal, which runs the full existing sidebar journey but saves only the four requested states. capture-ui.ps1 accepts sidebar-icon-final and reports coordinates/handles when rejecting an obscured capture. DesktopSidebarCheck selects these four names without overwriting earlier captures. Its native-input setup now checks actual foreground focus, clicks the test window title bar if inactive and separates key press/release by a short pulse. Existing assertions remain intact; no production event handling changed.

**Intermediate failures:** First capture attempt failed pointer collapse; a noncapture run passed pointer toggles and Space but failed Enter. Two later captures were rejected as obscured, with no new screenshot saved. The Windows session was active; these exposed native foreground/input delivery instability. The harness focus/key-timing setup was corrected, and capture rejection remained enforced. A subsequent complete capture run and reduced-motion run each passed all 86 assertions. No failed run is counted as passed, and no input assertion or capture guard was bypassed. Screenshot files from incomplete attempts were replaced by the successful full run.

**Final actual verification:**
- java -version and mvn -version: exit 0, Temurin 21.0.12.1 and Maven 3.9.16.
- mvn clean verify: **exit 0, BUILD SUCCESS, 16:11:23 +05:00 October 27, 2026; 289 tests, 0 failures/errors/skips**, independently summed from Surefire XML. All five architecture checks pass; 91 production/18 test sources. All existing JUnit sources preserved. No compiled source or CSS changed afterward; the later capture-script change only improves rejection diagnostics.
- powershell -NoProfile -File scripts/run-desktop.ps1 -CaptureIconFinal: **exit 0, 86 actual-window assertions**, motion enabled, four screenshots. Covers both glyphs and accessible action descriptions, real clicks, eight repeated toggles, keyboard Space/Enter, focus, tooltips, all rail destinations, state preservation and six views at both sizes in both sidebar modes.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -ReduceMotion: **exit 0, 86 assertions**, motion disabled.
- Retained -VerifyS7C: **exit 0, 77 assertions**; retained -Verify: **exit 0, 13 assertions**. Actual packaged JavaFX runtime and application services; all harness windows close.
- scripts/demo.ps1 and scripts/verify-console.ps1: **both exit 0, nine packaged JAR child processes exit 0**, after the final build. Runtime uses cached dependencies offline.
- All four screenshots are nonempty, have valid PNG signatures and decode successfully. All four were opened and visually inspected; arrows visibly reverse with state, and focus/layout remain clear. No generated or edited images, uploads or prior-capture deletion.

**Exact screenshots:** Requested outer 1440x900 and 1024x768; saved DWM pixels exclude invisible resize margins at 125% Windows scaling.

| Path | Saved pixels | Bytes |
| --- | --- | --- |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-icon-final\01-desktop-expanded.png` | 1424x892 | 73490 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-icon-final\02-desktop-collapsed.png` | 1424x892 | 67187 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-icon-final\03-compact-collapsed.png` | 1008x760 | 47686 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-icon-final\04-compact-expanded.png` | 1008x760 | 52574 |

**Protection/boundary:** Start/end source/script hash comparison identifies only NavigationIcon.java, theme.css, DesktopSidebarCheck.java, run-desktop.ps1 and capture-ui.ps1 as changed in this correction; all other source/scripts, including DesktopShell and existing tests, are preserved. PROJECT_MEMORY.md and this history updated. HEAD remains 400b1b7ec325c5fc5654e1ef0d400306fc9563f7; main/origin/main remain b8417e91960c3e632c16b2a3e1b14763445c5382. ZIP remains 1,372,171 bytes, SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. Index empty; git diff --check passed; screenshots remain ignored. No staging, commit, push, merge or ZIP regeneration. Stop after this single correction. No unresolved final verified failure; Windows-only/accessibility-certification, compact vertical scrolling and startup-only motion-preference limits from the prior handoff remain.

## October 27, 2026 - Final sidebar refinement publication preparation

**Reviewed 14-file publication allowlist:**
- Implementation/assets: src/main/java/com/appointease/presentation/desktop/DesktopShell.java, src/main/java/com/appointease/presentation/desktop/NavigationIcon.java, src/main/resources/com/appointease/presentation/desktop/theme.css.
- GUI checks: src/test/java/com/appointease/presentation/desktop/DesktopSidebarCheck.java, DesktopS7BCheck.java and DesktopVisualCheck.java in that same directory.
- Tooling: scripts/run-desktop.ps1, scripts/capture-ui.ps1.
- Documentation: AGENTS.md, PROJECT_MEMORY.md, README.md, docs/SESSION_HISTORY.md, docs/testing-strategy.md, docs/ui-review/sidebar-toggle.md.

These are the approved persistent icon rail, state-preserving accessible toggle, preference-aware glyph feedback, corrected directional vectors, associated verification/capture tooling and documentation. No domain/application/infrastructure/console/controller/composition/POM/existing JUnit-source changes. No production, test or script edits were made during this publication pass. Documentation reconciles visual approval and the new publication authorization, preserving historical local-only outcomes.

**Fresh executed QA:**
- java -version: exit 0, Temurin 21.0.12.1. mvn -version: exit 0, Maven 3.9.16.
- mvn clean verify: **exit 0, BUILD SUCCESS at 16:22:53 +05:00; 289 tests, 0 failures, 0 errors, 0 skipped**. Independently summed Surefire XML agrees. All five architecture checks pass; 91 production/18 test sources compiled.
- Motion-enabled original compiled sidebar harness: **exit 0, 86 actual-window assertions**. Direct invocation: `java -Dappointease.reduceMotion=false --module-path <cached JavaFX base/graphics/controls win JARs> --add-modules javafx.controls -cp 'target/test-classes;target/appointease-0.1.0-SNAPSHOT.jar' com.appointease.presentation.desktop.DesktopSidebarCheck`. This uses the exact unchanged test class built by Maven and the packaged application, with no diagnostic class on its classpath.
- powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -ReduceMotion: **exit 0, 86 assertions**, reducedMotion=true. Both sidebar runs cover native clicks, repeated toggles, Space/Enter, focus, actual tooltip, accessibility labels, all six destinations, preserved draft/review/selection/confirmation/scroll and both modes at 1440x900 and 1024x768.
- Final powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C: **exit 0, 77 assertions**. Final -Verify: **exit 0, 13 Overview assertions**. Packaged JavaFX and cached modules run offline; harness windows close.
- scripts/demo.ps1 and scripts/verify-console.ps1: **both exit 0; all nine packaged java -jar child processes exit 0**, including workflow/lifecycle/conflict/history, malformed input, explicit exit, EOF, UTC and Karachi. Demo date October 28; transcripts remain ignored in target.

**Failed attempts and limitations:** Two initial motion-enabled sidebar script launches and the initial reduced-motion launch exited 1 at Pointer collapse retains a navigation rail. A later motion-enabled script launch passed the initial click but exited 1 at Eight rapid pointer toggles return to the intended rail state. The first S7C launch exited 1 at Keyboard Tab traverses appointment blocks; its unchanged final rerun passed. Windows session inspection reported an active console; that query command itself returned exit 1. An ignored diagnostic copy logged actual mouse targets/coordinates and passed all 86 assertions, exit 0. Its initial compilation was blocked by sandbox JavaFX JAR access, exit 1; the escalated diagnostic compile/run succeeded. The original compiled motion-enabled harness and reduced-motion script then passed as recorded above. No production or release-harness change, removed assertion, retry inside a harness, synthetic action substitution or capture-guard bypass was introduced. These results support working interactions but do not establish reliable unattended native input on every launch; exact cause remains unconfirmed. Existing startup-only animation preference, non-Windows/screen-reader certification and process-local/simulated/synchronous limitations remain.

**Privacy/artifacts and protected state:** Candidate payload scanner reviewed 152 files (146 text, six unchanged academic UML PNGs), with zero targeted credential/private-key/token/credential-URL/assigned-secret/personal-profile-path/unwanted-artifact findings. It reports finding locations only, not potential secret values. Local screenshots, target, .audit-tools diagnostics, extracted submission checks and ZIP remain ignored; no captures were regenerated or uploaded. The initial sandbox ls-remote attempt failed network access; the escalated read succeeded. Local main, tracking main and advertised remote main remain b8417e91960c3e632c16b2a3e1b14763445c5382. Academic ZIP remains 1,372,171 bytes, SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. Existing sandbox user-ignore warning is unrelated; repository ignore rules and explicit allowlist are checked.

## October 27, 2026 - Repository hygiene and attribution audit

**Scope:** Repository cleanup and feature-branch publication only, using commit message `Clean repository tooling artifacts and project metadata`. No application feature, business-rule, pattern, source, test, CSS, Maven dependency, UML, main-branch or academic ZIP change.

**Baseline and attribution:** Started with a clean feature/javafx-ui at 7de416eb1de201671d1bcec0bbd9fac44268500d, matching origin and GitHub. This confirms the preceding sidebar publication completed. Local/tracking/GitHub main matched b8417e91960c3e632c16b2a3e1b14763445c5382. Repository remains private with main as default branch and its existing academic description. All three existing commits were inspected, including full messages, authors and committers: arslanhakim <93035770+arslanhakim@users.noreply.github.com>. No Co-authored-by trailers, agent signatures or automated session metadata were found in commit messages. The configured Git identity is unchanged; no history rewriting or force-push is involved.

**Cleanup inventory and reasons:**
- Removed AGENTS.md: coding-agent execution and approval instructions, with no build/runtime requirement.
- Removed PROJECT_MEMORY.md after consolidating current technical behavior, release SHAs, engineering evidence and limitations into docs/project-status.md and the existing technical documentation.
- Renamed docs/SESSION_HISTORY.md to docs/engineering-history.md. Removed conversation interruptions, editing anecdotes, skill-use notes and redundant approval/execution instructions. Preserved implementation decisions, test counts, commands, failed checks, limitations and historical archive/file inventories. Historical filenames are retained for traceability, not as current dependencies.
- Updated README and documentation references. Preserved all Java sources/tests, JavaFX CSS, POM, six UML diagram sets, academic documentation and application scripts.
- Updated scripts/package-submission.ps1 to use the technical documentation, exclude local screenshots and agent configuration, and include the required JavaFX CSS. Evaluated its manifest/validation prefix without opening an archive: 151 existing files, exactly matching the intended source payload. No ZIP was created or regenerated.
- No active .claude, CLAUDE.md, .codex, .cursor or .aider artifacts were present. Ignored .audit-tools and .submission-check contain historical AGENTS/memory/history copies and local helpers; these were retained along with screenshots, build outputs and the protected ZIP. Existing ignored files were not purged.

**Ignore additions:** .claude/, .cursor/, .aider*, .continue/, .windsurf/, .gemini/, AGENTS.md, AGENTS.override.md, CLAUDE.md, GEMINI.md, .cursorrules, .cursorignore, .windsurfrules, .github/copilot-instructions.md, .github/instructions/, .github/prompts/, /PROJECT_MEMORY.md, /docs/SESSION_HISTORY.md, .cache/, .history/, *.session.log and *.session.jsonl. Existing .codex/, .agents/, audit/build/temp/log/ZIP/screenshot exclusions remain. All 23 new ignore probes passed using --no-index so tracked deletions did not mask the rules.

**Fresh executed verification:**

| Command/check | Actual result |
| --- | --- |
| java -version | Exit 0; Temurin 21.0.12.1 |
| mvn -version | Exit 0; Maven 3.9.16 |
| mvn clean verify | Exit 0; BUILD SUCCESS at 17:11:57 +05:00; 289 tests, 0 failures/errors/skips; independent Surefire XML sum agrees; five architecture checks pass |
| powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar | Exit 0; 86 assertions; reducedMotion=false |
| powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -ReduceMotion | Exit 0; 86 assertions; reducedMotion=true |
| powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C | Exit 0; 77 assertions |
| powershell -NoProfile -File scripts/run-desktop.ps1 -Verify | Exit 0; 13 assertions |
| powershell -NoProfile -File scripts/demo.ps1 | Exit 0; three packaged java -jar processes exit 0 |
| powershell -NoProfile -File scripts/verify-console.ps1 | Exit 0; six packaged java -jar processes exit 0 |
| Package script syntax and manifest | Passed; all 151 source payload files exist; CSS included; agent files and captures excluded; no archive written |
| Documentation and historical evidence | Local Markdown links and whitespace checked; concrete historical verification lines retained, apart from documented removal of editing anecdotes |
| Staged privacy/artifact scan | 151 files: 145 text, six existing UML PNGs; zero targeted credential/private-key/token/credential-URL/assigned-secret/personal-profile-path/unwanted-artifact findings |

All four Windows harness launches passed on their first attempt in this audit and closed their windows. They cover the actual packaged application with cached JavaFX modules, including booking/lifecycle/conflicts, keyboard interactions, state preservation and supported window sizes. Prior native-input reliability and non-Windows/accessibility limitations remain historical limitations, not disproved by this run. Runtime remains offline. Network inspection and Git index writes initially encountered sandbox access restrictions; permitted retries succeeded. No application check failed or was weakened.

**Protected artifacts and publication record:** Main remains b8417e91960c3e632c16b2a3e1b14763445c5382. The academic ZIP remains 1,372,171 bytes with SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A. This entry records completed pre-commit checks; the resulting cleanup SHA and push/local/tracking/GitHub equality are recorded after execution in the release report and ignored .audit-tools/hygiene-publication-result.json. Earlier commits, historical document references and ignored snapshot copies retain their original tooling metadata; no published history or academic archive was rewritten.

## 2026-10-27 - S8A: in-session customer, provider and service management

**Preparation and scope:** Confirmed a clean `feature/javafx-ui` working tree before changes; read README, architecture, engineering history, pattern matrix and current status/testing documentation. Inspected BookingFacade/assembler, immutable domain values, repository lock/commands, composition, JavaFX screens and existing tests. No current AGENTS.md or deleted agent-specific handoff file was assumed or restored. Work remained on the feature branch. The academic archive and main were not modified.

**Capabilities:** Customers supports list/search, Add/Edit of its existing name and per-customer appointment history. A compact New Appointment action uses the shared customer form, preserves the complete booking draft and selects the new customer immediately; success is visible above the form. Providers gains create/edit, category, supported services, fixed Online/In-Person modes, seven-day hours including split shifts/closed days, preparation buffer and enabled state. Services gains list/search, create/edit of name/category/type/duration/base fee and provider relationship editing through the same provider form. New records are available without restart. All data remains process-local.

**Architecture decisions:** Extended the existing facade/repository rather than creating a parallel catalog service/store. One ReentrantLock protects catalog and appointment mutations/reads; immutable record snapshots and typed IDs remain. Create checks duplicate IDs and provider references. Update compares the expected record and stable ID under the lock, rejecting stale/missing records. Pending/confirmed appointments prevent incompatible provider support/mode/enabled/hour changes; validation covers their stored preparation intervals. Buffer changes affect future bookings/reschedules, not stored reservations. Service/customer edits do not replace appointment/event snapshots or reprice/reschedule them. Reviewed desktop bookings compare the agreed catalog values before reserve's atomic catalog check. Overview/controller now read the live customer catalog. All nine GoF roles and layer boundaries remain intact; no dependencies or network/database service were introduced.

Reschedule preserves original immutable appointment details and State validation, and additionally checks current provider configuration under the lock. Strategy is evaluated once against the current provider. The conservative boundary is explicit: the new interval must fit original snapshot hours as well as current hours. Expanding a provider's hours benefits new bookings but does not expand an old appointment's reschedule range. Catalog changes produce no appointment lifecycle events. Exact policies are in [architecture](architecture.md).

**Changed files:** Paths under the Java rows are relative to `src/main/java/com/appointease/` unless stated otherwise.

| Area | Files |
| --- | --- |
| Application | `application/BookingFacade.java`, `application/port/AppointmentRepository.java` |
| Domain | `domain/model/Service.java`, `domain/model/ServiceProvider.java`, `domain/scheduling/ReservationRules.java` |
| Infrastructure | `infrastructure/memory/InMemoryAppointmentRepository.java`, `infrastructure/seed/DemoCatalog.java` |
| New presentation | `presentation/desktop/CatalogForms.java`, `CustomersView.java`, `ServicesView.java` |
| Updated presentation | `presentation/desktop/BookingView.java`, `DesktopController.java`, `DesktopShell.java`, `NavigationIcon.java`, `OverviewModel.java`, `ProvidersView.java` |
| Styling | `src/main/resources/com/appointease/presentation/desktop/theme.css` |
| New tests | `src/test/java/com/appointease/domain/CatalogManagementTest.java`, `src/test/java/com/appointease/presentation/desktop/DesktopS8ACheck.java` |
| Runners | `scripts/run-desktop.ps1`, `scripts/capture-ui.ps1` |
| Documentation | `README.md`, `docs/architecture.md`, `docs/design-pattern-matrix.md`, `docs/project-status.md`, `docs/testing-strategy.md`, `docs/engineering-history.md`, new `docs/ui-review/S8A.md` |

All existing test sources, POM, console implementation, original State/factory/decorator/observer/adapter classes and UML artifacts are unchanged. Local PNGs are additional ignored artifacts, indexed in [S8A review](ui-review/S8A.md).

**Executed verification:**

| Command/check | Result |
| --- | --- |
| Final `mvn clean verify` | Exit 0, BUILD SUCCESS at **19:21:12 +05:00**; **312 tests, 0 failures/errors/skips**; 94 production and 20 test sources compiled |
| Retained test baseline | All original **289 invocations unchanged**; all five architecture checks pass |
| `CatalogManagementTest` | **23 passing invocations**: validation, create/edit/lookup, new-record booking, live selection/projections, immutable service/customer/provider/event/fee snapshots, stale edits/reviews/candidates, provider constraints, disabled/missing records, current buffer and exactly-one Strategy evaluation, concurrent creates/edits and 20 provider-disable/reservation races |
| `run-desktop.ps1 -VerifyS8A -Capture` | Exit 0; **40 actual-window assertions** on final JAR, including visible success without scrolling; **16 raw Windows PNGs** |
| `run-desktop.ps1 -VerifyS7C` | Exit 0; **85 assertions**, including existing booking/lifecycle/grid/keyboard flows and both-size layout checks extended automatically to eight destinations |
| `run-desktop.ps1 -VerifySidebar` | Exit 0 on unchanged rerun; **106 assertions**, motion enabled |
| `run-desktop.ps1 -VerifySidebar -ReduceMotion` | Exit 0; **106 assertions**, motion disabled |
| `run-desktop.ps1 -Verify` | Exit 0; **13 Overview assertions**, final JAR |
| `scripts/demo.ps1` and `scripts/verify-console.ps1` | Both exit 0 on final JAR; **nine successful JAR children**; workflow, invalid input, explicit exit, EOF and UTC/Karachi cases |

S7C and both sidebar runs preceded only the final two-line repositioning of inline customer success feedback; that final branch was exercised by the final S8A visibility assertion. No original test/assertion was removed or weakened. The old harness messages still say "six" in a few labels, but their Page.values/lookup loops inspected all eight destinations. Surefire reports and regenerated console transcripts are under `target/`.

**Intermediate results and corrections:** The first sandbox clean build failed before compilation because Maven could not access dependencies; the approved run then found an existing AppointEase Activity window holding the JAR. After identifying and closing that exact window, the first full build passed 309 tests. Three additional tests increased the total to 312; two initially used `UTC` in the assembler against repository zone `Z`, correctly failing exact-zone validation. Correcting the fixture identifiers produced 312 passing tests; no production timezone rule changed. Screenshot review moved the customer action into the field heading to keep the normal desktop Review action visible, then moved inline creation success above the form. The first sidebar run failed at native tooltip hover; the unchanged rerun passed all 106 checks, and reduced-motion checks passed. Native input remains timing-sensitive. No failing result is omitted or reclassified as a pass.

**Screenshots and visual review:** `docs/ui-review/screenshots/S8A/` contains customer list/new/success, provider management/new, service management/new, booking with newly created records, inline success, compact list/form/booking views and the scrolled weekly-hours/save section. Captures are unmodified Windows DWM pixels. Requested 1440x900/1024x768 outer windows yield 1424x892/1008x760 PNGs at 125% scaling. Actual screens were decoded and visually inspected; horizontal management action bounds pass at both sizes. Long forms and compact tables intentionally scroll vertically. Screenshot 09 confirms the visible success banner with the selected new customer and retained options. No generated mockups or image edits were used.

**Limitations and exact next boundary:** Customer details are name-only, matching the existing domain. Supported modes are provider-wide; no service-specific mode override, arbitrary mode family, account/role system, catalog audit stream, deletion, persistence, import/export, automatic slot search or week/month scheduling is included. UI duration/buffer fields use whole minutes; operations are synchronous and catalogs/logs process-local. Original/current-hour reschedule restriction is documented above. Other platforms, exhaustive assistive-technology support and very large catalogs remain unverified. S8A ends at these catalog commands, forms, tests, captures and documentation. Stop for user review. S8B requires a separate agreed specification; none of the excluded features is implicitly authorized. No commit, push, merge, submission repackaging or S8B work occurred.

**Protected artifacts:** Main remains `b8417e91960c3e632c16b2a3e1b14763445c5382`. The academic ZIP remains 1,372,171 bytes, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. The source changes remain unstaged on `feature/javafx-ui`; deleted AI-specific configuration files were not restored.

## 2026-10-27 — S8B: full-height Appointments and multi-view calendar

**Preparation and scope:** The user approved S8A visually and explicitly authorized S8B against its 312-test baseline. Inspected README, status, architecture, engineering history, S8A/S7C review evidence, actual shell/table/schedule/controller/facade/repository code and Windows harnesses. Confirmed `feature/javafx-ui` and preserved all existing S8A working changes. Main and the verified academic ZIP were recorded before work; no agent configuration was restored.

**Implementation:** Appointments now fills the workspace viewport, grows/shrinks with the real window, has a 150-logical-pixel minimum, and scrolls internally only when rows overflow. Native columns/rows, filters, keyboard/double-click details and lifecycle actions remain. Resize keeps the current view and selected record; explicit refresh restores selection by ID where still visible.

Schedule adds compact Day/Week/Month toggles sharing date, provider and cancelled state. CalendarModel is a pure projection of existing facade histories, using business-zone half-open boundaries and Monday-first ranges. There is no existing range API, so no parallel repository/store or business booking rules were introduced. Week has seven columns with elapsed-time placement, common planning hours, selected/today indication, minimum-width overlap lanes and horizontal scrolling above the canvas. Day retains the original proportional TimeGrid and now shares interval partitioning with Week. Month uses bounded non-cancelled/cancelled counts, complete leading/trailing weeks and exact-date navigation to Day. Its month/year heading and count-only cells keep the six-week desktop grid visible. S8B modifies presentation only; domain/application/infrastructure/console/POM and production fixtures remain as they were at S8B start.

**Changed files (S8B delta, separate from retained S8A):** New `presentation/desktop/CalendarModel.java`; updated `AppointmentsView.java`, `ScheduleView.java`, `DesktopShell.java`; theme.css; new `CalendarModelTest.java` and `DesktopS8BCheck.java`; run-desktop.ps1/capture-ui.ps1 S8B modes; README, architecture, project status, this history and new [S8B review](ui-review/S8B.md). The review contains exact paths, design comparisons, screenshot links and recommended S8C boundaries.

**Executed verification:**

| Check | Result |
| --- | --- |
| Final `mvn clean verify` | Exit 0, BUILD SUCCESS **20:11:03 +05:00**, **339 tests, 0 failures/errors/skips**; independent Surefire XML sum agrees |
| Baseline / architecture | All **312 original invocations** preserved; **27 CalendarModel invocations** added; all five architecture tests pass; no original test/harness source edited |
| `run-desktop.ps1 -VerifyS8B -Capture` | Exit 0; **80 actual-window assertions**, **12 native PNGs**, repeated real 1440×900/1024×768 resizes, small/empty/one-row/56-record table cases, preserved filters/selection, week geometry/overlap/scrolling/navigation, month counts/date selection and details |
| Retained `-VerifyS7C` | Exit 0; **85 assertions**, including all Day/keyboard/lifecycle behaviors and both-size viewport checks |
| Retained `-VerifyS8A` | Exit 0; **40 assertions** |
| Retained `-VerifySidebar` | Exit 0; **106 assertions**, motion enabled |
| Retained `-Verify` | Exit 0; **13 Overview assertions** |
| Console demo / standalone QA | Both scripts exit 0; all **nine packaged-JAR children** exit 0 |
| Screenshots | All 12 nonempty PNG signatures/decode checks pass; 1424×892 desktop and 1008×760 compact DWM pixels at 125% scaling; actual captures visually inspected |

S8B Windows fixtures compose the packaged production shell/controller/context, use real facade commands and never alter the production demo seed. Native Robot covers view/date clicks and keyboard details; deterministic control actions cover the remaining form/navigation checks. The actual stage is repeatedly resized, not just an offscreen layout. The retained harnesses launch actual DesktopMain. Final review extended per-day UTC-offset-axis detection to cover a Week planning origin normalized after a spring gap, with four additional regression invocations. S8B captures and S7C checks were repeated after that correction and final build. Supplemental S8A/sidebar/Overview checks preceded only that transition-axis refinement; their exercised ordinary dates and production paths are unchanged.

**Intermediate failures and refinements:** Initial new harness compilation called a nonexistent controller method; corrected to the existing act API. A table assertion incorrectly assumed physical desktop dimensions imply more than 320 logical pixels of table height; actual 125%-scaled available height was 302.4. Replaced that assumption with available-height geometry, minimum and repeated-resize assertions. Additional harness failures arose from cancelling the first randomly ordered appointment instead of an explicit provider's record, querying new ScrollPane children before CSS/layout, and selecting an alphabetical provider with no busy-day records. Fixtures now specify the intended provider and layout before inspection. Screenshot review replaced repeated Week axes with a shared axis, placed horizontal scrolling above the canvas, and removed verbose Month previews. All final checks above passed; existing assertions were neither removed nor weakened. A local edit attempt used unavailable Python and made no changes; subsequent edits used the patch tool.

**Limits and stop:** Process-local synchronous history scans, potentially wide dense weeks, ellipsized labels with full tooltip/details, compact vertical calendar scrolling and fresh Schedule state on re-entry remain explicit. Month-end navigation clamps via LocalDate.plusMonths. Tests exercise calendar DST boundaries, but real-window DST screenshots, full assistive-technology certification, non-Windows rendering and production-scale histories are not claimed. The recommended S8C scope is only a future navigation/accessibility/performance discussion, not work begun. No persistence, recurrence, drag/drop, automatic slot search or new integrations.

**Protected state:** Branch remains `feature/javafx-ui`; index is empty. Main remains `b8417e91960c3e632c16b2a3e1b14763445c5382`. Academic ZIP is still 1,372,171 bytes with SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. Captures remain local/ignored. No commit, push, merge, archive rewrite or S8C implementation.
