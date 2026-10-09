package dev.muazkadan.myapplication.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Settings : Screen
}
