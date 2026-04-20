package com.example.axiom.ui.screens.finances.customer.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerBusinessStats
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerInvoiceRow
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerProductUsage
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerTopProduct
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerListViewModel(
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val selectedCustomerId = MutableStateFlow<String?>(null)

    private val _editingParty = MutableStateFlow<PartyWithContacts?>(null)
    val editingParty: StateFlow<PartyWithContacts?> = _editingParty


    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedParty: StateFlow<PartyWithContacts?> =
        selectedCustomerId
            .filterNotNull()
            .flatMapLatest { id ->
                // Fetches the customer details automatically
                flow { emit(partyDao.getPartyWithContacts(id)) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    fun selectCustomer(id: String) {
        selectedCustomerId.value = id
    }


    fun loadCustomerForEdit(id: String) {
        viewModelScope.launch {
            _editingParty.value = partyDao.getPartyWithContacts(id)
        }
    }

    fun clearEditSelection() {
        _editingParty.value = null
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<PartyEntity>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    partyDao.getByType(PartyType.CUSTOMER)
                } else {
                    partyDao.searchByType(PartyType.CUSTOMER, query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    val customersWithContacts: StateFlow<List<PartyWithContacts>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    partyDao.getByTypeWithContacts(PartyType.CUSTOMER)
                } else {
                    partyDao.searchByTypeWithContacts(
                        PartyType.CUSTOMER,
                        query
                    )
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


    fun saveCustomer(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.upsertPartyWithContacts(
                party.copy(
                    partyType = PartyType.CUSTOMER,
                    updatedAt = System.currentTimeMillis()
                ),
                contacts
            )
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            partyDao.deleteById(id)
        }
    }

    fun deleteAll(ids: List<String>) {
        viewModelScope.launch {
            if (ids.isNotEmpty()) {
                partyDao.softDeleteAll(ids)
            }
        }
    }

    fun insertCustomerWithContacts(
        customer: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.insertPartyWithContacts(
                customer.copy(partyType = PartyType.CUSTOMER),
                contacts
            )
        }
    }

    fun updateCustomerWithContacts(
        customer: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.insertPartyWithContacts(
                customer.copy(updatedAt = System.currentTimeMillis()),
                contacts
            )
        }
    }


    val invoices: StateFlow<List<CustomerInvoiceRow>> =
        selectedCustomerId
            .filterNotNull()
            .flatMapLatest { invoiceDao.getInvoicesForCustomer(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val productsPurchased: StateFlow<List<CustomerProductUsage>> =
        selectedCustomerId
            .filterNotNull()
            .flatMapLatest { invoiceDao.getProductsUsedByCustomer(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val topProducts: StateFlow<List<CustomerTopProduct>> =
        selectedCustomerId
            .filterNotNull()
            .flatMapLatest { invoiceDao.getTopProductsForCustomer(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val businessStats: StateFlow<CustomerBusinessStats?> =
        selectedCustomerId
            .filterNotNull()
            .flatMapLatest { invoiceDao.getCustomerBusinessStats(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )
}

class CustomerListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.partyDao()
            val invoiceDao = db.invoiceDao()
            return CustomerListViewModel(dao, invoiceDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}