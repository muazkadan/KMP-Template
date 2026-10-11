package dev.muazkadan.myapplication.presentation.navigation

/**
 * The URIs that open a screen: `cmptemplate://nav/<screen>`. Each platform passes the ones it's
 * given to [ExternalNavigator]: Android through MainActivity's intent filter, iOS through
 * `.onOpenURL`, desktop through the macOS app's URL type and Windows' registered protocol.
 *
 * Rename [SCHEME] with the app, and with it the scheme in androidApp's AndroidManifest.xml, iosApp's
 * Info.plist and desktopApp's macOS infoPlist.
 */
object DeepLinks {
    const val SCHEME = "cmptemplate"

    const val HOME = "$SCHEME://nav/home"
    const val SETTINGS = "$SCHEME://nav/settings"

    /** The screen [uri] opens, or null when it isn't one of the app's deep links. */
    fun parse(uri: String?): Screen? =
        when (uri?.trim()?.lowercase()?.trimEnd('/')) {
            HOME -> Screen.Home
            SETTINGS -> Screen.Settings
            else -> null
        }
}
