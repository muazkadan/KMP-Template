# Kotlin Multiplatform Template

A modern Kotlin Multiplatform project template with Compose Multiplatform targeting Android, iOS, and Desktop, structured following the latest multiplatform architecture.

## Project Structure

- **`/sharedUI`** - Kotlin Multiplatform shared library module (`com.android.kotlin.multiplatform.library`)
  - `commonMain` - Shared UI, screens, navigation, resources, and business logic
  - `androidMain` - Android-specific shared implementations
  - `jvmMain` - Desktop/JVM-specific shared implementations
  - `iosMain` - iOS-specific shared implementations (exports `SharedUI.framework`)
- **`/androidApp`** - Android Application entry module (`com.android.application`)
- **`/desktopApp`** - Desktop Application entry module (`kotlin("jvm")` + Compose Desktop)
- **`/iosApp`** - iOS app entry point and SwiftUI integration (Xcode project)

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Navigation 3 Compose
- Koin (Dependency Injection)
- kotlinx.serialization
- Compose Hot Reload (Desktop)
- Lifecycle ViewModel
- DataStore Preferences (settings storage)
- kotlinx-datetime
- ktlint

## Getting Started

### Android
To build the application APK:
```bash
./gradlew :androidApp:assembleDebug
```
To install and run:
```bash
./gradlew :androidApp:installDebug
```

### Desktop
Run the desktop application:
```bash
./gradlew :desktopApp:run
```
Run with [Compose Hot Reload](https://github.com/JetBrains/compose-hot-reload) enabled:
```bash
./gradlew :desktopApp:hotRun --auto
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run standard configuration.

## What's Included

- **Settings storage** - `PreferencesManager` wraps a DataStore that each platform module creates at its own path. The stored theme (system, light or dark) is the worked example: the Settings screen changes it and `AppTheme` applies it.
- **Theme** - `presentation/theme` holds the colour scheme, typography and `AppTheme`. Replace `Color.kt` with an export from [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).
- **One-shot events** - `ObserveAsEvents` collects navigation and snackbar events from a ViewModel only while the screen is started. The splash screen uses it to move on to Home.
- **Testable time** - `Clock` and `TimeZone` are bound in Koin, so code that depends on "now" can be tested with fixed values.
- **Desktop app shell** - one running instance (a second launch brings the window back, and can pass the running app a message), a tray icon to open, hide or quit the app, closing the window hides it to the tray, the window's size and position are remembered, and a File menu with Close and Quit shortcuts. On Linux and Windows, FlatLaf draws the menus in the app's theme.
- **Test helpers** - `InMemoryPreferencesDataStore` for DataStore-backed code in `commonTest`, and `cancelScopeForTest()` to stop a ViewModel's coroutines before `Dispatchers.resetMain()`.

## Code Style

The project uses [ktlint](https://pinterest.github.io/ktlint/), and CI runs `ktlintCheck`.
```bash
./gradlew ktlintFormat
```
Install the pre-commit hook to format staged Kotlin files on every commit:
```bash
./hooks/install-hooks.sh
```

## Testing

Run the shared tests on the JVM (the fastest target):
```bash
./gradlew :sharedUI:jvmTest
```
and the desktop app's own tests:
```bash
./gradlew :desktopApp:test
```

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)