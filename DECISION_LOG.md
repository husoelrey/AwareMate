# AwareMate - Decision Log

This log records major architectural and product decisions for the AwareMate project.

## Initial & P0 Architectural Decisions

### D-001: UI & Platform Framework
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Need a framework to build a cross-platform mobile app quickly with a unified UI.
- **Options Considered:** Flutter, React Native, .NET MAUI, Kotlin Multiplatform + Compose Multiplatform.
- **Decision:** KMP + Compose Multiplatform.
- **Rationale:** High portfolio impact, deep integration with the Kotlin ecosystem, native performance, and strong backing from Google/JetBrains.
- **Consequences:** 100% shared UI code using Compose across platforms.

### D-002: Backend Infrastructure
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Need a backend for user authentication, cloud sync, and remote notifications.
- **Options Considered:** Firebase, Supabase, Custom backend (Ktor/Spring).
- **Decision:** Firebase.
- **Rationale:** Fastest setup time, generous free tier, FCM for push notifications, and Firestore includes built-in offline caching.
- **Consequences:** NoSQL data modeling required; synchronized asynchronously with local database.

### D-003: Gamification Philosophy
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Need a mechanism to encourage user retention and positive digital habits.
- **Options Considered:** Punitive streaks (Habitica-style), Compassionate gamification (Finch-style).
- **Decision:** Compassionate gamification over punitive streaks.
- **Rationale:** Anti-shame design yields higher long-term retention for mental health/awareness apps. Aligns with Erasmus+ ethical guidelines.
- **Consequences:** Requires balanced math for "Momentum" (gradual decay, soft recovery) rather than binary streak resets.

### D-004: Companion Theme
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Selecting the visual representation of the user's progress.
- **Options Considered:** Animal (bird/dog), Abstract Orb, Plant-based.
- **Decision:** Plant-based companion (Seed -> Sprout -> Sapling -> Blooming Tree -> Ancient Tree).
- **Rationale:** Thematic connection to personal growth, peaceful aesthetic, aligns with environmental awareness modules.
- **Consequences:** UI design will feature organic shapes, greens, and earth tones.

### D-005: Launch Topology & Scope Boundaries
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Choosing target platform release timeline.
- **Options Considered:** Simultaneous multi-platform launch, iOS-first, Android-first.
- **Decision:** Android-first MVP (Phase 0-7) with KMP core; iOS/Desktop implementations deferred to v1.4+.
- **Rationale:** Fastest iteration speed, native Android `UsageStats` API access, largest global market share.
- **Consequences:** Keep platform-specific code minimal and scoped to current phase (AGENTS.md Section 9).

### D-006: Licensing Strategy
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Defining open source licensing.
- **Options Considered:** Closed source, GPL v3, MIT, Apache 2.0.
- **Decision:** Apache 2.0.
- **Rationale:** Enterprise-friendly open source, patent protection, transparent and compliant with non-monetized open-source goals.
- **Consequences:** Zero proprietary secrets in repo; clean open-source contributions.

### D-007: Localization Strategy
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** App language and i18n support.
- **Options Considered:** English-only hardcoded, English-first with i18n resource architecture.
- **Decision:** English-first with i18n infrastructure.
- **Rationale:** Global reach, ready for European youth demographic translations.
- **Consequences:** All strings extracted to resource files from day one.

### D-008: Presentation Architecture
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Managing UI state and events.
- **Options Considered:** MVVM, MVC, MVI.
- **Decision:** MVI (Model-View-Intent) pattern.
- **Rationale:** Enforces unidirectional data flow, predictable state transitions, high testability.
- **Consequences:** Explicit State and Intent definitions for UI screens.

### D-009: Navigation Library
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Screen navigation across Compose Multiplatform.
- **Options Considered:** Jetpack Navigation Compose, Decompose, Voyager.
- **Decision:** Voyager.
- **Rationale:** Simple API, native KMP Compose support, ScreenModel integration, Koin DI integration.
- **Consequences:** Standardized Voyager `Screen` implementations for screens and tabs.

### D-010: Local Persistence Layer
- **Date:** 2026-08-30
- **Status:** Accepted
- **Context:** Local single source of truth database.
- **Options Considered:** SQLDelight, Realm, Room KMP.
- **Decision:** Room KMP with SQLite Bundled driver.
- **Rationale:** Official Google KMP support, familiar DAO pattern, type safety with KSP.
- **Consequences:** Requires Room KMP runtime and compiler in Gradle Version Catalog.

### D-011: Multi-Module Architecture & Package Naming
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Project structure for KMP + Compose Multiplatform.
- **Options Considered:** Monolithic single module, Multi-module (`:androidApp`, `:shared`).
- **Decision:** Multi-module architecture with `:androidApp` application harness and `:shared` KMP library.
- **Namespaces:**
  - `org.awaremate.shared` for shared KMP library
  - `org.awaremate.android` for Android application module
- **Source Set Layout:**
  - `shared/src/commonMain/kotlin/org/awaremate/shared/` (domain, data, presentation, App.kt)
  - `shared/src/androidMain/kotlin/org/awaremate/shared/` (Platform.android.kt, Android-specific helpers)
  - `shared/src/iosMain/kotlin/org/awaremate/shared/` (Platform.ios.kt stub)
  - `shared/src/commonTest/kotlin/org/awaremate/shared/` (unit tests)
- **Rationale:** Clean separation of concerns, maximum code sharing, clean build isolation.

### D-012: Dependency Management via Gradle Version Catalog
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Centralizing library and plugin versions without hardcoding.
- **Decision:** Centralized Gradle Version Catalog (`gradle/libs.versions.toml`).
- **Rationale:** Single source of truth for versions, type-safe accessors in Gradle KTS (`libs.plugins.*`, `libs.*`), simplifies dependency updates.
- **Consequences:** Zero hardcoded version strings in subproject `build.gradle.kts` files.

### D-013: Repository Governance & Tool-Neutral Git Policy
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Keeping repository clean, reproducible, and tool-neutral per AGENTS.md Section 7.
- **Decision:** Comprehensive `.gitignore` covering `.idea/`, `.gradle/`, `build/`, `local.properties`, `google-services.json`, JVM crash logs (`hs_err_pid*.log`), and OS artifacts.
- **Rationale:** Prevents committing local IDE state or secrets; ensures clean CI/CD and developer onboarding.

### D-014: Gradle Wrapper & Build Tooling Baseline
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Guaranteeing consistent Gradle execution across development environments and CI.
- **Decision:** Gradle Wrapper version 8.11.1 with optimized JVM memory arguments (`-Xmx512m -XX:MaxMetaspaceSize=256m`) and `kotlin.native.ignoreDisabledTargets=true`.
- **Rationale:** Ensures Android Gradle Plugin 8.8.0 compatibility, low memory footprint on constrained host systems, and smooth KMP builds on non-macOS host environments.
- **Consequences:** Executable `gradlew` / `gradlew.bat` provided in repository.
