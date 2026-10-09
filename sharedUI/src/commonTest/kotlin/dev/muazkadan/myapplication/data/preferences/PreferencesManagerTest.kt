package dev.muazkadan.myapplication.data.preferences

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesManagerTest {
    @Test
    fun themeModeDefaultsToSystem() =
        runTest {
            val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())

            assertEquals(ThemeMode.SYSTEM, preferencesManager.themeMode.first())
        }

    @Test
    fun themeModeReadsBackWhatWasSet() =
        runTest {
            val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())

            preferencesManager.setThemeMode(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, preferencesManager.themeMode.first())
        }

    @Test
    fun unknownStoredThemeModeFallsBackToSystem() =
        runTest {
            val dataStore =
                InMemoryPreferencesDataStore(preferencesOf(stringPreferencesKey("theme_mode") to "SEPIA"))
            val preferencesManager = PreferencesManager(dataStore)

            assertEquals(ThemeMode.SYSTEM, preferencesManager.themeMode.first())
        }

    @Test
    fun startupSettingsDefaultToOffAndReadBack() =
        runTest {
            val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())
            assertEquals(false, preferencesManager.launchAtStartup.first())
            assertEquals(false, preferencesManager.startMinimized.first())

            preferencesManager.setLaunchAtStartup(true)
            preferencesManager.setStartMinimized(true)

            assertEquals(true, preferencesManager.launchAtStartup.first())
            assertEquals(true, preferencesManager.startMinimized.first())
        }

    @Test
    fun appLanguageFollowsTheSystemUntilChosenAndAgainOnceCleared() =
        runTest {
            val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())
            assertEquals(null, preferencesManager.appLanguage.first())

            preferencesManager.setAppLanguage("ar")
            assertEquals("ar", preferencesManager.appLanguage.first())

            preferencesManager.setAppLanguage(null)
            assertEquals(null, preferencesManager.appLanguage.first())
        }
}
