# S7A desktop visual review

Scope: JavaFX presentation shell and read-only Overview on `feature/javafx-ui`. S7B is not authorized. Main and the verified submission ZIP must remain unchanged; no commits, pushes or external screenshot uploads.

## Navigation and planned screen structure

| Destination | S7A state | S7B boundary, only after approval |
| --- | --- | --- |
| Overview | Implemented: daily summaries, appointment list, proportional agenda, upcoming visit, date navigation, empty state, refresh | Refresh after successful commands; add useful links to implemented destinations |
| Appointments | Unavailable navigation item | Search/filter current facade snapshots; selection and read-only details; lifecycle actions |
| New Appointment | Unavailable navigation item | Guided customer/provider/service, date/time, mode/options/policy, review and final confirmation |
| Providers | Unavailable navigation item | Catalog, supported services/modes, hours, duration and EUR fees |
| Schedule | Unavailable navigation item | Dedicated day/week schedule, provider lanes, date navigation; never infer availability from blank space |
| Activity | Unavailable navigation item | Separate simulated notification and audit history, event/revision detail and delivery warnings |

The small agenda embedded in Overview is not the S7B Schedule screen. Unavailable destinations have no placeholder pages or empty controllers. No booking, confirm, reschedule, cancel or complete handlers are wired in S7A.

## Theme and behavior

- Ink `#202A35`: navigation, headings, selected days and focus. Ivory `#F5F3ED`: workspace. Terracotta `#C56B4E`: selected navigation edge, pending agenda edge and upcoming accent. Sage `#7D927B`: confirmed agenda edge and restrained focus treatment.
- Georgia headings and numbers pair with Segoe UI controls and data. Both are local Windows fonts; no web-font/network dependency. Native font fallback applies on other platforms.
- White rows, thin neutral dividers and three-pixel radii. No gradients, decorative charts, icon library, shadows on content cards or animation. Status is conveyed by words as well as color. Smaller text uses darker variants rather than low-contrast terracotta/sage foregrounds.
- Shared JavaFX button/label/status helpers and one stylesheet. Native DatePicker and buttons retain keyboard behavior; focus has an explicit visible treatment. Date picker is selection-only to avoid a second free-text date parser.
- Below 850 logical pixels of Overview width, the agenda stacks below the appointment list. At compact sizes vertical scrolling is expected; Menu can release sidebar space. Windows display scaling changes the logical breakpoint; screenshot dimensions are physical pixels.
- Four appointments on the next weekday use the existing fictional catalog, both modes, reminder decorators and real facade book/confirm commands. Three confirmed, one pending. Historical console seed remains separate and available when its date is selected. Demo data resets on exit.
- Counts and appointment list reflect the selected day, including historical/cancelled snapshots. Cancelled visits remain in the list but not in occupied agenda blocks or providers-scheduled counts. Next appointment is a future pending/confirmed visit on that day. Reads emit no commands/events.

## Architecture

Main.createContext owns the same repository, clock, facade, creator registration, shared dispatcher, simulated calendar and observers previously constructed inside Main.create. Main.create retains its signature and constructs ConsoleApplication using that context. ApplicationContext is an immutable dependency bundle, not a singleton/container.

DesktopMain is the JavaFX lifecycle/composition entry point. It requests Main's shared composition, applies DesktopDemoSeed before exposing the UI, and injects OverviewModel into the view. Presentation imports only explicit facade contracts and domain display values; it cannot access repositories, dispatcher or infrastructure. Domain/application sources and the nine pattern implementations are unchanged.

ArchitectureTest retains all original five checks and all-class coverage. Its external-dependency rule admits `javafx.*` only in `presentation.desktop` and DesktopMain; named root types are explicitly allowlisted. Domain/application/console cannot import JavaFX. Package-cycle checks still traverse every project package. No test is deleted or disabled.

## Run and inspect

```powershell
java -version
mvn -version
mvn clean verify
powershell -NoProfile -File scripts/run-desktop.ps1
```

The Windows script runs the packaged application using locally cached platform modules, with no Maven/network at runtime. It accepts `-MavenRepository` for a nondefault local cache. `mvn javafx:run` is the cross-platform development entry point; native dependencies are chosen by Maven. Non-Windows graphical launch has not been verified.

In another PowerShell window, capture the running application:

```powershell
powershell -NoProfile -File scripts/capture-ui.ps1 -View Desktop
powershell -NoProfile -File scripts/capture-ui.ps1 -View Compact
powershell -NoProfile -File scripts/capture-ui.ps1 -View Navigation
```

The script selects only the exact AppointEase window, restores it, resizes it, brings it forward, captures raw visible pixels within its DWM frame bounds and reopens the PNG to validate its format. It does not edit or enhance captures. Keep the window unobscured. DWM excludes invisible resize margins, so resulting PNG dimensions can be slightly smaller than the requested 1440x900 or 1024x768 outer rectangles. `03-navigation.png` is the complete window with the sidebar, not another implemented screen.

