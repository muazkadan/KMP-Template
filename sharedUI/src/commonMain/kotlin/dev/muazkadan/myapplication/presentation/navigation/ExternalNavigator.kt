package dev.muazkadan.myapplication.presentation.navigation

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * Hands deep links from the platform (an Android intent, an iOS URL, a desktop launch) to
 * [Navigation].
 *
 * The request is state rather than an event, so it waits until the app can act on it: the splash
 * screen opens it on a cold start, and [Navigation] once the app is running. An event would be
 * dropped whenever nothing is collecting yet.
 */
class ExternalNavigator {
    private val log = Logger.withTag("ExternalNavigator")
    private val _pendingDestination = MutableStateFlow<Screen?>(null)
    val pendingDestination: StateFlow<Screen?> = _pendingDestination.asStateFlow()

    /**
     * Queues the screen [uri] opens.
     *
     * @return false when [uri] isn't one of the app's deep links, which is then ignored.
     */
    fun onDeepLink(uri: String?): Boolean {
        val destination = DeepLinks.parse(uri)
        if (destination == null) {
            log.d { "Ignoring $uri, which isn't one of the app's deep links" }
            return false
        }
        log.i { "Deep link $uri opens $destination" }
        _pendingDestination.value = destination
        return true
    }

    /** Returns the queued screen, if any, and clears it. */
    fun consume(): Screen? = _pendingDestination.getAndUpdate { null }
}
