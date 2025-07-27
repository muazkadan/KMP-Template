package dev.muazkadan.myapplication

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.muazkadan.myapplication.di.initKoin

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Genshin Chart Tier List",
        ) {
            App()
        }
    }
}
