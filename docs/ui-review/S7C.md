# S7C final JavaFX visual polish

Publication addendum, October 27: the user approved this visual review and authorized release QA, one commit and publication of feature/javafx-ui to the existing private repository. The following review preserves the pre-publication outcomes and restrictions as historical evidence. Current release status and verification are in [project status](../project-status.md) and [engineering history](../engineering-history.md). All screenshots remain local and excluded from publication.

Completed October 27, 2026 on `feature/javafx-ui`, under the user's explicit S7C authorization. Focused visual refinement of the working S7B desktop. All work remains uncommitted. Stop after S7C; no later slice, merge, commit, push, external screenshot upload or academic ZIP regeneration is authorized.

## Visual changes

| Before | After | Why |
| --- | --- | --- |
| Schedule entries used equal-height chronological rows | Hour/half-hour grid; start positions and heights use actual elapsed time at 1.5 logical pixels per minute; connected overlaps share columns, exact adjacency reuses them | Makes gaps, start times and duration directly comparable without obscuring simultaneous bookings |
| Cancelled entries always appeared in the planning list | Hidden by default; Show cancelled restores them with a dashed gray edge; Pending terracotta, Confirmed sage, Completed blue-gray; text labels and a legend remain | Separates planning from historical review without relying on color alone |
| One inline base/add-on/total sentence | Itemized quote, separated Georgia total, bordered optional-services group; three field columns on desktop, two at compact size, one below 480 logical pixels | Gives total cost priority and keeps desktop booking actions visible |
| Text-only table statuses | Shared status labels and a subtle sage selected row with a left border | Consistent with Overview, receipt and details, while preserving selection and lifecycle access |
| Activity type, channel, timestamp and identifiers competed on long lines | Event type first, timestamp beside it, description and booking reference below, channel/revision last; event UUID available by tooltip | Preserves newest-first chronology with clearer reading order |
| Review/receipt/details led with customer and secondary time text | Georgia date, bold time with explicit UTC offsets and provider grouped at the top; receipt total; eligible Confirm/Complete primary, Cancel quiet | Emphasizes the appointment decision and distinguishes management actions |
| Compact pages used desktop padding; clearing a filter could leave blank selected text | Smaller page/panel insets, wrapping controls and restored combo prompts; stable chronological keyboard traversal through grid blocks | Makes both requested sizes practical without horizontal scrolling |

Retained Ink #202A35, Ivory #F5F3ED, Terracotta #C56B4E, Sage #7D927B, Georgia headings and Segoe UI controls. Existing darker terracotta primary-button contrast variant remains. No gradients, glass, decorative dashboard, new dependencies or architectural changes. The local emil-design-eng skill informed hierarchy, spacing and focus review.

## Behavior and scope

Schedule uses the existing controller snapshot and only filters its presentation. The time range starts with 09:00-17:00 and extends to include appointments outside it. Heights and offsets use Instants; offset labels are included when the displayed range crosses an offset change. This is a day grid, not a slot-availability engine, provider-hours overlay or week/month view. Cancelled visibility persists while changing date/provider within the current Schedule instance and resets to hidden when re-entering the view.

Pricing still comes from the existing decorator quote, review retains the immutable request, and every lifecycle mutation still uses the facade. No changes to DesktopController, Main, DesktopMain, domain, application, infrastructure, existing JUnit tests, console, POM or UML in S7C. Before/after SHA-256 inventories confirm the source/script scope below. S7A/S7B uncommitted work was preserved.

## Actual verification

