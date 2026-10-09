package dev.muazkadan.myapplication.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.muazkadan.myapplication.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Typed access to the app's stored settings. Add a key and a getter/setter pair per setting.
 */
class PreferencesManager(
    private val dataStore: DataStore<Preferences>,
) {
    // Stored by name, so reordering ThemeMode doesn't change anyone's choice. An unknown name - from
    // a removed entry - falls back to the default instead of failing the read.
    val themeMode: Flow<ThemeMode> =
        dataStore.data.map { preferences ->
            ThemeMode.entries.firstOrNull { it.name == preferences[THEME_MODE] } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    // Whether the user asked for the app to start at login. The system's own setting can differ,
    // since the user can remove the entry there: the desktop app checks it at every launch.
    val launchAtStartup: Flow<Boolean> = dataStore.data.map { it[LAUNCH_AT_STARTUP] ?: false }

    suspend fun setLaunchAtStartup(enabled: Boolean) {
        dataStore.edit { it[LAUNCH_AT_STARTUP] = enabled }
    }

    // Applies to a start at login only; opened by hand, the app always shows its window
    val startMinimized: Flow<Boolean> = dataStore.data.map { it[START_MINIMIZED] ?: false }

    suspend fun setStartMinimized(enabled: Boolean) {
        dataStore.edit { it[START_MINIMIZED] = enabled }
    }

    // The language chosen in the app, on desktop: Android keeps it per app itself. Null follows the
    // system's language.
    val appLanguage: Flow<String?> = dataStore.data.map { it[APP_LANGUAGE] }

    suspend fun setAppLanguage(languageTag: String?) {
        dataStore.edit {
            if (languageTag == null) it.remove(APP_LANGUAGE) else it[APP_LANGUAGE] = languageTag
        }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAUNCH_AT_STARTUP = booleanPreferencesKey("launch_at_startup")
        val START_MINIMIZED = booleanPreferencesKey("start_minimized")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }
}