Local screenshot directory: `C:\dev\appointease\docs\ui-review\screenshots\`. Ignored by Git. Do not stage/upload screenshots without explicit user authorization.

If desktop capture is unavailable: launch with run-desktop.ps1, resize the visible window to approximately 1440x900, press Alt+Print Screen and save the unmodified clipboard image as 01-overview-desktop.png using Paint. Repeat at approximately 1024x768 as 02-overview-compact.png. Restore the sidebar and desktop size, focus Overview using Tab, and save 03-navigation.png. Capture only the application window; check each file opens as PNG and contains readable UI with no unrelated windows.

## Verification

Five OverviewModelTest cases run with the normal suite: facade-backed data/events, immutable snapshots and cancellation/reschedule refresh, empty date/no writes, weekend/configured-zone seeding and independent console composition. The opt-in DesktopVisualCheck is compiled with test sources but is not a Surefire test: run `powershell -NoProfile -File scripts/run-desktop.ps1 -Verify` on a Windows desktop. It launches the real DesktopMain, checks rows, date/empty states, refresh, navigation, resize and keyboard Tab behavior, then closes its own window.

Final command outcomes and screenshot inventory are recorded below after execution. The intermediate original-suite run passed 267; the expanded suite passed 272. Sandbox Maven access and the initial plugin dependency download failed before launch; these are not passing checks. An initial optional GUI-harness compile error (Scene versus root lookupAll) was corrected before its execution.

## Final executed results (October 27, 2026)

- Java 21.0.12.1 / Maven 3.9.16: exit 0. Final `mvn clean verify`: exit 0, 12:05:03 +05:00, **272 tests, zero failures/errors/skips**. XML totals independently summed; original 267 retained. 83 production and 15 test source files compiled.
- `scripts/run-desktop.ps1 -Verify`: exit 0, 13 real-window checks passed, including keyboard Tab. Actual packaged GUI also launched offline on Windows 11, using local JavaFX modules; final review window left open. This is actual graphical verification, not inferred from compilation.
- `scripts/demo.ps1` and `scripts/verify-console.ps1`: exit 0; all nine Java/JAR children exit 0, including full lifecycle/conflict/reuse demonstration and timezone/input/EOF checks. Run again after final build.
- `git diff --check`: exit 0. Domain/application/existing adapters/console source and original behavioral test sources have no diff against main. No commits/pushes/staging. Main and origin/main remain at b8417e91960c3e632c16b2a3e1b14763445c5382.
- Submission ZIP remains 1,372,171 bytes with SHA-256 D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A, matching pre-edit evidence.

All final images were reopened, decoded as PNG, checked for the `89-50-4E-47-0D-0A-1A-0A` signature and visually inspected. These are raw running-application screenshots, not mockups. No external upload. Only Overview is implemented, so no other screen captures are claimed.

| Exact filename | Requested outer window | Saved PNG | Bytes |
| --- | --- | --- | --- |
| 01-overview-desktop.png | 1440x900 | 1424x892 | 69,178 |
| 02-overview-compact.png | 1024x768 | 1008x760 | 50,804 |
| 03-navigation.png | 1440x900 | 1424x892 | 69,167 |

Final visual observations: sidebar branding/footer and controls are readable; desktop shows all four appointment rows beside the agenda; compact labels wrap and the agenda moves below the list. At this machine's 125% display scaling, the lower summary/final agenda hours need scrolling even in the desktop capture; compact content intentionally scrolls. There is no horizontal content clipping in these captures. No animation or image enhancement was applied. An intermediate capture contained an obscuring editor window; it was rejected and overwritten. The capture script now temporarily raises only AppointEase and checks window ownership before saving, then restores normal window ordering.

## Files changed

Created:

- src/main/java/com/appointease/ApplicationContext.java
- src/main/java/com/appointease/DesktopMain.java
- src/main/java/com/appointease/infrastructure/seed/DesktopDemoSeed.java
- src/main/java/com/appointease/presentation/desktop/DesktopShell.java
- src/main/java/com/appointease/presentation/desktop/OverviewModel.java
- src/main/java/com/appointease/presentation/desktop/OverviewView.java
- src/main/java/com/appointease/presentation/desktop/Ui.java
- src/main/resources/com/appointease/presentation/desktop/theme.css
- src/test/java/com/appointease/presentation/desktop/OverviewModelTest.java
- src/test/java/com/appointease/presentation/desktop/DesktopVisualCheck.java
- scripts/run-desktop.ps1
- scripts/capture-ui.ps1
- docs/ui-review/S7A.md

Modified:

- .gitignore
- pom.xml
- src/main/java/com/appointease/Main.java
- src/test/java/com/appointease/ArchitectureTest.java
- AGENTS.md
- PROJECT_MEMORY.md
- README.md
- docs/architecture.md
- docs/requirements.md
- docs/design-pattern-matrix.md
- docs/implementation-plan.md
- docs/testing-strategy.md
- docs/SESSION_HISTORY.md

The three ignored PNGs listed above are additional local review artifacts. No existing file was deleted; no submission archive or UML artifact was regenerated. Historical UML describes the preserved console baseline; the S7A composition extension is documented here and in architecture.md.

## S7B boundaries and known limits

S7A is a read-only visual prototype, not a complete desktop replacement for the console. Activity and write workflows remain unimplemented. Booking conflict/validation/reschedule/cancel interactions through desktop controllers must be tested in S7B; the S7A model tests do not claim that coverage. Existing console/domain integration tests continue to cover those rules.

In S7B, submit immutable requests to BookingFacade; keep command IDs/requests for retry semantics; use DomainTime.resolve for local input and show the chosen offset; never duplicate hard validation or state-transition rules. Distinguish validation failures from committed success with delivery warnings. Preserve original reservations after failed reschedules and refresh after successful commands. Introduce background tasks only when work warrants them, with JavaFX-thread-safe updates and no double submissions.

The Overview agenda displays 09:00-17:00 for the current demo catalog. Simultaneous records get narrow provider lanes and tooltips; arbitrary catalogs, dense schedules, large datasets and assistive-technology certification remain outside S7A. Counts/rows are loaded synchronously from the small process-local dataset, and the view does not refresh on a timer. No installer or bundled Java runtime is produced.

Stop here for visual approval. Do not implement S7B, commit, push or repackage the submission ZIP.
