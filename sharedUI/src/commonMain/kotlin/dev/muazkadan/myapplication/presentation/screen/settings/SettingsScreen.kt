package dev.muazkadan.myapplication.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmptemplate.sharedui.generated.resources.Res
import cmptemplate.sharedui.generated.resources.settings_theme
import cmptemplate.sharedui.generated.resources.settings_title
import cmptemplate.sharedui.generated.resources.theme_dark
import cmptemplate.sharedui.generated.resources.theme_light
import cmptemplate.sharedui.generated.resources.theme_system
import dev.muazkadan.myapplication.data.model.ThemeMode
import dev.muazkadan.myapplication.presentation.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    SettingsContent(
        themeMode = themeMode,
        onThemeModeChange = viewModel::setThemeMode,
    )
}

@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == themeMode,
                                onClick = { onThemeModeChange(mode) },
                                role = Role.RadioButton,
                            ).padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The row handles the click, so the button reports state only
                    RadioButton(selected = mode == themeMode, onClick = null)
                    Text(
                        text = stringResource(mode.label),
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
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
        SettingsContent(themeMode = ThemeMode.SYSTEM, onThemeModeChange = {})
    }
}
