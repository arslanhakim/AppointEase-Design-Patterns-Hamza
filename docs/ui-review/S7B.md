# S7B JavaFX desktop review

Authorized October 27, 2026 after S7A approval with refinements. All work remains uncommitted on `feature/javafx-ui`. Stop after S7B for visual and functional review. Main, the published console baseline and the academic submission ZIP are preserved. No commit, push, external screenshot upload or further slice is authorized.

## Implemented views and workflows

| View | Implemented behavior |
| --- | --- |
| Overview | Existing daily counts, appointment rows, date/week navigation, agenda and refresh; prominent New Appointment beside the heading; List/Schedule links; clickable and keyboard-selectable appointment rows. |
| Appointments | All-date history, case-insensitive customer/provider/service/reference search, combined status/provider filters, clear filters, constrained columns, selection and View appointment, Enter/double-click details. |
| New Appointment | Customer/provider/service/mode selection, date and HH:mm input, domain decorator prices, priority/reminder add-ons, optional provider buffer, advisory availability, review with UTC offset, final reserve action and saved receipt. Back to edit preserves values and validation. |
| Appointment details | Snapshot, instructions, price/options, event history, confirm/cancel/complete confirmations, reschedule date/time/policy review. Domain eligibility disables unavailable actions; commit always revalidates. Failed reschedules preserve the original appointment. |
| Providers | Provider catalog selection, services/durations/base fees, modes, working hours and preparation buffer, exact-time advisory availability and provider-prefilled booking. |
| Schedule | Date navigation, Today, provider filtering, List switch, time-ordered selectable day entries, text/color status differentiation, cancelled history and empty state. Separate rows keep simultaneous bookings legible. |
| Activity | Actual notification/audit supplier records, newest event sequence first, channel filter, timestamps/appointment descriptions/revisions/references, refresh, empty/error feedback and committed delivery warnings. |

All six navigation items work. No placeholder page/action or new runtime dependency was added. New bookings are Pending; the saved receipt is not a lifecycle confirmation. Confirm from details. Complete an elapsed confirmed appointment, such as `seed-ready-001`; future appointments correctly disable completion.

## Design review

| Before | After | Why |
| --- | --- | --- |
| S7A preview footer and unavailable destinations | Functional destinations and local workspace/timezone footer | Navigation conveys the actual application state. |
| Compact sidebar consumed scarce width | Automatic collapse below 1000 logical pixels; Menu restores it and selecting a destination collapses it | Keeps tables and forms readable at 1024x768 physical size. |
| No creation action on Overview | Prominent New Appointment alongside the heading; List/Schedule in the date toolbar | Direct access without adding a row that displaces the fourth booking. |
| Small secondary text | Secondary text 12-13px, consistent field labels and date formats | Improves readability while retaining Georgia/Segoe UI hierarchy. |
| No working forms/tables | Native controls, constrained tables, wrapping toolbars, shared focus/status/error styles | Keyboard interaction and readable columns without ornamental UI. |
| Empty feedback nodes could leave colored strips | Feedback is hidden/unmanaged when empty | Keeps review/details layouts clean. |

Retained ink #202A35, ivory #F5F3ED, terracotta #C56B4E and sage #7D927B. The primary button uses a darker terracotta text-contrast variant. No gradients, glass, glow, decorative animation, new icon library or content shadows. Focus uses a visible border. Desktop captures show all four Overview rows and the complete initial booking form including review action. Compact forms and longer pages scroll vertically; the agenda stacks beneath the list. Table actions are above the rows so they remain reachable without scrolling through the full table.

## Engineering decisions

DesktopController is a JavaFX-independent presentation controller with injected BookingFacade, customer catalog, DomainTime and observer suppliers. DesktopMain receives the shared composition from Main.createContext. Presentation never imports repositories, dispatcher implementations or infrastructure; all five existing architecture checks still cover every production class/package.

Quotes come from the real decorator chain. Mode products come from existing factories; selected availability policies are existing strategies. DomainTime resolves local input and the review shows its offset. No duplicate pricing, overlap, working-hour or state/time rule table was introduced. Action eligibility invokes detached immutable Appointment operations; mutation always uses the facade. Prepared requests/moves retain the command key and collaborator identity for retries. Tests verify duplicate submission receipts, no duplicate events and no duplicate warnings.

Availability is advisory. Book and reschedule revalidate under the existing atomic reservation path. A failed command emits no success event and does not replace the original slot. A committed operation with delivery warnings is shown as saved and warns against repeating it; Activity retains those warnings. Navigation reloads snapshots and successful detail actions refresh the current state. Booking input is retained for ordinary navigation/back-to-edit; explicitly starting another booking replaces the draft.

The Schedule timeline is ordered by time but row heights are not a duration scale. This favors selectable readable entries over overlapping tiny blocks. Overview retains the small proportional 09:00-17:00 demo agenda. Neither treats empty space as guaranteed availability. Providers uses a valid catalog customer for its read-only exact-slot check because the existing request contract requires one; no booking is made by that check.

## Run and verify

