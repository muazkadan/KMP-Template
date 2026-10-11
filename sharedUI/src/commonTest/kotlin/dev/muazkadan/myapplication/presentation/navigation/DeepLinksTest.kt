package dev.muazkadan.myapplication.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinksTest {
    @Test
    fun deepLinksOpenTheirScreens() {
        assertEquals(Screen.Home, DeepLinks.parse("cmptemplate://nav/home"))
        assertEquals(Screen.Settings, DeepLinks.parse("cmptemplate://nav/settings"))
        // As a browser or the system may pass them on
        assertEquals(Screen.Settings, DeepLinks.parse(" CMPTemplate://nav/Settings/ "))
    }

    @Test
    fun otherUrisOpenNothing() {
        assertNull(DeepLinks.parse(null))
        assertNull(DeepLinks.parse("cmptemplate://nav/splash"))
        assertNull(DeepLinks.parse("cmptemplate://open"))
        assertNull(DeepLinks.parse("https://example.com/nav/settings"))
    }

    @Test
    fun deepLinkWaitsUntilConsumed() {
        val navigator = ExternalNavigator()

        assertTrue(navigator.onDeepLink(DeepLinks.SETTINGS))
        assertEquals(Screen.Settings, navigator.pendingDestination.value)

        assertEquals(Screen.Settings, navigator.consume())
        assertNull(navigator.pendingDestination.value)
        assertNull(navigator.consume())
    }

    @Test
    fun unknownUriLeavesTheQueuedDeepLink() {
        val navigator = ExternalNavigator()
        navigator.onDeepLink(DeepLinks.SETTINGS)

        assertFalse(navigator.onDeepLink("cmptemplate://nav/nowhere"))
        assertEquals(Screen.Settings, navigator.pendingDestination.value)
    }
}
