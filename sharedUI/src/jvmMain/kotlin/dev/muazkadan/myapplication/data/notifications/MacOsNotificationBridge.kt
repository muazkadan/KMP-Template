package dev.muazkadan.myapplication.data.notifications

import co.touchlab.kermit.Logger
import java.io.File

/**
 * Native macOS notifications through UserNotifications.framework, by way of a small JNI library
 * (desktopApp/src/main/native). They carry the app's name and icon, where osascript's carry Script
 * Editor's, and a click on one reaches the app.
 *
 * The library ships in the app's resources (Compose's `appResourcesRootDir`), so it's signed and
 * notarized with the app. It works only from the packaged .app: [isAvailable] is false elsewhere -
 * other systems, `./gradlew run`, hot reload - and [DesktopNotifier] falls back to osascript.
 */
internal object MacOsNotificationBridge {
    private val log = Logger.withTag("MacOsNotificationBridge")
    private const val LIBRARY_FILE = "libmacos_notifications.dylib"

    val isAvailable: Boolean by lazy { load() }

    private fun load(): Boolean {
        if (!System.getProperty("os.name", "").lowercase().contains("mac")) return false
        val library =
            System
                .getProperty("compose.application.resources.dir")
                ?.let { File(it, LIBRARY_FILE) }
                ?.takeIf { it.isFile }
                ?: return false
        return try {
            System.load(library.absolutePath)
            nativeInit().also { initialized ->
                if (initialized) {
                    log.i { "Native macOS notifications ready" }
                } else {
                    log.d { "Not running from an app bundle, native macOS notifications are off" }
                }
            }
        } catch (e: Throwable) {
            log.w(e) { "Native macOS notifications could not be loaded" }
            false
        }
    }

    /** @return false when native notifications aren't available here, for the caller to fall back. */
    fun post(
        title: String,
        message: String,
        withSound: Boolean,
    ): Boolean {
        if (!isAvailable) return false
        return try {
            nativePostNotification(title, message, withSound)
            true
        } catch (e: Throwable) {
            log.w(e) { "Native macOS notification failed" }
            false
        }
    }

    /** Called from the native side, on a thread of its own, when the user clicks a notification. */
    @JvmStatic
    private fun onNotificationClicked() {
        DesktopNotifier.dispatchAction(DesktopNotifier.OPEN_ACTION)
    }

    @JvmStatic
    private external fun nativeInit(): Boolean

    @JvmStatic
    private external fun nativePostNotification(
        title: String,
        body: String,
        withSound: Boolean,
    )
}
