package com.example.axiom.ui.screens.finances.product.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProductListViewModel(
    private val productDao: ProductDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val _product = MutableStateFlow<ProductEntity?>(null)
    val product: StateFlow<ProductEntity?> = _product.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductBasic>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    productDao.getAllBasic()
                } else {
                    productDao.search(query)
                        .map { list ->
                            list.map {
                                ProductBasic(
                                    id = it.id,
                                    name = it.name,
                                    sellingPrice = it.sellingPrice,
                                    lastSellingPrice = it.lastSellingPrice,
                                    imageUrl = it.imageUrl,
                                    hsn = it.hsn,
                                    unit = it.unit,
                                    category = it.category
                                )
                            }
                        }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _product.value = productDao.getById(id)
        }
    }

    suspend fun getProductById(id: String): ProductEntity? {
        return productDao.getById(id)
    }

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
            loadProduct(updated.id)
        }
    }

    fun softDelete(id: String) {
        viewModelScope.launch {
            productDao.softDelete(id)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }


    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productDao.softDelete(productId)
        }
    }

    fun deleteAll(ids: List<String>) {
        viewModelScope.launch {
            if (ids.isNotEmpty()) {
                productDao.softDeleteAll(ids)
            }
        }
    }
}


class ProductListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.productDao()
            return ProductListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}