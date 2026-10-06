package dev.muazkadan.myapplication.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface SplashEvent {
    data object NavigateToHome : SplashEvent
}

class SplashViewModel(
    preferencesManager: PreferencesManager,
) : ViewModel() {
    // A Channel, not a SharedFlow: the event waits for the screen to collect it rather than being
    // dropped when it's sent before the screen starts
    private val _events = Channel<SplashEvent>()
    val events: Flow<SplashEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Startup work the first screen depends on goes here. The stored settings are read
            // first so Home opens in the chosen theme.
            preferencesManager.themeMode.first()
            _events.send(SplashEvent.NavigateToHome)
        }
    }
}
