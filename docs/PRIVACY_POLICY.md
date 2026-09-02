# AwareMate — Privacy Policy

**Effective Date:** September 3, 2026  
**Last Updated:** September 3, 2026  
**License:** Apache License 2.0  
**Contact:** privacy@awaremate.org / https://github.com/husoelrey/AwareMate  

---

## 1. Our Privacy Promise
AwareMate is designed as an **empathetic awareness companion** for youth. We believe personal growth, emotional reflections, and digital habits are deeply personal. We operate on three inviolable principles:
1. **Zero Ads, Zero Monetization of Data:** We do not display advertisements, sell data to third parties, or use tracking libraries for commercial profiling.
2. **Local-First Single Source of Truth:** All your screen time habits, companion growth, mood reflections, and challenges are stored locally on your device in an encrypted Room SQLite database.
3. **Transparent Cloud Synchronization:** Any cloud backup is strictly opt-in and utilized solely to restore your companion across your devices.

---

## 2. What Data We Process

### 2.1 On-Device Local Data (Never Transmitted without Consent)
The following information is processed strictly on your device:
- **Screen Time & App Usage:** Aggregated on-device via Android's `UsageStatsManager` API. AwareMate calculates screen time totals and app categories locally. Raw app usage logs are never uploaded to any remote server.
- **Focus Sessions:** Session durations, categories, and completion timestamps.
- **Offline Hobbies & Reflections:** Bookmarked hobbies and curiosity-driven self-discovery observations.

### 2.2 Synchronized Cloud Data (Firebase)
When cloud synchronization is active, only the minimum data required to restore your profile is backed up:
- **Account Identity (Firebase Authentication):** A unique, randomized user identifier (UID) created either anonymously or via your authenticated email address.
- **Companion State (Cloud Firestore):** Companion name, current evolution stage (Seed/Sprout/Bloom/Tree), experience points (XP), momentum score, and emotion state.
- **Mood Journal (Cloud Firestore):** Emoji indicator (1–5 scale), energy battery level, selected tags, and optional reflection notes.

---

## 3. Third-Party Services
AwareMate utilizes Google Firebase infrastructure solely for reliable authentication and encrypted cloud backup:
- **Firebase Authentication:** Handles secure user authentication tokens.
- **Cloud Firestore:** Provides encrypted cloud storage with strict security rules ensuring users can only read and write their own authenticated records.
- **Firebase Crashlytics & Analytics:** Anonymized crash diagnostic reports to identify and fix bugs, and high-level non-invasive app health metrics. No personally identifiable information (PII) is included in crash reports.

We do **NOT** integrate with:
- Facebook SDK / Meta Pixel
- Google AdMob / AdSense
- Data brokers or behavioral analytics networks

---

## 4. Youth Safety & Parental Transparency (COPPA / GDPR-K)
AwareMate is built specifically for young people:
- No social feeds, public profiles, direct messaging, or public interactions with strangers.
- No predatory mechanics, loot boxes, or guilt-inducing dark patterns.
- No algorithmic feeds designed to addict or extend screen time.

---

## 5. Your Rights: Data Control & Deletion
You retain complete ownership over your data:
- **Export & Review:** You can view all your logged data in the app's Profile and Analytics screens.
- **Complete Deletion:** You may delete your local database at any time by clearing application storage or uninstalling the app. You may also request deletion of any cloud-synced Firebase records directly from the app or by submitting an issue on our GitHub repository.

---

## 6. Open Source Transparency
AwareMate is 100% open source under the Apache 2.0 License. Anyone can audit our source code, data storage schemas, and network calls at:  
[https://github.com/husoelrey/AwareMate](https://github.com/husoelrey/AwareMate)

---

## 7. Changes to this Policy
If we update this Privacy Policy to reflect app enhancements or legal requirements, changes will be published in this document and reflected in the app's Settings screen.
