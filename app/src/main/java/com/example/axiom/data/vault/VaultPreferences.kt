package com.example.axiom.data.vault

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.axiom.DataStore.appDataStore

private val VAULT_LOCKED = booleanPreferencesKey("vault_locked")

class VaultPreferences(
    private val context: Context
) {

    val isLocked: Flow<Boolean> =
        context.appDataStore.data.map {
            it[VAULT_LOCKED] ?: true
        }

    suspend fun setLocked(locked: Boolean) {
        context.appDataStore.edit {
            it[VAULT_LOCKED] = locked
        }
    }
}

