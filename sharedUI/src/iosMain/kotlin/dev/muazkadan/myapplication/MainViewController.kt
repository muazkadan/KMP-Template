package dev.muazkadan.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import dev.muazkadan.myapplication.presentation.navigation.ExternalNavigator
import org.koin.mp.KoinPlatform

// Named like a type because Swift calls it as one: MainViewControllerKt.MainViewController()
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App() }

/** Called from Swift with a URL the app was opened with (`.onOpenURL`). */
fun handleDeepLink(uri: String) {
    KoinPlatform.getKoin().get<ExternalNavigator>().onDeepLink(uri)
}
