package dev.muazkadan.myapplication.data.notifications

import dev.muazkadan.myapplication.DesktopAppData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinuxNotificationsTest {
    @Test
    fun commandCarriesTheButtonAndTheDefaultAction() {
        val command =
            LinuxNotifications.buildCommand(
                Notification("Title", "Message", action = NotificationAction("open-settings", "Open settings")),
                booleanHints = true,
                actions = listOf("open-settings" to "Open settings", LinuxNotifications.ACTION_DEFAULT to "CMPTemplate"),
            )

        assertEquals(
            listOf(
                "notify-send",
                "--app-name=${DesktopAppData.APP_NAME}",
                "--hint=string:desktop-entry:${DesktopAppData.APP_ID}",
                "--hint=string:sound-name:message-new-instant",
                "--action=open-settings=Open settings",
                "--action=default=CMPTemplate",
                "--",
                "Title",
                "Message",
            ),
            command,
        )
    }

    @Test
    fun silentNotificationSuppressesSoundOnlyWhereHintsAreUnderstood() {
        val silent = Notification("Title", "Message", withSound = false)

        assertTrue("--hint=boolean:suppress-sound:true" in LinuxNotifications.buildCommand(silent, booleanHints = true, emptyList()))
        assertFalse(
            LinuxNotifications.buildCommand(silent, booleanHints = false, emptyList()).any { it.startsWith("--hint=boolean") },
        )
    }

    @Test
    fun titleStartingWithADashIsNotReadAsAnOption() {
        val command = LinuxNotifications.buildCommand(Notification("-title", "Message"), booleanHints = true, emptyList())

        assertEquals(listOf("--", "-title", "Message"), command.takeLast(3))
    }

    @Test
    fun versionCheck() {
        assertTrue(LinuxNotifications.isAtLeast("notify-send 0.8.3", listOf(0, 7, 11)))
        assertTrue(LinuxNotifications.isAtLeast("notify-send 0.7.11", listOf(0, 7, 11)))
        assertFalse(LinuxNotifications.isAtLeast("notify-send 0.7.9", listOf(0, 7, 11)))
        assertFalse(LinuxNotifications.isAtLeast("", listOf(0, 7, 11)))
    }
}
