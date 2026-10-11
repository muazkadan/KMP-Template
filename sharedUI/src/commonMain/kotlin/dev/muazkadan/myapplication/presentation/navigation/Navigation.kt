package dev.muazkadan.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.muazkadan.myapplication.presentation.screen.home.HomeScreen
import dev.muazkadan.myapplication.presentation.screen.settings.SettingsScreen
import dev.muazkadan.myapplication.presentation.screen.splash.SplashScreen
import kotlinx.coroutines.flow.filterNotNull
import org.koin.compose.koinInject

@Composable
fun Navigation(
    backStack: NavBackStack<Screen>,
    modifier: Modifier = Modifier,
    externalNavigator: ExternalNavigator = koinInject(),
) {
    // Deep links while the app is running. While it starts, the splash opens them once it's ready
    LaunchedEffect(backStack, externalNavigator) {
        externalNavigator.pendingDestination.filterNotNull().collect { destination ->
            if (Screen.Splash in backStack) return@collect
            externalNavigator.consume()
            // Over Home, so Back returns there rather than through the screens open before
            backStack.removeAll { it != Screen.Home }
            if (destination != Screen.Home) backStack.add(destination)
        }
    }

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
                            // Home replaces the splash, so Back from Home leaves the app. A deep link
                            // the app was started with opens over it
                            backStack.add(Screen.Home)
                            externalNavigator.consume()?.takeIf { it != Screen.Home }?.let(backStack::add)
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
