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

### D-015: Firebase Integration Architecture & Client Tooling Baseline
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Preparing client build system for Firebase Auth, Firestore, Cloud Messaging, and Analytics without leaking credentials or blocking local developer workflows.
- **Decision:** Apply Google Services plugin (`com.google.gms.google-services:4.4.2`) and Firebase BOM (`33.9.0`) in `:androidApp`. Provide `androidApp/google-services.json.example` template with dummy credentials for local/CI compilation while keeping `google-services.json` in `.gitignore`.
- **Rationale:** Enables clean decoupled development; developers can immediately build and run unit tests without requiring a pre-configured Firebase backend project.
- **Consequences:** CI and local onboarding copy the template file automatically if `google-services.json` is missing.

### D-016: GitHub Actions Continuous Integration (CI) Workflow Strategy
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Automating build, lint, and test validation on every push and pull request to `main`.
- **Decision:** GitHub Actions workflow (`.github/workflows/ci.yml`) on `ubuntu-latest` with JDK 17 (Temurin), official `gradle/actions/setup-gradle@v4` caching, running `lintDebug`, `test`, and `assembleDebug`.
- **Rationale:** Fast feedback loop (<5 min execution), scoped strictly to current active target (Android/KMP shared) per AGENTS.md Section 9, preventing regressions.
- **Consequences:** No extraneous deployment or multiplatform jobs until those target platforms enter project scope.

### D-017: Local-First Data Architecture & Background Firestore Synchronization Strategy
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Designing data persistence and cloud synchronization to guarantee 100% offline capability, immediate UI responsiveness, and zero data loss on intermittent connectivity.
- **Decision:** Establish Room KMP as the Single Source of Truth (`SSOT`). All write operations (`saveCompanion`, `insertMoodEntry`, `saveSession`, `completeChallenge`) write to Room first. Asynchronous background synchronization with Firestore occurs non-blockingly. Each entity maintains an `isSynced` flag; failure to reach Firestore never fails local UI actions.
- **Conflict Resolution Strategy:**
  1. **Append-Only Records (`MoodEntry`, `FocusSession`, `DailyChallenge`):** Keyed by client-generated UUIDs with timestamps. In case of concurrent sync, records are unioned/merged by ID with zero data loss.
  2. **Mutable Singleton State (`Companion`, `UserPreferences`):** Resolved using **Last-Write-Wins (LWW)** based on `lastUpdatedEpochMs`. In the event of exact timestamp tie, local mutations take precedence over remote values to protect offline user engagement.
- **Rationale:** Eliminates network latency from UI user experience, supports complete offline usage, and aligns with compassionate habit tracking where users can log mood and complete focus sessions anywhere.
- **Consequences:** `SyncRepository` provides explicit methods to sync pending items when network connectivity is restored.

### D-018: Room KMP Persistence Model & Multiplatform Database Driver
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Implementing multiplatform database with Kotlin 2.1+ and KSP.
- **Decision:** Use Room 2.7.0 (`androidx.room`) with SQLite Bundled Driver (`androidx.sqlite:sqlite-bundled:2.5.0`), `@ConstructedBy(AwareMateDatabaseConstructor::class)`, and KSP code generation.
- **Rationale:** Standardized Room DAO patterns across Android and native KMP targets without JVM dependencies.

### D-019: DataStore Preferences Multiplatform Architecture
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Storing lightweight user settings (onboarding flag, notification thresholds, bedtime reminder time, theme mode).
- **Decision:** Implement `PreferencesRepository` backed by `androidx.datastore.preferences.core.DataStore<Preferences>` with `PreferenceDataStoreFactory.createWithPath`.
- **Rationale:** Type-safe, asynchronous Flow-based preferences persistence across KMP targets.

### D-020: Companion Growth Stage Thresholds & Evolution Rules
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Establishing clear XP boundaries for companion evolution stages aligned with the plant metaphor.
- **Decision:** Stages defined as `SEED` (0..99 XP), `SPROUT` (100..299 XP), `BLOOM` (300..599 XP), `TREE` (600..999 XP), `ANCIENT_TREE` (1000+ XP). XP earned across four core categories (`HAPPINESS`, `ENERGY`, `WISDOM`, `CREATIVITY`) sums into total XP.
- **Rationale:** Aligns with Erasmus+ youth competencies and anti-shame design; stage progression is purely accumulative and irreversible downwards (companion never regresses in stage).

