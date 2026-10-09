package dev.muazkadan.myapplication.data.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi

/**
 * The app's language through Android 13's per-app language, which the system keeps and also offers
 * in Settings → Apps → Language. Setting it recreates the activity in the new language.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AndroidAppLanguage(
    context: Context,
) : AppLanguageSetting {
    private val localeManager = context.getSystemService(LocaleManager::class.java)

    // Just the language: the picker offers no regions, but the system's own one may set a tag with one
    override fun get(): String? =
        localeManager.applicationLocales
            .takeUnless { it.isEmpty }
            ?.get(0)
            ?.language

    override fun set(languageTag: String?) {
        localeManager.applicationLocales =
            if (languageTag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(languageTag)
    }
}
