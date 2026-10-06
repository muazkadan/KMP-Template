package dev.muazkadan.myapplication.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Creates the app's DataStore at the path each platform module provides. Bind the result as a
 * Koin `single`: two DataStores open on the same file throw at runtime.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

internal const val DATA_STORE_FILE_NAME = "app.preferences_pb"