### D-021: Non-Punitive Momentum Decay & Comeback Bonus
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Implementing retention gamification without punitive streaks or shame triggers.
- **Decision:** Replace binary streak resets with gradual exponential decay ($score \times 0.90^{daysInactive}$) on a 0.0..100.0 scale. A 1-day lapse results in gentle 10% decay (90.0) rather than a complete zero reset. A Comeback Bonus ($1.5\times$ activity gain) is granted upon return after $\ge 2$ inactive days to accelerate recovery.
- **Rationale:** Strict adherence to AGENTS.md Compassionate UX principles; supports intrinsic motivation and long-term user retention.

### D-022: Pure Companion Emotional State Machine
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Modeling companion emotional reactions to user actions, inactivity, and circadian rhythms.
- **Decision:** Pure state machine with emotions `PEACEFUL`, `CURIOUS`, `CHEERFUL`, `TIRED`, `RESTING`. Inactivity transitions companion to `RESTING` (peacefully napping, never sad/guilty). Deep focus sessions and challenge completions trigger `CHEERFUL`. Re-opening after absence triggers `CURIOUS` (welcoming).
- **Rationale:** Pure transition function guarantees deterministic, easily testable, anti-anxiety emotional feedback.

## Phase 4 Architectural Decisions

### D-023: Material 3 Organic Design System & Dynamic Color Integration
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Establishing visual design and color identity that reinforces anti-shame, compassionate wellness principles while supporting Android 12+ wallpaper dynamic theming.
- **Decision:** Built an organic Material 3 theme (`AwareMateTheme`) using deep forest green (`#2D5A27`), warm sage (`#8FBC8F`), and amber highlights, with gentle coral (`#E07A5F`) for error/warning states (eliminating aggressive high-saturation red). `rememberDynamicColorScheme` hook leverages Android 12+ (`SDK_INT >= 31`) system color extraction when enabled, with seamless fallback to custom light/dark schemes.
- **Rationale:** Warm, nature-inspired palette fosters mental calm and alignment with the plant companion metaphor; gentle coral alerts inform without inducing panic or guilt.
- **Consequences:** All interactive elements and custom cards adopt rounded organic shapes (`16.dp`–`24.dp` corners) and standard Material 3 color roles.

### D-024: Voyager Multiplatform Navigation & MVI ScreenModel Architecture
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Selecting navigation structure and state management pattern for multi-tab dashboard, onboarding flow, and nested modal screens.
- **Decision:** Implemented Voyager navigation with `RootScreen` splash router checking `PreferencesRepository.onboardingCompleted`. After onboarding, transitions to `MainScreen` hosting a 5-tab `NavigationBar` (Home, Companion, Focus, Growth, Settings). Each screen pairs with a Voyager `ScreenModel` managing unidirectional MVI (`StateFlow<State>` and `handleIntent(Intent)`).
- **Rationale:** Voyager provides clean decoupled screen state lifecycles with built-in Koin integration (`koinScreenModel()`), cross-platform backstack management, and transitions.
- **Consequences:** ScreenModels live in `commonMain` and can be tested identically across JVM and native targets without Compose runtime mocking.

### D-025: Pure Compose Canvas Companion Rendering Engine
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Rendering the gamified companion character across 5 growth stages and 5 emotional states without relying on heavy external asset files or proprietary vector engines.
- **Decision:** Built a pure Compose `Canvas` drawing engine (`CompanionCanvas`) with mathematical bezier curves, layered lighting gradients, procedural particles (sparkles, sleepy 'Z's, ambient fireflies), and multi-layer animations (`rememberInfiniteTransition` for breathing pulse, leaf sway, and spring tap bounce).
- **Rationale:** 100% lightweight, scalable to any display density, zero bitmap dependencies, instant load times, and dynamic emotional facial variations (open eyes, cheerful smiles, peaceful blinks, tired eye-curves).
- **Consequences:** Every visual attribute of the companion is parameterized and reactive to domain state changes with built-in accessibility semantic descriptions.

