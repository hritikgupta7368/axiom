package com.example.axiom.ui.screens.finances.product.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.data.finances.Product
import com.example.axiom.data.finances.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val products: StateFlow<List<Product>> =
        _query
            .flatMapLatest { q ->
                repository.search(q)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    fun updateQuery(value: String) {
        _query.value = value
    }

    fun getByCategory(category: String): Flow<List<Product>> =
        repository.getByCategory(category)


    fun insert(product: Product) =
        viewModelScope.launch { repository.insert(product) }

    fun update(product: Product) =
        viewModelScope.launch { repository.update(product) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repository.deleteById(id)
        }
}


class ProductViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.productDao()
            val repo = ProductRepository(dao)
            return ProductViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}