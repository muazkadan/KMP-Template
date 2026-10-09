package dev.muazkadan.myapplication.data.language

import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DesktopAppLanguageTest {
    private val systemLocale = Locale.getDefault()
    private val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())
    private val appLanguage = DesktopAppLanguage(preferencesManager)

    @AfterTest
    fun tearDown() {
        Locale.setDefault(systemLocale)
    }

    @Test
    fun setAppliesTheLanguageAtOnceAndStoresIt() =
        runBlocking {
            appLanguage.set("ar")

            assertEquals("ar", Locale.getDefault().language)
            assertEquals("ar", appLanguage.get())
            // Stored on another dispatcher
            assertEquals("ar", withTimeoutOrNull(5_000) { preferencesManager.appLanguage.firstOrNull { it == "ar" } })
        }

    @Test
    fun followingTheSystemAgainRestoresItsLocale() {
        appLanguage.set("ar")
        appLanguage.set(null)

        assertEquals(systemLocale, Locale.getDefault())
        assertNull(appLanguage.get())
    }

    @Test
    fun restoreAppliesTheStoredLanguage() =
        runBlocking {
            preferencesManager.setAppLanguage("ar")

            appLanguage.restore()

            assertEquals("ar", Locale.getDefault().language)
            assertEquals("ar", preferencesManager.appLanguage.first())
        }
}
