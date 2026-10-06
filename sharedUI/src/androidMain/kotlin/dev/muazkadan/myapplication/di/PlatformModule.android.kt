package dev.muazkadan.myapplication.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }
