package com.example.axiom.data.finances

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val SELECTED_SELLER_ID = stringPreferencesKey("selected_seller_id")
        private val SELECTED_SELLER_NAME = stringPreferencesKey("selected_seller_name")
        private val SELECTED_SELLER_STATE_CODE = stringPreferencesKey("selected_seller_state_code")
        private val LAST_INVOICE_NUMBER = longPreferencesKey("last_invoice_number")
        private val LAST_PRODUCT_ID = longPreferencesKey("last_product_id")
        private val LAST_CUSTOMER_ID = longPreferencesKey("last_customer_id")
        private val LAST_SUPPLIER_ID = longPreferencesKey("last_supplier_id")
    }

    // Selected Seller
    val selectedSeller: Flow<SelectedSellerPref> = context.appDataStore.data.map { prefs ->
        SelectedSellerPref(
            id = prefs[SELECTED_SELLER_ID],
            name = prefs[SELECTED_SELLER_NAME],
            stateCode = prefs[SELECTED_SELLER_STATE_CODE]
        )
    }

    suspend fun saveSelectedSellerFirm(id: String, name: String, stateCode: String) {
        context.appDataStore.edit { prefs ->
            prefs[SELECTED_SELLER_ID] = id
            prefs[SELECTED_SELLER_NAME] = name
            prefs[SELECTED_SELLER_STATE_CODE] = stateCode
        }
    }

    suspend fun clearSelectedSellerFirm() {
        context.appDataStore.edit { prefs ->
            prefs.remove(SELECTED_SELLER_ID)
            prefs.remove(SELECTED_SELLER_NAME)
            prefs.remove(SELECTED_SELLER_STATE_CODE)
        }
    }

    // Invoice Number
    val lastInvoiceNumber: Flow<Long> = context.appDataStore.data
        .map { it[LAST_INVOICE_NUMBER] ?: 0L }

    suspend fun saveLastInvoiceNumber(number: Long) {
        context.appDataStore.edit { it[LAST_INVOICE_NUMBER] = number }
    }

    suspend fun getAndIncrementInvoiceNumber(): Long {
        var nextNumber = 0L
        context.appDataStore.edit { prefs ->
            val current = prefs[LAST_INVOICE_NUMBER] ?: 0L
            nextNumber = current + 1
            prefs[LAST_INVOICE_NUMBER] = nextNumber
        }
        return nextNumber
    }

    // Product ID Counter (optional utility)
    suspend fun getAndIncrementProductId(): Long {
        var nextId = 0L
        context.appDataStore.edit { prefs ->
            val current = prefs[LAST_PRODUCT_ID] ?: 0L
            nextId = current + 1
            prefs[LAST_PRODUCT_ID] = nextId
        }
        return nextId
    }

    // Customer ID Counter (optional utility)
    suspend fun getAndIncrementCustomerId(): Long {
        var nextId = 0L
        context.appDataStore.edit { prefs ->
            val current = prefs[LAST_CUSTOMER_ID] ?: 0L
            nextId = current + 1
            prefs[LAST_CUSTOMER_ID] = nextId
        }
        return nextId
    }

    // Supplier ID Counter (optional utility)
    suspend fun getAndIncrementSupplierId(): Long {
        var nextId = 0L
        context.appDataStore.edit { prefs ->
            val current = prefs[LAST_SUPPLIER_ID] ?: 0L
            nextId = current + 1
            prefs[LAST_SUPPLIER_ID] = nextId
        }
        return nextId
    }
}
//```
//
//**Important:** Don't forget to add these DAOs to your `AppDatabase.kt`:
//```kotlin
//abstract fun productDao(): ProductDao
//abstract fun supplierFirmDao(): SupplierFirmDao
//abstract fun purchaseRecordDao(): PurchaseRecordDao
//abstract fun sellerFirmDao(): SellerFirmDao
//abstract fun customerFirmDao(): CustomerFirmDao
//abstract fun invoiceDao(): InvoiceDao
//```
//
//And add `FinanceTypeConverters` to your database type converters list.