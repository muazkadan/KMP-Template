package dev.muazkadan.myapplication.presentation.screen.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cmptemplate.sharedui.generated.resources.Res
import cmptemplate.sharedui.generated.resources.app_name
import dev.muazkadan.myapplication.utils.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SplashEvent.NavigateToHome -> onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
