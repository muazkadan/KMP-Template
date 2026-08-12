package dev.muazkadan.myapplication.desktopApp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.muazkadan.myapplication.App
import dev.muazkadan.myapplication.di.initKoin
import java.awt.Dimension

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP App",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
        ) {
            window.minimumSize = Dimension(350, 600)
            App()
        }
    }
}
