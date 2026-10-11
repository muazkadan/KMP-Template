package dev.muazkadan.myapplication

import java.io.File

/** The desktop app's identity and where it keeps its files. Rename these with the app. */
object DesktopAppData {
    // The macOS bundle ID (bundleID in desktopApp/build.gradle.kts). Also names the app's login
    // entries, its Linux desktop entry, and its Windows notification registration.
    const val APP_ID = "dev.muazkadan.myapplication.desktopApp"

    // packageName in desktopApp/build.gradle.kts: the launcher's name, and how the system lists the app
    const val APP_NAME = "CMPTemplate"

    // The URL scheme Windows notifications' clicks start the app with (cmptemplate://...)
    const val URL_SCHEME = "cmptemplate"

    // Under the user's home directory: settings, and the single-instance port
    private const val DIR_NAME = ".cmptemplate"

    val dir: File
        get() = File(System.getProperty("user.home"), DIR_NAME)
}
