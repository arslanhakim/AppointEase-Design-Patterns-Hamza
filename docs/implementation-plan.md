# Implementation slices

S0 through S6.1 and S7A-S7C are complete. S7C passed visual review on October 27; the user authorized fresh release QA and publication of feature/javafx-ui using the specified commit message. Stop after feature-branch publication; no further slice, main merge or submission ZIP change is authorized. Historical boundaries below describe their original slice. See [S7C review](ui-review/S7C.md), [S7B review](ui-review/S7B.md), the historical [S7A screen map](ui-review/S7A.md) and [publication history](SESSION_HISTORY.md).

| Slice | Proposed scope | Acceptance criteria / exit gate |
| --- | --- | --- |
| S0 — foundation (Oct 26) | Maven, Java 21, Main, smoke test, requirements/architecture/pattern/UML/test plans. | Clean verify succeeds, Jupiter discovers test, runnable jar prints welcome and exits 0; no booking logic. |
| S1 — domain values and lifecycle (implemented Oct 26) | Typed IDs, Customer/ServiceProvider/Service, TimeSlot, money/options/status/mode/kind/category values, working periods, Clock/zone rules, immutable Appointment and State classes. Public fully validated construction seam, to replace with S2 creator/spec path. AppointmentEvent is deferred to S4 when event delivery is implemented. | Unit tests cover half-open overlap, duration, fees, compatibility, working hours, local-time/DST rules, every state/operation pair and exact time guards. Domain imports only domain/JDK. No repositories, facade, other patterns or menus. |
| S2 - creation and add-ons (implemented Oct 26) | Factory Method, Abstract Factory, Builder and Decorator, integrated with S1 aggregate; replace direct Appointment construction with validated spec and abstract aggregate/subtypes, retaining immutable lifecycle semantics. | Both appointment kinds and modes created via validated spec; incompatible families/required fields rejected; base/priority/reminder/combinations quote correctly; four patterns have behavioral tests; S1 invariants and lifecycle tests still pass after construction fixtures migrate. |
| S3 — scheduling and atomic persistence (implemented Oct 26) | Strategies, hard rules, application repository contracts, thread-safe in-memory implementation. | Atomic reserve; two conflicting threads yield one winner; duplicate IDs rejected; adjacency/history/buffers correct; failing reschedule preserves original; reads immutable. |
| S4 — use cases and integrations (implemented Oct 26) | BookingFacade, Observer, ordered dispatcher, CalendarPort/Adapter/simulated incompatible client. | Full lifecycle through facade; success-only ordered events; failing observer isolation and mapped calendar warnings; idempotent fake sync; no external network. All nine patterns now executable. |
| S5 — console and seed demo (implemented Oct 26) | Input/output adapters, menus, deterministic seed catalog, composition root, injectable streams and configured zone. | Browse/book/confirm/reschedule/cancel/complete/history through scripted sessions; malformed input recoverable, EOF exits; demo includes both modes/types/add-ons/policies and all categories. |
| S6 — submission verification (completed Oct 27; deadline Oct 30) | Regression/concurrency/architecture checks, reconcile UML, evidence, demo transcript and documentation. | Clean build/tests and jar run; every requirement/pattern linked to real code/tests/UML, no planned item falsely claimed complete; offline runtime demo and safe failure cases verified. |

Historical S3 boundary (superseded by authorized S4): retain the documented future-only confirmation/cancellation policy, elapsed duration across DST, implemented whole-store locking, and in-memory delivery limitations unless explicitly revised. S1 uses immutable transition results to support S3 atomic replacement. S3 implements repositories and global overlap reservation; observer dispatch, calendar integration and menus remain outside this slice.

Historical S2/S3 exit evidence (superseded by S4 below): 156 tests passed (119 original + 37 new), Maven verify and welcome jar exit 0. Public validated spec/subtype constructors refine the planned restricted constructor API; see architecture.md. Historical S2 boundary superseded by explicit S3 authorization. S3 exit evidence: 197 tests (156 retained + 41 scheduling), clean verify and welcome jar exit 0. Six patterns implemented. Next proposed S4: events/revisions committed with repository writes, ordered synchronous dispatch, Observer, application BookingFacade and CalendarPort/Adapter with simulated client. No S4 code or authorization; no menus until S5.

## S4 exit boundary

All nine patterns are executable through BookingFacade; Adapter and Observer integrate with atomic event/revision recording. Main/POM and original tests remain unchanged. S5 may add only explicitly authorized console parsing/rendering, seed catalog and Main composition, sharing one dispatcher per repository. No S5 code was implemented. Final verification is recorded in SESSION_HISTORY.md and rubric-evidence.md.

## S5 exit boundary (supersedes historical S4 boundary above)

S5 explicitly authorized and complete October 26: Main composition, ConsoleInput/ConsoleApplication, DemoCatalog/DemoSeed, 26 deterministic console test invocations, migrated process EOF smoke, and repeatable packaged demo script. All 11 operations and exit implemented through the facade, with seed completion and graceful EOF. Final clean verify: 262 tests, 0 failures/errors/skips, exit 0. Packaged workflow and two EOF processes exit 0; details in SESSION_HISTORY.md. No commits.

Stop after S5. Recommended S6 only upon explicit authorization: final requirement/pattern evidence audit, rendered UML, permanent architecture regression checks, broader terminal/timezone QA and submission packaging. Do not interpret this recommendation as authorization.

## S6 final boundary (supersedes all earlier stop conditions)

Explicitly authorized by the user and completed October 26-27, 2026: independent nine-pattern and architecture audit, two automated architecture checks, all six UML sources corrected/rendered, 100-point traceability and Moodle evidence draft, demonstration walkthrough, packaged console QA and clean submission ZIP. No feature development, production Java change, dependency change, commit or upload. Source build: 264 passing tests, zero failures/errors/skips, exit 0. See S6-AUDIT.md for findings, including intra-layer package cycles and the fluent Builder qualification, and SESSION_HISTORY.md for package/fresh-extraction evidence.

Stop after S6. Remaining work is manual Moodle metadata/template confirmation, upload and receipt verification by the user before October 30. No additional implementation is authorized.


## S6.1 final boundary (supersedes the S6 stop condition)

Explicitly authorized October 27: eliminate intra-layer package cycles, preserve behavior and nine patterns, strengthen architecture checks, synchronize documentation/UML and regenerate/verify the submission ZIP. Six classes relocated by responsibility; all original 264 tests retained, three architecture invocations added (267 total). No feature, business-rule, dependency or console-operation changes. No commits or upload. Final command and extraction evidence: S6-AUDIT.md and SESSION_HISTORY.md. Stop after S6.1; any additional implementation requires explicit authorization.
