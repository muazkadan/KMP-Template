package dev.muazkadan.myapplication.di

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.muazkadan.myapplication.data.language.AndroidAppLanguage
import dev.muazkadan.myapplication.data.language.AppLanguageSetting
import dev.muazkadan.myapplication.data.preferences.DATA_STORE_FILE_NAME
import dev.muazkadan.myapplication.data.preferences.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module =
    module {
        single<DataStore<Preferences>> {
            createDataStore(
                producePath = { get<Context>().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath },
            )
        }
        // Before Android 13 there's no per-app language, and the app follows the system's
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            single<AppLanguageSetting> { AndroidAppLanguage(get()) }
        }
    }
