import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.muazkadan.myapplication.MainView

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainView()
    }
}