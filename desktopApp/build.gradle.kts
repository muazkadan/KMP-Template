import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(libs.flatlaf)
    implementation(libs.composeNativeTray)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kermit.test)
}

// The JNI library behind MacOsNotificationBridge (sharedUI), a universal binary. It goes in the
// app's resources rather than a jar, so packaging signs it with the app and nothing is unpacked at
// runtime. Elsewhere than macOS the task is skipped and the app uses its other notifications.
val appResourcesDir = layout.buildDirectory.dir("appResources")
val compileMacOsNotifications =
    tasks.register<Exec>("compileMacOsNotifications") {
        description = "Compiles the native macOS notification library."
        val source = file("src/main/native/MacOsNotifications.m")
        val output = appResourcesDir.get().file("macos/libmacos_notifications.dylib").asFile
        // Read here rather than in onlyIf, which the configuration cache can't serialize a reference
        // to the build script from
        val isMac = providers.systemProperty("os.name").get().startsWith("Mac")
        inputs.file(source)
        outputs.file(output)
        onlyIf { isMac }
        if (isMac) {
            // The JNI headers come from a JDK toolchain, not from the JVM running Gradle: Android
            // Studio runs Gradle on its own runtime, which has no include directory
            val jniHeaders =
                javaToolchains
                    .compilerFor { languageVersion.set(JavaLanguageVersion.of(17)) }
                    .get()
                    .metadata.installationPath
                    .dir("include")
                    .asFile
            doFirst { output.parentFile.mkdirs() }
            commandLine(
                "xcrun",
                "clang",
                "-dynamiclib",
                "-arch",
                "arm64",
                "-arch",
                "x86_64",
                // The presentation options it asks for arrived in macOS 11
                "-mmacosx-version-min=11.0",
                "-O2",
                "-fobjc-arc",
                "-Wall",
                "-Werror",
                "-I",
                jniHeaders.path,
                "-I",
                jniHeaders.resolve("darwin").path,
                "-framework",
                "Cocoa",
                "-framework",
                "UserNotifications",
                source.path,
                "-o",
                output.path,
            )
        }
    }

// The icon of the app's Windows toasts (WindowsToasts in sharedUI): Windows takes it from a file
// path, and the packaged app's resources are where one is known to be
val copyWindowsNotificationIcon =
    tasks.register<Copy>("copyWindowsNotificationIcon") {
        description = "Copies the icon Windows shows on the app's notifications."
        from(rootProject.file("sharedUI/src/commonMain/composeResources/drawable/app_icon.png"))
        into(appResourcesDir.map { it.dir("windows") })
        rename { "notification_icon.png" }
    }

// Compose registers it once the project is evaluated
tasks.matching { it.name == "prepareAppResources" }.configureEach {
    dependsOn(compileMacOsNotifications, copyWindowsNotificationIcon)
}

compose.desktop {
    application {
        mainClass = "dev.muazkadan.myapplication.desktopApp.MainKt"

        nativeDistributions {
            appResourcesRootDir.set(appResourcesDir)
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CMPTemplate"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("appIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("appIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("appIcons/MacosIcon.icns"))
                bundleID = "dev.muazkadan.myapplication.desktopApp"
            }
        }
    }
}
