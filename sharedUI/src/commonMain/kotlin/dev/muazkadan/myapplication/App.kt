package dev.muazkadan.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.muazkadan.myapplication.presentation.navigation.Navigation
import dev.muazkadan.myapplication.presentation.navigation.Navigator
import dev.muazkadan.myapplication.presentation.navigation.Screen
import dev.muazkadan.myapplication.presentation.navigation.rememberNavigationState

@Composable
fun App() {
    val navigationState =
        rememberNavigationState(
            initialRoute = Screen.Splash,
            startRoute = Screen.Home,
            topLevelRoutes = setOf(Screen.Home, Screen.Settings),
        )

    val navigator = remember { Navigator(navigationState) }
    MaterialTheme {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = remember { SnackbarHostState() })
                },
            ) {
                Navigation(
                    modifier = Modifier.padding(it),
                    navigationState = navigationState,
                    navigator = navigator,
                )
            }
        }
    }
}
