package dev.muazkadan.myapplication.data.startup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoStartManagerTest {
    @Test
    fun buildMacPlist_opensBundleThroughLaunchServicesWithAutostartArgument() {
        val plist = AutoStartManager.buildMacPlist("/Applications/CMPTemplate.app/Contents/MacOS/CMPTemplate")

        assertTrue(
            """
            |    <array>
            |        <string>/usr/bin/open</string>
            |        <string>-g</string>
            |        <string>-a</string>
            |        <string>/Applications/CMPTemplate.app</string>
            |        <string>--args</string>
            |        <string>--autostart</string>
            |    </array>
            """.trimMargin() in plist,
        )
        assertTrue("<key>RunAtLoad</key>\n    <true/>" in plist)
        assertTrue("<key>AssociatedBundleIdentifiers</key>\n    <string>${AutoStartManager.APP_ID}</string>" in plist)
    }

    @Test
    fun macProgramArguments_startsLauncherOutsideBundleDirectly() {
        assertEquals(
            listOf("/opt/bin/CMPTemplate", "--autostart"),
            AutoStartManager.macProgramArguments("/opt/bin/CMPTemplate"),
        )
    }

    @Test
    fun buildMacPlist_escapesXmlInPath() {
        val plist = AutoStartManager.buildMacPlist("/Users/a&b/<Apps>/CMPTemplate.app/Contents/MacOS/CMPTemplate")

        assertTrue("<string>/Users/a&amp;b/&lt;Apps&gt;/CMPTemplate.app</string>" in plist)
    }

    @Test
    fun buildLinuxDesktopEntry_quotesExecPerDesktopEntrySpec() {
        val entry = AutoStartManager.buildLinuxDesktopEntry("/opt/cmptemplate/bin/CMP Template")

        assertTrue(entry.startsWith("[Desktop Entry]\n"))
        assertTrue("Exec=\"/opt/cmptemplate/bin/CMP Template\" --autostart\n" in entry)
    }

    @Test
    fun buildLinuxDesktopEntry_escapesReservedCharacters() {
        val entry = AutoStartManager.buildLinuxDesktopEntry("/home/u/100%/\$dir/a\"b")

        assertTrue("Exec=\"/home/u/100%%/\\\\\$dir/a\\\\\"b\" --autostart\n" in entry)
    }

    @Test
    fun windowsRunCommand_escapesQuotesForRegExe() {
        assertEquals(
            """\"C:\Program Files\CMPTemplate\CMPTemplate.exe\" --autostart""",
            AutoStartManager.windowsRunCommand("""C:\Program Files\CMPTemplate\CMPTemplate.exe"""),
        )
    }

    @Test
    fun isAppLauncher_rejectsJavaAndTranslocatedApps() {
        assertTrue(DesktopAppPathResolver.isAppLauncher("/Applications/CMPTemplate.app/Contents/MacOS/CMPTemplate"))
        assertTrue(DesktopAppPathResolver.isAppLauncher("/opt/cmptemplate/bin/CMPTemplate"))
        assertFalse(DesktopAppPathResolver.isAppLauncher("/Library/Java/JavaVirtualMachines/jdk/Contents/Home/bin/java"))
        assertFalse(DesktopAppPathResolver.isAppLauncher("""C:\Program Files\Java\bin\javaw.exe"""))
        assertFalse(
            DesktopAppPathResolver.isAppLauncher(
                "/private/var/folders/x/AppTranslocation/ABC/d/CMPTemplate.app/Contents/MacOS/CMPTemplate",
            ),
        )
    }

    @Test
    fun dev_runIsNotSupported() {
        // Tests run on a plain java, as ./gradlew run does
        assertFalse(AutoStartManager.isSupported)
    }
}
