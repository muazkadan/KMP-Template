package dev.muazkadan.myapplication.data.notifications

import co.touchlab.kermit.Logger
import dev.muazkadan.myapplication.DesktopAppData
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Windows 10/11 toast notifications, in place of the tray icon's balloon tips: they carry the app's
 * name and icon, stay in the notification center, and can have a button.
 *
 * An unpackaged app gets them by registering an AppUserModelID under HKCU, and posts them through
 * WinRT's ToastNotificationManager from Windows PowerShell, which reaches WinRT without native code
 * of the app's own. A click on one launches a `cmptemplate://...` URL, a protocol registered to the
 * app's launcher, which hands it on to the running instance (SingleInstanceController in desktopApp).
 *
 * Only the packaged app registers ([register] needs its launcher): `./gradlew run` and hot reload,
 * and a failed registration, leave [show] false for the caller to fall back. A toast for an
 * AppUserModelID that isn't registered would be dropped by Windows without an error.
 */
internal object WindowsToasts {
    private val log = Logger.withTag("WindowsToasts")

    // Shipped in the app's resources by desktopApp's copyWindowsNotificationIcon
    private const val NOTIFICATION_ICON = "notification_icon.png"

    private val powerShellCommand = listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "-")

    private const val REG_AUMID_KEY = """HKCU\Software\Classes\AppUserModelId\${DesktopAppData.APP_ID}"""
    private const val REG_PROTOCOL_KEY = """HKCU\Software\Classes\${DesktopAppData.URL_SCHEME}"""

    @Volatile
    private var registered = false

    /**
     * Registers the AppUserModelID toasts are posted under, and the URL protocol their clicks launch,
     * for the launcher at [executablePath]. Rewritten on every start, so the entries follow the app if
     * it moves.
     */
    fun register(executablePath: String): Boolean {
        val icon =
            System
                .getProperty("compose.application.resources.dir")
                ?.let { File(it, NOTIFICATION_ICON) }
                ?.takeIf { it.isFile }
        registered =
            runReg("add", REG_AUMID_KEY, "/v", "DisplayName", "/t", "REG_SZ", "/d", DesktopAppData.APP_NAME, "/f") &&
            // Without it the toast has a generic icon
            (icon == null || runReg("add", REG_AUMID_KEY, "/v", "IconUri", "/t", "REG_SZ", "/d", icon.path, "/f")) &&
            runReg("add", REG_PROTOCOL_KEY, "/ve", "/t", "REG_SZ", "/d", "URL:${DesktopAppData.APP_NAME}", "/f") &&
            runReg("add", REG_PROTOCOL_KEY, "/v", "URL Protocol", "/t", "REG_SZ", "/d", "", "/f") &&
            runReg(
                "add",
                """$REG_PROTOCOL_KEY\shell\open\command""",
                "/ve",
                "/t",
                "REG_SZ",
                "/d",
                protocolOpenCommand(executablePath),
                "/f",
            )
        if (registered) {
            log.i { "Registered Windows toast notifications for $executablePath" }
        } else {
            log.w { "Could not register Windows toast notifications, falling back to tray balloons" }
        }
        return registered
    }

    /** @return false when toasts aren't available here, for the caller to fall back. */
    fun show(notification: Notification): Boolean {
        if (!registered) return false
        return try {
            // Windows PowerShell, not pwsh, which can't load WinRT types. Java starts it without a
            // console window, so there's nothing to hide
            val process =
                ProcessBuilder(powerShellCommand)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
            process.outputStream.bufferedWriter(StandardCharsets.US_ASCII).use { stdin ->
                stdin.write(buildPowerShellScript(buildToastXml(notification)))
            }
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                log.w { "Windows toast notification timed out" }
                false
            } else {
                (process.exitValue() == 0).also { shown ->
                    if (!shown) log.w { "Windows toast notification exited with code ${process.exitValue()}" }
                }
            }
        } catch (e: Exception) {
            log.w(e) { "Windows toast notification failed" }
            false
        }
    }

    /**
     * The toast's XML. Every activation is a protocol one: the app registers no COM activator, so a
     * foreground activation would reach it only while it runs, and only from a process that posted
     * the toast itself.
     */
    internal fun buildToastXml(notification: Notification): String =
        buildString {
            append("<toast activationType=\"protocol\" launch=\"${DesktopNotifier.urlFor(DesktopNotifier.OPEN_ACTION)}\">")
            append("<visual><binding template=\"ToastGeneric\">")
            append("<text>${notification.title.escapeXml()}</text>")
            append("<text>${notification.message.escapeXml()}</text>")
            append("</binding></visual>")
            if (notification.withSound) {
                append("<audio src=\"ms-winsoundevent:Notification.Default\"/>")
            } else {
                append("<audio silent=\"true\"/>")
            }
            notification.action?.let { action ->
                append("<actions>")
                append(
                    "<action content=\"${action.label.escapeXml()}\" activationType=\"protocol\" " +
                        "arguments=\"${DesktopNotifier.urlFor(action.id).escapeXml()}\"/>",
                )
                append("</actions>")
            }
            append("</toast>")
        }

    /**
     * The Windows PowerShell script posting [toastXml]. It goes in on stdin and the XML inside it as
     * Base64, so neither is quoted on a command line, and the script stays plain ASCII.
     */
    internal fun buildPowerShellScript(toastXml: String): String {
        val base64Xml = Base64.getEncoder().encodeToString(toastXml.toByteArray(StandardCharsets.UTF_8))
        return """
            ${'$'}ErrorActionPreference = 'Stop'
            [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null
            [Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] | Out-Null
            ${'$'}xml = [Windows.Data.Xml.Dom.XmlDocument]::new()
            ${'$'}xml.LoadXml([System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('$base64Xml')))
            ${'$'}toast = [Windows.UI.Notifications.ToastNotification]::new(${'$'}xml)
            [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('${DesktopAppData.APP_ID}').Show(${'$'}toast)
            """.trimIndent() + "\n"
    }

    /**
     * The protocol's open command, `"C:\...\CMPTemplate.exe" "%1"`, written as reg.exe must receive
     * it: Java quotes an argument with spaces but leaves quotes inside it alone (AutoStartManager).
     */
    internal fun protocolOpenCommand(executablePath: String): String = "\\\"$executablePath\\\" \\\"%1\\\""

    private fun runReg(vararg args: String): Boolean =
        try {
            val process =
                ProcessBuilder("reg", *args)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                log.w { "reg ${args.first()} timed out" }
                false
            } else {
                process.exitValue() == 0
            }
        } catch (e: Exception) {
            log.w(e) { "reg ${args.first()} failed" }
            false
        }

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
