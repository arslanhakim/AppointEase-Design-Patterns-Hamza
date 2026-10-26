# AppointEase project memory

Last reviewed: October 27, 2026. Detailed evidence: [session history](docs/SESSION_HISTORY.md), [S6/S6.1 audit](docs/S6-AUDIT.md), [rubric evidence](docs/rubric-evidence.md).

## Current boundary

S0-S6 complete; S6.1 explicitly authorized package dependency cleanup, regression verification, documentation and repackaging only. S6.1 correction and release-payload verification complete (all acceptance checks pass on the extracted candidate). Final ZIP is regenerated after this handoff and undergoes the same checks; exact final identity/results are emitted in the external release report. S6.1 remains the development boundary. The user subsequently authorized Git hygiene, privacy cleanup, local verification, one initial release commit and private GitHub publication only. No new features or Moodle upload. Existing project files were untracked and preserved; local baseline snapshot supports review. Read AGENTS/memory/latest history/relevant docs and inspect actual Git/source before any further action.

## Implemented design and correction

- Nine patterns remain verified: Factory Method, Abstract Factory, Builder, Adapter, Decorator, Facade, Strategy, Observer, State. Fluent concrete Builder and modest appointment subtype differences remain disclosed academic qualifications.
- Four original package cycles resolved: model/state, model/mode, model/addon, application/event. Appointment, ConsultationAppointment, ServiceAppointment, ValidatedAppointmentSpec and AppointmentRules moved from domain.model to domain.appointment; CommittedEventDispatcher from application.event to application. Only package/import declarations changed in production and existing behavioral tests. Six FQCN migrations are the sole public compatibility change.
- Shared values stay in domain.model. State/mode/addon never depend on the aggregate; application.event never depends on command orchestration. All **19 production packages are acyclic**, with no exclusions. Five ArchitectureTest checks cover layers, packages, detector regression cases, source imports and static fields; no dependencies added.
- Main remains sole composition root. Immutable sealed appointments, injected Clock/explicit business zone, half-open occupancy, buffers and DST rules unchanged. One repository lock owns current-state validation/reservation/revision/event writes; shared dispatcher delivers ordered callbacks after commit/unlock. Console exposes the same eleven operations, exit and EOF.

## Actual S6.1 verification

- Java 21.0.12.1 / Maven 3.9.16: exit 0. Sandbox baseline failed before compilation (dependency access); approved baseline at 00:53:32 +05:00 passed all 264.
- Final workspace mvn clean verify, 00:57:23 +05:00: exit 0, **267 tests, 0 failures/errors/skips**. Original 264 retained plus three architecture invocations. All eleven concurrency scenarios pass. Intermediate new import-check failure fixed with explicit console contracts.
- Packaged demo and console QA scripts exit 0; all nine Java children exit 0, including normal exit and EOF. Runtime remains offline.
- Six UML sources validated/rendered; affected overview ownership/layout synchronized. PNG embedded sources match PUML and SVGs match independent fresh renders.
- Submission target: AppointEase_DesignPatterns_Submission.zip, 125 allowlisted entries; entry hashes verified by scripts/package-submission.ps1. Final hash/size supplied outside archive in release report (self-hash cannot be embedded). Fresh external-temp candidate extraction passed clean verify (267/0/0/0, exit 0 at 01:05:21 +05:00), demo and console QA (both scripts and all nine Java children exit 0). All 125 entries match source bytes. Final regeneration/re-extraction repeats those checks; results and exact ZIP hash/size are recorded in the final response and local .audit-tools/final-release.json without modifying archive contents.

## Limits and next action

No supported-workflow defect found; no package cycles remain. Process-local unbounded storage/logs/receipts, synchronous delivery, no durability/automatic retry, simulated integrations, exact-slot availability, English/wide console and non-exhaustive historical timezone/terminal coverage remain. Invalid zone prints an error but exits 0. Same-host/cache verification is not another-OS or clean-cache certification. Git user-ignore access warning persists.

No further development authorized. Release protocol: freeze this completed handoff, regenerate ZIP, repeat inventory/external extraction/build/runtime checks, report final identity, then stop after S6.1. User manual tasks: confirm Moodle template/size/deadline timezone, add identifiers/declaration, upload ZIP/evaluation form, verify attachment and final receipt before October 30. GitHub publication is explicitly authorized; Moodle upload and additional development remain unauthorized.


## GitHub publication (October 27, 2026)

- Repository created and confirmed **PRIVATE**: https://github.com/arslanhakim/AppointEase-Design-Patterns-Hamza, using the authenticated GitHub CLI account. Requested description retained. Initial inspection found unborn master, no commits, no remotes and 125 intended project files; no existing remote repository was found or overwritten.
- Publication branch: main. Initial commit message: Initial release: AppointEase appointment booking system. Repository-local author uses the authenticated account login and GitHub no-reply address; global identity remains unchanged. No AI/co-author attribution.
- Fresh mvn clean verify completed October 27 at 01:44:41 +05:00, exit 0: **267 tests, zero failures/errors/skips**. Java 21.0.12.1, Maven 3.9.16. Packaged scripts/verify-console.ps1 exited 0; all six children passed normal exit, EOF, input recovery and timezone checks. No Java/POM changes.
- Expanded .gitignore excludes build/Git/IDE/tool/temp/log/environment/credential/key/ZIP files. Intended-file scan found no credential/private-key/personal-path/personal-email signatures. Host-specific paths in historical evidence were replaced with portable references; historical outcomes preserved. README now maps all nine patterns to source packages.
- This handoff is written before the initial commit. Commit/push outcomes and exact resulting SHA must be verified from local HEAD, origin/main and GitHub main, then reported externally; no unexecuted push is claimed here. Use Git state to resume safely if interrupted. Never force-push.
- The verified S6.1 ZIP is preserved unchanged and ignored; it is the earlier academic artifact, so its documentation predates publication/privacy edits. It is not pushed. Stop after publication; no further development authorized.
