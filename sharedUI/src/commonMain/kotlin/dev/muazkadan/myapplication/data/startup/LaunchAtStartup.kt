package dev.muazkadan.myapplication.data.startup

/**
 * Starting the app when the user logs in. Bound only on desktop: mobile systems don't let an app
 * start itself at login.
 */
interface LaunchAtStartup {
    /** False where there's no installed app to start, such as under `./gradlew run`. */
    val isSupported: Boolean

    /** @return whether the system now matches [enabled]. */
    suspend fun setEnabled(enabled: Boolean): Boolean
}
