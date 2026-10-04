package dev.muazkadan.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.muazkadan.myapplication.presentation.screen.home.HomeScreen
import dev.muazkadan.myapplication.presentation.screen.settings.SettingsScreen
import dev.muazkadan.myapplication.presentation.screen.splash.SplashScreen

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navigationState: NavigationState,
    navigator: Navigator,
) {
    val entryProvider: (Screen) -> NavEntry<Screen> =
        entryProvider {
            entry<Screen.Splash> {
                SplashScreen(onNavigateToHome = { navigator.replaceAll(Screen.Home) })
            }

            entry<Screen.Home> {
                HomeScreen(onOpenSettings = { navigator.navigate(Screen.Settings) })
            }

            entry<Screen.Settings> {
                SettingsScreen()
            }
        }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