## Phase 5 Architectural Decisions

### D-026: Android UsageStats API On-Device Aggregation & Permission Bridge
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Accessing device app usage without violating user privacy or failing permission checks. `PACKAGE_USAGE_STATS` is a special system permission requiring navigation to Android Settings rather than standard runtime dialogs.
- **Decision:** Created `hasUsageStatsPermission(context)` via `AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS)` and `openUsageAccessSettings(context)` directing users to `Settings.ACTION_USAGE_ACCESS_SETTINGS`. Aggregated metrics are stored in Room's `ScreenTimeSnapshotEntity` 100% on-device (zero telemetry/transmission), respecting GDPR and local-first principles.
- **Rationale:** Transparent consent flow and complete privacy protection while maintaining accurate screen time tracking.
- **Consequences:** UI displays clear guidance when access is missing, offering an empathetic explanation of on-device privacy.

### D-027: Vico Multiplatform Chart Architecture with Native Android & Canvas Fallback
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Rendering weekly screen time analytics charts across multiplatform targets while leveraging modern Vico 2.0.3 charting on Android.
- **Decision:** Defined `expect @Composable fun ScreenTimeBarChart(data: List<DailyScreenTimeData>, dailyGoalMinutes: Int)`. Implemented with Vico (`CartesianChartHost`, `rememberColumnCartesianLayer`, `HorizontalAxis.rememberBottom`, `VerticalAxis.rememberStart`) on Android, with a pure Compose Canvas fallback on iOS.
- **Rationale:** Delivers high-performance native Cartesian charts with smooth animations on Android while keeping the shared presentation layer 100% multiplatform compliant.
- **Consequences:** Allows instant cross-platform compilation while maximizing native visual fidelity on the primary Android target.

### D-028: Non-Punitive Mindful Nudge & Digital Sunset Architecture
- **Date:** 2026-09-02
- **Status:** Accepted
- **Context:** Triggering reminder notifications for continuous usage and evening wind-down without guilt, shame, or cognitive overload.
- **Decision:** Implemented `MindfulNudgeRuleEngine` enforcing continuous usage thresholds (30, 45, 60m), daily screen time goals, 30-minute minimum cooldown intervals, and strict muting during active `FocusSession`s. All notification messages (`MindfulNudgeCatalog`) are vetted against strict anti-guilt rules (zero shame words like "failed", "too much", "wasted"). Implemented `DigitalSunsetUseCase` calculating a 45-minute pre-bedtime sunset window with gentle twilight banners and companion `RESTING` sleep states.
- **Rationale:** Direct adherence to AGENTS.md Compassionate UX principles; nurtures self-awareness and healthy transitions rather than resentment or anxiety.
- **Consequences:** Users can customize bedtime and nudge thresholds in Settings with complete confidence in supportive feedback.

## Phase 6 Architectural Decisions

### D-029: Room KMP Persistence Model for Offline Hobbies & Self-Discovery Prompts
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Delivering 100% offline, persistent storage for offline hobby exploration, bookmarking, session completion, and curiosity-driven self-discovery reflections per PROJECT_SPEC.md Section 9.
- **Decision:** Expanded `AwareMateDatabase` to version 2 with `HobbyEntity` and `SelfDiscoveryPromptEntity` tables backed by `HobbyDao` and `SelfDiscoveryPromptDao`. Enabled `.fallbackToDestructiveMigration(true)` during active development across Android and iOS builders. Repositories automatically populate initial offline seed catalogs (`HobbyCatalog`, `SelfDiscoveryCatalog`) on first launch.
- **Rationale:** Guarantees zero network dependency for habit and growth tracking, immediate local reactivity, and type-safe query flows.
- **Consequences:** Hobbies and reflection observations persist reliably across app restarts without remote server latency.

### D-030: Pure Compose Canvas Radial Breathing Guide Engine
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Providing animated pacing guidance for calming exercises (Box Breathing, 4-7-8, Grounding Reset) without heavy third-party animation libraries or video assets.
- **Decision:** Built an animated radial visualizer using Compose Canvas, `animateFloatAsState`, radial gradients, and a multi-ring glowing halo. State is driven by a coroutine-backed state machine (`BreathingSessionState`) emitting 100ms pacing ticks.
- **Rationale:** 100% lightweight, battery-efficient, accessible with live semantic descriptions, smoothly scalable across all display densities, and awards XP (+20 Energy XP) directly to the companion upon completion.
- **Consequences:** Smooth, soothing visual feedback that naturally transitions companion emotion to `PEACEFUL`.

