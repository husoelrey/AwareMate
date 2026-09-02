# Development Workflow

This document outlines the standard development procedures, conventions, and collaboration rules for the AwareMate project. Adhering to these guidelines ensures consistency, maintainability, and code quality across the Kotlin Multiplatform codebase.

## 1. Branch Naming Conventions

All development should occur on dedicated branches to maintain a clean and stable `main` branch. Use the following prefixes depending on the work type:

- **Features:** `feature/p<phase>-<feature-name>` (e.g., `feature/p1-onboarding-ui`)
- **Bug Fixes:** `fix/<issue-topic>` (e.g., `fix/usage-stats-permission`)
- **Documentation:** `docs/<topic>` (e.g., `docs/update-readme`)
- **Spikes/Research:** `spike/<topic>` (e.g., `spike/compose-canvas-performance`)

Use lowercase and hyphens for readability.

## 2. Commit Policy

- **Atomic Commits:** Make small, single-purpose commits. Each commit should represent a logical, standalone change.
- **Validation:** Only commit code that passes local validation (compiles without errors).
- **Commit Messages:** Follow standard conventional commits format:
  - `<type>(<scope>): <subject>`
  - Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
  - Example: `feat(ui): add mood journal entry screen`

## 3. Pull Request and Merge Rules

- The `main` branch must always remain in a verified, deployable state.
- Direct pushes to `main` are prohibited.
- All changes must be integrated via Pull Requests (PRs).
- PRs require at least one approval from a peer or lead maintainer.
- Ensure all CI/CD checks pass before merging.
- Use the "Squash and Merge" strategy to keep the main branch history clean.

## 4. Build Verification

Before submitting a PR, verify the build locally using the following Gradle commands:

- `./gradlew check` (Runs all checks)
- `./gradlew :androidApp:lintDebug` (Runs Android Lint)
- `./gradlew :shared:testDebugUnitTest` (Runs KMP shared unit tests)

## 5. Code Style and Enforcement

- Follow the [Official Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html).
- Enforced automatically via **ktlint** and **detekt**.
- Any PR failing lint checks will be blocked from merging.
- Format code locally before committing to save time.

## 6. Documentation Update Rules

Documentation is a first-class citizen. Whenever code changes impact the project architecture, status, or design decisions, update the relevant docs:

- `PLAN.md`: If phase objectives or major structural pieces change.
- `CURRENT_STATUS.md`: After completing tasks, features, or entire phases.
- `DECISION_LOG.md`: When making architectural, dependency, or design choices.

## 7. CI/CD Pipeline

The project utilizes GitHub Actions for automated integration:

- **Trigger:** On every PR targeting `main`.
- **Workflow Steps:**
  - Check out repository
  - Set up JDK 17
  - Cache Gradle packages
  - Run `./gradlew check :androidApp:lintDebug :shared:testDebugUnitTest`
  - Report coverage and lint results

## 8. Release Process

- **Versioning:** We use Semantic Versioning (SemVer) in the format `MAJOR.MINOR.PATCH`.
- **Distribution:** Initial releases are pushed to the Google Play Store Internal Testing track.
- **Tags:** Releases are triggered by pushing a lightweight Git tag (e.g., `v1.0.0`). The CI pipeline automatically builds the signed AAB and publishes it to the console.

## 9. Agent Collaboration Rules

When AI agents are contributing to the codebase:

- **Bounded Tasks:** Handle one strictly bounded task or feature at a time. Do not wander into unrelated code adjustments.
- **Verification:** Always run local build and test commands to verify functionality before marking a task as complete.
- **Communication:** Document findings and update context docs as needed during the execution of a sub-task.
