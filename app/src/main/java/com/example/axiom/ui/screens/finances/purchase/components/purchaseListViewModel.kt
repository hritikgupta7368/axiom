package com.example.axiom.ui.screens.finances.purchase.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceDao
import com.example.axiom.ui.screens.finances.product.components.ProductDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseDao: PurchaseDao,
    private val productDao: ProductDao,   // INJECT THIS
    private val invoiceDao: InvoiceDao    // INJECT THIS
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val _selectedPurchase =
        MutableStateFlow<PurchaseWithItems?>(null)

    val selectedPurchase: StateFlow<PurchaseWithItems?> =
        _selectedPurchase

    // ---------- LIST ----------
    @OptIn(ExperimentalCoroutinesApi::class)
    val allPurchases: StateFlow<List<PurchaseWithItems>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    purchaseDao.getAllPurchases()
                } else {
                    purchaseDao.searchPurchases(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    // ---------- LOAD SINGLE ----------
    fun loadPurchase(id: String) {
        viewModelScope.launch {
            _selectedPurchase.value =
                purchaseDao.getPurchaseWithItems(id)
        }
    }

    // ---------- SEARCH ----------
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // ---------- ADD / UPDATE ----------
    fun savePurchase(
        record: PurchaseRecordEntity,
        items: List<PurchaseItemEntity>
    ) {
        viewModelScope.launch {
            // 1. Save the purchase normally
            purchaseDao.upsertPurchaseWithItems(record, items)

            // 2. THE FIX: Loop through bought items to update costs
            items.forEach { purchaseItem ->

                // A. Update the Product's default Cost Price for FUTURE sales
                val product = productDao.getById(purchaseItem.productId)
                if (product != null) {
                    productDao.update(
                        product.copy(
                            costPrice = purchaseItem.costPrice,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                // B. Retroactively fix PAST sales that lacked a cost price
                invoiceDao.applyRetroactiveCostToUnlinkedItems(
                    productId = purchaseItem.productId,
                    actualCostPrice = purchaseItem.costPrice,
                    purchaseItemId = purchaseItem.id
                )
            }
        }
    }

    // ---------- DELETE ----------
    fun deletePurchase(id: String) {
        viewModelScope.launch {
            purchaseDao.deletePurchase(id)
        }
    }

    // ---------- CLEAR ----------
    fun clearSelection() {
        _selectedPurchase.value = null
    }

    // ---------- DIRECT FETCH ----------
    suspend fun getPurchaseById(id: String): PurchaseWithItems? {
        return purchaseDao.getPurchaseWithItems(id)
    }

}


class PurchaseViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.purchaseDao()
            val productDao = db.productDao()
            val invoiceDao = db.invoiceDao()
            return PurchaseViewModel(dao, productDao, invoiceDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}