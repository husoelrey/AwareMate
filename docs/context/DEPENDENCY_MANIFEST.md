# Dependency Manifest

This document catalogs all major external dependencies, libraries, and frameworks utilized in the AwareMate project. It serves as a reference for architecture decisions, license compliance, and version alignment.

## 1. Core Dependencies

| Dependency | Version | Purpose | License | Repository |
|---|---|---|---|---|
| Kotlin | 2.2+ | Core Language | Apache 2.0 | JetBrains |
| Compose Multiplatform | Latest | UI Framework | Apache 2.0 | JetBrains |
| Compose BOM | Latest | Version alignment for Android Compose | Apache 2.0 | Google |
| Material 3 | Latest | Design system components | Apache 2.0 | Google |
| Room KMP | 3.0+ | Local database storage | Apache 2.0 | Google |
| DataStore | 1.1+ | Key-value preferences | Apache 2.0 | Google |
| Koin | 4.x | Dependency injection | Apache 2.0 | InsertKoin |
| Voyager | 1.x | Multiplatform Navigation | MIT | adrielcafe |
| Ktor Client | 3.x | HTTP networking | Apache 2.0 | JetBrains |
| kotlinx.coroutines | 1.9+ | Async programming & Flow | Apache 2.0 | JetBrains |
| kotlinx.serialization | 1.7+ | JSON serialization | Apache 2.0 | JetBrains |
| kotlinx.datetime | Latest | Date/time handling | Apache 2.0 | JetBrains |
| Coil 3 | 3.x | Image loading and caching | Apache 2.0 | coil-kt |
| Vico | Latest | Chart and graph library | Apache 2.0 | patrykandpatrick |

## 2. Firebase Infrastructure

| Dependency | Version | Purpose | License | Repository |
|---|---|---|---|---|
| Firebase BOM | Latest | Firebase version alignment | Apache 2.0 | Google |
| Firebase Auth | Latest | User Authentication | Apache 2.0 | Google |
| Firebase Firestore | Latest | Cloud database synchronization | Apache 2.0 | Google |
| Firebase Messaging | Latest | Push notifications | Apache 2.0 | Google |
| Firebase Analytics | Latest | Usage analytics | Apache 2.0 | Google |
| Firebase Crashlytics | Latest | Crash reporting | Apache 2.0 | Google |

## 3. Testing Dependencies

| Dependency | Version | Purpose | License | Repository |
|---|---|---|---|---|
| kotlin.test | Latest | Core unit testing framework | Apache 2.0 | JetBrains |
| Turbine | Latest | Flow testing | Apache 2.0 | cashapp |
| MockK | Latest | Mocking framework | Apache 2.0 | mockk |

## 4. Policy Notes

- **License Compatibility:** All chosen libraries utilize permissive open-source licenses (Apache 2.0 or MIT). This strictly aligns with AwareMate's own Apache 2.0 licensing and open-source goals, ensuring no copyleft conflicts.
- **Offline Availability:** The core libraries (Room, DataStore, Koin, Compose) are specifically chosen to support a robust offline-first experience. The app must function identically offline, with Firebase merely acting as a background sync mechanism when network availability returns.
- **Version Pinning Policy:**
  - Avoid using dynamic versions (e.g., `1.+`) in Gradle.
  - Utilize Bill of Materials (BOM) for Compose and Firebase to ensure transitive dependency compatibility.
  - Centralize version definitions in `libs.versions.toml` (Gradle Version Catalogs) for easy maintenance and Dependabot integration.
