package dev.muazkadan.myapplication.desktopApp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.util.prefs.Preferences

/** Remembers the window's size, position and maximized state between launches. */
class WindowStateManager(
    private val prefs: Preferences = Preferences.userNodeForPackage(WindowStateManager::class.java),
) {
    fun load(): WindowState {
        val width = prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH).coerceAtLeast(MIN_WIDTH)
        val height = prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT).coerceAtLeast(MIN_HEIGHT)
        val x = prefs.getInt(KEY_X, Int.MIN_VALUE)
        val y = prefs.getInt(KEY_Y, Int.MIN_VALUE)

        // A position on a screen that's since been unplugged would open the window out of sight
        val position =
            if (x != Int.MIN_VALUE && y != Int.MIN_VALUE && isOnAScreen(x, y)) {
                WindowPosition(x.dp, y.dp)
            } else {
                WindowPosition.PlatformDefault
            }

        return WindowState(
            placement = if (prefs.getBoolean(KEY_MAXIMIZED, false)) WindowPlacement.Maximized else WindowPlacement.Floating,
            position = position,
            size = DpSize(width.dp, height.dp),
        )
    }

    fun save(state: WindowState) {
        val maximized = state.placement == WindowPlacement.Maximized
        prefs.putBoolean(KEY_MAXIMIZED, maximized)
        // A maximized window keeps the floating bounds it returns to
        if (!maximized) {
            prefs.putInt(
                KEY_WIDTH,
                state.size.width.value
                    .toInt(),
            )
            prefs.putInt(
                KEY_HEIGHT,
                state.size.height.value
                    .toInt(),
            )
            if (state.position.isSpecified) {
                prefs.putInt(
                    KEY_X,
                    state.position.x.value
                        .toInt(),
                )
                prefs.putInt(
                    KEY_Y,
                    state.position.y.value
                        .toInt(),
                )
            }
        }
        runCatching { prefs.flush() }
    }

    private fun isOnAScreen(
        x: Int,
        y: Int,
    ): Boolean =
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.any { device ->
                device.defaultConfiguration.bounds.contains(Point(x, y))
            }
        } catch (_: Exception) {
            // Headless: nothing to check against
            true
        }

    companion object {
        const val MIN_WIDTH = 350
        const val MIN_HEIGHT = 600
        private const val DEFAULT_WIDTH = 800
        private const val DEFAULT_HEIGHT = 600

        private const val KEY_WIDTH = "window_width"
        private const val KEY_HEIGHT = "window_height"
        private const val KEY_X = "window_x"
        private const val KEY_Y = "window_y"
        private const val KEY_MAXIMIZED = "window_maximized"
    }
}

/**
 * Saves [state] whenever the window moves, resizes or is maximized, once it has settled. Saving as
 * it changes, rather than on exit, keeps it through a quit that skips the app's own code, such as
 * Quit in the macOS app menu.
 */
@Composable
fun SaveWindowStateOnChange(
    manager: WindowStateManager,
    state: WindowState,
) {
    LaunchedEffect(manager, state) {
        snapshotFlow { Triple(state.placement, state.position, state.size) }
            .drop(1)
            .collectLatest {
                delay(500)
                manager.save(state)
            }
    }
}
