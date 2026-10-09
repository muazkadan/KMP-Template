package dev.muazkadan.myapplication.presentation.screen.splash

import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.testing.InMemoryPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun becomesReadyOnceSettingsAreLoaded() =
        runTest {
            val viewModel = SplashViewModel(PreferencesManager(InMemoryPreferencesDataStore()))
            assertFalse(viewModel.isReady.value)

            advanceUntilIdle()

            assertTrue(viewModel.isReady.value)
        }
}
