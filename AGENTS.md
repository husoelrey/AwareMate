# AwareMate - Agent Operation Rules & Project Context

## 1. REQUIRED READING ORDER
All agents operating within this repository MUST consume context in the following sequence:
1. **AGENTS.md** (This file - Behavioral & boundary rules)
2. **PLAN.md** (Execution phases and current tasks)
3. **CURRENT_STATUS.md** (Real-time sprint state - if exists)
4. **PROJECT_SPEC.md** (Detailed technical specs - if exists)
5. **DECISION_LOG.md** (Architectural decisions - if exists)

This full read applies at the start of a session or after a context reset — not on every message turn within the same session.

## 2. CONFLICT PRIORITY
If conflicting instructions or patterns are encountered, prioritize in this order:
1. Direct User Instruction
2. This `AGENTS.md` file
3. `DECISION_LOG.md`
4. `PROJECT_SPEC.md`
5. Legacy code patterns or implicit assumptions

## 3. PRODUCT BOUNDARIES
AwareMate is a **compassionate awareness companion app** designed to help youth align their digital habits with personal growth.
- **IT IS:** An empathetic, gamified (non-punitive) system for building healthy habits.
- **IT IS NOT:** A medical device, therapy replacement, rigid parental control app, absolute screen-blocking tool, or social media platform.

## 4. NON-NEGOTIABLE PRINCIPLES
- **Compassionate UX:** No punitive streaks. No shame-driven design. No guilt-inducing notifications. Focus on momentum and gentle nudges.
- **Youth Safety:** Age-appropriate content. Zero dark patterns. No predatory monetization (100% free app).
- **Accessibility:** Must meet WCAG 2.1 AA standards. Built-in support for TalkBack/VoiceOver. Inclusive design across all modules.
- **Open Source:** Licensed under Apache 2.0. No proprietary lock-in.
- **Sustainability:** 100% free for users. Infrastructure costs (Firebase, hosting) covered via optional donations/sponsorship, never ads or data monetization.

## 5. TECHNICAL BASELINE
All code must adhere to the following stack:
- **Language:** Kotlin 2.2+
- **UI Framework:** Compose Multiplatform
- **Architecture:** KMP multi-module (shared domain/data/presentation, platform-specific UI)
- **Build System:** Gradle KTS + Version Catalog (`libs.versions.toml`)
- **Database:** Room KMP
- **Dependency Injection:** Koin
- **Navigation:** Voyager
- **Networking:** Ktor Client
- **Backend:** Firebase (Auth, Firestore, FCM)
- **Media/Charts:** Coil 3, Vico charts
- **Core Libraries:** kotlinx.coroutines, kotlinx.serialization, kotlinx.datetime

## 6. ARCHITECTURE RULES
- **Clean Architecture + MVI:** Strict separation of concerns.
- **Shared Code:** Maximize code sharing in `shared` (domain, data, presentation modules).
- **Platform Specifics:** Keep platform-specific code minimal in `androidApp`, `iosApp`, `desktopApp`, `webApp`.
- **MVI Pattern:** UI communicates Intents to ViewModels, which update State via Reducers.

## 7. DEVELOPMENT BEHAVIOR
- Operate on feature branches whenever possible.
- Complete one bounded task at a time.
- Verify functionality before marking a task complete (tests passing, UI verifying).
- Maintain tool-neutral branches (do not commit IDE-specific configurations).

## 8. DEFINITION OF DONE
A feature or task is complete when:
1. The requested behavior exists and functions correctly (verified by running/building, not just reading the code).
2. If the task touches business logic (domain/data layers): at least one unit test covers the main path.
3. If the task touches UI: basic accessibility is in place (content descriptions on interactive elements). A full TalkBack/contrast audit is deferred to the P7 polish phase, not required per-task.
4. Documentation is updated only when the change affects architecture, public APIs, or setup steps — routine internal changes don't require doc updates.

Apply this DoD contextually, not as a checklist to pad every commit. Don't block early-phase tasks (P2-P3, before any UI exists) on accessibility audits that don't yet apply.

## 9. AGENT EFFICIENCY RULES
- Re-read AGENTS.md / PLAN.md / DECISION_LOG.md at the start of a session or after a context reset — not on every message turn.
- Keep `CURRENT_STATUS.md` up to date at the end of a session so the next session can resume from it instead of re-deriving state by re-reading the whole codebase.
- Build only for the current phase's scope. Don't add abstraction, config, or code for platforms/features not yet in scope (e.g. no iOS/Desktop-specific scaffolding before v1.4 — see ROADMAP.md).
- Keep changes scoped to the requested task. Don't refactor unrelated code, rename things, or "clean up while you're in there" unless asked — that's how small tasks turn into large diffs and regressions.
- When a design decision is made (architecture, library choice, data model), write it to `DECISION_LOG.md` immediately so it isn't re-litigated or re-explored in a future session.
- Prefer the simplest implementation that satisfies the current task over a more "flexible" or "future-proof" one — add flexibility when a second real use case appears, not before.
