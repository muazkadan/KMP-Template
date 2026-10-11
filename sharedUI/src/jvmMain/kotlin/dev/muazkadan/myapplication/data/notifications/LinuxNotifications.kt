package dev.muazkadan.myapplication.data.notifications

import co.touchlab.kermit.Logger
import dev.muazkadan.myapplication.DesktopAppData
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Linux notifications through `notify-send` (libnotify).
 *
 * With actions, notify-send waits until the notification is closed and prints the action clicked,
 * which reaches [DesktopNotifier]: [ACTION_DEFAULT] for a click on the notification itself, or a
 * button's ID.
 */
internal object LinuxNotifications {
    private val log = Logger.withTag("LinuxNotifications")

    // The one a click on the notification itself invokes; servers don't show it as a button
    const val ACTION_DEFAULT = "default"

    private val MODERN_LIBNOTIFY = listOf(0, 7, 11)

    // notify-send learned --action in libnotify 0.7.10 and boolean hints in 0.7.11. An older one
    // (Ubuntu 22.04 has 0.7.9) rejects either and shows nothing, so it gets a plain notification
    private val modernNotifySend: Boolean by lazy {
        val version =
            try {
                val process = ProcessBuilder("notify-send", "--version").redirectErrorStream(true).start()
                process.inputStream
                    .bufferedReader()
                    .use { it.readText() }
                    .also { process.waitFor(5, TimeUnit.SECONDS) }
            } catch (_: Exception) {
                ""
            }
        isAtLeast(version, MODERN_LIBNOTIFY)
    }

    /** @return false when notify-send couldn't be run, for the caller to fall back. */
    fun show(
        notification: Notification,
        onAction: (String) -> Unit,
    ): Boolean {
        val interactive = modernNotifySend
        val command =
            buildCommand(
                notification,
                booleanHints = modernNotifySend,
                actions =
                    if (interactive) {
                        listOfNotNull(
                            notification.action?.let { it.id to it.label },
                            ACTION_DEFAULT to DesktopAppData.APP_NAME,
                        )
                    } else {
                        emptyList()
                    },
            )
        val process =
            try {
                ProcessBuilder(command)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .apply { if (!interactive) redirectOutput(ProcessBuilder.Redirect.DISCARD) }
                    .start()
            } catch (e: Exception) {
                log.w(e) { "notify-send could not be run" }
                return false
            }
        if (interactive) {
            thread(isDaemon = true, name = "LinuxNotificationActions") {
                // Printed when the user clicks; nothing when the notification is closed otherwise
                val action =
                    try {
                        process.inputStream
                            .bufferedReader()
                            .use { it.readLine() }
                            ?.trim()
                    } catch (e: Exception) {
                        log.w(e) { "Could not read the notification's action" }
                        null
                    }
                process.waitFor()
                if (!action.isNullOrEmpty()) onAction(action)
            }
        }
        return true
    }

    /** Whether `notify-send --version`'s [output] is [minimum] or later. */
    internal fun isAtLeast(
        output: String,
        minimum: List<Int>,
    ): Boolean {
        val version =
            Regex("""(\d+)\.(\d+)\.(\d+)""")
                .find(output)
                ?.groupValues
                ?.drop(1)
                ?.map(String::toInt) ?: return false
        return version.zip(minimum).firstOrNull { (have, need) -> have != need }?.let { (have, need) -> have > need } ?: true
    }

    internal fun buildCommand(
        notification: Notification,
        booleanHints: Boolean,
        actions: List<Pair<String, String>>,
    ): List<String> =
        buildList {
            add("notify-send")
            add("--app-name=${DesktopAppData.APP_NAME}")
            // The notification takes its icon and name from the desktop entry with this ID, and the
            // desktop groups the app's notifications under it. Change it if your Linux package
            // installs its desktop entry under another name
            add("--hint=string:desktop-entry:${DesktopAppData.APP_ID}")
            if (notification.withSound) {
                add("--hint=string:sound-name:message-new-instant")
            } else if (booleanHints) {
                add("--hint=boolean:suppress-sound:true")
            }
            actions.forEach { (id, label) -> add("--action=$id=$label") }
            // Ends the options, so a title starting with "-" isn't read as one
            add("--")
            add(notification.title)
            add(notification.message)
        }
}
