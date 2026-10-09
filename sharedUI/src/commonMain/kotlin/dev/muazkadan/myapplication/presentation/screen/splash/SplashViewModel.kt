package dev.muazkadan.myapplication.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    preferencesManager: PreferencesManager,
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)

    /** True once the app has what its first screen needs. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            // Startup work the first screen depends on goes here. The stored settings are read
            // first so Home opens in the chosen theme.
            preferencesManager.themeMode.first()
            _isReady.value = true
        }
    }
}
