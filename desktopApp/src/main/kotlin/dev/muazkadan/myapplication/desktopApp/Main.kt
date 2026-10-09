package dev.muazkadan.myapplication.desktopApp

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cmptemplate.sharedui.generated.resources.Res
import cmptemplate.sharedui.generated.resources.app_icon
import cmptemplate.sharedui.generated.resources.app_name
import cmptemplate.sharedui.generated.resources.desktop_hide_to_tray
import cmptemplate.sharedui.generated.resources.desktop_menu_close_window
import cmptemplate.sharedui.generated.resources.desktop_menu_file
import cmptemplate.sharedui.generated.resources.desktop_open_app
import cmptemplate.sharedui.generated.resources.desktop_quit
import dev.muazkadan.myapplication.App
import dev.muazkadan.myapplication.DesktopAppData
import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.di.initKoin
import dev.muazkadan.myapplication.presentation.theme.isDark
import dev.nucleusframework.composenativetray.tray.api.Tray
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.awt.Dimension
import java.awt.desktop.AppReopenedListener

private val isMac = System.getProperty("os.name", "").lowercase().contains("mac")

fun main() {
    val singleInstance = SingleInstanceController(DesktopAppData.dir)
    if (singleInstance.notifyExistingInstance()) return

    val koin = initKoin().koin
    val preferencesManager = koin.get<PreferencesManager>()
    // Read up front, so the Swing look and the first frame are already in the stored theme
    val initialThemeMode = runBlocking { preferencesManager.themeMode.first() }
    SwingTheme.install(initialThemeMode)

    val windowStateManager = WindowStateManager()

    application {
        val windowState = remember { windowStateManager.load() }
        var isVisible by remember { mutableStateOf(true) }
        var restoreRequests by remember { mutableIntStateOf(0) }

        fun restoreWindow() {
            isVisible = true
            windowState.isMinimized = false
            restoreRequests++
        }

        DisposableEffect(Unit) {
            singleInstance.startListener { message ->
                if (message == SingleInstanceController.RESTORE) restoreWindow()
            }

            // A click on the Dock icon brings a hidden window back on macOS
            val reopenedListener = AppReopenedListener { restoreWindow() }
            val desktop =
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.APP_EVENT_REOPENED) }
                } else {
                    null
                }
            desktop?.addAppEventListener(reopenedListener)

            onDispose {
                desktop?.removeAppEventListener(reopenedListener)
                singleInstance.stop()
            }
        }

        SaveWindowStateOnChange(windowStateManager, windowState)

        val appName = stringResource(Res.string.app_name)
        val appIcon = painterResource(Res.drawable.app_icon)
        val openLabel = stringResource(Res.string.desktop_open_app)
        val hideLabel = stringResource(Res.string.desktop_hide_to_tray)
        val quitLabel = stringResource(Res.string.desktop_quit)

        // A StatusNotifierItem on Linux, which opens on a single click on KDE and keeps the icon's
        // transparency, where Compose's own AWT tray icon does neither. The tray sets itself up
        // again whenever primaryAction changes, so it stays the same lambda
        val onTrayClick = remember { { restoreWindow() } }
        Tray(
            icon = appIcon,
            tooltip = appName,
            primaryAction = onTrayClick,
            menuContent = {
                Item(openLabel, onClick = { restoreWindow() })
                Item(hideLabel, onClick = { isVisible = false })
                Divider()
                Item(quitLabel, onClick = ::exitApplication)
            },
        )

        Window(
            // Closing hides to the tray; Quit ends the app
            onCloseRequest = { isVisible = false },
            state = windowState,
            visible = isVisible,
            title = appName,
            icon = appIcon,
        ) {
            window.minimumSize = Dimension(WindowStateManager.MIN_WIDTH, WindowStateManager.MIN_HEIGHT)

            val themeMode by preferencesManager.themeMode.collectAsState(initialThemeMode)
            SwingThemeEffect(darkTheme = themeMode.isDark())

            LaunchedEffect(restoreRequests) {
                if (restoreRequests > 0) {
                    window.toFront()
                    window.requestFocus()
                }
            }

            MenuBar {
                Menu(stringResource(Res.string.desktop_menu_file)) {
                    Item(
                        stringResource(Res.string.desktop_menu_close_window),
                        shortcut = KeyShortcut(Key.W, meta = isMac, ctrl = !isMac),
                        onClick = { isVisible = false },
                    )
                    Separator()
                    Item(
                        quitLabel,
                        shortcut = KeyShortcut(Key.Q, meta = isMac, ctrl = !isMac),
                        onClick = ::exitApplication,
                    )
                }
            }

            App()
        }
    }
}
