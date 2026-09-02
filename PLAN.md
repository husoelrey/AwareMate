# AwareMate - Execution Plan

## P0 — Repository governance and project context
- [x] Create root execution checklist and detailed implementation plan
- [x] Initialize KMP + Compose Multiplatform multi-module project
- [x] Set up Gradle version catalog (`libs.versions.toml`) and convention plugins
- [x] Configure `.gitignore` for KMP project (`build/`, `.gradle/`, `local.properties`, `google-services.json`)
- [x] Establish branch and commit policy
- [x] Verify all context documents are consistent

## P1 — Development environment and tooling
- [ ] Verify Android SDK, JDK 17+, Kotlin 2.2+, Gradle 8.x versions
- [ ] Configure Firebase project and `google-services.json`
- [ ] Set up CI/CD with GitHub Actions (build, test, lint)
- [ ] Verify emulator/device build and run
- [ ] Document environment setup in `RUNTIME_READINESS.md`

## P2 — Core domain and data layer
- [ ] Define domain entities (`User`, `Companion`, `MoodEntry`, `FocusSession`, `DailyChallenge`, etc.)
- [ ] Define repository interfaces in domain layer
- [ ] Implement Room KMP database with entities and DAOs
- [ ] Implement DataStore preferences for user settings
- [ ] Implement Firebase Auth service (Google Sign-in + Anonymous)
- [ ] Implement Firestore sync service
- [ ] Set up Koin DI modules (shared + android)
- [ ] Complete domain and data layer unit tests

## P3 — Companion system and gamification engine
- [ ] Implement companion growth logic (XP calculation, stage transitions)
- [ ] Implement momentum score system (gradual decay, not binary streak)
- [ ] Implement daily challenge generation and tracking
- [ ] Implement awareness score calculation
- [ ] Implement companion mood/emotion state machine
- [ ] Complete companion system unit tests

## P4 — UI foundation and navigation
- [ ] Set up Material 3 theme (Dynamic Color + custom palette)
- [ ] Implement Voyager navigation graph with all screen definitions
- [ ] Implement onboarding flow (Welcome → Interests → Companion naming → Permissions → Intentions)
- [ ] Implement home dashboard (companion widget, score card, quick actions, daily sparks)
- [ ] Implement companion screen with Compose Canvas rendering
- [ ] Implement settings and profile screens
- [ ] Complete Compose UI tests

## P5 — Digital awareness module
- [ ] Implement Android UsageStats API integration
- [ ] Implement screen time analytics with Vico charts
- [ ] Implement mindful nudge notification system (configurable thresholds)
- [ ] Implement focus session timer with companion animation
- [ ] Implement digital sunset reminder system
- [ ] Implement weekly digital awareness report
- [ ] Complete digital awareness module tests

## P6 — Personal growth module
- [ ] Implement mood journal (emoji picker + note + Firestore sync)
- [ ] Implement breath & ground exercises (animated breathing guide)
- [ ] Implement hobby discovery system (categorized database, personalized suggestions)
- [ ] Implement daily micro-challenges (generation, completion, XP rewards)
- [ ] Implement reflection prompts
- [ ] Implement "Self-Discovery" prompt card type — curated self-awareness questions about habitual behaviors (e.g. noticing a one-sided habit you never registered). Framed as discovery, not comparison: no fabricated or unsourced statistics, no "X% of people..." framing that could read as a pass/fail test.
- [ ] Implement weekly insights (mood trends, achievements)
- [ ] Complete personal growth module tests

## P7 — Polish and release readiness
- [ ] Implement error handling and offline-first behavior
- [ ] Accessibility review (TalkBack, content descriptions, contrast ratios)
- [ ] Performance optimization (baseline profiles, startup time)
- [ ] App icon, splash screen, store listing assets
- [ ] Minimal privacy policy page (required for Play Store listing / Firebase Auth account creation, independent of internal priority)
- [ ] Set up optional donation/sponsorship link (e.g. GitHub Sponsors, Buy Me a Coffee) for sustainability — no ads, no paywall
- [ ] `README.md` and Contributing guide
- [ ] GitHub Actions CI/CD pipeline
- [ ] Google Play Store internal test track
- [ ] Firebase Crashlytics + Analytics integration
- [ ] Run complete acceptance checklist

---

### Explicitly out of scope (MVP)
- iOS, Desktop, Web builds
- Environmental and social/civic awareness modules
- AI-powered personalization
- Multi-language support
- Youthpass/DigComp certificate generation
- Doom-scroll interceptor (accessibility service)
