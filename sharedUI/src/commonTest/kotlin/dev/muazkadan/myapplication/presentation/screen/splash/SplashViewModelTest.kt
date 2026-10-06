package dev.muazkadan.myapplication.presentation.screen.splash

import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import dev.muazkadan.myapplication.testing.cancelScopeForTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private lateinit var viewModel: SplashViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SplashViewModel(PreferencesManager(InMemoryPreferencesDataStore()))
    }

    @AfterTest
    fun tearDown() {
        viewModel.cancelScopeForTest()
        Dispatchers.resetMain()
    }

    @Test
    fun navigatesHomeOnceSettingsAreLoaded() =
        runTest {
            // The event is buffered until collected, as it is while the screen isn't started yet
            assertEquals(SplashEvent.NavigateToHome, viewModel.events.first())
        }
}
