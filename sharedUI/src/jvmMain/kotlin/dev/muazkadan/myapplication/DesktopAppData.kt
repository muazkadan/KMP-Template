package dev.muazkadan.myapplication

import java.io.File

/** Where the desktop app keeps its files: settings, and the single-instance port. */
object DesktopAppData {
    // Under the user's home directory. Rename it with the app.
    private const val DIR_NAME = ".cmptemplate"

    val dir: File
        get() = File(System.getProperty("user.home"), DIR_NAME)
}
