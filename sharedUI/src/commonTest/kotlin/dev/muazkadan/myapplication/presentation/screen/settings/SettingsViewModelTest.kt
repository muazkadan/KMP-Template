package dev.muazkadan.myapplication.presentation.screen.settings

import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.data.startup.LaunchAtStartup
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())
    private lateinit var viewModel: SettingsViewModel

    private val launchAtStartup = FakeLaunchAtStartup()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SettingsViewModel(preferencesManager, launchAtStartup)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun themeModeReflectsTheStoredValue() =
        runTest {
            preferencesManager.setThemeMode(ThemeMode.LIGHT)
            // WhileSubscribed only reads the store while someone collects
            backgroundScope.launch { viewModel.themeMode.collect {} }
            advanceUntilIdle()

            assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)
        }

    @Test
    fun setThemeModeStoresTheChoice() =
        runTest {
            viewModel.setThemeMode(ThemeMode.DARK)
            advanceUntilIdle()

            assertEquals(ThemeMode.DARK, preferencesManager.themeMode.first())
        }

    @Test
    fun launchAtStartupIsStoredOnceTheSystemAccepts() =
        runTest {
            viewModel.setLaunchAtStartup(true)
            advanceUntilIdle()

            assertEquals(listOf(true), launchAtStartup.requests)
            assertTrue(preferencesManager.launchAtStartup.first())
        }

    @Test
    fun launchAtStartupIsNotStoredWhenTheSystemRefuses() =
        runTest {
            launchAtStartup.accepts = false

            viewModel.setLaunchAtStartup(true)
            advanceUntilIdle()

            assertFalse(preferencesManager.launchAtStartup.first())
        }

    @Test
    fun withoutLaunchAtStartupTheSettingIsHidden() {
        assertTrue(viewModel.isLaunchAtStartupSupported)
        assertFalse(SettingsViewModel(preferencesManager, launchAtStartup = null).isLaunchAtStartupSupported)
    }

    private class FakeLaunchAtStartup : LaunchAtStartup {
        override val isSupported = true
        var accepts = true
        val requests = mutableListOf<Boolean>()

        override suspend fun setEnabled(enabled: Boolean): Boolean {
            requests += enabled
            return accepts
        }
    }
}
