package dev.muazkadan.myapplication.desktopApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.presentation.theme.backgroundDark
import dev.muazkadan.myapplication.presentation.theme.backgroundLight
import dev.muazkadan.myapplication.presentation.theme.inversePrimaryDark
import dev.muazkadan.myapplication.presentation.theme.primaryLight
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme

/**
 * The look of the window's Swing parts - the menu bar and its menus - on Linux and Windows, where
 * Swing draws them itself in a dated light look. FlatLaf draws them instead, in the app's light or
 * dark theme and colours. macOS keeps its own look: the menus go to the system menu bar.
 */
object SwingTheme {
    private val isSupported = !System.getProperty("os.name", "").lowercase().contains("mac")
    private var appliedDarkTheme: Boolean? = null

    /**
     * Installs the look for [themeMode]. Call it before application(), which reads the property set
     * here, and before any window exists: a Swing component keeps the look it was created with.
     */
    fun install(themeMode: ThemeMode) {
        if (!isSupported) return
        // application() would otherwise install the system look over this one - on most Linux
        // desktops, Swing's old Metal look
        System.setProperty("skiko.rendering.laf.global", "false")
        // FlatLaf would otherwise replace the Windows title bar with its own, which Compose's
        // window isn't built for
        System.setProperty("flatlaf.useWindowDecorations", "false")
        apply(
            when (themeMode) {
                // isSystemInDarkTheme only works inside a window's content, which doesn't exist yet
                ThemeMode.SYSTEM -> currentSystemTheme == SystemTheme.DARK

                ThemeMode.LIGHT -> false

                ThemeMode.DARK -> true
            },
        )
    }

    /** Switches the look, and every open window with it. Call on the AWT event thread. */
    fun apply(darkTheme: Boolean) {
        if (!isSupported || appliedDarkTheme == darkTheme) return
        appliedDarkTheme = darkTheme

        val background = (if (darkTheme) backgroundDark else backgroundLight).toHex()
        FlatLaf.setGlobalExtraDefaults(
            mapOf(
                "@background" to background,
                // FlatLaf shades menus 5% off the background; these match the app instead
                "@menuBackground" to background,
                // A mid-tone in both: the dark scheme's primary is too pale to highlight a selection
                "@accentColor" to (if (darkTheme) inversePrimaryDark else primaryLight).toHex(),
            ),
        )
        if (darkTheme) FlatDarkLaf.setup() else FlatLightLaf.setup()
        FlatLaf.updateUI()
    }

    private fun Color.toHex(): String = "#%06X".format(toArgb() and 0xFFFFFF)
}

/** Keeps [SwingTheme] on [darkTheme]. Place it in a window's content. */
@Composable
fun SwingThemeEffect(darkTheme: Boolean) {
    SideEffect { SwingTheme.apply(darkTheme) }
}
