package com.example.axiom.ui.screens.finances.Invoice.components


import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.screens.finances.analytics.B2bInvoice
import com.example.axiom.ui.screens.finances.analytics.HsnSummary
import com.example.axiom.ui.screens.finances.product.components.ProductDao
import com.example.axiom.ui.utils.InvoicePdfRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class InvoiceViewModel(
    application: Application,
    private val invoiceDao: InvoiceDao,
    private val productDao: ProductDao
) : AndroidViewModel(application) {


    // 1. State for selected Date (Defaults to current month/year)
    private val _selectedMonth =
        MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0 = Jan, 11 = Dec
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // 2. State for the UI Data
    private val _b2bInvoices = MutableStateFlow<List<B2bInvoice>>(emptyList())
    val b2bInvoices: StateFlow<List<B2bInvoice>> = _b2bInvoices.asStateFlow()

    private val _hsnSummaryList = MutableStateFlow<List<HsnSummary>>(emptyList())
    val hsnSummaryList: StateFlow<List<HsnSummary>> = _hsnSummaryList.asStateFlow()

    val totalBusinessProfit: StateFlow<Double> =
        invoiceDao.getTotalBusinessProfitFlow()
            .map { it ?: 0.0 } // Handle null case if no invoices exist
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

    init {
        // Load data for the current month when ViewModel is created
        loadGstReportData()
    }


    private val pdfRepo = InvoicePdfRepository(getApplication())

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // 3. Update Function (Called from your UI picker)
    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        loadGstReportData()
    }

    // 4. Automatic Fetching Logic
    private fun loadGstReportData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()

            // Set to 1st day of the selected month
            calendar.set(Calendar.YEAR, _selectedYear.value)
            calendar.set(Calendar.MONTH, _selectedMonth.value)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startMillis = calendar.timeInMillis

            // Move to 1st day of NEXT month, then subtract 1 millisecond
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endMillis = calendar.timeInMillis

            // Fetch and update state flows
            _b2bInvoices.value = getB2bInvoices(startMillis, endMillis)
            _hsnSummaryList.value = getHsnSummary(startMillis, endMillis)
        }
    }

    // --- Search & List State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedInvoiceWithItems = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoiceWithItems: StateFlow<InvoiceWithItems?> =
        _selectedInvoiceWithItems.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val invoiceCards: StateFlow<List<InvoiceCardDto>> =
        _searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    invoiceDao.getAllInvoiceCards()
                } else {
                    invoiceDao.searchInvoiceCards(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Actions ---
    fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        payment: PaymentTransactionEntity?
    ) {
        viewModelScope.launch {

            val finalInvoice =
                if (invoice.id.isBlank())
                    invoice.copy(id = UUID.randomUUID().toString())
                else invoice

            val linkedItems = items.map {
                it.copy(
                    id = if (it.id.isBlank()) UUID.randomUUID().toString() else it.id,
                    invoiceId = finalInvoice.id
                )
            }

            invoiceDao.upsertInvoiceWithItemsAndPayment(
                finalInvoice,
                linkedItems,
                payment
            )

            // recompute affected products
            val productIds = linkedItems.map { it.productId }
            recomputeProducts(productIds)
        }
    }


    fun editInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        payment: PaymentTransactionEntity?
    ) {
        viewModelScope.launch {

            // fetch OLD items first
            val old = invoiceDao.getInvoiceWithItemsByIdSync(invoice.id)

            val oldProductIds = old?.items?.map { it.productId } ?: emptyList()

            val updatedInvoice = invoice.copy(
                isEdited = true,
                updatedAt = System.currentTimeMillis()
            )

            val linkedItems = items.map {
                it.copy(invoiceId = updatedInvoice.id)
            }

            invoiceDao.upsertInvoiceWithItemsAndPayment(
                updatedInvoice,
                linkedItems,
                payment
            )

            val newProductIds = linkedItems.map { it.productId }

            // recompute BOTH old and new affected products
            recomputeProducts(oldProductIds + newProductIds)
        }
    }

    // payments related
    fun recordStandalonePayment(
        invoiceId: String,
        amount: Double,
        paymentMode: PaymentMode,
        notes: String,
        date: Long
    ) {
        viewModelScope.launch {
            // 1. Fetch the required customerId for this invoice
            val customerId = invoiceDao.getCustomerIdForInvoice(invoiceId) ?: return@launch

            // 2. Create the payment entity
            val payment = PaymentTransactionEntity(
                id = UUID.randomUUID().toString(),
                partyId = customerId,
                documentId = invoiceId,
                type = TransactionType.CREDIT,
                amount = amount,
                paymentMode = paymentMode,
                transactionDate = date,
                notes = notes.takeIf { it.isNotBlank() } ?: "Subsequent payment"
            )

            // 3. Execute the lightweight transaction
            invoiceDao.recordStandalonePayment(payment)
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {

            val old = invoiceDao.getInvoiceWithItemsByIdSync(invoiceId)
            val productIds = old?.items?.map { it.productId } ?: emptyList()

            invoiceDao.deleteInvoiceById(invoiceId)

            recomputeProducts(productIds)
        }
    }


    fun cancelInvoice(invoiceId: String) {
        viewModelScope.launch {

            val old = invoiceDao.getInvoiceWithItemsByIdSync(invoiceId)
            val productIds = old?.items?.map { it.productId } ?: emptyList()

            invoiceDao.updateInvoiceStatus(
                invoiceId = invoiceId,
                status = InvoiceStatus.CANCELLED,
                updatedAt = System.currentTimeMillis()
            )
            recomputeProducts(productIds)
        }
    }


    // In InvoiceViewModel.kt
    suspend fun getInvoiceByIdSync(invoiceId: String): InvoiceWithItems? {
        return invoiceDao.getInvoiceWithItemsByIdSync(invoiceId)
    }

    /**
     * Call this if you want to clear the state (e.g., when exiting the detail screen)
     */
    fun clearSelectedInvoice() {
        _selectedInvoiceWithItems.value = null
    }

    // --- Analytics / Metric Card Logic ---

    data class MetricCardState(
        val valueMonthly: String = "₹ 0",
        val valueAnnually: String = "₹ 0",
        val barHeightsMonthly: List<Float> = List(7) { 0f },
        val barHeightsAnnually: List<Float> = List(7) { 0f }
    )

    private val _metricState = MutableStateFlow(MetricCardState())
    val metricState: StateFlow<MetricCardState> = _metricState.asStateFlow()


    fun fetchMetricData() {
        viewModelScope.launch {
            val now = Calendar.getInstance()

            // 1. Calculate Monthly (Current Month Sum)
            val startOfMonth = now.clone() as Calendar
            startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
            val monthlySum = invoiceDao.getTotalSalesBetween(startOfMonth.timeInMillis, now.timeInMillis) ?: 0.0

            // 2. Calculate Annually (Current Year Sum)
            val startOfYear = now.clone() as Calendar
            startOfYear.set(Calendar.DAY_OF_YEAR, 1)
            startOfYear.set(Calendar.HOUR_OF_DAY, 0)
            val annualSum = invoiceDao.getTotalSalesBetween(startOfYear.timeInMillis, now.timeInMillis) ?: 0.0

            // 3. Generate Actual Bar Heights (Concurrent DB Queries for Optimization)

            // Fetch Last 7 Days (Index 6 is Today, Index 0 is 6 days ago)
            val last7DaysDeferred = (0..6).map { i ->
                async {
                    val calStart = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -(6 - i))
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val calEnd = calStart.clone() as Calendar
                    calEnd.add(Calendar.DAY_OF_YEAR, 1)
                    calEnd.add(Calendar.MILLISECOND, -1)

                    invoiceDao.getTotalSalesBetween(calStart.timeInMillis, calEnd.timeInMillis) ?: 0.0
                }
            }

            // Fetch Last 7 Months (Index 6 is Current Month, Index 0 is 6 months ago)
            val last7MonthsDeferred = (0..6).map { i ->
                async {
                    val calStart = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -(6 - i))
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val calEnd = calStart.clone() as Calendar
                    calEnd.add(Calendar.MONTH, 1)
                    calEnd.add(Calendar.MILLISECOND, -1)

                    invoiceDao.getTotalSalesBetween(calStart.timeInMillis, calEnd.timeInMillis) ?: 0.0
                }
            }

            // Wait for all queries to finish in parallel
            val last7DaysSales = last7DaysDeferred.awaitAll()
            val last7MonthsSales = last7MonthsDeferred.awaitAll()

            // Find max values to normalize the bars between 0.0f and 1.0f
            val maxDaySale = last7DaysSales.maxOrNull()?.takeIf { it > 0 } ?: 1.0
            val maxMonthSale = last7MonthsSales.maxOrNull()?.takeIf { it > 0 } ?: 1.0

            val actualMonthlyBars = last7DaysSales.map { (it / maxDaySale).toFloat() }
            val actualAnnualBars = last7MonthsSales.map { (it / maxMonthSale).toFloat() }

            _metricState.value = MetricCardState(
                valueMonthly = formatCurrencyShort(monthlySum),
                valueAnnually = formatCurrencyShort(annualSum),
                barHeightsMonthly = actualMonthlyBars,
                barHeightsAnnually = actualAnnualBars
            )
        }
    }

    // Helper: Formats 48200.0 -> "₹ 48.2k", 10500000.0 -> "₹ 1.05Cr"
    private fun formatCurrencyShort(amount: Double): String {
        if (amount < 1000) return "₹ ${amount.toInt()}"

        val format = NumberFormat.getInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 1

        return when {
            amount >= 10000000 -> "₹ ${format.format(amount / 10000000)}Cr" // Crores
            amount >= 100000 -> "₹ ${format.format(amount / 100000)}L"     // Lakhs
            amount >= 1000 -> "₹ ${format.format(amount / 1000)}k"         // Thousands
            else -> "₹ ${amount.toInt()}"
        }
    }


    suspend fun getB2bInvoices(
        start: Long,
        end: Long
    ): List<B2bInvoice> {

        return invoiceDao.getB2bInvoiceRows(start, end)
            .map {

                B2bInvoice(
                    id = it.id,
                    gstin = it.gstNumber ?: "",
                    invoiceNo = it.invoiceNumber,
                    date = SimpleDateFormat("dd-MM-yyyy", Locale("en", "IN"))
                        .format(Date(it.invoiceDate)),
                    totalVal = it.grandTotal,
                    taxableVal = it.totalTaxableAmount,
                    igst = it.igstAmount,
                    cgst = it.cgstAmount,
                    sgst = it.sgstAmount
                )
            }
    }

    suspend fun getHsnSummary(
        start: Long,
        end: Long
    ): List<HsnSummary> {
        return invoiceDao.getHsnSummary(start, end)
            .map {
                HsnSummary(
                    id = UUID.randomUUID().toString(),
                    hsnCode = it.hsnCode,
                    uqc = it.uqc,                  // Use real UQC from DB
                    totalQty = it.totalQty,
                    taxableVal = it.totalTaxableValue,
                    rate = it.taxRate,             // Use real Tax Rate from DB
                    igst = it.igst,
                    cgst = it.cgst,
                    sgst = it.sgst
                )
            }
    }

    private suspend fun recomputeProducts(productIds: List<String>) {
        productIds.distinct().forEach { productId ->
            val product = productDao.getById(productId) ?: return@forEach

            val usages = invoiceDao.getInvoicesForProductSync(productId)
                .filter { it.invoiceStatus != InvoiceStatus.CANCELLED }

            if (usages.isEmpty()) {
                productDao.update(
                    product.copy(
                        lastSellingPrice = 0.0,
                        peakPrice = 0.0,
                        floorPrice = 0.0,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                return@forEach
            }

            var peak = Double.MIN_VALUE
            var floor = Double.MAX_VALUE
            var lastPrice = 0.0

            usages.sortedBy { it.invoiceDate }.forEach { usage ->
                val price = usage.sellingPrice
                peak = maxOf(peak, price)
                floor = minOf(floor, price)
                lastPrice = price
            }

            productDao.update(
                product.copy(
                    lastSellingPrice = lastPrice,
                    peakPrice = peak,
                    floorPrice = floor,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }


    fun generatePdf(invoice: InvoiceWithItems, logoUri: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pdfUri.value = pdfRepo.generate(invoice, logoUri)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearPdf() {
        _pdfUri.value = null
    }


}


class InvoiceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            val app = context.applicationContext as Application
            val db = AppDatabase.get(context)
            val dao = db.invoiceDao()
            val productDao = db.productDao()
            return InvoiceViewModel(app, dao, productDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}