### D-031: Compassionate Mood Journaling & Anti-Shame Weekly Insights Engine
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Empowering youth to track emotional climates without anxiety, fear of failure, or guilt from skipping days or feeling down.
- **Decision:** Implemented `MoodCheckInDialog` offering a 5-level non-stigmatizing emoji picker (`😄`, `😊`, `🌿`, `🥱`, `🌧️`), energy slider (1-5), optional reflection notes, and contextual tags. Synchronized asynchronously with Firestore via `CloudSyncService`. Designed `GetWeeklyMoodInsightsUseCase` to aggregate the last 7 days of entries with a visual rhythm bar and compassionate narrative takeaways that validate all feelings ("every season matters 🌧️").
- **Rationale:** Strict adherence to AGENTS.md Section 4 (Compassionate UX, zero dark patterns, no shame-driven metrics).
- **Consequences:** Fosters psychological safety and sustainable self-reflection habits.

## Phase 7 Architectural Decisions

### D-032: Multiplatform Network Connectivity Observer & Offline-First Resilience
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Ensuring AwareMate functions 100% offline without crashing, stalling, or throwing unhandled network exceptions when airplane mode or spotty connectivity occurs.
- **Decision:** Implemented `ConnectivityObserver` (Android `ConnectivityManager.NetworkCallback` + StateFlow, iOS stub) registered in Koin DI. Enhanced `SyncRepositoryImpl` and `MoodRepositoryImpl` to silently catch network drops, maintain unsynced items in Room with `isSynced = false`, and sync queues seamlessly upon reconnection. Added `AppContextProvider` static fallback for application context safety across all Android platform helpers.
- **Rationale:** Youth must never experience fear of data loss or confusing error dialogs while offline.
- **Consequences:** The app operates identically offline and online; local SQLite Room DB is the unwavering Single Source of Truth.

### D-033: Non-Intrusive In-App Privacy Architecture & Voluntary Sponsorship Model
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Fulfilling Google Play Store and Firebase Auth privacy compliance while honoring strict open-source sustainability rules (no ads, no paywalls).
- **Decision:** Authored comprehensive `PRIVACY_POLICY.md` and integrated an in-app viewer dialog in `SettingsScreen`. Added voluntary sponsorship card with direct outbound browser intents (`openBrowserUrl`) to GitHub Sponsors and Buy Me a Coffee.
- **Rationale:** Full legal compliance and financial sustainability without exploiting youth attention or locking features behind paywalls.
- **Consequences:** AwareMate remains 100% free and open source forever.

### D-034: Google Play App Bundle (AAB) & Baseline Profiles Optimization Architecture
- **Date:** 2026-09-03
- **Status:** Accepted
- **Context:** Preparing production release readiness for Google Play Internal Test Track with fast cold startup and minimal download sizes.
- **Decision:** Configured `signingConfigs` in `androidApp/build.gradle.kts` supporting CI/CD environment variables with fallback to debug signing for internal builds. Implemented `baseline-prof.txt` with ART rules for pre-compilation of Compose, Voyager, Koin, and Room classes. Added `AppStartupMetrics` utility for monitoring time-to-first-render. Updated CI/CD workflow to generate and archive `androidApp-release.aab` on every push to `main`.
- **Rationale:** Maximizes runtime performance and cold launch responsiveness (<1000ms) on modern Android devices while automating store bundle generation.
- **Consequences:** Produces signed `.aab` bundles ready for instant upload to Google Play Console.

