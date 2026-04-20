package com.example.axiom.ui.screens.finances.quotation.components


import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.utils.QuotationPdfRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class QuotationViewModel(
    application: Application,
    private val quotationDao: QuotationDao
) : AndroidViewModel(application) {

    // Holds the current search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val pdfRepo = QuotationPdfRepository(getApplication())

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri
    private val _loading = MutableStateFlow(false)

    // Reactively fetches from the database based on the search query
    @OptIn(ExperimentalCoroutinesApi::class)
    val quotationCards: StateFlow<List<QuotationCardDto>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                quotationDao.getAllQuotationCards()
            } else {
                quotationDao.searchQuotationCards(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun getFullQuotationById(id: String): FullQuotation? {
        return quotationDao.getFullQuotationById(id)
    }

    // --- Write Operations ---

    fun createQuotation(quotation: QuotationEntity, items: List<QuotationItemEntity>) {
        viewModelScope.launch {
            // Ensure IDs are set if they are blank
            val qId = quotation.id.ifBlank { UUID.randomUUID().toString() }
            val qToSave = quotation.copy(id = qId)

            val itemsToSave = items.map {
                it.copy(
                    id = it.id.ifBlank { UUID.randomUUID().toString() },
                    quotationId = qId
                )
            }

            quotationDao.createFullQuotation(qToSave, itemsToSave)
        }
    }

    fun updateQuotation(quotation: QuotationEntity, items: List<QuotationItemEntity>) {
        viewModelScope.launch {
            // Re-link items to the quotation just in case
            val itemsToSave = items.map {
                it.copy(
                    id = it.id.ifBlank { UUID.randomUUID().toString() },
                    quotationId = quotation.id
                )
            }
            quotationDao.updateFullQuotation(quotation, itemsToSave)
        }
    }

    fun deleteQuotation(id: String) {
        viewModelScope.launch {
            quotationDao.softDeleteQuotation(id)
        }
    }

    fun markQuotationAsAccepted(id: String) {
        viewModelScope.launch {
            quotationDao.updateQuotationStatus(id, QuotationStatus.ACCEPTED)
        }
    }

    fun generatePdf(quotaion: FullQuotation, logoUri: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pdfUri.value = pdfRepo.generate(quotaion, logoUri)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearPdf() {
        _pdfUri.value = null
    }
}

class QuotationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotationViewModel::class.java)) {
            val app = context.applicationContext as Application
            val db = AppDatabase.get(context)
            val dao = db.quotationDao()
            return QuotationViewModel(app, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}