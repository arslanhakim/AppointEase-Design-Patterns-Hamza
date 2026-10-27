# AppointEase project status

Last reviewed: October 28, 2026. Java 21 / Maven academic application demonstrating all nine GoF patterns. The console baseline S0-S6.1 is complete on `main`. The S8A-S8C release on `feature/javafx-ui` contains approved customer/provider/service management, full-height Appointments, Day/Week/Month scheduling and the final navigation/Activity refinements. Publication verification is recorded in [engineering history](engineering-history.md).

## S8C current capabilities and stop boundary

- Schedule retains mode, date, provider, cancelled visibility and relevant scroll positions across destinations and appointment details. Re-entry refreshes appointments and provider choices. Details opened from Schedule has Back to Schedule and restores originating focus when still present.
- Month supports arrows, Home/End, Page Up/Down and Enter/Space, with one Tab entry point and visible focus. Compact Week exposes Earlier days / Later days alongside the native scrollbar.
- Activity has a virtualized selectable chronological feed with event details, 62/38 desktop columns and a compact stacked arrangement. Existing filters and newest-first order remain. Refresh and resize retain event selection by ID/channel; empty/error states clear stale details.
- Maven: **351 tests, zero failures/errors/skips**; all **339 S8B tests** preserved plus 12 keyboard cases. S8C Windows: **70 assertions** and **11 real PNGs**. Exact retained checks, performance measurements, screenshot inventory and qualifications: [S8C review](ui-review/S8C.md).
- Measured **505 appointments / 2,024 activity records**, including a 100-appointment week. Queries remain inexpensive; Week rendering is the dominant synchronous cost. No background threading, new query store or persistence architecture.

**Stop boundary:** Publish S8A-S8C only to `origin/feature/javafx-ui`, without force-push, merge or academic repackaging. Main and the original ZIP remain unchanged. Session state is not durable; dense calendars/compact details scroll. No production-scale or full accessibility certification is claimed. Earlier review boundaries below are historical.

## S8B approved baseline

- Appointments fills the available workspace height with a 150-logical-pixel minimum. Internal scrolling handles excess rows; resize preserves selection, search, status and provider. Explicit Refresh restores the selected ID when still present.
- Schedule has native Day / Week / Month controls. Day preserves S7C proportional geometry and keyboard/details behavior. Week has seven Monday-first day columns, elapsed-duration blocks, safe overlap lanes, selected/today indicators and horizontal scrolling when required. Month has correct leading/trailing dates, bounded booking/cancelled counts and date-to-Day navigation.
- Switching views preserves date/provider/cancelled visibility. Existing facade histories remain the data source; business-zone half-open boundaries and immutable stored appointment Instants drive projection. No booking rules or new query store were added.
- Final `mvn clean verify`: **339 tests, zero failures/errors/skips**, exit 0 at **20:11:03 +05:00**; all 312 baseline tests preserved, 27 calendar invocations added, all five architecture checks pass.
- Windows S8B: **80 assertions and 12 real screenshots**. Retained S7C: **85 assertions**; S8A: **40**. Packaged console demo/QA: **nine successful child processes**. Detailed verification, supplemental regressions, file changes, screenshot inventory and intermediate failures: [S8B report](ui-review/S8B.md).

**Historical boundary:** S8B passed visual review and S8C was separately authorized. Main and the verified ZIP remain unchanged.

Retained limitations: synchronous process-local history scans, compact/dense calendar scrolling and truncated block labels with complete details/tooltips. S8C resolves the former Schedule re-entry reset and details-return limitation. No persistence, recurring bookings, drag/drop or automatic slot search. Real-window DST-transition rendering, very large histories, non-Windows graphics and full accessibility certification remain outside executed verification.

## S8A approved baseline

- Eight desktop destinations. Customers supports list/search, create/edit of the existing name field and appointment history. Inline **+ New customer** immediately selects the saved customer and retains the booking draft.
- Providers supports create/edit, category, supported services, weekly closed days/split working periods, preparation buffer, fixed Online/In-Person modes and enabled state. Services supports list/search, name/category/type/duration/EUR base fee and access to provider relationship editing.
- Catalog mutations and appointment reservations share one repository lock. Stable IDs, immutable read snapshots, expected-record comparison, duplicate/reference validation and rejection of incompatible active-appointment changes protect integrity. Existing appointments retain their agreed service, duration, fee and customer/provider snapshots. Reviewed bookings reject catalog changes before submission.
- `mvn clean verify`: **312 tests, zero failures/errors/skips**, completed October 27 at 19:21:12 +05:00, exit 0. All original 289 tests remain unchanged; CatalogManagementTest adds 23 invocations. All five architecture checks pass. S7C actual-window regression: **85 assertions**, exit 0. Demo and console QA: **nine successful packaged-JAR processes**, exit 0. Final management/sidebar/Overview evidence and screenshots are recorded in [S8A review](ui-review/S8A.md) and [engineering history](engineering-history.md).
- S8A Windows checks: **40 assertions** and **16 real screenshots**; Overview: **13 assertions**. Sidebar: **106 assertions** with motion and **106** with reduced motion; one native tooltip-hover failure preceded the unchanged successful rerun. All final command outcomes are exit 0. All 16 PNGs decode successfully; updated documentation links resolve and `git diff --check` passes.

