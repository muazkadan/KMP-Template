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

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)