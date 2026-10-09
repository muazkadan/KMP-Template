package dev.muazkadan.myapplication.desktopApp

import dev.muazkadan.myapplication.data.startup.AutoStartManager

/**
 * Decides from the command line whether the window starts hidden, leaving only the tray icon.
 */
object DesktopStartupHandler {
    const val MINIMIZED_FLAG = "--minimized"
    const val SHOW_FLAG = "--show"

    /**
     * [SHOW_FLAG] and [MINIMIZED_FLAG] decide outright. Otherwise the stored preference applies to
     * a launch at login ([AutoStartManager.AUTOSTART_ARG]) only: opened by hand, the app shows its
     * window, or it would look like it did not start.
     */
    fun shouldStartMinimized(
        args: Array<String>,
        startMinimizedPreference: Boolean,
    ): Boolean =
        when {
            SHOW_FLAG in args -> false
            MINIMIZED_FLAG in args -> true
            else -> startMinimizedPreference && AutoStartManager.AUTOSTART_ARG in args
        }
}
