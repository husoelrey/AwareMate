# Implementation Roadmap

This document outlines the phased execution strategy, dependencies, risk management, and critical paths for the AwareMate project. It serves as a timeline and tracking tool for delivering the MVP.

## 1. Estimated Timeline

The MVP implementation is divided into distinct, manageable phases:

- **Phase 0 (P0) - Foundation:** ~2 days
- **Phase 1 (P1) - Core UI & Navigation:** ~2 days
- **Phase 2 (P2) - Digital Awareness (App Tracking):** ~1 week
- **Phase 3 (P3) - Personal Growth (Journal & Challenges):** ~1 week
- **Phase 4 (P4) - Companion System:** ~1.5 weeks
- **Phase 5 (P5) - Firebase Integration:** ~1.5 weeks
- **Phase 6 (P6) - Polish & Testing:** ~1.5 weeks
- **Phase 7 (P7) - Launch Prep:** ~1 week

**Total Estimated Duration:** ~8.5 weeks

## 2. Phase Dependencies

- **P0** must be completed before any other phase.
- **P1** provides the skeleton; P2, P3, and P4 depend heavily on P1.
- **P2 and P3** can be executed in parallel after P1.
- **P4** (Companion) relies on data generated from P2 and P3 (usage and interactions).
- **P5** (Firebase) can be started iteratively alongside P2/P3, but finalizing sync depends on having models stabilized.
- **P6 and P7** follow sequentially after feature complete (P5).

## 3. Risk Mitigation Strategies

| Risk | Impact | Mitigation Strategy |
| :--- | :--- | :--- |
| **UsageStats API Limitations** | High | Abstract the tracking mechanism behind interfaces. Gracefully handle permission denials with onboarding flows. Avoid relying on real-time sub-second accuracy. |
| **Firebase Quota Limits** | Medium | Implement aggressive local caching via Room/DataStore. Only sync essential metadata to Firestore. Use offline-first architecture. |
| **Compose Canvas Performance** | High | (For Companion system) Optimize recomposition. Avoid heavy animations on the UI thread. Profile thoroughly using Android Studio Profiler during P4. |
| **KMP Library Maturity** | Medium | Stick to widely adopted libraries (Koin, Voyager, Ktor). Fall back to expect/actual implementations for complex platform-specific needs. |

## 4. Milestone Definitions and Acceptance Criteria

### Milestone 1: App Skeleton (P0 & P1)
- **Criteria:** KMP project builds successfully for Android. Material 3 theme is applied. Basic navigation between empty screens (Dashboard, Journal, Companion) works.

### Milestone 2: Core Functionality (P2 & P3)
- **Criteria:** UsageStats tracks app usage locally. Mood journal accepts and saves entries to Room DB.

### Milestone 3: Gamification & Sync (P4 & P5)
- **Criteria:** Plant companion evolves based on usage rules. Users can authenticate anonymously or via Google. Data syncs to Firestore upon connection.

### Milestone 4: MVP Release Ready (P6 & P7)
- **Criteria:** Zero critical bugs. App functions gracefully offline. CI/CD pipeline validates cleanly. AAB generated for Play Store Internal track.

## 5. Critical Path Analysis

The critical path for this project flows through:
`P0 (Setup) -> P1 (Nav/UI) -> P2 (Tracking) -> P4 (Companion Logic) -> P5 (Sync) -> P6 (Polish)`

Delays in **P2 (UsageStats integration)** will bottleneck **P4 (Companion progression)** since the companion's growth is inherently tied to the user's digital awareness metrics. Early prototyping of the UsageStats API is recommended to de-risk the timeline.
