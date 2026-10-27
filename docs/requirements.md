# Requirements baseline (S0)

S7B authorization addendum (October 27, 2026): S7A is approved with refinements. The user explicitly authorized all six JavaFX views, booking/lifecycle interactions through existing services, compact layout refinements, regression/controller/Windows verification and raw local screenshots. This supersedes the historical S7A-only restriction below without changing F01-F11 or B01-B11. No new business rules, integrations, commits, pushes or academic ZIP changes. Stop after S7B for visual and functional review; evidence: [ui-review/S7B.md](ui-review/S7B.md).

S7A authorization addendum (October 27, 2026): the user explicitly authorized a JavaFX presentation shell/theme and read-only Overview on feature/javafx-ui, superseding N08's historical no-GUI restriction only for this slice. Main retains shared service composition; DesktopMain owns JavaFX lifecycle. All business requirements and semantics below remain unchanged. Remaining desktop views and booking/lifecycle actions are S7B, requiring visual approval and explicit authorization. See [ui-review/S7A.md](ui-review/S7A.md). Main and the academic ZIP remain protected; no commits/pushes/uploads.

This document is the requirements baseline. S1 implements domain validation, time/money values and State; S2 implements validated creation for both kinds and mode families plus composable add-on fee/instruction snapshots (domain portions of F03/F04); S3 implements repository-backed reserve/transition/reschedule, immutable chronological history, catalog reference checks and Strategy (F05-F08 foundation). S4 adds application browsing/booking/lifecycle Facade, immutable Observer events and simulated Calendar Adapter (F01-F10 application behavior). S5 implements the interactive console and deterministic seed catalog; see README and the S5 refinement below. Deadline: October 30, 2026.

## Functional requirements

| ID | Requirement and future acceptance |
| --- | --- |
| F01 | Browse deterministic providers in healthcare consultations, tutoring, professional consulting, and salon/personal services; show supported services and modes. |
| F02 | Browse service duration, base price, provider working hours, and available slots in the configured zone. Displayed availability is advisory until reservation commits. |
| F03 | Create consultation or service appointments for an existing customer, provider, supported service, valid slot, and compatible online/in-person family. Initial status is PENDING. |
| F04 | Compose optional priority booking and reminder services; show base, add-on, and total EUR fees. Priority never bypasses availability rules. |
| F05 | Reserve atomically; pending and confirmed reserve capacity. Reject overlapping requests with a clear domain/application error. |
| F06 | Confirm PENDING; cancel PENDING or CONFIRMED; complete CONFIRMED only after its end instant. No other status transitions succeed. |
| F07 | Reschedule future PENDING/CONFIRMED appointments to a future valid slot, preserving ID, status, duration, provider, service, mode, options, and original reservation on failure. |
| F08 | Show customer appointment history with all statuses, chronological start then ID ordering. Completed intervals remain occupied; cancelled intervals become reusable subject to future-time rules. |
| F09 | Publish RESERVED, CONFIRMED, RESCHEDULED, CANCELLED, COMPLETED events following successful commits. Simulated notifications and audit consumers receive snapshots. |
| F10 | Synchronize simulated calendar entries through a stable port, with explicit timestamp conversion and mapped failures; no network calls. |
| F11 | Interactive console supports browsing and lifecycle operations with recoverable input errors and explicit exit in later slices. |

## Domain constraints