| Command/check | Actual result |
| --- | --- |
| `java -version` / `mvn -version` | Both exit 0; Temurin 21.0.12.1, Maven 3.9.16 |
| `mvn clean verify` | Exit 0, BUILD SUCCESS, October 27 at 13:46:19 +05:00; **289 tests, 0 failures, 0 errors, 0 skipped**; independent Surefire XML sum agrees; all five architecture checks pass |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C` | Exit 0; **77 actual-window assertions** |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C -Capture` | Exit 0; **78 assertions**, including full conflict-feedback visibility; **27 raw PNGs** |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -Verify` | Exit 0; **13 retained Overview assertions** |
| `scripts/demo.ps1` and `scripts/verify-console.ps1` | Both exit 0 after final build; **nine packaged `java -jar` children exit 0**, including workflow, malformed input, EOF and UTC/Karachi |
| Screenshot validation | All 27 nonempty, correct eight-byte PNG signatures, decode as PNG; final captures visually inspected |
| Git/artifact checks | `git diff --check` exit 0; staged diff empty; screenshots ignored; protected refs and ZIP unchanged |

Windows checks launch the actual packaged DesktopMain with cached JavaFX modules, perform real native control interactions, and close their windows. Extra overlap fixtures are made through the real booking form in the verification process only. They are not startup/demo-seed changes. The final capture journey checks all six views at 1440x900 and 1024x768, timeline quarter-hour placement, 30/45/60-minute scaling, overlap column reuse, provider/date controls, show-cancelled, completed history, displayed EUR 52.00 quote, desktop action visibility and keyboard Tab/scroll/Enter behavior. Runtime remains offline.

Intermediate failures are recorded rather than counted as passes: the first sandbox build failed before compilation on dependency access, then approved Maven verification succeeded. Initial visual inspection found zero-sized hour labels and desktop booking actions below the initial viewport; both were corrected. A stronger keyboard test then caught a focus handler reordering grid children; removing that handler restored chronological traversal. Final verification above follows all code fixes. No production/test/CSS/script change followed the final build.

## Files changed in S7C

- `src/main/java/com/appointease/presentation/desktop/`: ActivityView.java, AppointmentDetailsView.java, AppointmentsView.java, BookingView.java, DesktopShell.java, ScheduleView.java, Ui.java.
- `src/main/resources/com/appointease/presentation/desktop/theme.css`.
- `src/test/java/com/appointease/presentation/desktop/DesktopS7BCheck.java`: extended opt-in journey; original 289 Surefire tests retained.
- `scripts/run-desktop.ps1`, `scripts/capture-ui.ps1`: S7C verification mode and separate capture destination; S7B captures preserved.
- Documentation: AGENTS.md, PROJECT_MEMORY.md, README.md, docs/engineering-history.md, docs/implementation-plan.md, docs/testing-strategy.md and this review.
- 27 ignored local PNGs in the directory below. No generated/edited images or uploads.

## Protected state and limits

Main and origin/main remain `b8417e91960c3e632c16b2a3e1b14763445c5382`. `AppointEase_DesignPatterns_Submission.zip` remains 1,372,171 bytes, SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. No staging, commit, merge or push occurred. The preexisting Git user-ignore permission warning remains.

Compact forms, longer histories and later hours scroll vertically; screenshot 16 shows lower compact pricing/actions, and 21/22 show the later-day cancelled entry. The compact review and receipt show their primary actions. Grid labels may ellipsize under high overlap density; tooltips and opened details retain full information. Very dense/custom catalogs, services shorter than catalog durations, exhaustive screen-reader accessibility, UI rendering across DST transitions and non-Windows graphics were not certified. Existing domain DST tests remain green; the elapsed-time grid calculation does not constitute full DST visual certification. Process-local data/receipts/logs, simulated integrations, synchronous operations and exact-slot availability limits remain unchanged. No known unresolved tested functional failure.

## Exact local screenshot paths

Requested outer windows are 1440x900 (desktop) and 1024x768 (compact). At 125% Windows display scaling, DWM excludes invisible resize margins: raw captures are 1424x892 and 1008x760 respectively. Screenshots show the live runtime date and synthetic fixtures. Existing S7B screenshots remain untouched.

| Exact path | Requested window | Saved pixels | Bytes |
| --- | --- | --- | --- |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\01-overview-desktop.png | 1440x900 | 1424x892 | 73006 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\02-overview-compact.png | 1024x768 | 1008x760 | 43914 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\03-appointments.png | 1440x900 | 1424x892 | 66642 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\04-new-appointment.png | 1440x900 | 1424x892 | 58261 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\05-providers.png | 1440x900 | 1424x892 | 57394 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\06-schedule.png | 1440x900 | 1424x892 | 55851 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\07-activity.png | 1440x900 | 1424x892 | 56569 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\08-appointment-details.png | 1440x900 | 1424x892 | 60161 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\09-booking-confirmation.png | 1440x900 | 1424x892 | 50681 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\10-conflict-error.png | 1024x768 | 1008x760 | 38619 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\11-appointments-compact.png | 1024x768 | 1008x760 | 45884 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\12-new-appointment-compact.png | 1024x768 | 1008x760 | 35322 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\13-booking-review.png | 1440x900 | 1424x892 | 58036 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\14-appointments-selected-desktop.png | 1440x900 | 1424x892 | 67839 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\15-appointments-selected-compact.png | 1024x768 | 1008x760 | 45907 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\16-new-appointment-pricing-compact.png | 1024x768 | 1008x760 | 37860 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\17-booking-review-compact.png | 1024x768 | 1008x760 | 41322 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\18-booking-confirmation-compact.png | 1024x768 | 1008x760 | 33531 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\19-appointment-details-compact.png | 1024x768 | 1008x760 | 41089 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\20-schedule-compact.png | 1024x768 | 1008x760 | 39368 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\21-schedule-cancelled-compact.png | 1024x768 | 1008x760 | 23808 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\22-schedule-cancelled-desktop.png | 1440x900 | 1424x892 | 42978 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\23-activity-compact.png | 1024x768 | 1008x760 | 34207 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\24-schedule-overlap-desktop.png | 1440x900 | 1424x892 | 61568 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\25-schedule-overlap-compact.png | 1024x768 | 1008x760 | 44904 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\26-schedule-completed-desktop.png | 1440x900 | 1424x892 | 52755 |
| C:\dev\appointease\docs\ui-review\screenshots\S7C\27-schedule-completed-compact.png | 1024x768 | 1008x760 | 36403 |
