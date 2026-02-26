package com.example.axiom.ui.screens.finances.customer.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerListViewModel(
    private val partyDao: PartyDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    private val _selectedParty =
        MutableStateFlow<PartyWithContacts?>(null)

    val selectedParty: StateFlow<PartyWithContacts?> =
        _selectedParty

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

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun insertCustomer(customer: PartyEntity) {
        viewModelScope.launch {
            partyDao.insert(customer.copy(partyType = PartyType.CUSTOMER))
        }
    }

    fun updateCustomer(customer: PartyEntity) {
        viewModelScope.launch {
            partyDao.update(
                customer.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

//    suspend fun getCustomerById(id: String): PartyEntity? {
//        return partyDao.getById(id)
//    }


    fun loadCustomer(id: String) {
        viewModelScope.launch {
            _selectedParty.value =
                partyDao.getPartyWithContacts(id)
        }
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
                partyDao.deleteAll(ids)
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

    fun clearSelection() {
        _selectedParty.value = null
    }
}

class CustomerListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.partyDao()
            return CustomerListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}