package dev.muazkadan.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
fun rememberNavigationState(
    initialRoute: Screen,
    startRoute: Screen,
    topLevelRoutes: Set<Screen>,
): NavigationState {
    require(startRoute in topLevelRoutes) {
        "startRoute must be included in topLevelRoutes"
    }

    val topLevelRoute =
        rememberSerializable(
            initialRoute,
            startRoute,
            topLevelRoutes,
            serializer = MutableStateSerializer(Screen.serializer()),
        ) {
            mutableStateOf(initialRoute)
        }

    val stackRoutes =
        buildSet {
            add(initialRoute)
            add(startRoute)
            addAll(topLevelRoutes)
        }

    val backStacks: Map<Screen, NavBackStack<NavKey>> =
        stackRoutes.associateWith { key ->
            rememberNavBackStack(savedStateConfig, key)
        }

    return remember(initialRoute, startRoute, topLevelRoutes) {
        NavigationState(
            initialRoute = initialRoute,
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            topLevelRoutes = topLevelRoutes,
            backStacks = backStacks,
        )
    }
}

/**
 * State holder for navigation state.
 *
 * @param startRoute - the start route. The user will exit the app through this route.
 * @param topLevelRoute - the current top level route
 * @param backStacks - the back stacks for each top level route
 */
class NavigationState(
    val initialRoute: Screen,
    val startRoute: Screen,
    topLevelRoute: MutableState<Screen>,
    val topLevelRoutes: Set<Screen>,
    val backStacks: Map<Screen, NavBackStack<NavKey>>,
) {
    var topLevelRoute: Screen by topLevelRoute

    val stacksInUse: List<Screen>
        get() =
            when {
                topLevelRoute == initialRoute -> listOf(initialRoute)
                topLevelRoute == startRoute -> listOf(startRoute)
                topLevelRoute in topLevelRoutes -> listOf(startRoute, topLevelRoute)
                else -> listOf(topLevelRoute)
            }
}

/**
 * Convert NavigationState into NavEntries.
 */
@Composable
fun NavigationState.toEntries(entryProvider: (Screen) -> NavEntry<Screen>): SnapshotStateList<NavEntry<NavKey>> {
    val navKeyEntryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        val screen =
            key as? Screen
                ?: error("Unsupported NavKey type: ${key::class}")

        @Suppress("UNCHECKED_CAST")
        entryProvider(screen) as NavEntry<NavKey>
    }

    val decoratedEntries =
        backStacks.mapValues { (_, stack) ->
            val decorators =
                listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    // Then add the view model store decorator
                    rememberViewModelStoreNavEntryDecorator(),
                )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = navKeyEntryProvider,
            )
        }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}
