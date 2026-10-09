package dev.muazkadan.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.muazkadan.myapplication.presentation.screen.home.HomeScreen
import dev.muazkadan.myapplication.presentation.screen.settings.SettingsScreen
import dev.muazkadan.myapplication.presentation.screen.splash.SplashScreen

@Composable
fun Navigation(
    backStack: NavBackStack<Screen>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // Each screen's ViewModel lives as long as the screen is on the back stack
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<Screen.Splash> {
                    SplashScreen(
                        onReady = {
                            // Home replaces the splash, so Back from Home leaves the app
                            backStack.add(Screen.Home)
                            backStack.remove(Screen.Splash)
                        },
                    )
                }

                entry<Screen.Home> {
                    HomeScreen(onOpenSettings = { backStack.add(Screen.Settings) })
                }

                entry<Screen.Settings> {
                    SettingsScreen()
                }
            },
    )
}
