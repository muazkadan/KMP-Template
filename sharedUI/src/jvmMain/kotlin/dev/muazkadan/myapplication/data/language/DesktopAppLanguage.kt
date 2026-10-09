package dev.muazkadan.myapplication.data.language

import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * The app's language on desktop, which has no per-app language of its own. Compose resources look
 * strings up in the JVM's default locale, so the language is applied by replacing it, and stored in
 * the app's preferences for the next launch.
 */
class DesktopAppLanguage(
    private val preferencesManager: PreferencesManager,
) : AppLanguageSetting {
    // The system's, kept from before the app first replaces it, for when the app follows it again
    private val systemLocale: Locale = Locale.getDefault()

    @Volatile
    private var languageTag: String? = null

    // One write at a time, so choices made in quick succession are stored in the order they were made
    private val writes = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    /** Applies the stored language. Call it at startup, before anything is put on screen. */
    suspend fun restore() {
        apply(preferencesManager.appLanguage.first())
    }

    override fun get(): String? = languageTag

    // Applied at once, so strings looked up from here on are in the new language; the window
    // composes again in it when the stored value reaches it
    override fun set(languageTag: String?) {
        apply(languageTag)
        writes.launch { preferencesManager.setAppLanguage(languageTag) }
    }

    private fun apply(languageTag: String?) {
        this.languageTag = languageTag
        Locale.setDefault(languageTag?.let(Locale::forLanguageTag) ?: systemLocale)
    }
}
