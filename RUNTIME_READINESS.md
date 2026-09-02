# AwareMate - Runtime Readiness & Environment Setup

This document provides complete instructions for configuring, verifying, and troubleshooting a local development environment for AwareMate on Windows, macOS, and Linux.

---

## 1. System & Hardware Requirements

| Component | Minimum | Recommended |
|---|---|---|
| **OS** | Windows 10/11 (64-bit), macOS 13+ (Ventura/Sonoma), Ubuntu 22.04+ LTS | Windows 11 / macOS 14+ |
| **CPU** | x86_64 or ARM64 (Apple Silicon) with hardware virtualization | Intel Core Ultra / AMD Ryzen 7 / Apple M-series |
| **RAM** | 8 GB | 16 GB+ (to comfortably run Android Studio + Gradle Daemon + AVD) |
| **Disk Space** | 10 GB free space | 25 GB+ SSD space for SDKs and build caches |

---

## 2. Software Version Matrix

All core tooling versions are strictly pinned in the Gradle Version Catalog ([`gradle/libs.versions.toml`](file:///gradle/libs.versions.toml)):

| Tool / Component | Version | Role / Configuration |
|---|---|---|
| **JDK** | **17+** (JDK 17 or JDK 21 LTS) | Java 17 target compatibility (`sourceCompatibility = JavaVersion.VERSION_17`, `jvmTarget = "17"`) |
| **Android SDK Platform** | **API 35** (Android 15) | `compileSdk = 35`, `targetSdk = 35` |
| **Android SDK Min** | **API 26** (Android 8.0 Oreo) | `minSdk = 26` |
| **Android Build Tools** | **35.0.0+** | Required for Android 15 compilation |
| **Gradle** | **8.11.1** | Configured via `./gradlew` / `gradlew.bat` wrapper |
| **Android Gradle Plugin (AGP)**| **8.8.0** | Core Android build plugin |
| **Kotlin** | **2.1.20** | Multiplatform compiler & stdlib |
| **Compose Multiplatform** | **1.7.3** | Declarative cross-platform UI framework (Material 3) |
| **KSP** | **2.1.20-2.0.1** | Kotlin Symbol Processing (for Room KMP) |
| **Firebase BOM** | **33.9.0** | Firebase client library management |
| **Google Services Plugin** | **4.4.2** | Parses `google-services.json` |

---

## 3. Environment Variables Configuration

Ensure the following environment variables are set in your system shell / user environment:

### Windows (PowerShell)
```powershell
# Set JAVA_HOME (example path for Eclipse Adoptium Temurin JDK 17/21)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot", "User")

# Set ANDROID_HOME
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")

# Add platform-tools to PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$currentPath;$env:LOCALAPPDATA\Android\Sdk\platform-tools", "User")
```

### macOS / Linux (`~/.zshrc` or `~/.bashrc`)
```bash
# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17) # macOS
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 # Ubuntu/Debian

# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Library/Android/sdk # macOS
# export ANDROID_HOME=$HOME/Android/Sdk # Linux

# Add platform-tools & emulator to PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator
```

---

## 4. Step-by-Step Local Setup

### Step 1: Clone Repository
```bash
git clone https://github.com/husoelrey/AwareMate.git
cd AwareMate
```

### Step 2: Configure `google-services.json`
`google-services.json` is gitignored to keep project credentials secure.

- **Option A — Offline / Local Dev (Template):**
  A ready-to-use template is included. Copy it to create the local configuration:
  ```bash
  # Windows
  Copy-Item androidApp/google-services.json.example androidApp/google-services.json

  # macOS / Linux
  cp androidApp/google-services.json.example androidApp/google-services.json
  ```

- **Option B — Real Firebase Project:**
  1. Open the [Firebase Console](https://console.firebase.google.com/) and create a new project (e.g., `awaremate-dev`).
  2. Add an Android app with package name:
     - Release/Default: `org.awaremate.android`
     - Debug: `org.awaremate.android.debug`
  3. (Optional) Retrieve your debug SHA-1 signing fingerprint to enable Google Sign-in:
     ```bash
     ./gradlew signingReport
     ```
  4. Download `google-services.json` and place it in the `androidApp/` directory.
  5. In Firebase Console:
     - Enable **Authentication** (Anonymous + Google Sign-In providers).
     - Enable **Cloud Firestore** in test/production mode.

### Step 3: Verify Build & Tests
Run the following commands using the Gradle wrapper:

```bash
# Windows
.\gradlew.bat assembleDebug test lintDebug

# macOS / Linux
./gradlew assembleDebug test lintDebug
```

Expected output: `BUILD SUCCESSFUL` with all unit tests passing.

---

## 5. Android Emulator & Physical Device Setup

### Android Virtual Device (AVD)
1. Open Android Studio > **Device Manager**.
2. Create a Virtual Device:
   - **Hardware:** Phone (e.g., Pixel 8 or Pixel 7).
   - **System Image:** API 35 (Android 15) or API 34 (UpsideDownCake) with Google APIs.
3. Start the emulator.

### Physical Device
1. Enable **Developer Options** on your device (Settings > About Phone > Tap "Build Number" 7 times).
2. Enable **USB Debugging** under Developer Options.
3. Connect the device via USB and accept the debugging authorization prompt.
4. Verify device detection: `adb devices`.

### Usage Access Permission (Digital Awareness Module Readiness)
AwareMate's Digital Awareness features (Phase 5) utilize Android's `UsageStatsManager`. To grant this permission on a test device or emulator:
- Navigate to: **Settings** → **Apps** → **Special app access** → **Usage access**.
- Select **AwareMate** (or AwareMate Debug) and toggle **Allow usage tracking** to **ON**.
- Or via ADB:
  ```bash
  adb shell appops set org.awaremate.android PACKAGE_USAGE_STATS allow
  ```

---

## 6. CI/CD Pipeline

The project includes an automated GitHub Actions CI workflow in [`.github/workflows/ci.yml`](file:///.github/workflows/ci.yml):
- **Triggers:** Push to `main`, Pull Requests to `main`, and manual dispatch.
- **Environment:** Ubuntu with JDK 17 (Temurin).
- **Validation Steps:**
  1. Sets up `google-services.json` fallback from `google-services.json.example`.
  2. Runs Android lint checks (`./gradlew lintDebug`).
  3. Executes all common and Android unit tests (`./gradlew test`).
  4. Assembles the debug APK (`./gradlew assembleDebug`).

---

## 7. Troubleshooting & Common Issues

### 1. Windows Long Path Errors (`Filename too long`)
Kotlin Multiplatform build artifacts can exceed the Windows 260-character path limit.
- **Fix in Git:**
  ```powershell
  git config --system core.longpaths true
  ```
- **Fix in Windows Registry:**
  Open PowerShell as Administrator:
  ```powershell
  Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1
  ```

### 2. Gradle Memory Footprint
Gradle JVM arguments in [`gradle.properties`](file:///gradle.properties) are configured to:
```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
```
If you are on a memory-constrained machine (e.g. <= 8 GB RAM), you can adjust `-Xmx` in your local `~/.gradle/gradle.properties`.

### 3. Missing `google-services.json` Error
If you see `File google-services.json is missing`:
- Ensure you have executed `cp androidApp/google-services.json.example androidApp/google-services.json`.
- Resync Gradle in Android Studio (`File > Sync Project with Gradle Files`).

### 4. Compose Preview Cache Invalidation
If Compose multiplatform previews fail to render after updating dependencies:
- In Android Studio: **File** > **Invalidate Caches...** > Select **Clear file system cache and Local History** > Click **Invalidate and Restart**.
