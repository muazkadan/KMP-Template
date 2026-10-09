package dev.muazkadan.myapplication.data.startup

import java.io.File

/**
 * The native launcher of the packaged app, which the OS can start at login: the binary in
 * `CMPTemplate.app/Contents/MacOS` on macOS, `CMPTemplate.exe` on Windows, and
 * `/opt/cmptemplate/bin/CMPTemplate` on Linux. jpackage's launchers run the JVM in their own
 * process, so the current process's command is the launcher.
 *
 * Null when there is none to point at: `./gradlew run` and hot reload run a plain `java`, which
 * would start without the app's classpath, and a macOS app run from a translocated (quarantined)
 * copy lives at a random path that is gone after a restart.
 */
object DesktopAppPathResolver {
    private val javaLaunchers = setOf("java", "java.exe", "javaw", "javaw.exe")

    fun resolveExecutablePath(): String? {
        val command =
            ProcessHandle
                .current()
                .info()
                .command()
                .orElse(null)
                ?: return null
        return command.takeIf { isAppLauncher(it) && File(it).isFile }
    }

    internal fun isAppLauncher(command: String): Boolean =
        command.substringAfterLast('/').substringAfterLast('\\').lowercase() !in javaLaunchers &&
            "/AppTranslocation/" !in command
}
