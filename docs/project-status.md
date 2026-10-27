# AppointEase project status

Last reviewed: October 27, 2026. Java 21 / Maven academic application demonstrating all nine GoF patterns. The console baseline S0-S6.1 is complete on `main`; `feature/javafx-ui` adds the six JavaFX views, booking/lifecycle flows, S7C visual refinement and accessible sidebar.

## Published milestones

| Milestone | Commit |
| --- | --- |
| Console release | `b8417e91960c3e632c16b2a3e1b14763445c5382` |
| JavaFX interface | `400b1b7ec325c5fc5654e1ef0d400306fc9563f7` |
| Sidebar navigation and toggle icons | `7de416eb1de201671d1bcec0bbd9fac44268500d` |

Local and GitHub branch inspection confirmed the sidebar publication and unchanged main on October 27. Repository visibility remains private. Detailed historical checks, including intermediate failures, are preserved in [engineering history](engineering-history.md). Reproducible commands and coverage: [testing strategy](testing-strategy.md).

## Design and behavior

- `Main.createContext` composes the shared facade and instance-owned services. `DesktopMain` supplies JavaFX lifecycle wiring. The console runs without JavaFX modules; both entry points run offline after Maven dependencies are cached.
- All six desktop destinations and booking/lifecycle flows use the existing facade and domain rules. The day grid uses actual start/duration geometry, overlap columns and optional cancelled history. Pricing shows decorator fees and totals; status remains textual as well as colored.
- The sidebar retains six native navigation buttons in a 214-pixel labeled mode or 64-pixel rail. Toggle actions preserve page, form, selection and scroll state. Pointer glyph feedback lasts 140 ms; keyboard activation is immediate. The Windows runner samples animation preference at startup; direct launches default to reduced motion.
- Domain ownership, immutable values, injected clocks, atomic reservation/rescheduling, DST resolution and post-commit delivery are documented in [architecture](architecture.md), [requirements](requirements.md) and the [pattern matrix](design-pattern-matrix.md).

## Evidence and limitations

Repository hygiene verification on October 27, 2026: Java/Maven version checks exited 0; `mvn clean verify` completed at 17:11:57 +05:00 with exit 0 and **289 tests, zero failures/errors/skips**, independently summed from Surefire XML. All five architecture checks passed. Packaged Windows checks passed on their first launches: sidebar **86 assertions** with motion enabled and **86** with reduced motion, S7C **77**, Overview **13**; each exited 0. Demo and console QA scripts exited 0 with **nine successful JAR children**. Source, tests, CSS, POM and UML are unchanged by this cleanup. The packaging manifest was evaluated without creating an archive: all 151 intended files exist, CSS is included, local captures and agent instructions are excluded. See the latest [engineering history](engineering-history.md) entry for the cleanup inventory and attribution audit.

The sidebar publication build on October 27 at 16:22:53 +05:00 passed all 289 tests (zero failures/errors/skips), including five architecture checks. Windows sidebar checks passed 86 assertions with motion enabled and 86 with reduced motion; S7C passed 77 and Overview 13. Demo and console regression scripts passed with nine successful packaged-JAR processes. Earlier native-input attempts failed before unchanged reruns passed; unattended Windows input reliability remains a limitation.

Visual records: [S7A](ui-review/S7A.md), [S7B](ui-review/S7B.md), [S7C](ui-review/S7C.md), [sidebar](ui-review/sidebar-toggle.md). Raw captures remain local and ignored. Requested 1440x900/1024x768 windows yielded 1424x892/1008x760 DWM images at 125% scaling; all four final sidebar-icon PNGs were decoded and visually reviewed.

Data, receipts and logs are process-local and unbounded; integrations are simulated and operations synchronous. Availability checks one exact slot. Compact content scrolls vertically; dense overlaps can ellipsize labels, with details/tooltips retaining text. Cancelled visibility resets when re-entering Schedule. Motion preference changes require restart. Non-Windows graphics, exhaustive screen-reader support, very dense/custom catalogs and DST-transition UI rendering remain unverified; domain DST tests pass. There is no installer, durable storage, automatic slot search or week/month view. Historical console invalid-zone handling prints an error but exits zero.

The separate academic console ZIP remains 1,372,171 bytes with SHA-256 `D00D7E0F9FF82FBD49F6D4B0E27914AACBC90765A7E3541087D546F88A1D1A3A`. Its historical contents differ from the current feature branch. Source-only hygiene does not regenerate it. Academic audit and packaging evidence: [S6 audit](S6-AUDIT.md), [rubric evidence](rubric-evidence.md).
