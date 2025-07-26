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
import androidx.navigation.compose.rememberNavController
import dev.muazkadan.myapplication.presentation.navigation.Navigation

@Composable
fun App() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = remember { SnackbarHostState() })
                },
            ) {
                Navigation(modifier = Modifier.padding(it), navController = navController)
            }
        }
    }
}
