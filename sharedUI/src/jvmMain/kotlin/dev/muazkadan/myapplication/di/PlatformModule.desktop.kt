package dev.muazkadan.myapplication.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.muazkadan.myapplication.DesktopAppData
import dev.muazkadan.myapplication.data.language.AppLanguageSetting
import dev.muazkadan.myapplication.data.language.DesktopAppLanguage
import dev.muazkadan.myapplication.data.preferences.DATA_STORE_FILE_NAME
import dev.muazkadan.myapplication.data.preferences.createDataStore
import dev.muazkadan.myapplication.data.startup.AutoStartManager
import dev.muazkadan.myapplication.data.startup.LaunchAtStartup
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module =
    module {
        single<DataStore<Preferences>> {
            createDataStore(
                producePath = {
                    val appDataDir = DesktopAppData.dir
                    appDataDir.mkdirs()
                    File(appDataDir, DATA_STORE_FILE_NAME).absolutePath
                },
            )
        }
        single<LaunchAtStartup> { AutoStartManager }
        singleOf(::DesktopAppLanguage) { bind<AppLanguageSetting>() }
    }
