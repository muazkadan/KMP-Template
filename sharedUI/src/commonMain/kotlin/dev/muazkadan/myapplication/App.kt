package dev.muazkadan.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.presentation.navigation.Navigation
import dev.muazkadan.myapplication.presentation.navigation.Screen
import dev.muazkadan.myapplication.presentation.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App(
    // Saved with Screen's own serializer, so a new screen is restored after process death without
    // being registered anywhere. A host that composes App anew, as the desktop app does on a
    // change of language, passes one it keeps outside, so the user stays on the same screen.
    backStack: NavBackStack<Screen> =
        rememberSerializable(serializer = NavBackStackSerializer(Screen.serializer())) {
            NavBackStack(Screen.Splash)
        },
) {
    val preferencesManager = koinInject<PreferencesManager>()
    val themeMode by preferencesManager.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)

    AppTheme(themeMode = themeMode) {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = remember { SnackbarHostState() })
                },
            ) {
                Navigation(backStack = backStack, modifier = Modifier.padding(it))
            }
        }
    }
}