- B01: IDs are typed, nonblank, immutable, and globally unique per entity type; fixed namespaced seed IDs cannot collide with generated UUID IDs. Repository insertion rejects duplicate IDs atomically. One immutable catalog is loaded once per application instance.
- B02: Customer/provider/service references must resolve. Provider supports the selected service and mode. Categories describe services; consultation/service appointment kind is an explicit service property, not inferred from category text.
- B03: Service duration is positive and determines end = start instant + duration. User cannot supply a different duration. Every interval has start < end.
- B04: Appointment fits a single provider working interval and global constraints hold for every availability strategy. No overnight working intervals in the first release; split such hours into dated intervals if needed later.
- B05: Intervals are half-open: overlap iff a.start < b.end AND b.start < a.end. Exact adjacency is allowed under standard policy. PENDING, CONFIRMED, and COMPLETED occupy their intervals; CANCELLED does not.
- B06: Check plus reservation write is atomic. Failed reschedule leaves the original slot and aggregate unchanged. Appointment ID is stable across rescheduling.
- B07: Booking/rescheduling start must be strictly after Clock.instant(); rescheduling/cancelling/confirming requires the original start to be in the future. Completion requires now >= end. This is a documented project policy; terminal states reject every mutation.
- B08: Required fields, mode family consistency, working hours, overlap, current time, and lifecycle rules cannot be disabled by add-ons or policies.
- B09: Rejected operations emit no success events and make no calendar mutation. Successful mutations emit one lifecycle event each, with ordering specified in architecture.md.
- B10: Money uses EUR, BigDecimal, scale 2, HALF_UP at monetary construction/fee calculation; addition uses normalized amounts, never double. No taxes, exchange, or payments. Fees are immutable booking-time snapshots.
- B11: Europe/Vilnius is the default configurable zone. Store Instants; reject DST gaps; choose the earlier instant in overlaps. Rules apply to user inputs and dated provider working hours. See architecture.md for exact resolution.

## Non-functional requirements

| ID | Requirement |
| --- | --- |
| N01 | Java 21 LTS, Maven, JUnit Jupiter, console, standard libraries where sufficient; no runtime network requirement. |
| N02 | Domain has no imports from outer layers; constructor injection and one composition root; small cohesive classes, SOLID, no circular dependencies or global mutable state. |
| N03 | Thread-safe in-memory writes, immutable reads, deterministic race tests, no flaky sleep-based assertions. Persistence lasts only for the current process. |
| N04 | Fixed Clock and seeded inputs reproduce tests/demos. Runtime Clock is injected separately from deterministic demo Clock. |
| N05 | Explicit typed errors; user errors do not terminate future console sessions. Listener/calendar failures remain visible without undoing committed business changes. |
| N06 | Each of nine GoF patterns has distinct executable responsibilities and tests by S6; no ceremonial interfaces or static switch substituted for polymorphism. |
| N07 | UML, requirements, code, tests, and evidence stay aligned. Planned evidence must never be presented as implemented. |
| N08 | No medical records, authentication, real email/SMS, calendar APIs, databases, GUI/web framework, payment gateways, quantity/order models, or extra features. |

## Original deterministic catalog contract (historical plan; refined by S5 below)

Seed one customer and four providers with fixed IDs (`customer-demo-001`, `provider-health-001`, `provider-tutor-001`, `provider-consult-001`, `provider-salon-001`), one service each (30, 60, 45, 30 minutes respectively), EUR prices 40.00, 25.00, 60.00, 20.00, and Monday–Friday 09:00–17:00 local hours. Health/tutor/consult support both modes; salon is in-person. Health/tutor/consult use consultation creators; salon uses service creator. Preparation buffer is 15 minutes when that policy is selected. Priority fee 10.00; reminder fee 2.00. Demonstration Clock: 2026-10-26T07:00:00Z, demo booking date 2026-10-27. Real sessions use a system Clock and future user-selected dates. Catalog sorting uses IDs, never map iteration order.

## S5 seed and console refinement (implemented)

The original one-customer/fixed-demo-date plan is superseded: three fictional customers with IDs customer-demo-001 through -003; the original four provider IDs and matching service-{health,tutor,consult,salon}-001 IDs, durations/prices/hours/modes/buffers retained. Production dates are relative to the injected runtime clock. One previous-weekday appointment is booked and confirmed using an isolated startup clock through the facade, then runtime time resumes permanently. No overlapping seeds or arbitrary historical-state import. Details and identifiers are listed in README.

F01-F11 now have console entry points, including exact-slot advisory availability, customer appointment snapshots, per-appointment event history and observer records. Mode/service compatibility, lifecycle, future-time and collision rules remain owned by existing domain/application APIs. User selection of appointment type filters services; service kind chooses the creator. S6 was explicitly authorized and completed; see S6-AUDIT.md. S6.1 corrects the four intra-layer package cycles; automated analysis now verifies acyclicity across all production packages as well as inward layer dependencies (N02).
