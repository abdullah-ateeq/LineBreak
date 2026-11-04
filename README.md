# LineBreak 🎯⚡

**LineBreak** is a fast-paced, high-precision arcade game for Android built entirely with **Kotlin** and **Jetpack Compose**. Test your reflexes by pinpointing and shattering moving weak points on descending lines before they breach the danger zone!

---

## 🎮 Gameplay & Mechanics

- **Precision Tapping:** Lines with varying tilt angles descend towards a danger line at the bottom of the screen. Tap directly on each line's glowing **weak point** to shatter it.
- **Timing & Precision Tiers:**
  - 🎯 **PERFECT!** (+100 pts) – Center hit with maximum bonus score and streak multipliers.
  - 💥 **GREAT** (+50 pts) – Solid hit near the core.
  - 👍 **GOOD** (+25 pts) – Edge hit.
- **Combo & Streak System:** Chain consecutive successful breaks to build up combo streak multipliers for massive high scores.
- **Dynamic Difficulty:** As your score rises, line fall speeds increase, spawn intervals shrink, and line tilt angles become steeper.
- **Fail Conditions:**
  - ❌ Missing the weak point when tapping a line.
  - 🚨 Allowing any line to reach the danger line.

---

## ✨ Features

- 🎨 **100% Declarative UI with Jetpack Compose & Material 3**
- ⚡ **Custom Canvas Physics Engine:** Smooth 60 FPS physics loop for line motion, collision detection, screen shake, and particle explosion effects.
- 🔊 **Immersive Audio & Haptic Feedback:** Toggleable custom sound effects and haptic vibrations.
- 🌙 **Dynamic Theme Support:** Full support for Dark Theme, Light Theme, and System Default options.
- 💾 **Persistence:** Persistent high scores and settings using **Jetpack DataStore Preferences**.
- 📐 **Edge-to-Edge & Responsive Design:** Scales across modern Android screen aspect ratios.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow using `StateFlow`
- **State & Storage:** [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Asynchronous Execution:** Kotlin Coroutines & Flow
- **Build System:** Gradle KTS with Version Catalog (`libs.versions.toml`)
- **Target SDK:** Android 16 (API 37) | **Min SDK:** Android 7.0 (API 24)

---

## 📁 Project Structure

```
app/src/main/java/com/example/linebreak/
├── data/           # DataStore preferences & settings management
├── engine/         # GamePhysics collision, spawner & precision mechanics
├── model/          # Game state, particle, line, and result data models
├── sound/          # SoundManager & HapticManager integration
└── ui/
    ├── about/      # About & Instructions screen
    ├── game/       # Core game canvas view & GameViewModel
    ├── home/       # Home screen with animated preview
    ├── navigation/ # Screen routes definition
    ├── result/     # Game Over & High Score summary screen
    ├── settings/   # Audio, haptics, and theme options screen
    └── theme/      # Custom Material 3 color palettes, typography & theme setup
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug / Jellyfish or newer recommended)
- **JDK 11** or higher
- **Android SDK Platform 37** (or compatible setup)

### Building & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abdullah-ateeq/LineBreak.git
   cd LineBreak
   ```

2. **Build with Gradle:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```

4. **Install on connected device / emulator:**
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Testing

Unit tests for game physics, collision math, score calculations, precision tier evaluations, and difficulty scaling are located in `app/src/test/java/com/example/linebreak/GamePhysicsTest.kt`.

To run tests:
```bash
./gradlew test
```

---

## 👨‍💻 Author

Created by [Abdullah Ateeq](https://github.com/abdullah-ateeq).

Contributions, issues, and feature requests are welcome! Feel free to check out the [issues page](https://github.com/abdullah-ateeq/LineBreak/issues).

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
