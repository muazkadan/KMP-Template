package dev.muazkadan.myapplication.data.notifications

import co.touchlab.kermit.Logger
import dev.muazkadan.myapplication.DesktopAppData
import dev.muazkadan.myapplication.data.startup.DesktopAppPathResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities

/**
 * Desktop notifications, the way each system shows its own:
 * - macOS: UserNotifications through [MacOsNotificationBridge] in the packaged .app; osascript under
 *   `./gradlew run` and hot reload, where the notification shows under Script Editor's name.
 * - Linux: notify-send ([LinuxNotifications]), with the notification's button.
 * - Windows: toasts ([WindowsToasts]), with the notification's button, once [registerWithSystem]
 *   has registered the packaged app.
 * - Anywhere they fail: a tray balloon, and a beep for the sound.
 *
 * A click reaches [addActionListener]'s listeners as [OPEN_ACTION], or a button's ID. On Windows the
 * click starts the app with a URL ([urlFor]), which the desktop app hands to [handleUrl].
 */
object DesktopNotifier : Notifier {
    private val log = Logger.withTag("DesktopNotifier")

    /** What a click on the notification itself reports. */
    const val OPEN_ACTION = "open"

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

    private val actionListeners = CopyOnWriteArrayList<(String) -> Unit>()

    /** [listener] is called on the AWT event thread with the action the user clicked. */
    fun addActionListener(listener: (String) -> Unit) {
        actionListeners += listener
    }

    fun removeActionListener(listener: (String) -> Unit) {
        actionListeners -= listener
    }

    /**
     * Registers the packaged app with Windows, which shows toasts only for a registered app. Call it at
     * startup; it does nothing elsewhere, and under `./gradlew run`, which has no launcher to register.
     */
    suspend fun registerWithSystem() {
        if (os != Os.WINDOWS) return
        withContext(Dispatchers.IO) {
            DesktopAppPathResolver.resolveExecutablePath()?.let(WindowsToasts::register)
        }
    }

    override suspend fun show(notification: Notification) {
        withContext(Dispatchers.IO) {
            val shown =
                when (os) {
                    Os.MAC -> {
                        MacOsNotificationBridge.post(notification.title, notification.message, notification.withSound) ||
                            showWithOsaScript(notification)
                    }

                    Os.LINUX -> {
                        LinuxNotifications.show(notification) { action ->
                            dispatchAction(if (action == LinuxNotifications.ACTION_DEFAULT) OPEN_ACTION else action)
                        }
                    }

                    Os.WINDOWS -> {
                        WindowsToasts.show(notification)
                    }

                    Os.OTHER -> {
                        false
                    }
                }
            if (!shown) showTrayBalloon(notification)
        }
    }

    /**
     * The URL a Windows notification starts the app with for [actionId]: `cmptemplate://open` for a
     * click on the notification, `cmptemplate://action/<id>` for a button. Keep IDs to letters,
     * digits and dashes.
     */
    fun urlFor(actionId: String): String =
        if (actionId == OPEN_ACTION) "${DesktopAppData.URL_SCHEME}://open" else "${DesktopAppData.URL_SCHEME}://action/$actionId"

    /** The action in a URL from [urlFor], or null when [url] isn't one. Windows may add a slash. */
    fun actionFromUrl(url: String): String? {
        val prefix = "${DesktopAppData.URL_SCHEME}://"
        if (!url.startsWith(prefix, ignoreCase = true)) return null
        val path = url.substring(prefix.length).trimEnd('/')
        return when {
            path.equals("open", ignoreCase = true) -> OPEN_ACTION
            path.startsWith("action/", ignoreCase = true) -> path.substringAfter('/').takeIf { it.isNotEmpty() }
            else -> null
        }
    }

    /**
     * Hands the action in a URL a Windows notification started the app with to the listeners.
     *
     * @return whether [url] was one.
     */
    fun handleUrl(url: String): Boolean {
        val action = actionFromUrl(url) ?: return false
        dispatchAction(action)
        return true
    }

    internal fun dispatchAction(action: String) {
        SwingUtilities.invokeLater {
            actionListeners.forEach { listener ->
                try {
                    listener(action)
                } catch (e: Exception) {
                    log.e(e) { "Notification action listener failed for $action" }
                }
            }
        }
    }

    private fun showWithOsaScript(notification: Notification): Boolean {
        val title = notification.title.replace("\\", "\\\\").replace("\"", "\\\"")
        val message = notification.message.replace("\\", "\\\\").replace("\"", "\\\"")
        val sound = if (notification.withSound) " sound name \"default\"" else ""
        return try {
            ProcessBuilder("osascript", "-e", "display notification \"$message\" with title \"$title\"$sound")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start()
            true
        } catch (e: Exception) {
            log.w(e) { "osascript could not be run" }
            false
        }
    }

    private fun showTrayBalloon(notification: Notification) {
        if (!SystemTray.isSupported()) return
        try {
            val tray = SystemTray.getSystemTray()
            val trayIcon =
                tray.trayIcons.firstOrNull()
                    ?: TrayIcon(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), DesktopAppData.APP_NAME).also(tray::add)
            trayIcon.displayMessage(notification.title, notification.message, TrayIcon.MessageType.INFO)
            if (notification.withSound) Toolkit.getDefaultToolkit().beep()
        } catch (e: Exception) {
            log.w(e) { "No tray to show the notification in" }
        }
    }
}
