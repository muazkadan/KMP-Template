package dev.muazkadan.myapplication.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
