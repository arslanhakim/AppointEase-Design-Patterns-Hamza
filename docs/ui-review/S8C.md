# S8C — Final desktop usability and Activity refinement

Historical review record; S8A-S8C subsequently passed visual review and were authorized for feature-branch publication. See [publication verification](../engineering-history.md) for current release checks and boundaries. The measurements and screenshots below belong to the original review run.

Implemented October 27–28, 2026 on `feature/javafx-ui`, following S8B visual approval. The reviewed S8A/S8B working tree was preserved. S8C changes presentation, verification scripts and documentation only. No commit, push, merge, academic repackaging or next-slice work.

## Interaction and layout decisions

| Before | After | Why |
| --- | --- | --- |
| Schedule recreated on every visit | Retained controls with fresh facade reads on re-entry | Preserve navigation without keeping stale appointment data |
| Calendar details returned to Appointments | Back to Schedule restores mode, date, filters, scroll and originating focus | Continue the calendar task after inspecting or changing a booking |
| Month required many Tab stops | One Tab entry point; arrows, Home/End, Page Up/Down; Enter/Space activation | Traverse dates and month boundaries predictably |
| Compact Week relied on a small scrollbar and general text | Conditional Earlier days / Later days buttons plus the existing scrollbar | Make horizontal navigation discoverable and keyboard operable |
| Activity was a narrow, fully materialized vertical feed | Virtualized selectable feed and 62/38 desktop detail columns | Use desktop width and bound rendered rows as history grows |
| Event metadata repeated in every feed row | Concise event/type/time/customer/provider rows and selected snapshot details | Make the chronology scannable while retaining status, revision, classification and appointment reference |
| No compact detail arrangement | Same feed and details stack below 800 logical pixels of page width | Retain selection and filters across actual window resizes |

No new animations, decorative dashboard components, shadows or gradients were added. Existing ink, ivory, terracotta and sage colors and Georgia headings remain. Selected rows have a sage background and border; keyboard focus remains visibly outlined. Activity detail fields wrap without losing scheduled time. Event reference is available through the detail tooltip/accessibility text. The displayed lifecycle status belongs to the selected event's immutable snapshot.

Schedule re-entry also refreshes provider choices by stable ID. Every return/explicit refresh reads current appointment histories; confirmation, rescheduling and cancellation are covered. Horizontal scroll belongs to Week; vertical scroll belongs to the shell. Restoration happens after layout and is clamped by the current viewport if dimensions/content change. Preferences last for this process, not across app restarts. Month arithmetic retains Java's month-end clamping. No date/time parsing, business-zone range projection or DST rules changed.

## Verification

| Check | Executed result |
| --- | --- |
| `mvn clean verify` | Exit 0, BUILD SUCCESS at **00:35:53 +05:00 on October 28**; **351 tests, 0 failures/errors/skips**; all **339 baseline invocations** preserved plus **12 CalendarKeyboardTest cases**; all five architecture checks pass; independent XML sum agrees |
| `run-desktop.ps1 -VerifyS8C -Capture` | **70 assertions**, exit 0; real packaged shell/controller/facade, Windows Stage, native pointer/keyboard, both requested physical sizes |
| Retained `-VerifyS8A` | **40 assertions**, exit 0 |
| Retained `-VerifyS8B` | **80 assertions**, exit 0 |
| Retained `-VerifyS7C` | **85 assertions**, exit 0 |
| Retained `-VerifySidebar` | **106 assertions**, motion enabled, exit 0 |
| Retained `-Verify` | **13 assertions**, exit 0 |
| Console demo and standalone QA | Both scripts exit 0; all **nine packaged-JAR child processes** pass |

S8C Windows coverage includes all three modes on re-entry, date/provider/cancelled retention, vertical and horizontal scroll restoration, Week details return, native Enter opening an appointment, Day return and focus restoration after confirmation, explicit refresh after rescheduling, cancellation filtering on re-entry, all eight Month movement keys, year/month boundaries, one Tab entry point, native Enter/Space date activation and a computed two-pixel focus border. Activity checks cover newest-first ordering, native Down selection, matching details, selection retention on refresh/resize, both channel filters, desktop proportions, compact stacking, full wrapped detail height, populated-to-empty results, supplier failure/recovery (including cleared accessible metadata) and bounded cells with a larger history.

The retained S7C harness has exactly two structural adaptations: Activity record-count assertions now read ListView items instead of counting `.history-row` nodes. The original thresholds (20 all records, 10 audit records) and journey remain. This is necessary because virtualization deliberately does not create a node for every record. S8B's native input uses separate press/release events and an explicitly foreground test stage, retaining all 80 assertions. No existing JUnit tests were edited or removed. The new Windows harness remains opt-in, not a skipped Surefire test.

Intermediate failures are not counted as passes. The initial sandbox Maven attempt could not access dependencies; the approved run passed. Early S8C native-key attempts received no key events despite JavaFX reporting window focus. A native click into the tested control established actual input focus; the retained sidebar sequence independently passed. The final harness uses separated native press/release events and checks foreground focus. A later S8C capture lost foreground focus mid-journey; the unchanged rerun passed. The retained sidebar also had one foreground-focus failure before an unchanged successful rerun. A later S7C run missed native Tab traversal; its unchanged direct rerun passed all 85 assertions. Two retained S8B attempts missed the native Week click; its input timing/foreground setup was updated without changing assertions, then all 80 passed. Screenshot inspection found a compressed Activity service/time label; minimum preferred field heights corrected it and a regression now checks full wrapped height. Early captures were replaced by final captures; capture now moves the pointer to the title bar to avoid incidental tooltip overlays.

