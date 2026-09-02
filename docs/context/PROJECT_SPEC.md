# AwareMate - Project Specification

## 1. Product Definition
**AwareMate** is a compassionate, gamified awareness companion application built with Kotlin Multiplatform (KMP) and Compose Multiplatform. It combines digital well-being features (screen time monitoring, doom-scrolling intervention) with personal growth tools (mood journaling, hobby discovery, breath exercises, and micro-challenges). Unlike punitive habit trackers, AwareMate leverages a plant-based companion character system that grows and reacts to the user's positive habits, fostering a supportive environment for personal growth and digital balance.

## 2. Problem Statement
The modern digital landscape presents significant challenges for youth and young adults:
- **Digital Overload & Doom-scrolling:** Constant connection leading to burnout and lost time.
- **Lack of Offline Engagement:** Decreased participation in physical hobbies, civic duties, and real-world interactions.
- **Punitive Habit Trackers:** Existing screen time apps rely on negative reinforcement (shame, broken streaks, device locking) that often demoralize rather than inspire users.

## 3. Target Users
- **Primary:** Youth and young adults (ages 15-30).
- **Secondary:** Erasmus+ project participants, university students.
- **Tertiary:** Anyone seeking a healthier relationship with technology and seeking compassionate digital balance.

## 4. Product Boundary
### IS (In Scope)
- A compassionate awareness companion and habit reflection tool.
- A platform for hobby discovery and personal growth micro-challenges.
- A portfolio piece demonstrating modern KMP and Android development.
- An Erasmus+ aligned youth project promoting digital well-being.
- 100% free, open-source (Apache 2.0).

### IS NOT (Out of Scope)
- A medical device or professional therapy replacement.
- A restrictive parental control application.
- A social media platform.
- A hard screen-blocker (we nudge, we do not lock devices).

## 5. Primary Workflow
1. **Onboarding:** User sets baseline goals, creates an account (or anonymous login), and hatches their companion (Seed stage).
2. **Daily Check-in:** User logs mood, intentions, and reviews yesterday's screen time.
3. **Awareness Modules:** User completes micro-challenges, breath exercises, or logs focus sessions.
4. **Companion Growth:** Positive actions yield XP, causing the companion to evolve and display positive emotion states.
5. **Weekly Insights:** Summaries of digital habits, mood trends, and companion momentum.

## 6. Companion System Outputs
- **Growth Stages:** Seed → Sprout → Bloom → Tree → Ancient Tree.
- **XP Rewards:** Earned through daily check-ins, completing challenges, and maintaining screen time within goals.
- **Emotion States:** Happy, resting, encouraging (never dying or angry, just "sleeping" or "waiting" if user is inactive).
- **Momentum Score:** A rolling average of positive engagement used instead of fragile daily streaks.

## 7. Awareness Module Scope
- **MVP Scope:** 
  - *Digital Awareness:* Screen time tracking, focus mode timer, doom-scrolling nudges.
  - *Personal Growth:* Mood journaling, breath exercises, curated offline hobby discovery, daily micro-challenges.
- **Future Scope:** Environmental awareness challenges, civic engagement modules.

## 8. Backend Scope
- **Firebase Authentication:** Google Sign-in, Anonymous Auth, Account Linking.
- **Cloud Firestore:** Cloud sync for user profiles, companion state, and journal entries.
- **Firebase Cloud Messaging (FCM):** Remote engagement and positive nudges.
- **Firebase Crashlytics & Analytics:** Stability tracking and minimal, anonymized usage insights.

## 9. Persistence Scope
Built on **Room for KMP** with the following core entities:
1. `users`: Profile and settings.
2. `companions`: Current XP, stage, and state.
3. `mood_entries`: Timestamped emotional states and notes.
4. `focus_sessions`: Duration and tags for deep work.
5. `challenges`: Available and completed micro-challenges.
6. `screen_time_snapshots`: Aggregated device usage data.
7. `hobbies`: Offline hobby catalog and user interest status.

## 10. UI Scope
- **Framework:** Compose Multiplatform.
- **Design System:** Material Design 3 with Dynamic Color support.
- **Animations:** High-performance, 60fps animations for companion states (using Compose Canvas or KMP Lottie equivalents).

## 11. Privacy & Security Requirements
- **GDPR Compliant:** Clear consent flows, right to be forgotten (account deletion).
- **Data Minimization:** Only collect what is strictly necessary for syncing.
- **No Data Selling:** 100% free and open, zero ad-trackers.
- **Local-first Architecture:** Core features work fully offline.
- **Security:** Strict Firebase Security Rules validating all reads/writes.

## 12. Non-Functional Requirements
- **Offline-First:** All actions must persist locally and sync asynchronously.
- **Performance:** <2s cold start time on average devices.
- **Smoothness:** 60fps scrolling and companion animations.
- **Footprint:** <50MB initial APK size.

## 13. MVP Acceptance Criteria
1. User can authenticate anonymously or via Google.
2. User completes onboarding and hatches a Seed companion.
3. User grants Android UsageStats permission seamlessly.
4. App tracks daily screen time and displays it on a dashboard.
5. User can initiate a Focus Session (timer) which mutes nudges.
6. User can perform a guided breath exercise (e.g., 4-7-8 breathing).
7. User can submit a daily mood journal entry.
8. Companion updates visually (XP/stage) upon task completion.
9. App triggers local WorkManager nudges if doom-scrolling is detected.
10. UI adapts to system dark/light mode and Material 3 dynamic colors.
11. App functions offline and syncs data to Firestore when online.
12. User can view a library of offline hobbies and mark them as "interested".
13. User can export or delete all personal data.
14. CI/CD pipeline builds the Android APK and runs unit tests automatically.

## 14. Stretch Goals
- **iOS Target:** Compile KMP shared code and UI for iOS natively.
- **Desktop/Web Target:** Dedicated dashboard for long-term insight viewing.
- **Widgets:** Android App Widget for quick mood logging and companion viewing.

## 15. Repository Deliverables
- Source code in `shared/`, `androidApp/`, etc.
- Comprehensive markdown documentation (`docs/context/`).
- CI/CD workflow files (GitHub Actions).
- UI/UX Design assets and architecture diagrams.
- MIT / Apache 2.0 License file and Contribution guidelines.
