package com.example.axiom.ui.screens.finances.seller.components


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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SellerListViewModel(
    private val partyDao: PartyDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val selectedSellerId = MutableStateFlow<String?>(null)

    private val _editingParty = MutableStateFlow<PartyWithContacts?>(null)
    val editingParty: StateFlow<PartyWithContacts?> = _editingParty


    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedParty: StateFlow<PartyWithContacts?> =
        selectedSellerId
            .filterNotNull()
            .flatMapLatest { id ->
                // Fetches the seller details automatically
                flow { emit(partyDao.getPartyWithContacts(id)) }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )


    fun selectSeller(id: String) {
        selectedSellerId.value = id
    }

    fun loadSellerForEdit(id: String) {
        viewModelScope.launch {
            _editingParty.value = partyDao.getPartyWithContacts(id)
        }
    }

    fun clearEditSelection() {
        _editingParty.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val sellers: StateFlow<List<PartyEntity>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    partyDao.getByType(PartyType.SELLER)
                } else {
                    partyDao.searchByType(PartyType.SELLER, query)
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


    fun saveSeller(party: PartyEntity, contacts: List<PartyContactEntity>) {
        viewModelScope.launch {
            partyDao.upsertPartyWithContacts(
                party.copy(
                    partyType = PartyType.SELLER,
                    updatedAt = System.currentTimeMillis()
                ),
                contacts
            )
        }
    }


    fun updateSeller(seller: PartyEntity) {
        viewModelScope.launch {
            partyDao.update(
                seller.copy(updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun deleteSeller(id: String) {
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
}

class SellerListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerListViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.partyDao()
            return SellerListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}