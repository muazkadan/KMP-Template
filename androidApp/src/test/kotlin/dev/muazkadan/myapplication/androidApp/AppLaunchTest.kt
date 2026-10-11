package dev.muazkadan.myapplication.androidApp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

// The real app on the JVM: MyApplication starts Koin, MainActivity shows App()
@RunWith(RobolectricTestRunner::class)
class AppLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // Each test gets a new MyApplication, which starts Koin again
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun splashMovesOnToHomeAndSettingsOpens() {
        // The splash screen waits for the stored settings, read on another dispatcher
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Settings").performClick()

        composeRule.onNodeWithText("Theme").assertIsDisplayed()
        // Android 13+ keeps a per-app language
        composeRule.onNodeWithText("Language").assertIsDisplayed()
        // Desktop only
        composeRule.onNodeWithText("Notifications").assertDoesNotExist()
        composeRule.onNodeWithText("System startup").assertDoesNotExist()
    }
}
