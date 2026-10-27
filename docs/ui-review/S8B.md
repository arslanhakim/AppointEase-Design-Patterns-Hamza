# S8B — Full-height Appointments and Day / Week / Month

Historical review record; S8A-S8C subsequently passed visual review and were authorized for feature-branch publication. See [publication verification](../engineering-history.md) for current release checks and boundaries.

Implemented October 27, 2026 on `feature/javafx-ui`, after S8A visual approval. S8A's existing uncommitted changes were preserved. This slice adds no domain, application, repository, console, dependency, production seed or academic-submission changes. No commit, push, merge or S8C work.

## Layout and interaction decisions

| Before | After | Why |
| --- | --- | --- |
| Appointments inherited a 320-pixel preferred table inside a content-height workspace | Shell fitToHeight for Appointments; VBox ALWAYS growth, unrestricted maximum, 150-pixel minimum | Table uses the available workspace, keeping row size, filters and selection stable during resize |
| Schedule offered Day only | Small native Day / Week / Month toggles next to date navigation | Keeps the current date, provider and cancelled setting without decorative cards |
| Overlap calculation lived inside the day renderer | Shared pure interval partitioning; Day and Week reuse TimeGrid | Preserves elapsed-time geometry and gives every overlapping record a separate lane |
| No week overview | Seven Monday-first columns, common planning-hour range, shared time axis, selected/today borders, per-day empty states | Supports actual seven-day navigation and proportional appointment placement |
| Compact/dense week could squeeze records | Minimum 100 logical pixels per concurrent lane, with a horizontal scrollbar above the grid | Keeps click targets accessible; the shell alone owns vertical scrolling |
| No month overview | Monday-first 28/35/42-cell grid, bounded non-cancelled and cancelled counts, adjacent-month dates, selected date | Avoids overcrowding; selecting any cell opens the exact Day schedule |

The month/year is Month's heading; duplicate Schedule heading and the block-status legend are hidden there. Non-cancelled counts include pending, confirmed and completed appointments; cancelled counts appear separately only when Show cancelled is enabled. Day/Week blocks retain textual status, status colors, cancelled dashed border, tooltips and full details. At desktop size the captured six-week month is visible; compact Month uses ordinary vertical scrolling without horizontal overflow.

Day retains S7C's 1.5 logical pixels per elapsed minute, 09:00–17:00 default range extended around actual bookings, 30/45/60-minute duration scale, overlap-column reuse, provider filtering, optional cancelled history and keyboard details access. Week shares this renderer. A DST-changing column has its own offset-labelled axis rather than treating repeated wall-clock hours as equal Instants. Detection covers the entire calendar day, including a planning origin normalized beyond a spring gap; four additional regression invocations cover this case and ordinary/UTC days.

Data comes from existing facade customer histories through DesktopController. There is no existing range-query API to call. CalendarModel applies half-open, business-zone start/end boundaries and groups stored starts by local date. It contains calendar projection and lane geometry only. Blank calendar space remains unrelated to availability. Month navigation clamps invalid month-end dates through LocalDate.plusMonths.

## Verification

| Check | Result |
| --- | --- |
| Final `mvn clean verify` | Exit 0, BUILD SUCCESS at **20:11:03 +05:00**; **339 tests, 0 failures/errors/skips**; 95 production and 22 test sources |
| Preserved baseline | All **312 original test invocations** remain; no original JUnit or Windows harness source edited; five architecture checks pass |
| New CalendarModelTest | **27 invocations**: Monday/year boundaries, 28/35/42-day month grids, leap years, day/week/month navigation, selected date, half-open midnight boundaries, Karachi business-date grouping, provider/cancelled projection for all views, overlap chains/adjacency/separate clusters, cancelled/replacement overlap and 23/25-hour DST days |
| `run-desktop.ps1 -VerifyS8B -Capture` | Exit 0; **80 Windows assertions**, **12 raw PNGs** |
| Retained `-VerifyS7C` | Exit 0; **85 assertions**; actual DesktopMain, lifecycle/forms, day geometry/overlaps, keyboard Tab/scroll/Enter, all eight destinations at both sizes |
| Retained `-VerifyS8A` | Exit 0; **40 assertions**, catalog management and existing-booking integrity |
| Retained `-VerifySidebar` | Exit 0; **106 assertions**, motion enabled, native pointer/keyboard, state retention and all destinations at both sizes |
| Retained `-Verify` | Exit 0; **13 Overview assertions** |
| Console demo and QA | Both scripts exit 0; all **nine packaged-JAR child processes** exit 0, including UTC/Karachi and EOF paths |
| Artifact validation | All **12 PNGs** have valid signatures and decode at expected dimensions; independent Surefire XML sum confirms 339/0/0/0; protected main/ZIP match baseline |

S8B's opt-in harness composes the real Main application context, DesktopDemoSeed, controller and shell from the packaged JAR in an actual Windows Stage, using the same CSS and minimum dimensions as DesktopMain. It uses native Robot clicks for Week and month-date selection and native Enter for details, plus direct control actions for deterministic filter/navigation coverage. It repeatedly resizes the actual stage to physical 1440×900 and 1024×768, not just a detached layout. At 125% output scaling, the desktop table height is 302.4 logical pixels and changes with the workspace. It verifies viewport-bottom alignment, minimum height, no outer vertical overflow, all three retained filters, selected ID, explicit refresh selection, empty/one-row/five-row tables and 56 real facade-created records with internal scrolling. Larger fixtures exist only in that verification process.