## Measured larger-history behavior

The Windows harness adds **500 appointments through real booking and confirmation commands**, across four providers, five times each weekday for five weeks. Together with the unchanged production seed, the fixture has **505 appointments and 2,024 notification/audit records**. A busy week contains **100 appointments**. Fixture generation took **470.52 ms** in the recorded run and exists only in the test process.

| Operation | Median | Maximum |
| --- | ---: | ---: |
| Query all 505 appointments through DesktopController/facade histories | 2.37 ms | 3.32 ms |
| Query/sort 2,024 Activity records | 0.65 ms | 0.99 ms |
| Activity refresh plus CSS/layout, desktop | 15.30 ms | 20.74 ms |
| Week refresh plus CSS/layout, 100 appointments, desktop | 128.90 ms | 164.81 ms |
| Month refresh plus CSS/layout, compact | 38.50 ms | 59.37 ms |

Each row uses 12 `System.nanoTime` measurements after three warmups on the JavaFX thread; median is the average of the two middle sorted samples. Render measurements include query, node creation, CSS and layout; they exclude OS composition, screenshot latency and input-to-photon time. These are observations on this Windows machine at 125% display scaling, not performance guarantees or timing assertions. Activity creates fewer than 40 ListCell nodes for the entire 2,024-record history; its oldest event remains selectable. Both the large-history feed and dense Week were exercised at the requested sizes.

Query cost does not justify background queries or a new repository API for this dataset. Week node creation/layout is the dominant cost and can briefly block input, reaching 165 ms in this sample. No background threading or persistence change was introduced. Much larger histories/dense weeks require separate profiling; this is not a production-scale benchmark.

## Native screenshot inventory

All captures are raw Windows DWM pixels under `docs/ui-review/screenshots/S8C/`, Git-ignored as in earlier slices. Physical **1440×900** and **1024×768** outer windows yield **1424×892** and **1008×760** PNGs at this session's 125% scaling. Every PNG is signature-checked and decoded; no generated or edited mockups.

| Screenshot | State |
| --- | --- |
| [01-activity-desktop-selected.png](screenshots/S8C/01-activity-desktop-selected.png) | Selected lifecycle event with feed and complete event details side by side |
| [02-activity-compact.png](screenshots/S8C/02-activity-compact.png) | Same selected event after compact resize; details below feed |
| [03-schedule-before-navigation.png](screenshots/S8C/03-schedule-before-navigation.png) | Compact Week at nonzero horizontal and vertical positions |
| [04-schedule-after-navigation.png](screenshots/S8C/04-schedule-after-navigation.png) | Same calendar after visiting Services and returning |
| [05-week-compact-scroll-controls.png](screenshots/S8C/05-week-compact-scroll-controls.png) | Named horizontal controls and native scrollbar above the week canvas |
| [06-month-keyboard-focus.png](screenshots/S8C/06-month-keyboard-focus.png) | February 5, 2028 selected through native keyboard traversal, visible focus border |
| [07-details-back-to-schedule.png](screenshots/S8C/07-details-back-to-schedule.png) | Appointment details with explicit Back to Schedule action |
| [08-schedule-after-details-mutation.png](screenshots/S8C/08-schedule-after-details-mutation.png) | Returned Day context, freshly confirmed appointment and restored focus |
| [09-activity-large-history.png](screenshots/S8C/09-activity-large-history.png) | Oldest event selected from the 2,024-record history |
| [10-week-large-compact.png](screenshots/S8C/10-week-large-compact.png) | Dense 100-appointment Week, compact horizontal navigation |
| [11-activity-compact-details.png](screenshots/S8C/11-activity-compact-details.png) | Compact page scrolled to all selected-event fields |

## File scope and handoff

Production: `DesktopShell.java`, `ScheduleView.java`, new `CalendarKeyboard.java`, `AppointmentDetailsView.java`, `ActivityView.java`, and `theme.css`. Verification: new `CalendarKeyboardTest.java`, `DesktopS8CCheck.java`, the two retained S7C Activity assertions in `DesktopS7BCheck.java`, native-input timing in `DesktopS8BCheck.java`, and S8C options in `run-desktop.ps1` / `capture-ui.ps1`. Documentation: README, architecture, testing strategy, project status, engineering history and this report. Earlier S8A/S8B changes remain in the same uncommitted working tree.

Limits: process-local state and synchronous history scans; dense Week labels may ellipsize with full tooltip/details available; compact calendars and Activity details scroll vertically; Activity filters/selection are retained during refresh and resize, not after leaving Activity. No full assistive-technology certification, non-Windows render verification, real-window DST-transition captures or durable preferences. Existing domain/calendar DST tests remain passing. No known failing final S8C application workflow.

Protected artifacts: main remains `b8417e91960c3e632c16b2a3e1b14763445c5382`; academic ZIP remains 1,372,171 bytes, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. Stop after S8C for final review. No commit, push, merge or ZIP modification.
