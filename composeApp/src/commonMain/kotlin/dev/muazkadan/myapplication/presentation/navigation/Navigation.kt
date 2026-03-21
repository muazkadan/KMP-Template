package dev.muazkadan.myapplication.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.delay

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navigationState: NavigationState,
    navigator: Navigator,
) {
    val entryProvider =
        entryProvider {
            entry<Screen.Splash> {
                LaunchedEffect(Unit) {
                    delay(2000)
                    navigator.navigate(Screen.Home)
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Splash Screen")
                }
            }

            entry<Screen.Home> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Home Screen")
                }
            }

            entry<Screen.Settings> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Settings Screen")
                }
            }
        }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
    )
}