### D-035: Jetpack Glance Companion Check-In Widget
- **Date:** 2026-09-04
- **Status:** Accepted
- **Context:** Adding a responsive Android home-screen widget without creating a parallel mood reward path or duplicating the Compose Canvas companion artwork in a RemoteViews-only surface.
- **Decision:** Use stable Jetpack Glance 1.2.0 with a `GlanceAppWidgetReceiver` and `ActionCallback`. The widget derives its compact plant/emotion visual from the same `Companion` stage and emotion state used by the in-app canvas, and imports the same mood option mapping used by `MoodCheckInDialog`. Widget taps invoke the existing singleton `LogMoodUseCase`; a mutex-protected one-entry-per-local-day guard ensures the shared +15 Wisdom XP path can run only once. Glance instances refresh after both widget and in-app check-ins.
- **Rationale:** Glance is the official Compose-style Android widget API, but it renders through RemoteViews and cannot host the existing Compose `Canvas`. Sharing domain visual state and check-in logic preserves a single source of truth without duplicating artwork or gamification rules.
- **Consequences:** The Android launcher widget reflects an existing daily check-in and disables further emoji actions for that day; stale or rapid taps are also rejected in the shared use case with no duplicate MoodEntry or XP.

### D-036: WorkManager Unique One-Time Scheduling for Evening Check-In Invitations
- **Date:** 2026-09-04
- **Status:** Accepted
- **Context:** The missed-check-in invitation must run after a configurable local time, remain at most once per local day, respond immediately to settings changes, and avoid Digital Sunset without adopting an escalating reminder cadence.
- **Decision:** Use stable AndroidX WorkManager 2.11.2. Schedule one uniquely named `OneTimeWorkRequest` for the next eligible local time with `ExistingWorkPolicy.REPLACE`, then schedule the following day only after the worker runs. Re-evaluate preferences, today's Room mood entries, notification-date guard, and current Digital Sunset state inside `MissedCheckInWorker` before posting the fixed invitation text.
- **Rationale:** Recalculating a one-time request preserves local wall-clock intent across setting changes and avoids the flex-window and cadence limitations of periodic work. A unique work name prevents parallel schedules, while the persisted local-date guard is the final at-most-once control.
- **Consequences:** Disabling either notification toggle cancels pending work; changing the time replaces it; missed days never increase frequency or alter tone.

### D-037: Recoverable Remote-First Account Deletion with Transactional Local Erasure
- **Date:** 2026-09-04
- **Status:** Accepted
- **Context:** Play-compliant account deletion must complete in-app, remove Firestore/Auth/Room state, avoid offline partial deletion, and still support legacy users whose data predates an authenticated Firebase session.
- **Decision:** New onboarding establishes an anonymous Firebase account before first persistence, and Firestore writes use that Firebase UID as their cloud owner key. For signed-in deletion, preflight connectivity, read all known user cloud documents before any write, delete them in bounded Firestore batches, and restore captured documents if a later batch or Firebase Auth deletion fails. Only after remote success does `AccountDataDao` clear all Room tables in one transaction, reset DataStore preferences, explicitly sign out, and route to onboarding. Local-only legacy profiles skip the nonexistent remote phase and erase locally.
- **Rationale:** Remote-first ordering keeps the local source of truth available for retry whenever the network or Auth operation fails. Firestore batches plus compensating restore minimize inconsistent remote state, while one Room transaction prevents partial on-device table clearing.
- **Consequences:** Signed-in offline attempts perform zero destructive writes and show a retryable message. Auth providers subject to Firebase's recent-login rule receive a clear re-authentication error, with cloud records restored before it is surfaced.

### D-038: Compose GraphicsLayer Capture and Scoped FileProvider Sharing
- **Date:** 2026-09-04
- **Status:** Accepted
- **Context:** Weekly insights need a private user-initiated image export without a custom gallery, social feed, broad storage permission, or a second rendering implementation for the correlation chart.
- **Decision:** Render the weekly mood strip and existing `MoodScreenTimeCorrelationChart` inside one visible Compose card, record that card with `rememberGraphicsLayer`, convert it with `toImageBitmap`, and write a PNG only to the app cache. Share it through a narrowly scoped `FileProvider` URI and Android `ACTION_SEND` chooser with temporary read permission.
- **Rationale:** This follows the official Compose capture and Android secure-file-sharing patterns, preserves the exact in-app visual, and keeps the exported artifact private until the user explicitly chooses a recipient.
- **Consequences:** No media/storage permission, public feed, comparison, leaderboard, or retained in-app gallery is introduced; the correlation portion stays subject to the same five-day availability gate.



