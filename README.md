# Kotlin Multiplatform Template

A Kotlin Multiplatform project template with Compose Multiplatform targeting Android, iOS, and Desktop.

## Project Structure

- **`/composeApp`** - Shared code and UI using Compose Multiplatform
  - `commonMain` - Code shared across all platforms
  - `androidMain` - Android-specific code
  - `iosMain` - iOS-specific code  
  - `desktopMain` - Desktop-specific code

- **`/iosApp`** - iOS app entry point and SwiftUI integration

## Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Navigation Compose
- Koin (Dependency Injection)
- Lifecycle ViewModel

## Getting Started

### Android
Run the Android configuration in Android Studio or use:
```bash
./gradlew :composeApp:installDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run.

### Desktop
```bash
./gradlew :composeApp:run
```

## Learn More

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)