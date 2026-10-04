package dev.muazkadan.myapplication.presentation.screen.settings

import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import dev.muazkadan.myapplication.testing.cancelScopeForTest
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val preferencesManager = PreferencesManager(InMemoryPreferencesDataStore())
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SettingsViewModel(preferencesManager)
    }

    @AfterTest
    fun tearDown() {
        viewModel.cancelScopeForTest()
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
}
