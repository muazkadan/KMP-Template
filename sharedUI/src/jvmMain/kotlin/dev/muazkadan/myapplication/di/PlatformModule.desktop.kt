package dev.muazkadan.myapplication.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.muazkadan.myapplication.data.preferences.DATA_STORE_FILE_NAME
import dev.muazkadan.myapplication.data.preferences.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

// Where the desktop app keeps its data, under the user's home directory. Rename it with the app.
private const val APP_DATA_DIR_NAME = ".cmptemplate"

actual val platformModule: Module =
    module {
        single<DataStore<Preferences>> {
            createDataStore(
                producePath = {
                    val appDataDir = File(System.getProperty("user.home"), APP_DATA_DIR_NAME)
                    appDataDir.mkdirs()
                    File(appDataDir, DATA_STORE_FILE_NAME).absolutePath
                },
            )
        }
    }
