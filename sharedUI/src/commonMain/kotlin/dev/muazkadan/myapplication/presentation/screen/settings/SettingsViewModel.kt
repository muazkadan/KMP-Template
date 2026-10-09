package dev.muazkadan.myapplication.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.muazkadan.myapplication.data.language.AppLanguageSetting
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.data.startup.LaunchAtStartup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    // Null where the platform has no launch at login
    private val launchAtStartup: LaunchAtStartup?,
    // Null where the app can only follow the system's language
    private val appLanguageSetting: AppLanguageSetting?,
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

    val isAppLanguageSupported: Boolean = appLanguageSetting != null

    // Read from the platform rather than stored here: on Android the system keeps it, and the user
    // can change it in the system's settings too
    private val _appLanguage = MutableStateFlow(appLanguageSetting?.get())
    val appLanguage: StateFlow<String?> = _appLanguage.asStateFlow()

    fun setAppLanguage(languageTag: String?) {
        appLanguageSetting?.set(languageTag)
        _appLanguage.value = languageTag
    }

    /** Re-reads the app language, which the system's settings can change while the app is away. */
    fun refreshAppLanguage() {
        _appLanguage.value = appLanguageSetting?.get()
    }

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
