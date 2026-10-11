package dev.muazkadan.myapplication.data.notifications

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowsToastsTest {
    @Test
    fun clickOnTheToastOpensTheAppThroughItsUrl() {
        val xml = WindowsToasts.buildToastXml(Notification("Title", "Message"))

        assertTrue("""activationType="protocol" launch="cmptemplate://open"""" in xml)
        assertFalse("<actions>" in xml)
    }

    @Test
    fun buttonLaunchesItsActionsUrl() {
        val xml =
            WindowsToasts.buildToastXml(
                Notification("Title", "Message", action = NotificationAction("open-settings", "Open settings")),
            )

        assertTrue(
            """<action content="Open settings" activationType="protocol" arguments="cmptemplate://action/open-settings"/>""" in xml,
        )
    }

    @Test
    fun soundFollowsTheNotification() {
        assertTrue("ms-winsoundevent:Notification.Default" in WindowsToasts.buildToastXml(Notification("T", "M")))
        assertTrue("""<audio silent="true"/>""" in WindowsToasts.buildToastXml(Notification("T", "M", withSound = false)))
    }

    @Test
    fun textIsEscapedAndTheXmlParses() {
        val xml = WindowsToasts.buildToastXml(Notification("<Fajr & \"Dhuhr\">", "it's"))

        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8)))
        val texts = document.getElementsByTagName("text")
        assertEquals("<Fajr & \"Dhuhr\">", texts.item(0).textContent)
        assertEquals("it's", texts.item(1).textContent)
    }

    @Test
    fun scriptIsAsciiAndCarriesTheXmlIntact() {
        val xml = WindowsToasts.buildToastXml(Notification("العربية", "Türkçe"))
        val script = WindowsToasts.buildPowerShellScript(xml)

        assertTrue(script.all { it.code < 128 })
        val base64 = Regex("""FromBase64String\('([^']+)'\)""").find(script)!!.groupValues[1]
        assertEquals(xml, String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8))
    }

    @Test
    fun protocolCommandIsQuotedForRegExe() {
        assertEquals(
            """\"C:\Program Files\CMPTemplate\CMPTemplate.exe\" \"%1\"""",
            WindowsToasts.protocolOpenCommand("""C:\Program Files\CMPTemplate\CMPTemplate.exe"""),
        )
    }

    @Test
    fun toastsAreOffUntilRegistered() {
        assertFalse(WindowsToasts.show(Notification("Title", "Message")))
    }
}
