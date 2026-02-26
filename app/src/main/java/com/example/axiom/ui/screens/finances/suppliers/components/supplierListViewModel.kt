package com.example.axiom.ui.screens.finances.suppliers.components


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.customer.components.PartyDao
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupplierListViewModel(
    private val partyDao: PartyDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val suppliers: StateFlow<List<PartyEntity>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    partyDao.getByType(PartyType.SUPPLIER)
                } else {
                    partyDao.searchByType(PartyType.SUPPLIER, query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun insertSupplier(supplier: PartyEntity) {
        viewModelScope.launch {
            partyDao.insert(supplier.copy(partyType = PartyType.SUPPLIER))
        }
    }

    fun updateSupplier(supplier: PartyEntity) {
        viewModelScope.launch {
            partyDao.update(
                supplier.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun deleteSupplier(id: String) {
        viewModelScope.launch {
            partyDao.deleteById(id)
        }
    }

    fun deleteAll(ids: List<String>) {
        viewModelScope.launch {
            if (ids.isNotEmpty()) {
                partyDao.deleteAll(ids)
            }
        }
    }
}

class SupplierListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupplierListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.partyDao()
            return SupplierListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}