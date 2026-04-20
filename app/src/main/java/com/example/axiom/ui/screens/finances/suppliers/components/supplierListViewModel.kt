package com.example.axiom.ui.screens.finances.suppliers.components


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyDao
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyType
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupplierListViewModel(
    private val partyDao: PartyDao,
    private val purchaseDao: PurchaseDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val selectedSupplierId = MutableStateFlow<String?>(null)


    private val _editingParty = MutableStateFlow<PartyWithContacts?>(null)
    val editingParty: StateFlow<PartyWithContacts?> = _editingParty

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedParty: StateFlow<PartyWithContacts?> =
        selectedSupplierId
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

    fun selectSupplier(id: String) {
        selectedSupplierId.value = id
    }


    fun loadSupplierForEdit(id: String) {
        viewModelScope.launch {
            _editingParty.value = partyDao.getPartyWithContacts(id)
        }
    }

    fun clearEditSelection() {
        _editingParty.value = null
    }


    @OptIn(ExperimentalCoroutinesApi::class)
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


    @OptIn(ExperimentalCoroutinesApi::class)
    val suppliersWithContacts: StateFlow<List<PartyWithContacts>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    partyDao.getByTypeWithContacts(PartyType.SUPPLIER)
                } else {
                    partyDao.searchByTypeWithContacts(
                        PartyType.SUPPLIER,
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

    fun saveSupplier(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.upsertPartyWithContacts(
                party.copy(
                    partyType = PartyType.SUPPLIER,
                    updatedAt = System.currentTimeMillis()
                ),
                contacts
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
                partyDao.softDeleteAll(ids)
            }
        }
    }

    fun insertSupplierWithContacts(
        supplier: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.insertPartyWithContacts(
                supplier.copy(partyType = PartyType.SUPPLIER),
                contacts
            )
        }
    }

    fun updateSupplierWithContacts(
        supplier: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModelScope.launch {
            partyDao.insertPartyWithContacts(
                supplier.copy(updatedAt = System.currentTimeMillis()),
                contacts
            )
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
            val purchaseDao = db.purchaseDao()
            return SupplierListViewModel(dao, purchaseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}