package dev.muazkadan.myapplication.data.language

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// The app's translations are listed in three places that nothing else keeps in step
class AppLanguagesTest {
    // Tests run from the sharedUI module's directory
    private val resourcesDir = File("src/commonMain/composeResources")
    private val localesConfig = File("../androidApp/src/main/res/xml/locales_config.xml")

    @Test
    fun everyTranslationIsListed() {
        val translated =
            resourcesDir
                .listFiles { file -> file.isDirectory && file.name.startsWith("values") }!!
                // values holds the default, English
                .map {
                    it.name
                        .removePrefix("values")
                        .removePrefix("-")
                        .ifEmpty { "en" }
                }.toSet()

        assertEquals(translated, appLanguages.map { it.tag }.toSet())
    }

    @Test
    fun androidOffersTheSameLanguages() {
        val offered =
            Regex("""<locale android:name="([^"]+)"""")
                .findAll(localesConfig.readText())
                .map { it.groupValues[1] }
                .toSet()

        assertEquals(appLanguages.map { it.tag }.toSet(), offered)
    }
}
