# AwareMate - Detailed Implementation Plan

## 1. Purpose
This document expands upon the initial project specification and architecture by providing concrete technical designs, schemas, and a rigorous checklist for implementing the AwareMate application. It serves as the master execution guide.

## 2. Locked Project Decisions
- **Stack:** Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Backend:** Firebase (Auth, Firestore, Crashlytics, Analytics, FCM).
- **Local DB:** Room for KMP.
- **Architecture:** Clean Architecture + MVI UI pattern.
- **Design Philosophy:** Compassionate gamification (Finch-style), no punitive streaks.
- **License:** Apache 2.0 (Open Source).
- **Language:** English-first with i18n infrastructure ready.

## 3. Current Environment Baseline
- **OS:** Windows 11
- **Hardware:** Intel Core Ultra 5 125H
- **SDKs:** Android SDK (API 34/35 target), JDK 17+
- **Language:** Kotlin 2.2+
- **Build System:** Gradle (libs.versions.toml managed)

## 4. Target Topology
1. **Phase 1 (MVP):** Android-first release. Validates core UsageStats, Background processing, and Material 3 UI.
2. **Phase 2:** iOS target integration.
3. **Phase 3:** Desktop (JVM) client for deep analytics and journaling.
4. **Phase 4:** Web (Wasm/JS) lightweight dashboard.

## 5. Companion System Design
The companion is a plant-based entity.
- **Growth Stages:**
  - 0-100 XP: `Seed`
  - 101-500 XP: `Sprout`
  - 501-2000 XP: `Bloom`
  - 2001-5000 XP: `Tree`
  - 5000+ XP: `Ancient Tree`
- **Emotion State Machine:**
  - `Happy`: Nurtured today (logged mood or hit screen time goal).
  - `Resting`: Nighttime hours or focus session active.
  - `Waiting`: No interactions in > 24 hours (No wilting or dying, just neutral/waiting).
- **Momentum:** Rolling 7-day average of completed positive actions (0.0 to 1.0). Controls the visual "glow" of the companion.

## 6. Digital Awareness Module Design
- **UsageStats Contract:** `expect interface ScreenTimeProvider` fetching app usage duration per day.
- **Doom-scrolling Detection:** WorkManager job runs every 30 mins; if foreground app category is "Social" and duration > threshold, triggers local nudge notification.
- **Focus Timer:** Standard countdown timer. Saves `FocusSession` to DB upon completion.
- **App Categorization:** Local mapping of package names to categories (Productivity, Social, Games, etc.).

## 7. Personal Growth Module Design
- **Mood Schema:** Enums (`AWFUL`, `POOR`, `NEUTRAL`, `GOOD`, `EXCELLENT`), selected tags, and optional text notes.
- **Breath Exercises:** Hardcoded presets. MVP includes:
  - *4-7-8 Breathing:* Inhale 4s, Hold 7s, Exhale 8s.
  - *Box Breathing:* Inhale 4s, Hold 4s, Exhale 4s, Hold 4s.
- **Hobby Database:** Pre-populated JSON/SQLite list of offline hobbies (e.g., "Origami", "Urban Sketching") with difficulty and tags.
- **Micro-challenges:** Generated daily based on user momentum (e.g., "Drink 1 glass of water", "No social media for 1 hour").

## 8. Firebase Integration
- **Auth Flows:** 
  - Anonymous auth on first launch for zero-friction onboarding.
  - Google Sign-in to link account and persist remotely.
- **Firestore Schema:**
  - `users/{uid}`: `{ joinedDate, momentum, preferences }`
  - `users/{uid}/companion/{doc}`: `{ xp, stage, state }`
  - `users/{uid}/mood_entries/{id}`: `{ timestamp, rating, notes }`
- **Security Rules:** `allow read, write: if request.auth != null && request.auth.uid == userId;`
- **Offline Persistence:** Firestore offline cache enabled.

## 9. Room Database Schema

### Table: `users`
| Column | Type | Notes |
|---|---|---|
| id | String | PK, UUID |
| name | String | User's preferred name |
| created_at | Long | Timestamp |

### Table: `companions`
| Column | Type | Notes |
|---|---|---|
| id | String | PK, UUID |
| user_id | String | FK to users |
| stage | String | Enum string |
| xp | Int | Total XP |

### Table: `mood_entries`
| Column | Type | Notes |
|---|---|---|
| id | String | PK |
| timestamp | Long | Epoch time |
| rating | Int | 1 to 5 scale |
| notes | String | Nullable |

### Table: `focus_sessions`
| Column | Type | Notes |
|---|---|---|
| id | String | PK |
| duration_mins | Int | |
| timestamp | Long | Epoch |
| tags | String | Comma separated |

## 10. UI/UX Specifications
- **Screen Inventory:**
  - `SplashScreen`: Loading & Auth check.
  - `OnboardingScreen`: Permissions, goal setting.
  - `HomeScreen`: Companion view, quick stats, primary CTA (Check-in).
  - `DashboardScreen`: Detailed screen time charts, mood graphs.
  - `FocusScreen`: Timer UI.
  - `BreathScreen`: Animated breathing circle.
  - `SettingsScreen`: Auth link, export data, theme toggles.
- **Material 3:** Heavy use of dynamic color for personalized feel. Extensive use of Cards, rounded corners (anti-anxiety design).

## 11. Notification Strategy
- **Local Notifications:** Handled by Android WorkManager.
- **Timing Rules:** SILENT PERIOD from 10:00 PM to 08:00 AM.
- **Tone:** Encouraging, never demanding. "Hey, you've been scrolling a bit. How about a breath exercise?"

## 12. Verification Strategy
- **Unit Tests:** ViewModels and Use Cases (MockK + Coroutines Test).
- **UI Tests:** Compose UI tests for Home and Onboarding flows.
- **Accessibility:** Ensure all interactive elements have minimum 48dp touch targets and content descriptions.
- **Performance:** Macrobenchmark for cold start time on Android.

## 13. Definition of Done (DoD)
For every feature task:
- [ ] Feature implemented matching UI specs.
- [ ] Unit tests written and passing.
- [ ] Code formatted via Ktlint.
- [ ] Handles offline state gracefully.
- [ ] No regression in cold start time.
- [ ] PR reviewed and merged.

## 14. Explicitly Deferred Work
- iOS target build and configuration.
- Desktop and Web targets.
- Social sharing features.
- Advanced AI analysis of mood journals.
- Civic engagement module.
