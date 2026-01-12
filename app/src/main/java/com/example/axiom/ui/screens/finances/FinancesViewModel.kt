package com.example.axiom.ui.screens.finances

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.data.finances.domain.CustomerFirm
import com.example.axiom.data.finances.domain.Invoice
import com.example.axiom.data.finances.domain.Product
import com.example.axiom.data.finances.domain.PurchaseRecord
import com.example.axiom.data.finances.domain.SellerFirm
import com.example.axiom.data.finances.domain.SupplierFirm
import com.example.axiom.data.finances.repository.FinancesRepository
import com.example.axiom.ui.utils.InvoicePdfRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinancesViewModel(private val repository: FinancesRepository) : ViewModel() {

    // --- Products ---
    val products: StateFlow<List<Product>> = repository.getProducts()
        .catch { e -> 
            // Handle error (e.g., emit empty list or log)
            e.printStackTrace()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProduct(product: Product) = viewModelScope.launch {
        repository.addProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(productId: String) = viewModelScope.launch {
        repository.deleteProduct(productId)
    }

    // --- Supplier Firms ---
    val supplierFirms: StateFlow<List<SupplierFirm>> = repository.getSupplierFirms()
        .catch { e -> e.printStackTrace() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSupplierFirm(firm: SupplierFirm) = viewModelScope.launch {
        repository.addSupplierFirm(firm)
    }

    fun updateSupplierFirm(firm: SupplierFirm) = viewModelScope.launch {
        repository.updateSupplierFirm(firm)
    }

    fun deleteSupplierFirm(firmId: String) = viewModelScope.launch {
        repository.deleteSupplierFirm(firmId)
    }

    // --- Purchase Records ---
    val purchaseRecords: StateFlow<List<PurchaseRecord>> = repository.getPurchaseRecords()
        .catch { e -> e.printStackTrace() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addPurchaseRecord(record: PurchaseRecord) = viewModelScope.launch {
        repository.addPurchaseRecord(record)
    }

    fun updatePurchaseRecord(record: PurchaseRecord) = viewModelScope.launch {
        repository.updatePurchaseRecord(record)
    }

    fun deletePurchaseRecord(recordId: String) = viewModelScope.launch {
        repository.deletePurchaseRecord(recordId)
    }

    // --- Customer Firms ---
    val customerFirms: StateFlow<List<CustomerFirm>> = repository.getCustomerFirms()
        .catch { e -> e.printStackTrace() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCustomerFirm(firm: CustomerFirm) = viewModelScope.launch {
        repository.addCustomerFirm(firm)
    }

    fun updateCustomerFirm(firm: CustomerFirm) = viewModelScope.launch {
        repository.updateCustomerFirm(firm)
    }

    fun deleteCustomerFirm(firmId: String) = viewModelScope.launch {
        repository.deleteCustomerFirm(firmId)
    }
    
    // --- Seller Firms ---
    val sellerFirms: StateFlow<List<SellerFirm>> = repository.getSellerFirms()
        .catch { e -> e.printStackTrace() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSellerFirm(firm: SellerFirm) = viewModelScope.launch {
        repository.addSellerFirm(firm)
    }

    fun updateSellerFirm(firm: SellerFirm) = viewModelScope.launch {
        repository.updateSellerFirm(firm)
    }

    fun deleteSellerFirm(firmId: String) = viewModelScope.launch {
        repository.deleteSellerFirm(firmId)
    }

    // --- Invoices ---
    val invoices: StateFlow<List<Invoice>> = repository.getInvoices()
        .catch { e -> e.printStackTrace() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    private val _currentInvoice = MutableStateFlow<Invoice?>(null)
    val currentInvoice: StateFlow<Invoice?> = _currentInvoice.asStateFlow()
    
    private val _currentInvoiceCustomer = MutableStateFlow<CustomerFirm?>(null)
    val currentInvoiceCustomer: StateFlow<CustomerFirm?> = _currentInvoiceCustomer.asStateFlow()

//    fun fetchInvoiceDetails(invoiceId: String) = viewModelScope.launch {
//        val invoice = repository.getInvoiceById(invoiceId)
//        _currentInvoice.value = invoice
//        if (invoice != null) {
//            val customer = repository.getCustomerById(invoice.customerDetails!!.id)
//            _currentInvoiceCustomer.value = customer
//        }
//    }

    fun fetchInvoiceDetails(invoiceId: String) {
        viewModelScope.launch {
            _currentInvoice.value = null // Reset for loading state
            try {
                // Use the repository to get the invoice.
                // The repository should handle fetching and deserializing.
                val invoice = repository.getInvoiceById(invoiceId)
                _currentInvoice.value = invoice

            } catch (e: Exception) {
                // Handle exceptions, e.g., document not found or network error
                Log.e("FinancesViewModel", "Error fetching invoice details for ID: $invoiceId", e)
                _currentInvoice.value = null
            }
        }
    }






    private val _isCreatingInvoice = MutableStateFlow(false)
    val isCreatingInvoice = _isCreatingInvoice

    fun addInvoice(invoice: Invoice, onDone: () -> Unit) {
        if (_isCreatingInvoice.value) return

        viewModelScope.launch {
            _isCreatingInvoice.value = true
            repository.addInvoice(invoice)
            _isCreatingInvoice.value = false
            onDone()
        }
    }




    fun updateInvoice(invoice: Invoice) = viewModelScope.launch {
        repository.updateInvoice(invoice)
    }

    fun deleteInvoice(invoiceId: String) = viewModelScope.launch {
        repository.softDeleteInvoice(invoiceId)
    }
}

// Factory for creating FinancesViewModel
class FinancesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinancesViewModel(FinancesRepository(FirebaseFirestore.getInstance())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




class InvoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = InvoicePdfRepository(application)

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri = _pdfUri.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun generatePdf(invoice: Invoice, logoUri: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pdfUri.value = repo.generate(invoice, logoUri)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clear() {
        _pdfUri.value = null
    }
}
