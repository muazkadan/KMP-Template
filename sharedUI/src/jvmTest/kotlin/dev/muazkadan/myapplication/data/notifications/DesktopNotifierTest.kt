package dev.muazkadan.myapplication.data.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DesktopNotifierTest {
    @Test
    fun urlsCarryTheActionBackToTheApp() {
        assertEquals("cmptemplate://open", DesktopNotifier.urlFor(DesktopNotifier.OPEN_ACTION))
        assertEquals("cmptemplate://action/open-settings", DesktopNotifier.urlFor("open-settings"))

        assertEquals(DesktopNotifier.OPEN_ACTION, DesktopNotifier.actionFromUrl("cmptemplate://open"))
        assertEquals("open-settings", DesktopNotifier.actionFromUrl("cmptemplate://action/open-settings"))
    }

    @Test
    fun windowsTrailingSlashAndCaseAreIgnored() {
        assertEquals(DesktopNotifier.OPEN_ACTION, DesktopNotifier.actionFromUrl("CMPTemplate://open/"))
        assertEquals("open-settings", DesktopNotifier.actionFromUrl("cmptemplate://action/open-settings/"))
    }

    @Test
    fun otherArgumentsAreNotNotificationUrls() {
        assertNull(DesktopNotifier.actionFromUrl("--autostart"))
        assertNull(DesktopNotifier.actionFromUrl("https://example.com/open"))
        assertNull(DesktopNotifier.actionFromUrl("cmptemplate://action/"))
        assertNull(DesktopNotifier.actionFromUrl("cmptemplate://elsewhere"))
    }
}
