package dev.muazkadan.myapplication.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenTest {
    // What happens to the back stack when the app is put away and its process ends
    @Test
    fun backStackIsRestoredAfterBeingSaved() {
        val serializer = NavBackStackSerializer(Screen.serializer())
        val backStack = NavBackStack<Screen>(Screen.Home, Screen.Settings)

        val restored = decodeFromSavedState(serializer, encodeToSavedState(serializer, backStack))

        assertEquals(backStack.toList(), restored.toList())
    }
}