Editing policy and compatibility contracts: [architecture](architecture.md). In-session configuration is available to the local operator; there is no account/role system. Customer details remain name-only. Data resets at exit; demo records are still initial data. No deletion, persistence, import/export, catalog-specific audit stream, new mode families or network service. Whole-minute UI fields and synchronous catalog operations suit this small desktop session. Reschedules of existing appointments must fit both original snapshot hours and current provider hours/configuration; expanding hours benefits new bookings but does not expand old bookings' reschedule range.

**Historical boundary:** S8A ended with management commands, UI, tests, screenshots and documentation. It subsequently passed visual review; S8B was separately authorized and is recorded above. Persistence, identity/contact-field expansion, deletion/import/export and permissions remain outside both slices. `main` and the separate academic ZIP remain unchanged; no commit, push or merge was performed.

## Published milestones

| Milestone | Commit |
| --- | --- |
| Console release | `b8417e91960c3e632c16b2a3e1b14763445c5382` |
| JavaFX interface | `400b1b7ec325c5fc5654e1ef0d400306fc9563f7` |
| Sidebar navigation and toggle icons | `7de416eb1de201671d1bcec0bbd9fac44268500d` |

Local and GitHub branch inspection confirmed the sidebar publication and unchanged main on October 27. Repository visibility remains private. Detailed historical checks, including intermediate failures, are preserved in [engineering history](engineering-history.md). Reproducible commands and coverage: [testing strategy](testing-strategy.md).

## Design and behavior

- `Main.createContext` composes the shared facade and instance-owned services. `DesktopMain` supplies JavaFX lifecycle wiring. The console runs without JavaFX modules; both entry points run offline after Maven dependencies are cached.
- All eight desktop destinations and booking/lifecycle flows use the existing facade and domain rules. Day and Week use actual start/duration geometry, overlap columns and optional cancelled history; Month projects daily counts. Pricing shows decorator fees and totals; status remains textual as well as colored.
- The sidebar retains eight native navigation buttons in a 214-pixel labeled mode or 64-pixel rail. Toggle actions preserve page, form, selection and scroll state. Pointer glyph feedback lasts 140 ms; keyboard activation is immediate. The Windows runner samples animation preference at startup; direct launches default to reduced motion.
- Domain ownership, immutable values, injected clocks, atomic reservation/rescheduling, DST resolution and post-commit delivery are documented in [architecture](architecture.md), [requirements](requirements.md) and the [pattern matrix](design-pattern-matrix.md).

## Evidence and limitations

Repository hygiene verification on October 27, 2026: Java/Maven version checks exited 0; `mvn clean verify` completed at 17:11:57 +05:00 with exit 0 and **289 tests, zero failures/errors/skips**, independently summed from Surefire XML. All five architecture checks passed. Packaged Windows checks passed on their first launches: sidebar **86 assertions** with motion enabled and **86** with reduced motion, S7C **77**, Overview **13**; each exited 0. Demo and console QA scripts exited 0 with **nine successful JAR children**. Source, tests, CSS, POM and UML are unchanged by this cleanup. The packaging manifest was evaluated without creating an archive: all 151 intended files exist, CSS is included, local captures and agent instructions are excluded. See the latest [engineering history](engineering-history.md) entry for the cleanup inventory and attribution audit.

The sidebar publication build on October 27 at 16:22:53 +05:00 passed all 289 tests (zero failures/errors/skips), including five architecture checks. Windows sidebar checks passed 86 assertions with motion enabled and 86 with reduced motion; S7C passed 77 and Overview 13. Demo and console regression scripts passed with nine successful packaged-JAR processes. Earlier native-input attempts failed before unchanged reruns passed; unattended Windows input reliability remains a limitation.

Visual records: [S7A](ui-review/S7A.md), [S7B](ui-review/S7B.md), [S7C](ui-review/S7C.md), [sidebar](ui-review/sidebar-toggle.md). Raw captures remain local and ignored. Requested 1440x900/1024x768 windows yielded 1424x892/1008x760 DWM images at 125% scaling; all four final sidebar-icon PNGs were decoded and visually reviewed.

Data, receipts and logs are process-local and unbounded; integrations are simulated and operations synchronous. Availability checks one exact slot. Compact content scrolls vertically; dense overlaps can ellipsize labels, with details/tooltips retaining text. Schedule preferences now survive re-entry within the session. Motion preference changes require restart. Non-Windows graphics, exhaustive screen-reader support, very dense/custom catalogs and DST-transition UI rendering remain unverified; domain and calendar-range DST tests pass. There is no installer, durable storage or automatic slot search. Historical console invalid-zone handling prints an error but exits zero.

The separate academic console ZIP remains 1,372,171 bytes with SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. Its historical contents differ from the current feature branch. Source-only hygiene does not regenerate it. Academic audit and packaging evidence: [S6 audit](S6-AUDIT.md), [rubric evidence](rubric-evidence.md).
