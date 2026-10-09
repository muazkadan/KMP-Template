package dev.muazkadan.myapplication.data.language

/**
 * A language the app is translated into, named in that language: "العربية" reads as Arabic to an
 * Arabic speaker whatever language the app is in now.
 */
data class AppLanguage(
    val tag: String,
    val nativeName: String,
)

/**
 * The app's translations. Adding one takes three steps, which AppLanguagesTest checks agree:
 * strings in `composeResources/values-<tag>`, an entry here, and a `<locale>` in androidApp's
 * `res/xml/locales_config.xml`. English is the default, in `values`.
 */
val appLanguages: List<AppLanguage> =
    listOf(
        AppLanguage("en", "English"),
        AppLanguage("ar", "العربية"),
    )

/**
 * Choosing the app's language apart from the system's. Bound on Android 13+, which keeps the choice
 * per app, and on desktop. Elsewhere the app follows the system's language.
 */
interface AppLanguageSetting {
    /** The chosen language's tag, or null when the app follows the system's language. */
    fun get(): String?

    fun set(languageTag: String?)
}
