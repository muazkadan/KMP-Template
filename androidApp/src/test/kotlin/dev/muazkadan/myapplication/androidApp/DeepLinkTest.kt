package dev.muazkadan.myapplication.androidApp

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    // Each test gets a new MyApplication, which starts Koin again
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun appStartedWithADeepLinkOpensItsScreenAfterTheSplash() {
        ActivityScenario.launch<MainActivity>(deepLink("cmptemplate://nav/settings")).use {
            waitForText("Theme")
            composeRule.onNodeWithText("Language").assertIsDisplayed()
        }
    }

    @Test
    fun deepLinkWhileRunningOpensItsScreen() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup()
        waitForText("Home")

        // What the system calls on the running singleInstance activity
        activity.newIntent(deepLink("cmptemplate://nav/settings"))

        waitForText("Theme")
        activity.close()
    }

    private fun deepLink(uri: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setClass(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
