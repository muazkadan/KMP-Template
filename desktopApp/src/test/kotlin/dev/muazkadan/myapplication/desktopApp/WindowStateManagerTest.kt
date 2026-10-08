package dev.muazkadan.myapplication.desktopApp

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowStateManagerTest {
    // A throwaway node, removed after each test
    private val prefs = Preferences.userRoot().node("window-state-test-${UUID.randomUUID()}")
    private val manager = WindowStateManager(prefs)

    @AfterTest
    fun tearDown() {
        prefs.removeNode()
    }

    @Test
    fun savedBoundsComeBack() {
        manager.save(
            WindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition(40.dp, 60.dp),
                size = DpSize(900.dp, 700.dp),
            ),
        )

        val loaded = manager.load()

        assertEquals(WindowPlacement.Floating, loaded.placement)
        assertEquals(DpSize(900.dp, 700.dp), loaded.size)
    }

    @Test
    fun maximizedKeepsTheFloatingSizeItReturnsTo() {
        manager.save(WindowState(size = DpSize(900.dp, 700.dp)))
        manager.save(WindowState(placement = WindowPlacement.Maximized, size = DpSize(1920.dp, 1080.dp)))

        val loaded = manager.load()

        assertEquals(WindowPlacement.Maximized, loaded.placement)
        assertEquals(DpSize(900.dp, 700.dp), loaded.size)
    }

    @Test
    fun sizeIsNeverBelowTheMinimum() {
        manager.save(WindowState(size = DpSize(100.dp, 100.dp)))

        assertEquals(
            DpSize(WindowStateManager.MIN_WIDTH.dp, WindowStateManager.MIN_HEIGHT.dp),
            manager.load().size,
        )
    }
}
