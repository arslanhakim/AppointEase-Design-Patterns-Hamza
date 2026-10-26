# Project instructions

AppointEase is a Java 21 / Maven academic console project. S0-S6 and S6.1 are complete. S6.1 explicitly authorized package dependency cleanup, regression verification, documentation and repackaging only. Stop after S6.1; further implementation requires explicit authorization. No commits unless requested after inspecting repository state.

October 27 publication authorization: the user explicitly requested Git hygiene, privacy cleanup, final local verification, an initial release commit and a private GitHub publication under the authenticated account. This authorizes publication only; no business logic, features or frontend work. Stop after publication and verification.

## Permanent session management

- Every new or resumed session must read PROJECT_MEMORY.md, this AGENTS.md, the latest docs/SESSION_HISTORY.md entries, and relevant requirements/architecture/pattern/slice documentation before editing code.
- Inspect actual source code, tests and Git status. Do not trust memory blindly or assume an untracked file is disposable. Preserve existing work and reconcile discrepancies against actual evidence and the user's current instructions.
- Implement only the currently authorized development slice. A roadmap or recommendation is not authorization. Never automatically start another slice.
- Run `mvn clean verify` after implementation; record the actual command outcome, exit status and test counts. A blocked/failed/unrun check must never be reported as passed. Recheck the runnable application when closing a slice.
- Before ending a slice, update PROJECT_MEMORY.md and append a dated entry to docs/SESSION_HISTORY.md with scope/authorization, completed files, significant decisions, verification, blockers and the next boundary. Record interrupted/incomplete work honestly so a new session can resume safely.
- Keep the main memory file concise and current; put detailed chronological evidence in session history and link relevant documents. Preserve historical outcomes; append corrections rather than silently rewriting them.
- Record uncertainty, limitations and blockers explicitly. Distinguish implemented/tested facts from planned contracts and unverified assumptions.
- Never make Git commits without explicit authorization. Never add AI attribution to commits.

## Architecture and verification

Use `com.appointease`. Keep domain independent of application, infrastructure, and presentation. Application owns repository and calendar ports; infrastructure implements them. Main is the single composition root. Use constructor injection, immutable values, an injectable Clock, and no mutable global state.

Follow docs/requirements.md and docs/architecture.md for rules, transaction boundaries, DST resolution, and lifecycle behavior. Implement only authorized slices; never add empty placeholder classes. All planned classes and tests must be labelled planned until implemented.

Verification: `java -version`, `mvn -version`, `mvn clean verify`, then `java -jar target/appointease-0.1.0-SNAPSHOT.jar`; record actual outcomes and exit codes. Runtime must work offline. Maven dependency downloads at build time are allowed. No real external integrations, frameworks, databases, payment handling, or sensitive medical data.
