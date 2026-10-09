package dev.muazkadan.myapplication.di

import dev.muazkadan.myapplication.data.preferences.PreferencesManager
import dev.muazkadan.myapplication.presentation.screen.settings.SettingsViewModel
import dev.muazkadan.myapplication.presentation.screen.splash.SplashViewModel
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock

val appModule =
    module {
        // Inject these instead of calling Clock.System / TimeZone.currentSystemDefault() directly,
        // so tests can pin "now" and the time zone
        single<Clock> { Clock.System }
        single<TimeZone> { TimeZone.currentSystemDefault() }

        singleOf(::PreferencesManager)

        viewModelOf(::SplashViewModel)
        // getOrNull: LaunchAtStartup is bound only on desktop
        viewModel { SettingsViewModel(get(), getOrNull()) }
    }
