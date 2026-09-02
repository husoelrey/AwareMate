# AwareMate - Execution Plan

## P0 — Repository governance and project context
- [x] Create root execution checklist and detailed implementation plan
- [x] Initialize KMP + Compose Multiplatform multi-module project
- [x] Set up Gradle version catalog (`libs.versions.toml`) and convention plugins
- [x] Configure `.gitignore` for KMP project (`build/`, `.gradle/`, `local.properties`, `google-services.json`)
- [x] Establish branch and commit policy
- [x] Verify all context documents are consistent

## P1 — Development environment and tooling
- [x] Verify Android SDK, JDK 17+, Kotlin 2.2+, Gradle 8.x versions
- [x] Configure Firebase project and `google-services.json` (placeholder template + plugin setup)
- [x] Set up CI/CD with GitHub Actions (build, test, lint)
- [x] Verify emulator/device build and run
- [x] Document environment setup in `RUNTIME_READINESS.md`

## P2 — Core domain and data layer
- [x] Define domain entities (`User`, `Companion`, `MoodEntry`, `FocusSession`, `DailyChallenge`, etc.)
- [x] Define repository interfaces in domain layer
- [x] Implement Room KMP database with entities and DAOs
- [x] Implement DataStore preferences for user settings
- [x] Implement Firebase Auth service (Google Sign-in + Anonymous)
- [x] Implement Firestore sync service (Local-first Room SSOT + background sync)
- [x] Set up Koin DI modules (shared + android)
- [x] Complete domain and data layer unit tests

## P3 — Companion system and gamification engine
- [x] Implement companion growth logic (XP calculation, stage transitions)
- [x] Implement momentum score system (gradual decay, not binary streak)
- [x] Implement daily challenge generation and tracking
- [x] Implement awareness score calculation
- [x] Implement companion mood/emotion state machine
- [x] Complete companion system unit tests

## P4 — UI foundation and navigation
- [ ] Set up Material 3 theme (Dynamic Color + custom palette)
- [ ] Implement Voyager navigation graph with all screen definitions
- [ ] Implement onboarding flow (Welcome → Interests → Companion naming → Permissions → Intentions)
- [ ] Implement home dashboard (companion widget, score card, quick actions, daily sparks)
- [ ] Implement companion screen with Compose Canvas rendering
- [ ] Implement settings and profile screens
- [ ] Complete Compose UI tests

## P5 — Digital awareness module
- [x] Implement Android UsageStats API integration
- [x] Implement screen time analytics with Vico charts
- [x] Implement mindful nudge notification system (configurable thresholds)
- [x] Implement focus session timer with companion animation
- [x] Implement digital sunset reminder system
- [x] Implement weekly digital awareness report
- [x] Complete digital awareness module tests

## P6 — Personal growth module
- [x] Implement mood journal (emoji picker + note + Firestore sync)
- [x] Implement breath & ground exercises (animated breathing guide)
- [x] Implement hobby discovery system (categorized database, personalized suggestions)
- [x] Implement daily micro-challenges (generation, completion, XP rewards)
- [x] Implement reflection prompts
- [x] Implement "Self-Discovery" prompt card type — curated self-awareness questions about habitual behaviors (e.g. noticing a one-sided habit you never registered). Framed as discovery, not comparison: no fabricated or unsourced statistics, no "X% of people..." framing that could read as a pass/fail test.
- [x] Implement weekly insights (mood trends, achievements)
- [x] Complete personal growth module tests

## P7 — Polish and release readiness
- [x] Implement error handling and offline-first behavior
- [x] Accessibility review (TalkBack, content descriptions, contrast ratios)
- [x] Performance optimization (baseline profiles, startup time)
- [x] App icon, splash screen, store listing assets
- [x] Minimal privacy policy page (required for Play Store listing / Firebase Auth account creation, independent of internal priority)
- [x] Set up optional donation/sponsorship link (e.g. GitHub Sponsors, Buy Me a Coffee) for sustainability — no ads, no paywall
- [x] `README.md` and Contributing guide
- [x] GitHub Actions CI/CD pipeline
- [x] Google Play Store internal test track
- [x] Firebase Crashlytics + Analytics integration
- [x] Run complete acceptance checklist

---

### Explicitly out of scope (MVP)
- iOS, Desktop, Web builds
- Environmental and social/civic awareness modules
- AI-powered personalization
- Multi-language support
- Youthpass/DigComp certificate generation
- Doom-scroll interceptor (accessibility service)
