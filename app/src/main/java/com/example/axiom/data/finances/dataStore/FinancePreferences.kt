package com.example.axiom.data.finances.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.axiom.DataStore.appDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SelectedSellerPref(
    val id: String?,
    val name: String?,
    val stateCode: String?
)


class FinancePreferences(private val context: Context) {
    companion object {
        private val SELECTED_SELLER_FIRM_NAME_KEY = stringPreferencesKey("selected_seller_firm_name")
        private val SELECTED_SELLER_FIRM_ID_KEY = stringPreferencesKey("selected_seller_firm_id")
        private val SELECTED_SELLER_FIRM_STATE_CODE_KEY = stringPreferencesKey("selected_seller_firm_state_code")
        private val LAST_INVOICE_NUMBER_KEY = longPreferencesKey("last_invoice_number")
    }

    // Get selected Seller Firm Name
    val selectedSellerFirmName: Flow<String?> = context.appDataStore.data
        .map { preferences ->
            preferences[SELECTED_SELLER_FIRM_NAME_KEY]
        }
        
    // Get selected Seller Firm ID
    val selectedSellerFirmId: Flow<String?> = context.appDataStore.data
        .map { preferences ->
            preferences[SELECTED_SELLER_FIRM_ID_KEY]
        }

    val selectedSeller: Flow<SelectedSellerPref> =
        context.appDataStore.data.map { preferences ->
            SelectedSellerPref(
                id = preferences[SELECTED_SELLER_FIRM_ID_KEY],
                name = preferences[SELECTED_SELLER_FIRM_NAME_KEY],
                stateCode = preferences[SELECTED_SELLER_FIRM_STATE_CODE_KEY] // <--- Retrieve it
            )
        }


    // Save selected Seller Firm
    suspend fun saveSelectedSellerFirm(id: String, name: String, stateCode: String) {
        context.appDataStore.edit { preferences ->
            preferences[SELECTED_SELLER_FIRM_ID_KEY] = id
            preferences[SELECTED_SELLER_FIRM_NAME_KEY] = name
            preferences[SELECTED_SELLER_FIRM_STATE_CODE_KEY] = stateCode // <--- Save it
        }
    }
    
    // Clear selection if needed
    suspend fun clearSelectedSellerFirm() {
        context.appDataStore.edit { preferences ->
            preferences.remove(SELECTED_SELLER_FIRM_ID_KEY)
            preferences.remove(SELECTED_SELLER_FIRM_NAME_KEY)
            preferences.remove(SELECTED_SELLER_FIRM_STATE_CODE_KEY) // <--- Clear it
        }
    }

    val lastInvoiceNumber: Flow<Long> = context.appDataStore.data
        .map { it[LAST_INVOICE_NUMBER_KEY] ?: 0L }

    suspend fun saveLastInvoiceNumber(number: Long) {
        context.appDataStore.edit {
            it[LAST_INVOICE_NUMBER_KEY] = number
        }
    }
    // Get Last Invoice Number
    suspend fun getAndIncrementInvoiceNumber(): Long {
        var nextNumber = 0L
        context.appDataStore.edit { prefs ->
            val current = prefs[LAST_INVOICE_NUMBER_KEY] ?: 0L
            nextNumber = current + 1
            prefs[LAST_INVOICE_NUMBER_KEY] = nextNumber
        }
        return nextNumber
    }



}