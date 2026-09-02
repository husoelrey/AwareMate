# Contributing to AwareMate

Thank you for your interest in contributing to **AwareMate**! AwareMate is an open-source, compassionate awareness companion app built with Kotlin Multiplatform and Compose Multiplatform to empower youth toward balanced digital habits and mindful personal growth.

---

## 1. Code of Conduct & Core Values
All contributors, maintainers, and community members must uphold our non-negotiable principles:
- **Compassionate UX:** No punitive streaks, no guilt-inducing mechanics, and no shame-driven metrics.
- **Youth Safety:** Zero dark patterns, zero ads, zero data selling, and age-appropriate content.
- **Accessibility:** Meet WCAG 2.1 AA standards; ensure all interactive elements have semantic descriptions.
- **Inclusivity & Empathy:** Treat every contributor with kindness and constructive respect.

---

## 2. Development Setup
1. **Prerequisites:**
   - JDK 17+ (Java 17 target compatibility)
   - Android SDK API 35 (Android 15)
   - Gradle 8.11+ (via `./gradlew` wrapper)
2. **Clone & Prepare:**
   ```bash
   git clone https://github.com/husoelrey/AwareMate.git
   cd AwareMate
   # Copy Firebase placeholder if not connecting real project
   cp androidApp/google-services.json.example androidApp/google-services.json
   ```
3. **Verify Build & Tests:**
   ```bash
   ./gradlew test assembleDebug
   ```

---

## 3. Workflow & Branching Strategy
- **Base Branch:** All feature work branches off `main`.
- **Branch Naming:**
  - `feat/feature-name` (e.g. `feat/weekly-insights`)
  - `fix/bug-name` (e.g. `fix/timer-pause-state`)
  - `docs/doc-update` (e.g. `docs/privacy-policy`)
- **Commit Messages:** Follow [Conventional Commits](https://www.conventionalcommits.org/):
  - `feat(scope): brief description`
  - `fix(scope): brief description`
  - `test(scope): brief description`
  - `docs(scope): brief description`

---

## 4. Definition of Done (DoD)
Before opening a Pull Request, ensure:
1. **Functionality Verified:** The change compiles and runs cleanly (`./gradlew test assembleDebug`).
2. **Automated Tests:** Any business logic changes in domain or data layers must include unit tests.
3. **Accessibility:** Any UI touchpoints must include `contentDescription` on interactive components and meet 48dp minimum touch target sizing.
4. **Clean Diff:** No IDE-specific files, no `.DS_Store`, no sensitive keys, and no extraneous whitespace changes.

---

## 5. Submitting a Pull Request
1. Push your branch to your fork or `origin`.
2. Open a Pull Request against `main`.
3. Provide a concise description of what changed, how it was verified, and screenshots for UI updates.
4. CI/CD checks (lint, unit tests, debug build, bundle assemble) must pass.

Thank you for helping youth grow with kindness and digital awareness! 🌱
