package dev.muazkadan.myapplication.presentation.navigation // package com.example.project

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(
    val state: NavigationState,
) {
    fun navigate(route: Screen) {
        if (route in state.topLevelRoutes) {
            // Top-level destination: switch stacks.
            state.topLevelRoute = route
        } else {
            currentStack().add(route)
        }
    }

    fun goBack() {
        val currentStack = currentStack()
        val currentRoute = currentStack.lastOrNull() ?: return

        // If we're at the root of the current top-level stack,
        // switch back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute in state.topLevelRoutes && state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
        } else {
            currentStack.removeLastOrNull()
        }
    }

    fun replaceAll(route: Screen) {
        if (route in state.topLevelRoutes) {
            resetStack(state.backStacks.getValue(route), route)
            state.topLevelRoute = route
            return
        }

        resetStack(currentStack(), route)
    }

    private fun currentStack(): androidx.navigation3.runtime.NavBackStack<androidx.navigation3.runtime.NavKey> =
        state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")

    private fun resetStack(
        stack: androidx.navigation3.runtime.NavBackStack<androidx.navigation3.runtime.NavKey>,
        route: Screen,
    ) {
        while (stack.isNotEmpty()) {
            stack.removeLastOrNull()
        }
        stack.add(route)
    }
}
