package com.example.axiom.ui.screens.finances.product.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProductListViewModel(
    private val productDao: ProductDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    // selection state
    private val selectedProductId = MutableStateFlow<String?>(null)

    private val _editingProduct = MutableStateFlow<ProductEntity?>(null)
    val editingProduct: StateFlow<ProductEntity?> = _editingProduct

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProduct: StateFlow<ProductEntity?> =
        selectedProductId
            .filterNotNull()
            .flatMapLatest { id ->
                flow { emit(productDao.getById(id)) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    fun selectProduct(id: String) {
        selectedProductId.value = id
    }

    fun loadProductForEdit(id: String) {
        viewModelScope.launch {
            _editingProduct.value = productDao.getById(id)
        }
    }

    fun clearEditSelection() {
        _editingProduct.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductBasic>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    productDao.getAllBasic()
                } else {
                    productDao.searchBasic(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val categories: StateFlow<List<String>> =
        productDao.getAllCategories()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun insertProduct(product: ProductEntity) {
        viewModelScope.launch {
            productDao.insert(product)
        }
    }

    fun updateProduct(updated: ProductEntity) {
        viewModelScope.launch {
            productDao.update(
                updated.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun softDelete(id: String) {
        viewModelScope.launch {
            productDao.softDelete(id)
        }
    }

    fun deleteAll(ids: List<String>) {
        viewModelScope.launch {
            if (ids.isNotEmpty()) {
                productDao.softDeleteAll(ids)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun getInvoicesForProduct(productId: String): Flow<List<ProductInvoiceUsage>> {
        return invoiceDao.getInvoicesForProduct(productId)
    }

    // Add this to your ViewModel
    fun getProfitStatsForProduct(productId: String): StateFlow<ProductDao.ProductProfitStats?> {
        return productDao.getProductProfitStats(productId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null // Default state while loading
            )
    }

    val selectedProductStats: StateFlow<ProductDao.ProductProfitStats?> =
        selectedProductId
            .filterNotNull()
            .flatMapLatest { id ->
                // This is the DAO query we created in Phase 2
                productDao.getProductProfitStats(id)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )
}


class ProductListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.productDao()
            val invoiceDao = db.invoiceDao()
            return ProductListViewModel(dao, invoiceDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}