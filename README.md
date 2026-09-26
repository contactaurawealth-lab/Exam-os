# StudyOffline 🌿

> **An offline-first, private exam preparation companion for Android.**
> Built with Kotlin & Jetpack Compose. Zero servers, zero telemetry, zero advertisements.

---

## 📱 Features

- **Spaced Repetition (SM-2)**: Scientifically backed spaced repetition flashcards with custom rating (`Again`, `Hard`, `Good`, `Easy`) and 3D card flip animations.
- **Interactive Practice Quizzes**: Multiple-choice quiz sessions with calm feedback states, detailed score summaries, and weak-topics flagging.
- **Revision Calendar & Planner**: Weekly study load management (Light / Medium / Heavy days) and direct topic assignment.
- **On-Device Pomodoro Timer**: Circular countdown timer with configurable focus/break presets, screen wake-lock, and persistent Foreground Service status bar notification.
- **Exam Countdown Tracking**: Timezone-aware days-remaining computation with dynamic progress rings and milestone notifications at 7, 3, and 1 day.
- **Habit & Streak Intelligence**: Daily streak calculation with a midnight grace period and historical longest streak tracking.
- **Glance AppWidgets**:
  - *Exam Countdown Widget (2×1, 4×1)*: Glance-based home screen countdown ring.
  - *Today's Plan Widget (4×2)*: Interactive topic list with direct tap-to-complete checkboxes that write directly to the Room database.
- **Privacy & Data Freedom**: 100% offline Room database. Complete JSON export and schema-validated transactional import with Merge/Replace options via Storage Access Framework (SAF).

---

## 🎨 Design System

- **Warm-Neutral Palette**:
  - Light theme: `#FAF7F2` background, `#FFFFFF` surface, `#F1ECE3` surfaceMuted, `#7C9A82` sage green accent.
  - Dark theme: `#1C1B19` background, `#252420` surface, `#2E2C27` surfaceMuted, `#8FB396` accent.
- **Accessible Custom Accents**: Dynamic WCAG AA contrast verification with automatic `onAccent` derivation and 20% opacity surface tints.
- **Custom Line Icons**: Consistent 1.5dp stroke Phosphor-style vector iconography.
- **Hairline Borders**: 1dp `divider` borders instead of generic elevation drop shadows.
- **Typography**: Bundled `Manrope` font asset.

---

## 🛠 Tech Stack

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose + Material3 (customized tokens)
- **Dependency Injection**: Hilt 2.51.1
- **Database**: Room 2.6.1 (SQLite) with TypeConverters and indices
- **Settings**: Jetpack DataStore (Preferences)
- **Widgets**: Jetpack Glance 1.1.0 (AppWidget API)
- **Background Tasks**: WorkManager 2.9.1 + AlarmManager (exact/inexact)
- **Architecture**: Single-Activity, MVVM, Clean Architecture, Repository Pattern

---

## 🚀 Building & Running

### Requirements
- Android SDK 34+
- Java 17+
- Gradle 8.10+

### Build Commands

```bash
# Clone the repository
git clone <your-repo-url>
cd examos

# Build Release APK
./gradlew assembleRelease

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testReleaseUnitTest
```

### Pre-built APKs
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Root copy: `StudyOffline-release.apk`
