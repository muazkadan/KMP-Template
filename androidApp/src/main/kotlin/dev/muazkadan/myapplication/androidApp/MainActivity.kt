package dev.muazkadan.myapplication.androidApp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.muazkadan.myapplication.App
import dev.muazkadan.myapplication.presentation.navigation.ExternalNavigator
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val externalNavigator: ExternalNavigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // A recreated activity, or one reopened from recents, still holds the intent it started with:
        // opening its deep link again would leave the screen the user is on
        val launchedFromHistory = (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        if (savedInstanceState == null && !launchedFromHistory) externalNavigator.onDeepLink(intent.dataString)

        setContent {
            App()
        }
    }

    // A deep link while the app is running: the activity is singleInstance, so it comes here
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalNavigator.onDeepLink(intent.dataString)
    }
}
