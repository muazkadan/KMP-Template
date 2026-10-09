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

The template calls library APIs directly instead of wrapping them in its own helpers, so what you know from the official docs and other projects applies here unchanged.

- **Settings storage** - `PreferencesManager` wraps a DataStore that each platform module creates at its own path. The stored theme (system, light or dark) is the worked example: the Settings screen changes it and `AppTheme` applies it.
- **Theme** - `presentation/theme` holds the colour scheme, typography and `AppTheme`. Replace `Color.kt` with an export from [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).
- **Navigation** - plain Navigation 3: a `NavBackStack<Screen>` saved with `NavBackStackSerializer(Screen.serializer())`, shown by `NavDisplay`, and changed with ordinary list calls (`backStack.add(...)`, `removeLastOrNull()`). A new screen needs only its `Screen` entry and an `entry<...>` in `Navigation.kt`.
- **Testable time** - `Clock` and `TimeZone` are bound in Koin, so code that depends on "now" can be tested with fixed values.
- **Desktop app shell** - one running instance (a second launch brings the window back, and can pass the running app a message), a tray icon to open, hide or quit the app, closing the window hides it to the tray, the window's size and position are remembered, and a File menu with Close and Quit shortcuts. On Linux and Windows, FlatLaf draws the menus in the app's theme.
- **App language** - Settings → Language picks one of the app's translations, or follows the system. On Android 13+ it's the system's per-app language (also in Settings → Apps → Language); on desktop the app stores it and switches without a restart, right-to-left included. Arabic is included as an example translation. To add a language, add `composeResources/values-<tag>/strings.xml`, an entry in `appLanguages`, and a `<locale>` in `androidApp/src/main/res/xml/locales_config.xml`; `AppLanguagesTest` fails if they disagree.
- **Launch at login** - Settings → System startup can start the desktop app when the user logs in (a LaunchAgent on macOS, the Run registry key on Windows, an XDG autostart entry on Linux), optionally hidden in the tray. It only shows in a packaged app (`./gradlew :desktopApp:createDistributable`): `./gradlew run` has no launcher for the system to start. `--minimized` and `--show` override it from the command line. Rename `APP_ID` and `APP_NAME` in `AutoStartManager` with the app.
- **Test fake** - `InMemoryPreferencesDataStore`, a `DataStore` kept in memory for DataStore-backed code in `commonTest`.

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