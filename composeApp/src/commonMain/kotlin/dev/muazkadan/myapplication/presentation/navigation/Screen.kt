package dev.muazkadan.myapplication.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Splash : Screen, NavKey

    @Serializable
    data object Home : Screen, NavKey

    @Serializable
    data object Settings : Screen, NavKey
}

val savedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Splash::class, Screen.Splash.serializer())
                    subclass(Screen.Home::class, Screen.Home.serializer())
                    subclass(Screen.Settings::class, Screen.Settings.serializer())
                }
            }
    }
