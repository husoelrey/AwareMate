# Security and Privacy Guidelines

AwareMate is built with a privacy-first philosophy. Given its focus on digital wellbeing and youth empowerment (aligned with Erasmus+), protecting user data and mental wellbeing is paramount. This document outlines the security, privacy, and compliance standards for the project.

## 1. Privacy-First Design Philosophy

- **Local-First:** By default, all personal data (moods, detailed app usage, journal entries) is stored locally on the device.
- **Anonymity:** Users do not need to create an account to start using the app.
- **Transparency:** Clear, jargon-free explanations of why permissions are needed.

## 2. GDPR Compliance Checklist

- [ ] **Data Minimization:** Only collect usage metrics necessary for the companion system and digital awareness features.
- [ ] **Purpose Limitation:** Data is exclusively used to provide app functionality and insights to the user.
- [ ] **Storage Limitation:** Detailed usage history is aggregated locally; only abstract metadata syncs to the cloud.
- [ ] **Right to Erasure:** Provide a clear "Delete Account and Data" button in settings that purges local DB and triggers a Firebase Cloud Function for remote cleanup.
- [ ] **Data Portability:** Allow users to export their journal and usage data as a local JSON file.
- [ ] **Privacy by Design and Default:** Opt-in required for cloud backup and notifications.

## 3. Firebase Security

- **Firestore Security Rules:** Strict rules ensuring users can only read and write documents matching their `uid`. No global reads.
- **Authentication Configuration:** Restrict providers to Google and Anonymous. Ensure strong password policies if email/password is added later.
- **No Server-Side Exposures:** The Firebase Admin SDK is strictly prohibited in the client application.

## 4. Local Data Protection

- **Room Database:** Consider SQLCipher if highly sensitive data is stored, though standard Room is acceptable for MVP if device encryption is relied upon.
- **DataStore/SharedPreferences:** Use EncryptedSharedPreferences (or DataStore equivalent) for sensitive tokens or settings.
- **Analytics:** Ensure Firebase Analytics events do not contain Personally Identifiable Information (PII).

## 5. Youth Protection

Aligned with the Erasmus+ target audience, AwareMate strictly enforces:
- **No Dark Patterns:** Unsubscribing, deleting data, or leaving the app should be frictionless.
- **No Predatory Monetization:** The app is 100% free and open-source.
- **No Social Pressure:** No leaderboards, public streaks, or "fear of missing out" (FOMO) mechanics. The companion is compassionate, not punitive.
- **Age-Appropriate Content:** All micro-challenges and prompts are safe, positive, and constructive.
- **COPPA Considerations:** While not directly targeting under-13s, data collection is minimized to align with child privacy standards.

## 6. Network Security

- **HTTPS Only:** All network communication (via Ktor/Firebase) must use TLS. Cleartext traffic is disabled in the Android Manifest.
- **Certificate Pinning:** Consider for future Ktor API calls, though Firebase SDKs handle their own secure connections.
- **Minimal Calls:** Network calls are batched and restricted to essential syncs to conserve battery and limit exposure.

## 7. Android Permissions

- **`PACKAGE_USAGE_STATS`:** Required for tracking screen time. Must be accompanied by a clear, educational onboarding screen explaining the privacy-preserving nature of the tracking.
- **`POST_NOTIFICATIONS`:** (Android 13+) Required for gentle nudges and micro-challenges. Opt-in only.
- **Prohibited Permissions:** The app will *never* request Camera, Microphone, Location, or Contacts permissions.

## 8. Open-Source Security

- **Dependency Scanning:** Use Dependabot or Renovate to automatically monitor for vulnerable libraries.
- **Secret Management:** Never commit API keys, keystores, or secrets to the repository.
- **Configuration Files:** `google-services.json` (and the iOS equivalent) must be strictly included in `.gitignore`.
