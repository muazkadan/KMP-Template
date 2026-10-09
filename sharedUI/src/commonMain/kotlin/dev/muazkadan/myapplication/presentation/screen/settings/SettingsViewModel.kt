package dev.muazkadan.myapplication.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.data.startup.LaunchAtStartup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    // Null where the platform has no launch at login
    private val launchAtStartup: LaunchAtStartup?,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> =
        preferencesManager.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    val isLaunchAtStartupSupported: Boolean = launchAtStartup?.isSupported == true

    val launchAtStartupEnabled: StateFlow<Boolean> =
        preferencesManager.launchAtStartup.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val startMinimized: StateFlow<Boolean> =
        preferencesManager.startMinimized.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setLaunchAtStartup(enabled: Boolean) {
        val launchAtStartup = launchAtStartup ?: return
        viewModelScope.launch {
            // The switch follows the system: if the system couldn't be changed, it springs back
            if (launchAtStartup.setEnabled(enabled)) {
                preferencesManager.setLaunchAtStartup(enabled)
            }
        }
    }

    fun setStartMinimized(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setStartMinimized(enabled) }
    }
}
