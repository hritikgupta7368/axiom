package com.example.axiom.ui.screens.finances.customer.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.CustomerFirmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repo: CustomerFirmRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val customers: StateFlow<List<CustomerFirm>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeCustomers: StateFlow<List<CustomerFirm>> =
        repo.activeCustomers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun insert(customer: CustomerFirm) =
        viewModelScope.launch { repo.insert(customer) }

    fun update(customer: CustomerFirm) =
        viewModelScope.launch { repo.update(customer) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }

}

class CustomerFirmViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.customerFirmDao()
            val repo = CustomerFirmRepository(dao)
            return CustomerViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}