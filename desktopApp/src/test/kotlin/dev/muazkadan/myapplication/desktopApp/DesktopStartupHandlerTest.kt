package dev.muazkadan.myapplication.desktopApp

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopStartupHandlerTest {
    @Test
    fun minimizedFlag_startsHidden() {
        assertTrue(DesktopStartupHandler.shouldStartMinimized(arrayOf("--minimized"), startMinimizedPreference = false))
    }

    @Test
    fun autostartWithPreference_startsHidden() {
        assertTrue(DesktopStartupHandler.shouldStartMinimized(arrayOf("--autostart"), startMinimizedPreference = true))
    }

    @Test
    fun autostartWithoutPreference_showsTheWindow() {
        assertFalse(DesktopStartupHandler.shouldStartMinimized(arrayOf("--autostart"), startMinimizedPreference = false))
    }

    @Test
    fun openedByHand_showsTheWindowWhateverThePreference() {
        assertFalse(DesktopStartupHandler.shouldStartMinimized(emptyArray(), startMinimizedPreference = true))
        assertFalse(DesktopStartupHandler.shouldStartMinimized(emptyArray(), startMinimizedPreference = false))
    }

    @Test
    fun showFlag_overridesAutostart() {
        assertFalse(
            DesktopStartupHandler.shouldStartMinimized(arrayOf("--autostart", "--show"), startMinimizedPreference = true),
        )
    }
}
