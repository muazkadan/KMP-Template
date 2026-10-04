package dev.muazkadan.myapplication.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.muazkadan.myapplication.data.preferences.DATA_STORE_FILE_NAME
import dev.muazkadan.myapplication.data.preferences.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

fun initKoin() = initKoin {}

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module =
    module {
        single<DataStore<Preferences>> {
            createDataStore(
                producePath = {
                    val documentDirectory: NSURL? =
                        NSFileManager.defaultManager.URLForDirectory(
                            directory = NSDocumentDirectory,
                            inDomain = NSUserDomainMask,
                            appropriateForURL = null,
                            create = false,
                            error = null,
                        )
                    requireNotNull(documentDirectory).path + "/$DATA_STORE_FILE_NAME"
                },
            )
        }
    }
