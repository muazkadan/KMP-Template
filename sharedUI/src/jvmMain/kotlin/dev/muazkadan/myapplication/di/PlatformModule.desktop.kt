package dev.muazkadan.myapplication.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.muazkadan.myapplication.DesktopAppData
import dev.muazkadan.myapplication.data.preferences.DATA_STORE_FILE_NAME
import dev.muazkadan.myapplication.data.preferences.createDataStore
import org.koin.core.module.Module
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
    }
