package com.spgrvl.kissos.data.datastore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

typealias KissosDataStore = DataStore<Preferences>

val Context.dataStore: KissosDataStore by preferencesDataStore(
    name = "ivy_wallet_datastore_v1"
)

@Composable
fun datastore(): KissosDataStore {
    return LocalContext.current.dataStore
}