The same journey checks seven week columns, 30/60-minute geometry, empty weeks, navigation, horizontal reach to Sunday, provider/cancelled counts, all 11 busy-day targets without intersections, month boundary-cell count, selected-day highlighting, filter retention, changing the date by native cell click, Day details by Enter and Week details by action.

Intermediate failures were corrected, not counted as passes: the first new harness compilation used a nonexistent controller method and was corrected to the existing act API. The first Windows assertion wrongly expected the table to exceed 320 logical pixels at 125% scaling; it was replaced with available-height/bottom-alignment and repeated-resize checks. Later harness runs exposed reliance on random-ID ordering for the cancellation fixture, missing a layout pulse before querying new ScrollPane children, and an alphabetical first provider with no busy-week appointments. The harness now selects the intended provider explicitly and lays out before inspecting week nodes. Visual inspection prompted the shared Week axis, top horizontal scrollbar and compact count-only Month cells. Final review identified the spring-gap planning-origin edge and extended offset-axis detection to the full day. No baseline assertion was removed or weakened.

## Screenshot inventory

All files are under `docs/ui-review/screenshots/S8B/`, remain Git-ignored and contain unmodified native Windows DWM captures. Physical 1440×900/1024×768 outer windows produce 1424×892/1008×760 images at this session's 125% scaling. Files were decoded and visually inspected; no generated images or edited mockups.

| File | Actual state |
| --- | --- |
| [01-appointments-full-height-desktop.png](screenshots/S8B/01-appointments-full-height-desktop.png) | Five seed records, selected appointment, table extends to bottom padding |
| [02-appointments-compact.png](screenshots/S8B/02-appointments-compact.png) | One matching record with search/status/provider filters retained; no internal vertical scrollbar |
| [03-appointments-large-dataset.png](screenshots/S8B/03-appointments-large-dataset.png) | 56 records, table scrolled to selected last row |
| [04-day-schedule.png](screenshots/S8B/04-day-schedule.png) | Default day, proportional confirmed appointments |
| [05-week-desktop.png](screenshots/S8B/05-week-desktop.png) | Seven columns, shared time axis, today and selected date |
| [06-week-compact.png](screenshots/S8B/06-week-compact.png) | Top horizontal scrollbar, readable day columns |
| [07-month-desktop.png](screenshots/S8B/07-month-desktop.png) | Six-week November grid, leading October/trailing December, selected day, separate cancelled count |
| [08-month-compact.png](screenshots/S8B/08-month-compact.png) | Seven columns fit; lower weeks reached by vertical scrolling |
| [09-selected-appointment-details.png](screenshots/S8B/09-selected-appointment-details.png) | Real appointment opened from selected date, time/offset, status, actions and event history |
| [10-appointments-large-compact.png](screenshots/S8B/10-appointments-large-compact.png) | Same selected record after compact resize of large table |
| [11-week-overlaps.png](screenshots/S8B/11-week-overlaps.png) | 51 week records, cancelled/replacement overlap, separate targets and horizontal scroll |
| [12-selected-day.png](screenshots/S8B/12-selected-day.png) | Month cell changes date to November 3; provider and cancelled setting retained |

## S8B file inventory

| Area | Files |
| --- | --- |
| New presentation model | `src/main/java/com/appointease/presentation/desktop/CalendarModel.java` |
| Updated presentation | `AppointmentsView.java`, `ScheduleView.java`, `DesktopShell.java` in that same package |
| Styling | `src/main/resources/com/appointease/presentation/desktop/theme.css` |
| New tests | `src/test/java/com/appointease/presentation/desktop/CalendarModelTest.java`, `DesktopS8BCheck.java` |
| Runners | `scripts/run-desktop.ps1`, `scripts/capture-ui.ps1` |
| Documentation | `README.md`, `docs/architecture.md`, `docs/project-status.md`, `docs/engineering-history.md`, this report |

This inventory is the S8B delta; Git also contains the earlier reviewed S8A delta.

## Known limits and recommended S8C boundary

No failing final S8B workflow is known. Calendar refresh synchronously scans process-local customer histories; 56 appointments were exercised, not a production-scale dataset. Dense weeks intentionally become wider and require horizontal scrolling; block labels can ellipsize, with complete accessible text/tooltips/details. The shared axis scrolls with the week canvas. Very short appointments retain accurate proportional heights rather than artificial minimum durations. Compact six-week months need vertical scrolling. Full accessibility certification, non-Windows graphics and real-window DST-transition screenshots were not performed; date-range DST tests pass.

Schedule state persists across Day/Week/Month and resize within the current instance. Re-entering Schedule resets it; returning from details still goes to Appointments. Month navigation clamps month-end dates, so January 31 → February 28 → January 28 is intentional Java date arithmetic. No persistence, recurring bookings, drag/drop rescheduling, slot search, week-hours editing or external calendar integration is included.

Recommended S8C, only after separate approval: a narrowly scoped navigation/accessibility pass covering return-to-calendar context, keyboard date-grid traversal and measured larger-history performance. Agree any asynchronous/range-query work separately. Persistence, recurring booking semantics, new business rules and integration work should remain separate slices. S8C has not started.

Main and the academic archive remain protected: main `b8417e91960c3e632c16b2a3e1b14763445c5382`; ZIP 1,372,171 bytes, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`.
