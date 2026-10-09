package dev.muazkadan.myapplication.data.startup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Starts the app when the user logs in, the way each system expects of a per-user app:
 * - macOS: a LaunchAgent, `~/Library/LaunchAgents/<APP_ID>.plist`. Unlike SMAppService's login
 *   item it can pass [AUTOSTART_ARG], and AssociatedBundleIdentifiers shows it under the app's own
 *   name in System Settings → Login Items. It starts the app through `open`, as Finder would: a
 *   bundle's binary started by launchd itself is no app to the Dock, which shows a generic "exec"
 *   icon for it.
 * - Windows: a value under `HKCU\Software\Microsoft\Windows\CurrentVersion\Run`, which Task
 *   Manager's Startup apps tab lists and can turn off.
 * - Linux: an XDG autostart entry, `$XDG_CONFIG_HOME/autostart/<APP_ID>.desktop`.
 *
 * Every entry starts the packaged launcher ([DesktopAppPathResolver]) with [AUTOSTART_ARG], so the
 * app can tell a login from the user opening it.
 */
object AutoStartManager : LaunchAtStartup {
    // The macOS bundle ID (bundleID in desktopApp/build.gradle.kts) and the app's name
    // (packageName there). Rename them with the app.
    const val APP_ID = "dev.muazkadan.myapplication.desktopApp"
    private const val APP_NAME = "CMPTemplate"

    const val AUTOSTART_ARG = "--autostart"

    private enum class Os { MAC, WINDOWS, LINUX, OTHER }

    private val os: Os =
        System.getProperty("os.name", "").lowercase().let {
            when {
                it.contains("mac") -> Os.MAC
                it.contains("windows") -> Os.WINDOWS
                it.contains("linux") -> Os.LINUX
                else -> Os.OTHER
            }
        }

    /** False on other systems, and where there is no packaged launcher to start (dev runs). */
    override val isSupported: Boolean by lazy {
        os != Os.OTHER && DesktopAppPathResolver.resolveExecutablePath() != null
    }

    override suspend fun setEnabled(enabled: Boolean): Boolean = withContext(Dispatchers.IO) { setEnabledBlocking(enabled) }

