package dev.muazkadan.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.presentation.navigation.Navigation
import dev.muazkadan.myapplication.presentation.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val preferencesManager = koinInject<PreferencesManager>()
    val themeMode by preferencesManager.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)

    AppTheme(themeMode = themeMode) {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = remember { SnackbarHostState() })
                },
            ) {
                Navigation(modifier = Modifier.padding(it))
            }
        }
    }
}
