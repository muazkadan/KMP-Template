package dev.muazkadan.myapplication.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmptemplate.sharedui.generated.resources.Res
import cmptemplate.sharedui.generated.resources.language_system
import cmptemplate.sharedui.generated.resources.settings_language
import cmptemplate.sharedui.generated.resources.settings_launch_at_startup
import cmptemplate.sharedui.generated.resources.settings_start_minimized
import cmptemplate.sharedui.generated.resources.settings_start_minimized_summary
import cmptemplate.sharedui.generated.resources.settings_startup
import cmptemplate.sharedui.generated.resources.settings_theme
import cmptemplate.sharedui.generated.resources.settings_title
import cmptemplate.sharedui.generated.resources.theme_dark
import cmptemplate.sharedui.generated.resources.theme_light
import cmptemplate.sharedui.generated.resources.theme_system
import dev.muazkadan.myapplication.data.language.appLanguages
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.presentation.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val launchAtStartup by viewModel.launchAtStartupEnabled.collectAsStateWithLifecycle()
    val startMinimized by viewModel.startMinimized.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    // The system's settings can change the app's language while the app is away
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshAppLanguage()
        onPauseOrDispose {}
    }

    SettingsContent(
        themeMode = themeMode,
        onThemeModeChange = viewModel::setThemeMode,
        appLanguage = appLanguage,
        isAppLanguageSupported = viewModel.isAppLanguageSupported,
        onAppLanguageChange = viewModel::setAppLanguage,
        startup =
            if (viewModel.isLaunchAtStartupSupported) {
                StartupSettings(launchAtStartup, startMinimized)
            } else {
                null
            },
        onLaunchAtStartupChange = viewModel::setLaunchAtStartup,
        onStartMinimizedChange = viewModel::setStartMinimized,
    )
}

private data class StartupSettings(
    val launchAtStartup: Boolean,
    val startMinimized: Boolean,
)

@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    appLanguage: String?,
    isAppLanguageSupported: Boolean,
    onAppLanguageChange: (String?) -> Unit,
    // Null where the platform can't start the app at login
    startup: StartupSettings?,
    onLaunchAtStartupChange: (Boolean) -> Unit,
    onStartMinimizedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(Res.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp),
        )
        Column(modifier = Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                RadioRow(
                    label = stringResource(mode.label),
                    selected = mode == themeMode,
                    onClick = { onThemeModeChange(mode) },
                )
            }
        }

        if (isAppLanguageSupported) {
            Text(
                text = stringResource(Res.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp),
            )
            Column(modifier = Modifier.selectableGroup()) {
                // Null first: following the system's language
                (listOf(null) + appLanguages).forEach { language ->
                    RadioRow(
                        label = language?.nativeName ?: stringResource(Res.string.language_system),
                        selected = language?.tag == appLanguage,
                        onClick = { onAppLanguageChange(language?.tag) },
                    )
                }
            }
        }

        if (startup != null) {
            Text(
                text = stringResource(Res.string.settings_startup),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp),
            )
            SwitchRow(
                title = stringResource(Res.string.settings_launch_at_startup),
                checked = startup.launchAtStartup,
                onCheckedChange = onLaunchAtStartupChange,
            )
            SwitchRow(
                title = stringResource(Res.string.settings_start_minimized),
                summary = stringResource(Res.string.settings_start_minimized_summary),
                checked = startup.startMinimized,
                onCheckedChange = onStartMinimizedChange,
                // It applies to a start at login only
                enabled = startup.launchAtStartup,
            )
        }
    }
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The row handles the click, so the button reports state only
        RadioButton(selected = selected, onClick = null)
        Text(text = label, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // The row handles the click, so the switch reports state only
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

private val ThemeMode.label: StringResource
    get() =
        when (this) {
            ThemeMode.SYSTEM -> Res.string.theme_system
            ThemeMode.LIGHT -> Res.string.theme_light
            ThemeMode.DARK -> Res.string.theme_dark
        }

@Preview
@Composable
private fun SettingsContentPreview() {
    AppTheme {
        SettingsContent(
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
            appLanguage = null,
            isAppLanguageSupported = true,
            onAppLanguageChange = {},
            startup = StartupSettings(launchAtStartup = true, startMinimized = false),
            onLaunchAtStartupChange = {},
            onStartMinimizedChange = {},
        )
    }
}