```powershell
java -version
mvn -version
mvn clean verify
powershell -NoProfile -File scripts/demo.ps1
powershell -NoProfile -File scripts/verify-console.ps1
powershell -NoProfile -File scripts/run-desktop.ps1 -Verify
powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7B -Capture
powershell -NoProfile -File scripts/run-desktop.ps1
```

Only Maven build/development commands may download dependencies. The packaged desktop runner and console runtime use local files and simulated integrations. Close the live desktop before rebuilding: Windows locks its JAR. Capture requires one AppointEase window and an interactive Windows desktop large enough for the requested outer dimensions. `-VerifyS7B` runs without captures; `-Capture` includes the same real interaction checks and captures. Harnesses close their own windows and leave no review window running.

The original 272 Maven tests remain. DesktopControllerTest adds 17 invocations covering creation/quotes/retries, invalid/missing/unsupported input, review and post-review conflicts, preserved failed reschedules and successful moves, cancellation/reuse, confirmation/time-gated completion, filtering, navigation, refreshed projections, DST input, empty/broken activity suppliers and successful commit with delivery warnings. The compiled opt-in JavaFX harnesses are additional checks, not skipped JUnit tests. Final executed commands/counts/times are recorded in SESSION_HISTORY.md and PROJECT_MEMORY.md.

## Raw local screenshots

Final verification: `mvn clean verify` completed October 27 at 13:06:20 +05:00, exit 0, 289/0/0/0 tests/failures/errors/skips. Windows S7B capture journey: exit 0, 47 assertions; retained Overview journey: exit 0, 13 assertions. Both console QA scripts exit 0 with nine successful child processes. Final PNG signature/decoding/nonempty checks passed for all 13 files. Main/ZIP remain unchanged, staged diff empty, `git diff --check` exit 0. Full evidence and intermediate corrections are in SESSION_HISTORY.md.

Directory: `C:\dev\appointease\docs\ui-review\screenshots\S7B\`.

| Exact path | Requested outer window |
| --- | --- |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\01-overview-desktop.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\02-overview-compact.png | 1024x768 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\03-appointments.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\04-new-appointment.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\05-providers.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\06-schedule.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\07-activity.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\08-appointment-details.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\09-booking-confirmation.png | 1440x900 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\10-conflict-error.png | 1024x768 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\11-appointments-compact.png | 1024x768 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\12-new-appointment-compact.png | 1024x768 |
| C:\dev\appointease\docs\ui-review\screenshots\S7B\13-booking-review.png | 1440x900 |

These are raw pixels from the actual DesktopMain windows, captured after real control interactions. No generated mockups, image enhancement, scaling or external uploads. Windows DWM excludes invisible resize margins: saved desktop images are 1424x892, compact images 1008x760. Display scaling was 125%. The conflict capture is deliberately scrolled to show the feedback. Every file must decode as PNG, have the eight-byte PNG signature and be nonempty; final byte inventory is in session history. Images are ignored by Git.

## Files changed in S7B

New production files, all under `src/main/java/com/appointease/presentation/desktop/`:

- DesktopController.java
- AppointmentsView.java
- AppointmentDetailsView.java
- BookingView.java
- ProvidersView.java
- ScheduleView.java
- ActivityView.java

Modified production files:

- src/main/java/com/appointease/DesktopMain.java
- src/main/java/com/appointease/presentation/desktop/DesktopShell.java
- src/main/java/com/appointease/presentation/desktop/OverviewView.java
- src/main/java/com/appointease/presentation/desktop/Ui.java
- src/main/resources/com/appointease/presentation/desktop/theme.css

Tests/tooling:

- New src/test/java/com/appointease/presentation/desktop/DesktopControllerTest.java
- New src/test/java/com/appointease/presentation/desktop/DesktopS7BCheck.java
- Updated src/test/java/com/appointease/presentation/desktop/DesktopVisualCheck.java (all six destinations enabled instead of the S7A-only unavailable expectation; all other checks retained)
- Updated scripts/run-desktop.ps1 and scripts/capture-ui.ps1

Documentation: AGENTS.md, PROJECT_MEMORY.md, README.md, docs/SESSION_HISTORY.md, docs/architecture.md, docs/requirements.md, docs/design-pattern-matrix.md, docs/implementation-plan.md, docs/testing-strategy.md and this new docs/ui-review/S7B.md. The 13 ignored PNGs are additional local artifacts. Preexisting S7A files remain uncommitted and preserved; this inventory describes this slice, not the entire diff from main.

## Limitations and next boundary

Data, retry receipts, activity and warnings are process-local/unbounded. Notifications/calendar are simulated. Operations are synchronous for the small demo catalog; large datasets, assistive-technology certification, non-Windows graphics, installers and native font fallback are not verified. No auto-refresh timer, automatic slot discovery, week/month calendar or durable storage. The dedicated Schedule is a selectable chronological day timeline, not a proportional multi-provider calendar. Overview's mini-agenda remains calibrated to the demo hours. At compact size and 125% scaling, scroll to lower content. The native window title, breadcrumb and selected navigation track the current destination, including appointment details.

No unresolved tested functional failure is claimed. Next: user visual and functional review, then only explicitly authorized follow-up fixes or packaging/accessibility/scale work. No further implementation, commit, push or submission regeneration should start automatically.