    /**
     * Rewrites an existing entry for the launcher's current path, after the app moved or was
     * reinstalled elsewhere. An entry the user removed in the system's settings stays removed.
     *
     * @return whether the app starts at login.
     */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) { isEnabled() && setEnabledBlocking(true) }

    private fun isEnabled(): Boolean =
        when (os) {
            Os.MAC -> macPlistFile.exists()
            Os.WINDOWS -> runReg("query", WINDOWS_RUN_KEY, "/v", APP_NAME)
            Os.LINUX -> linuxAutostartFile.exists()
            Os.OTHER -> false
        }

    private fun setEnabledBlocking(enabled: Boolean): Boolean {
        if (!enabled) return disable()
        val executable = DesktopAppPathResolver.resolveExecutablePath() ?: return false
        return try {
            when (os) {
                Os.MAC -> {
                    macPlistFile.writeEntry(buildMacPlist(executable))
                }

                Os.WINDOWS -> {
                    runReg(
                        "add",
                        WINDOWS_RUN_KEY,
                        "/v",
                        APP_NAME,
                        "/t",
                        "REG_SZ",
                        "/d",
                        windowsRunCommand(executable),
                        "/f",
                    )
                }

                Os.LINUX -> {
                    linuxAutostartFile.writeEntry(buildLinuxDesktopEntry(executable))
                }

                Os.OTHER -> {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun disable(): Boolean =
        try {
            when (os) {
                Os.MAC -> !macPlistFile.exists() || macPlistFile.delete()
                Os.WINDOWS -> runReg("delete", WINDOWS_RUN_KEY, "/v", APP_NAME, "/f") || !isEnabled()
                Os.LINUX -> !linuxAutostartFile.exists() || linuxAutostartFile.delete()
                Os.OTHER -> true
            }
        } catch (_: Exception) {
            false
        }

    private fun File.writeEntry(content: String): Boolean {
        parentFile?.mkdirs()
        writeText(content)
        return true
    }

    // --- macOS ---

    private val macPlistFile: File
        get() = File(System.getProperty("user.home"), "Library/LaunchAgents/$APP_ID.plist")

    internal fun buildMacPlist(executablePath: String): String {
        val arguments =
            macProgramArguments(executablePath).joinToString("\n") { "        <string>${it.escapeXml()}</string>" }
        return """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            |<plist version="1.0">
            |<dict>
            |    <key>Label</key>
            |    <string>$APP_ID</string>
            |    <key>AssociatedBundleIdentifiers</key>
            |    <string>$APP_ID</string>
            |    <key>ProgramArguments</key>
            |    <array>
            |$arguments
            |    </array>
            |    <key>RunAtLoad</key>
            |    <true/>
            |</dict>
            |</plist>
            """.trimMargin()
    }

    /**
     * `open -g -a <bundle> --args --autostart`: through Launch Services, without taking focus from
     * whatever the user opens first at login. A launcher outside a bundle is started directly.
     */
    internal fun macProgramArguments(executablePath: String): List<String> {
        val bundle =
            File(executablePath)
                .parentFile
                ?.takeIf { it.name == "MacOS" }
                ?.parentFile
                ?.takeIf { it.name == "Contents" }
                ?.parentFile
                ?.takeIf { it.name.endsWith(".app") }
                ?: return listOf(executablePath, AUTOSTART_ARG)
        return listOf("/usr/bin/open", "-g", "-a", bundle.path, "--args", AUTOSTART_ARG)
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    // --- Windows ---

    private const val WINDOWS_RUN_KEY = """HKCU\Software\Microsoft\Windows\CurrentVersion\Run"""

    /**
     * The Run value, `"C:\…\CMPTemplate.exe" --autostart`, written as reg.exe must receive it.
     * Java quotes an argument with spaces but leaves quotes inside it alone, so they are escaped
     * here, or reg.exe would split the path at its first space.
     */
    internal fun windowsRunCommand(executablePath: String): String = "\\\"$executablePath\\\" $AUTOSTART_ARG"

    private fun runReg(vararg args: String): Boolean {
        val process =
            ProcessBuilder("reg", *args)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start()
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return false
        }
        return process.exitValue() == 0
    }

    // --- Linux ---

    private val linuxAutostartFile: File
        get() {
            val configHome =
                System
                    .getenv("XDG_CONFIG_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::File)
                    ?: File(System.getProperty("user.home"), ".config")
            return File(configHome, "autostart/$APP_ID.desktop")
        }

    internal fun buildLinuxDesktopEntry(executablePath: String): String {
        // jpackage puts the icon at /opt/<package>/lib/<launcher>.png
        val launcher = File(executablePath)
        val icon = launcher.parentFile?.parentFile?.let { File(it, "lib/${launcher.name}.png") }
        return buildString {
            appendLine("[Desktop Entry]")
            appendLine("Type=Application")
            appendLine("Name=$APP_NAME")
            appendLine("Exec=${executablePath.quoteDesktopExecArg()} $AUTOSTART_ARG")
            if (icon != null && icon.isFile) appendLine("Icon=${icon.absolutePath}")
            appendLine("Terminal=false")
            appendLine("X-GNOME-Autostart-enabled=true")
        }
    }

    // The Desktop Entry spec's quoting for Exec: in double quotes, backslash before " ` $ and \,
    // and % doubled since it starts a field code
    private fun String.quoteDesktopExecArg(): String {
        val escaped =
            replace("\\", "\\\\\\\\")
                .replace("\"", "\\\\\"")
                .replace("`", "\\\\`")
                .replace("$", "\\\\$")
                .replace("%", "%%")
        return "\"$escaped\""
    }
}
