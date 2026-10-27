# Sidebar toggle refinement

October 27, 2026. Completed locally on `feature/javafx-ui`, based on published commit `400b1b7ec325c5fc5654e1ef0d400306fc9563f7`. The original local refinement is documented below. The user subsequently approved the final sidebar-icon screenshots and authorized feature-branch publication; see the latest SESSION_HISTORY.md entry for fresh QA and publication gates. Main, business rules and the academic ZIP remain protected.

## Visual and interaction changes

Final icon correction: the toolbar now uses a full leftward collapse arrow and rightward expand arrow beside a narrow left panel, with 19.5px visible bounds, 1.5px strokes and an Ink stroke color. The four final screenshots in `docs/ui-review/screenshots/sidebar-icon-final/` supersede the earlier toolbar glyph examples below and are visually approved. They remain local and ignored; the exact inventory and final verification are preserved in [session history](../SESSION_HISTORY.md).

| Before | After |
| --- | --- |
| Text-based Menu action in the toolbar | Quiet 36-pixel native button with a local SVG panel-left-close/open arrow indicating collapse or expand |
| Collapsing removes navigation entirely | Persistent 64-pixel icon rail with all six destinations; expanded navigation remains 214 pixels wide with icons and labels |
| Text-only navigation | Consistent 1.5-pixel vector strokes, current-page terracotta edge and flat selected background |
| Hidden navigation requires reopening | Rail buttons retain accessible names and show flat tooltips; keyboard focus is distinct from selection |
| No state-specific toggle label | Collapse sidebar / Expand sidebar accessible text and tooltip, plus accessible state help |

Ink #202A35, Ivory #F5F3ED, Terracotta #C56B4E and Sage #7D927B remain. Georgia headings and the existing flat CSS architecture remain. No image/font/dependency, gradient or shadow was added. Navigation tooltips explicitly remove the default JavaFX shadow.

The same sidebar, navigation buttons and current content stay mounted. A toggle changes layout constraints once, without recreating pages or animating width. Compact windows start with the rail, and choosing a compact destination restores it. A manual desktop state persists during navigation. Crossing the compact breakpoint retains the existing automatic mode behavior.

Motion gate: occasional pointer interaction, with a short acknowledgment of the changed glyph. JavaFX FadeTransition changes only glyph opacity from 0.65 to 1 over 140 ms, using spline (0.23, 1, 0.32, 1); an interrupted fade continues from its current opacity. Keyboard activation is immediate. The runner reads Windows `SystemParameters.ClientAreaAnimation` at startup, using the built-in PresentationFramework assembly; the API exposes whether client-area animations are enabled ([Microsoft documentation](https://learn.microsoft.com/en-us/dotnet/api/system.windows.systemparameters.clientareaanimation?view=windowsdesktop-10.0)). `-ReduceMotion` disables the fade. Failed preference detection or direct launches without `appointease.reduceMotion` default to reduced motion. OS preference changes take effect on restart. No OS setting is changed.

## Executed verification

All commands below exited 0 after the final source/CSS/harness changes:

| Command | Actual result |
| --- | --- |
| `java -version`, `mvn -version` | Temurin 21.0.12.1; Maven 3.9.16 |
| `mvn clean verify` | BUILD SUCCESS at 15:52:03 +05:00; **289 tests, 0 failures, 0 errors, 0 skipped**; independent Surefire XML sum agrees; all five architecture checks pass |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -Capture` | **86 real-window assertions**, reducedMotion=false; seven final after captures |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifySidebar -ReduceMotion` | **86 real-window assertions**, reducedMotion=true |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS7C` | **77 retained real-window assertions**, full booking/lifecycle/validation/conflict/grid/keyboard/both-size journey |
| `powershell -NoProfile -File scripts/run-desktop.ps1 -Verify` | **13 retained Overview assertions** |
| `scripts/demo.ps1`, `scripts/verify-console.ps1` | Both exit 0; **nine packaged JAR child processes exit 0** |

The new opt-in harness tests actual packaged DesktopMain, real Robot clicks, eight repeated toggles, Space/Enter, current action labels/glyphs, focus retention, all six rail destinations, actual tooltip visibility, keyboard rail navigation, preserved search/selection, lifecycle confirmation, booking draft/review and Activity scroll position. All six destinations are checked at 1440x900 and 1024x768 with both expanded and rail navigation, including content/button widths and navigation bounds. Windows checks run sequentially and close their windows. The existing 289 JUnit tests are unchanged; GUI harness assertions are additional checks, not Surefire tests.

Early Robot checks failed because the harness window was not reliably in front, including after the capture helper released its native topmost flag. The harness now positions the test window explicitly, keeps it visible, restores its foreground state after capture and allows native move/press/release events to settle. No retry or bypass masks failed assertions. These corrections affect only the opt-in test setup. The final checks above passed afterward.

## Real screenshots

Eleven raw actual-window PNGs, all checked nonempty, with valid PNG signatures and successful System.Drawing decoding. Before images were captured from the unchanged published JAR before rebuilding production changes. After images were captured from the final verified JAR. No generated, edited or uploaded images. All remain Git-ignored.

Requested outer windows are 1440x900 and 1024x768. At the tested Windows 125% display scale, DWM captures exclude invisible resize margins and save 1424x892 and 1008x760 pixels respectively. The captured modes, tooltip and retained draft were visually inspected.

| Exact screenshot path | Saved pixels | Bytes |
| --- | --- | --- |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\01-before-desktop-expanded.png` | 1424x892 | 70347 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\02-before-desktop-hidden.png` | 1424x892 | 58971 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\03-before-compact-hidden.png` | 1008x760 | 42768 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\04-before-compact-expanded.png` | 1008x760 | 50443 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\05-after-desktop-expanded.png` | 1424x892 | 73501 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\06-after-desktop-rail.png` | 1424x892 | 67231 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\07-after-compact-rail.png` | 1008x760 | 47596 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\08-after-compact-expanded.png` | 1008x760 | 52464 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\09-after-compact-tooltip.png` | 1008x760 | 48199 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\10-after-compact-draft-rail.png` | 1008x760 | 39998 |
| `C:\dev\appointease\docs\ui-review\screenshots\sidebar-toggle\11-after-compact-draft-expanded.png` | 1008x760 | 46898 |

## Scope and limitations

Changed implementation: DesktopShell.java, new NavigationIcon.java and theme.css. Tooling: run-desktop.ps1 and capture-ui.ps1. Verification: new DesktopSidebarCheck.java and minimal rail-expectation updates in DesktopVisualCheck.java/DesktopS7BCheck.java. Handoff: AGENTS.md, README.md, PROJECT_MEMORY.md, docs/SESSION_HISTORY.md, docs/testing-strategy.md and this review. No domain/application/infrastructure/console/composition/controller/dependency or existing JUnit-source change.

Compact/lower forms and histories scroll vertically; supported-size checks found no horizontal overflow or clipped controls. The sidebar changes available content width once; responsive content reflows normally. Motion feel remains subjective and can be reviewed live using the runner. The OS preference is sampled only at startup, and screen-reader certification and other operating systems remain unverified. Existing S7C data-volume and process-local/simulated-integration limitations remain.

At the original local refinement handoff, HEAD and origin/feature/javafx-ui remained `400b1b7ec325c5fc5654e1ef0d400306fc9563f7`; main/origin/main remain `b8417e91960c3e632c16b2a3e1b14763445c5382`. Academic ZIP: 1,372,171 bytes; SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. No staging, commit, push, merge or ZIP regeneration in this refinement. That historical review gate was satisfied by the final sidebar-icon visual approval.
