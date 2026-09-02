# Runtime Readiness & Environment Setup

This document outlines the necessary hardware, software, and configuration steps required to set up a local development environment for the AwareMate Kotlin Multiplatform project.

## 1. Development Machine Specifications

Based on the primary developer environment:
- **OS:** Windows 11
- **CPU:** Intel Core Ultra 5 125H
- **RAM:** ~16 GB
- **GPU:** Intel Arc Graphics

*(Note: While these are the primary specs, the project can be built on standard macOS and Linux machines capable of running Android Studio).*

## 2. Required Software and Versions

Ensure the following tools are installed and configured in your system `PATH`:

- **JDK 17+:** Required for Gradle. Verify using `java -version`.
- **Android SDK:** API 34+, Build Tools 34+.
- **Kotlin:** 2.2+ (managed via Gradle).
- **Gradle:** 8.x (use the provided Gradle wrapper).
- **IDE:** Android Studio (latest stable) or IntelliJ IDEA Ultimate.
- **Version Control:** Git.

## 3. Firebase Project Setup Checklist

To run the application with cloud features, you must configure a Firebase project:

1. [ ] Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2. [ ] Register an Android app with the package name defined in `build.gradle.kts`.
3. [ ] Download the `google-services.json` file and place it in the `androidApp/` directory. (Ensure it is gitignored).
4. [ ] Enable **Authentication** (Providers: Anonymous, Google).
5. [ ] Create a **Firestore** database (Start in production mode, then apply strict rules).
6. [ ] Enable **Cloud Messaging** for push notifications.
7. [ ] Enable **Analytics** and **Crashlytics**.

## 4. Android Emulator / Device Setup

- **Emulator:** Create an AVD running API 26 or higher (Android 8.0+).
- **Physical Device:** Enable Developer Options and USB Debugging.
- **Permission Grant:** For local testing of the Digital Awareness features, you will need to manually grant the Usage Access permission:
  - Go to Settings > Apps > Special app access > Usage access.
  - Find "AwareMate" and toggle it ON.

## 5. Build Verification Commands

Run these commands from the project root to verify your environment is correctly configured:

- Build the Android debug APK:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- Run shared unit tests:
  ```bash
  ./gradlew :shared:testDebugUnitTest
  ```
- Run Android instrumented tests (requires emulator/device running):
  ```bash
  ./gradlew :androidApp:connectedDebugAndroidTest
  ```

## 6. Known Environment Issues and Workarounds

- **Windows Long Paths:** Building Kotlin Multiplatform on Windows can sometimes fail due to the 260-character path limit.
  - *Workaround:* Enable long paths in Windows Registry (`Computer\HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\FileSystem\LongPathsEnabled` set to `1`) and in Git (`git config --system core.longpaths true`).
- **Compose Preview Sync Issues:** If Compose Previews fail to render, clean the project and invalidate caches in Android Studio.
