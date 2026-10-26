# Moodle evaluation criteria - completed evidence draft

Project: AppointEase. Submission deadline: October 30, 2026. Prepared October 27, 2026. Weights are those supplied by the user; no separate Moodle template was available. This table summarizes evidence, not awarded or guaranteed marks. Add student/course identifiers in Moodle if required.

| Criterion | Maximum points | Completed implementation and evidence | Qualification |
| --- | ---: | --- | --- |
| Creational patterns | 15 | Factory Method, Abstract Factory, Builder in domain/creation and domain/mode; CreationPatternsTest 23 invocations. | Concrete fluent Builder variant; subtype behavior deliberately small. |
| Structural patterns | 15 | Adapter, Decorator, Facade in infrastructure/calendar, domain/addon and application; CalendarObserverTest, DecoratorTest and BookingIntegrationTest. | Calendar is simulated; reminder is not timed delivery. |
| Behavioral patterns | 15 | Strategy, Observer, State in domain/scheduling, application/event and domain/state; SchedulingTest, CalendarObserverTest, AppointmentStateTest. | Strategy varies preparation policy; delivery is synchronous/process-local. |
| Integrated application | 25 | All eleven console operations; Main composition; complete lifecycle, fees, events and calendar in BookingIntegrationTest and ConsoleApplicationTest; packaged demo. | Exact-slot query only; no persistence or real integrations required. |
| Code quality | 5 | Immutable values, shared validation, constructor injection, typed failures, no mutable globals; ArchitectureTest. | Dense formatting/wildcard imports and broad repository port are maintainability tradeoffs. |
| Architecture and package structure | 5 | Inward layer dependencies and application-owned ports; compiled jdeps checks; overview UML. | S6.1 removes all four original package cycles; compiled graph verifies all 19 production packages without exclusions. |
| Testability and business logic | 10 | 267 passing invocations, fixed clocks, DST/money/boundary tests, eleven coordinated race scenarios and failed-operation preservation. | Tests are evidence, not exhaustive proof or performance benchmarks. |
| Documentation and justification | 10 | README, architecture, nine-pattern audit/matrix, six rendered UML diagrams, traceability, walkthrough and reproducible scripts. | Dense diagrams best viewed as SVG; reviewer assigns final marks. |
| Total available | 100 | All categories have concrete evidence. | No grade prediction. |

See [rubric-evidence.md](rubric-evidence.md) for source/test paths and actual verification, and [S6-AUDIT.md](S6-AUDIT.md) for independent findings.

Manual submission checklist:

- Confirm Moodle's required filename, size limit, template and deadline/timezone.
- Add student name/ID, course/group and any required declaration in the submission form or institutional template.
- Upload `AppointEase_DesignPatterns_Submission.zip` and any separately required evaluation form.
- Open the uploaded attachment, confirm it is the intended ZIP, then complete Moodle's final submit step.
- Save the submission receipt before October 30, 2026. No upload has been performed by this audit.
