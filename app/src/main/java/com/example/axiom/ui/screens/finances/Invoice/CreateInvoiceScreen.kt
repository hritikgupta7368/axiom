package com.example.axiom.ui.screens.finances.Invoice

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.CreateInvoiceViewModel
import com.example.axiom.data.finances.CreateInvoiceViewModelFactory
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.GstBreakdown
import com.example.axiom.data.finances.Invoice
import com.example.axiom.data.finances.InvoiceItem
import com.example.axiom.data.finances.InvoiceStatus
import com.example.axiom.data.finances.Product
import com.example.axiom.data.finances.SupplyType
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.data.finances.dataStore.SelectedSellerPref
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.dialog.AppDialog
import com.example.axiom.ui.navigation.InvoiceFormMode
import com.example.axiom.ui.screens.finances.product.UnitSelectionDialog
import com.example.axiom.ui.utils.numberToWords
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.roundToLong


// Round to exactly 2 decimal places using banker's rounding (standard for money)
fun Double.toMoney(): Double {
    return (this * 100).roundToLong() / 100.0
}

// Safe parse from text input (TextField)
fun String?.toSafeMoney(default: Double = 0.0): Double {
    if (this.isNullOrBlank()) return default
    return this.trim()
        .replace(",", "")           // tolerate 1,234.56
        .toDoubleOrNull()
        ?.toMoney()
        ?: default
}

// For display in Text / labels
fun Double.formatMoney(): String = String.format("%.2f", this)
// or locale-aware: "₹ ${"%,.2f".format(this)}" if you prefer

// --- Colors from your React Native Design ---
val BgDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF1C1D27)
val BorderDark = Color(0xFF334155)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF94A3B8)
val PrimaryBlue = Color(0xFF3B82F6)


enum class SheetType { NONE, CUSTOMER, PRODUCT }
enum class ProductSheetMode { LIST, CREATE }
enum class PaymentMode { CASH, UPI, CHEQUE, BANK_TRANSFER }
enum class PaymentStatus { UNPAID, PARTIAL, PAID }

private const val DEFAULT_GST_RATE = 0.18
private const val INVOICE_NUMBER_MIN = 1L
private const val INVOICE_NUMBER_MAX = 99999999L  // reasonable upper limit
private const val INVOICE_NUMBER_PADDING = 3

private fun Long.toPaddedInvoiceNumber(): String {
    return if (this in INVOICE_NUMBER_MIN..999L) {
        "%0${INVOICE_NUMBER_PADDING}d".format(this)   // 001 to 999
    } else {
        this.toString()                               // 1000+
    }
}

// ────────────────────────────────────────────────
// GST Calculation
// ────────────────────────────────────────────────

fun calculateGst(taxableAmount: Double, supplyType: SupplyType): GstBreakdown {
    val amt = taxableAmount.toMoney()
    if (amt <= 0.0) return GstBreakdown()

    val rate = DEFAULT_GST_RATE

    return when (supplyType) {
        SupplyType.INTRA_STATE -> {
            val halfRate = rate / 2
            val halfAmount = (taxableAmount * halfRate).toMoney()
            GstBreakdown(
                cgstRate = halfRate * 100,
                sgstRate = halfRate * 100,
                cgstAmount = halfAmount,
                sgstAmount = halfAmount,
                totalTax = (halfAmount * 2).toMoney()
            )
        }

        SupplyType.INTER_STATE -> {
            val igst = (taxableAmount * rate).toMoney()
            GstBreakdown(
                igstRate = rate * 100,
                igstAmount = igst,
                totalTax = igst
            )
        }

        else -> GstBreakdown()
    }
}

fun resolveSupplyType(
    sellerStateCode: String?,
    customerStateCode: String?
): SupplyType = when {
    sellerStateCode.isNullOrBlank() || customerStateCode.isNullOrBlank() -> SupplyType.INTER_STATE
    sellerStateCode == customerStateCode -> SupplyType.INTRA_STATE
    else -> SupplyType.INTER_STATE
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    mode: InvoiceFormMode,
    onBack: () -> Unit,
    onInvoicePreview: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CreateInvoiceViewModel = viewModel(
        factory = CreateInvoiceViewModelFactory(context)
    )
    val scope = rememberCoroutineScope()

    // Collected flows
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val products by viewModel.products.collectAsState(initial = emptyList())
    val invoiceById by viewModel.invoiceById.collectAsState()

    val financePreferences = remember { FinancePreferences(context) }
    val lastInvoiceNo by financePreferences.lastInvoiceNumber.collectAsState(initial = 0L)
    val selectedSeller by financePreferences.selectedSeller.collectAsState(
        initial = SelectedSellerPref(null, null, null)
    )

    // ─── Invoice number logic ───────────────────────────────
    var invoiceNo by remember { mutableStateOf("") }
    var suggestedInvoiceNo by remember { mutableStateOf("") }
    var userEditedInvoiceNo by remember { mutableStateOf(false) }

    LaunchedEffect(lastInvoiceNo) {
        val nextNumber = (lastInvoiceNo + 1)
            .coerceIn(INVOICE_NUMBER_MIN, INVOICE_NUMBER_MAX)
        val padded = nextNumber.toPaddedInvoiceNumber()
        suggestedInvoiceNo = padded

        if (!userEditedInvoiceNo) {
            invoiceNo = padded
        }
    }

    // ─── Core invoice state ────────────────────────────────
    val invoiceId = remember(mode) {
        when (mode) {
            is InvoiceFormMode.Create -> UUID.randomUUID().toString()
            is InvoiceFormMode.Edit -> mode.invoiceId
        }
    }

    var selectedCustomer by remember { mutableStateOf<CustomerFirm?>(null) }
    val invoiceItems = remember { mutableStateListOf<InvoiceItem>() }

    var shippingCharges by remember { mutableDoubleStateOf(0.0.toMoney()) }
    var shippedTo by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }

    var isRoundOffEnabled by remember { mutableStateOf(true) }
    var isInvoiceNoEditable by remember { mutableStateOf(false) }

    // Payment related (you can expand later)
    var paymentMode by remember { mutableStateOf<PaymentMode?>(null) }
    var paymentStatus by remember { mutableStateOf(PaymentStatus.UNPAID) }
    var receivedAmount by remember { mutableDoubleStateOf(0.0.toMoney()) }
    var paymentDateMillis by remember { mutableStateOf<Long?>(null) }

    // ─── Date picker ───────────────────────────────────────
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = when (mode) {
            is InvoiceFormMode.Edit -> invoiceById?.date?.toLongOrNull()
            else -> System.currentTimeMillis()
        } ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(selectedDateMillis))
    }

    // ─── Business logic ────────────────────────────────────
    val sellerStateCode = selectedSeller.stateCode
    val supplyType = resolveSupplyType(sellerStateCode, selectedCustomer?.stateCode)

    val itemsTotal = invoiceItems.sumOf { it.total }.toMoney()
    val taxableAmount = (itemsTotal + shippingCharges).toMoney()

    val gstBreakdown = calculateGst(taxableAmount, supplyType)

    val totalBeforeTax = taxableAmount
    val totalWithTax = (totalBeforeTax + gstBreakdown.totalTax).toMoney()

    val totalAmount = if (isRoundOffEnabled) {
        totalWithTax.roundToInt().toDouble()
    } else {
        totalWithTax
    }

    val gst = calculateGst(
        taxableAmount = taxableAmount,
        supplyType = supplyType
    )
    val roundOffDifference = totalAmount - totalWithTax


    // ─── Load existing invoice (edit mode) ─────────────────
    LaunchedEffect(mode) {
        if (mode is InvoiceFormMode.Edit) {
            viewModel.getInvoiceById(mode.invoiceId)
        }
    }

    LaunchedEffect(invoiceById) {
        val invoice = invoiceById ?: return@LaunchedEffect

        invoiceNo = invoice.invoiceNo
        userEditedInvoiceNo = true

        selectedCustomer = invoice.customerDetails
        vehicleNumber = invoice.vehicleNumber.orEmpty()
        shippingCharges = invoice.shippingCharge ?: 0.0
        shippedTo = invoice.shippedTo.orEmpty()
        isRoundOffEnabled = true   // or load from DB if you store it

        invoiceItems.clear()
        invoiceItems.addAll(invoice.items)
    }

    // ─── Bottom sheets control ─────────────────────────────
    var activeSheet by remember { mutableStateOf(SheetType.NONE) }
    var productSheetMode by remember { mutableStateOf(ProductSheetMode.LIST) }

    // ─── Dialog control ─────────────────────────────
    var activeDialog by remember { mutableStateOf(false) }


    val savedSellerId by financePreferences.selectedSellerFirmId.collectAsState(initial = null)
    val savedSellerName by financePreferences.selectedSellerFirmName.collectAsState(initial = null)


    // ─── Main generation logic ─────────────────────────────

    fun canGenerateInvoice(): Boolean = when {
        selectedSeller.id == null -> false
        selectedCustomer == null -> false
        invoiceItems.isEmpty() -> false
        invoiceNo.isBlank() -> false
        else -> true
    }

    fun generateInvoice() {

        if (!canGenerateInvoice()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_LONG).show()
            return
        }

        val invoice = Invoice(
            id = invoiceId,
            invoiceNo = invoiceNo.trim(),
            date = selectedDateMillis.toString(),
            sellerId = selectedSeller.id?.toString() ?: return,
            customerDetails = selectedCustomer ?: return,
            supplyType = supplyType,
            vehicleNumber = vehicleNumber.trim(),
            items = invoiceItems.toList(),
            shippingCharge = shippingCharges.takeIf { it > 0 },
            shippedTo = shippedTo.trim(),
            totalBeforeTax = totalBeforeTax,
            gst = gstBreakdown,
            totalAmount = totalAmount,
            amountInWords = numberToWords(totalAmount),
            status = InvoiceStatus.FINAL
            // payment fields can be added here when implemented
        )

        scope.launch {
            when (mode) {
                is InvoiceFormMode.Create -> {
                    viewModel.insertInvoice(invoice)

                    // Only increment if user kept the auto-suggested value
                    // (this avoids incrementing when user typed custom like "2025-001")
                    val wasAutoUsed = !userEditedInvoiceNo &&
                            invoiceNo.trim() == suggestedInvoiceNo

                    if (wasAutoUsed) {
                        val nextNumeric = (lastInvoiceNo + 1)
                            .coerceIn(INVOICE_NUMBER_MIN, INVOICE_NUMBER_MAX)
                        financePreferences.saveLastInvoiceNumber(nextNumeric)
                    }
                }

                is InvoiceFormMode.Edit -> {
                    viewModel.updateInvoice(invoice)
                }
            }

            // increment ONLY if auto-suggested invoice number was used
            val autoNumberUsed =
                !userEditedInvoiceNo && invoiceNo == (lastInvoiceNo + 1).toString()

            if (autoNumberUsed) {
                financePreferences.saveLastInvoiceNumber(lastInvoiceNo + 1)
            }

            Toast.makeText(
                context,
                "Invoice generated successfully",
                Toast.LENGTH_LONG
            ).show()

            delay(400) // UX stability
            invoiceNo = ""
            suggestedInvoiceNo = ""
            userEditedInvoiceNo = false
            onInvoicePreview(invoiceId)
        }
    }




    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (mode is InvoiceFormMode.Edit) "Edit Invoice" else "New GST Invoice",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    if (mode is InvoiceFormMode.Create) {
                        TextButton(onClick = { /* Save Draft */ }) {
                            Text(
                                "Save as Draft",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        bottomBar = {
            BottomActionSection(
                totalAmount = "₹ ${String.format("%.2f", totalAmount)}",
                onGenerate = { generateInvoice() },
                mode = mode
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 100.dp), // Extra padding for bottom bar
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Invoice Details Section ---
                SectionGroup(title = "Invoice Details") {
                    // Invoice No
                    CustomTextField(
                        value = invoiceNo,
                        onValueChange = {
                            invoiceNo = it
                            userEditedInvoiceNo = true
                        },
                        placeholder = "001",
                        enabled = isInvoiceNoEditable,
                        highlightWhenEditable = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                isInvoiceNoEditable = !isInvoiceNoEditable
                            }) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = if (isInvoiceNoEditable) PrimaryBlue else TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                }

                // --- Date & Customer Row ---
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Date",
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        CustomTextField(
                            value = formattedDate,
                            onValueChange = {},
                            enabled = false, // Read only, click triggers picker
                            trailingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    null,
                                    tint = TextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { showDatePicker = true }
                        )
                    }

                    // Customer
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Customer",
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        CustomTextField(
                            value = selectedCustomer?.name ?: "",
                            onValueChange = {},
                            placeholder = "Select Customer",
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    null,
                                    tint = TextGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            onClick = { activeSheet = SheetType.CUSTOMER }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // --- Products Section ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Products",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Badge(
                            containerColor = PrimaryBlue.copy(alpha = 0.2f),
                            contentColor = PrimaryBlue
                        ) {
                            Text("${invoiceItems.size} Items", modifier = Modifier.padding(4.dp))
                        }
                    }

                    // Product List
                    invoiceItems.forEachIndexed { index, item ->
                        ProductItemCard(
                            item = item,
                            onDelete = { invoiceItems.remove(item) },

                            onQtyChange = { newQty ->
                                val updated = item.copy(
                                    quantity = newQty,
                                    total = newQty * item.price
                                )
                                invoiceItems[index] = updated
                            },
                            onPriceChange = { newPrice ->
                                val updated = item.copy(
                                    price = newPrice,
                                    total = newPrice * item.quantity
                                )
                                invoiceItems[index] = updated
                            }
                        )
                    }

                    // Add Product Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Color(0xFF475569),
                                RoundedCornerShape(12.dp)
                            ) // Dashed border simulated
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeSheet = SheetType.PRODUCT }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Add Product",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // --- Additional Charges ---
                SectionGroup(title = "Additional Charges") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Delivery
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Delivery", color = TextGray, fontSize = 12.sp)
                            CustomTextField(
                                value = if (shippingCharges == 0.0) "" else shippingCharges.toString(),
                                onValueChange = { shippingCharges = it.toDoubleOrNull() ?: 0.0 },
                                placeholder = "0.00",
                                prefix = {
                                    Text(
                                        "₹ ",
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        // Extra Charges
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Extra Charges", color = TextGray, fontSize = 12.sp)
                            CustomTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = "0.00",
                                prefix = {
                                    Text(
                                        "Rs ",
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }


                // --- Tax Breakdown ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(Color(0xFF1F2937))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            "TAX BREAKDOWN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )

                        // Base rows
                        TaxRow(
                            label = "Items Total",
                            value = "₹ ${"%.2f".format(itemsTotal)}"
                        )

                        TaxRow(
                            label = "Shipping Charges",
                            value = "₹ ${"%.2f".format(shippingCharges)}"
                        )

                        HorizontalDivider(color = BorderDark)

                        TaxRow(
                            label = "Taxable Amount",
                            value = "₹ ${"%.2f".format(taxableAmount)}",
                            isTotal = true
                        )

                        HorizontalDivider(color = BorderDark)

                        // GST rows
                        if (supplyType == SupplyType.INTRA_STATE) {

                            TaxRow(
                                label = "CGST (${gst.cgstRate.toInt()}%)",
                                value = "₹ ${"%.2f".format(gst.cgstAmount)}"
                            )

                            TaxRow(
                                label = "SGST (${gst.sgstRate.toInt()}%)",
                                value = "₹ ${"%.2f".format(gst.sgstAmount)}"
                            )

                        } else {

                            TaxRow(
                                label = "IGST (${gst.igstRate.toInt()}%)",
                                value = "₹ ${"%.2f".format(gst.igstAmount)}"
                            )
                        }

                        HorizontalDivider(color = BorderDark)

                        // Round Off Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Round Off",
                                color = TextGray,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRoundOffEnabled && roundOffDifference != 0.0) {
                                    Text(
                                        text = "${if (roundOffDifference > 0) "+" else ""}${
                                            "%.2f".format(
                                                roundOffDifference
                                            )
                                        }",
                                        color = if (roundOffDifference != 0.0) PrimaryBlue else TextGray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Switch(
                                    checked = isRoundOffEnabled,
                                    onCheckedChange = { isRoundOffEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TextWhite,
                                        checkedTrackColor = PrimaryBlue,
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = SurfaceDark,
                                        uncheckedBorderColor = BorderDark
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        HorizontalDivider(color = BorderDark)

                        TaxRow(
                            label = "Total Amount",
                            value = "₹ ${"%.2f".format(totalAmount)}",
                            isTotal = true
                        )
                    }
                }


                // --- Optional Triggers ---
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                ) {
                    SettingItem(icon = Icons.Outlined.AccountBox, title = "Bank Details")
                    HorizontalDivider(color = BorderDark)
                    SettingItem(
                        icon = Icons.Outlined.Edit,
                        title = "Select Signature",
                        badge = "Selected"
                    )
                    HorizontalDivider(color = BorderDark)
                    SettingItem(icon = Icons.Outlined.Info, title = "Notes & Terms")
                    HorizontalDivider(color = BorderDark)
                    SettingItem(
                        icon = Icons.Outlined.PlayArrow,
                        title = "Shipping Address",
                        subtitle = shippedTo.takeIf { it.isNotBlank() },
                        badge = if (shippedTo.isBlank()) "Add" else null,
                        onClick = { activeDialog = true },
                        onClear = if (shippedTo.isNotBlank()) {
                            { shippedTo = "" }
                        } else null
                    )


                }

                //payment
                SectionGroup(title = "Payment") {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    ) {
                        SettingItem(
                            icon = Icons.Outlined.CheckCircle,
                            title = "Payment Status",
                            subtitle = paymentStatus.name.replace("_", " "),
                            onClick = {
                                paymentStatus = when (paymentStatus) {
                                    PaymentStatus.UNPAID -> PaymentStatus.PARTIAL
                                    PaymentStatus.PARTIAL -> PaymentStatus.PAID
                                    PaymentStatus.PAID -> PaymentStatus.UNPAID
                                }
                            }
                        )

                        HorizontalDivider(color = BorderDark)

                        // Payment Mode
                        SettingItem(
                            icon = Icons.Outlined.Email,
                            title = "Payment Mode",
                            subtitle = paymentMode?.name ?: "Select",
                            badge = if (paymentMode == null) "Add" else null,
                            onClick = {
                                // open dialog / bottom sheet later
                            }
                        )

                        HorizontalDivider(color = BorderDark)

                        // Received Amount
                        SettingItem(
                            icon = Icons.Outlined.ShoppingCart,
                            title = "Amount Received",
                            subtitle = if (receivedAmount > 0) "₹ ${"%.2f".format(receivedAmount)}" else null,
                            badge = if (receivedAmount == 0.0) "Add" else null,
                            onClick = {
                                // open input dialog
                            }
                        )

                        HorizontalDivider(color = BorderDark)

                        // Payment Date
                        SettingItem(
                            icon = Icons.Outlined.DateRange,
                            title = "Payment Date",
                            subtitle = paymentDateMillis?.let {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(Date(it))
                            },
                            badge = if (paymentDateMillis == null) "Add" else null,
                            onClick = {
                                // date picker later
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Bottom Sheets ---
    AppBottomSheet(
        showSheet = activeSheet != SheetType.NONE,
        onDismiss = {
            activeSheet = SheetType.NONE
            productSheetMode = ProductSheetMode.LIST
        }
    ) {
        when (activeSheet) {
            SheetType.CUSTOMER -> {
                SelectionSheetContent(
                    title = "Select Customer",
                    items = customers,
                    searchPredicate = { c, q ->
                        c.name.contains(q, true) || (c.gstin?.contains(q, true) == true)
                    },
                    rowContent = { CustomerRow(it) },
                    onSelect = {
                        selectedCustomer = it
                        activeSheet = SheetType.NONE
                    }
                )

            }

            SheetType.PRODUCT -> {
                AnimatedContent(
                    targetState = productSheetMode,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "ProductSheetAnimation"
                ) { mode ->
                    when (mode) {

                        ProductSheetMode.LIST -> {
                            ProductListSheet(
                                products = products,
                                onAddClick = {
                                    productSheetMode = ProductSheetMode.CREATE
                                },
                                onSelect = { product ->
                                    invoiceItems.add(
                                        InvoiceItem(
                                            id = UUID.randomUUID().toString(),
                                            productId = product.id,
                                            name = product.name,
                                            unit = product.unit,
                                            price = product.sellingPrice,
                                            quantity = 1.0,
                                            hsn = product.hsn,
                                            total = product.sellingPrice
                                        )
                                    )
                                    activeSheet = SheetType.NONE
                                }
                            )
                        }

                        ProductSheetMode.CREATE -> {
                            CreateProductSheet(
                                onCreate = { newProduct ->
                                    viewModel.insertProduct(newProduct)
                                    productSheetMode = ProductSheetMode.LIST
                                },
                                onBack = {
                                    productSheetMode = ProductSheetMode.LIST
                                }
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
    // for shipped to input
    AppDialog(
        show = activeDialog,
        title = "Enter Shipping Address",
        message = "Provide the shipping address for the invoice.",
        showInput = true,
        inputLabel = "Shipping Address",
        confirmText = "Add",
        onConfirm = { address ->
            shippedTo = address.orEmpty()
            activeDialog = false
        },
        onDismiss = { activeDialog = false }
    )

}

@Composable
fun ProductListSheet(
    products: List<Product>,
    onAddClick: () -> Unit,
    onSelect: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredProducts = remember(query, products) {
        if (query.isBlank()) products
        else products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.hsn.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Select Product", fontWeight = FontWeight.Bold)

            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search product or HSN") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            }
        )

        LazyColumn {
            items(filteredProducts) { product ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(product) }
                        .padding(vertical = 8.dp)
                ) {
                    ProductCard(product)
                }
            }
        }
    }
}


@Composable
fun CreateProductSheet(
    onCreate: (Product) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hsn by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    var showUnitDialog by remember { mutableStateOf(false) }

    if (showUnitDialog) {
        UnitSelectionDialog(
            onDismiss = { showUnitDialog = false },
            onUnitSelected = {
                unit = it
                showUnitDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.85f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(
                "Add New Product",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // HSN
        OutlinedTextField(
            value = hsn,
            onValueChange = { hsn = it },
            label = { Text("HSN Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Price
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Selling Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Unit (clickable)
        Box {
            OutlinedTextField(
                value = unit,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit") },
                placeholder = { Text("Select Unit") },
                leadingIcon = { Icon(Icons.Default.Info, null) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showUnitDialog = true }
            )
        }

        // Category (optional)
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onCreate(
                        Product(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            hsn = hsn,
                            sellingPrice = price.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            category = category,
                            active = true,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }
        }
    }
}


// --- Components ---

@Composable
fun <T> SelectionSheetContent(
    title: String,
    items: List<T>,
    searchPredicate: (T, String) -> Boolean,
    rowContent: @Composable (T) -> Unit,
    onSelect: (T) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredItems = remember(query, items) {
        if (query.isBlank()) items
        else items.filter { searchPredicate(it, query) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp)
            .padding(16.dp)
    ) {

        // Title
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // Content
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No results", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                    ) {
                        rowContent(item)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerRow(customer: CustomerFirm) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            customer.name,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        customer.gstin?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                it,
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
    Divider()
}

@Composable
fun ProductCard(product: Product) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
//        border = BorderStroke(1.dp, BorderDark),
        color = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("HSN: ${product.hsn}", fontSize = 12.sp, color = TextGray)
                Text("Unit: ${product.unit}", fontSize = 12.sp, color = TextGray)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "₹${"%.2f".format(product.sellingPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
fun SectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            color = TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    highlightWhenEditable: Boolean = false
) {

    val borderColor = if (highlightWhenEditable && enabled) PrimaryBlue else BorderDark

    val backgroundColor =
        if (highlightWhenEditable && enabled)
            PrimaryBlue.copy(alpha = 0.08f)
        else
            SurfaceDark


    val containerModifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 16.dp)

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix != null) {
            prefix()
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled && onClick == null, // Disable editing if it's a click-action field
            textStyle = TextStyle(
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(placeholder, color = TextGray, fontSize = 16.sp)
                }
                innerTextField()
            }
        )
        if (trailingIcon != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                trailingIcon()
            }
        }
    }
}


@Composable
fun ProductItemCard(
    item: InvoiceItem,
    onDelete: () -> Unit,
    onQtyChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    isDark: Boolean = true
) {
    val TextSlate400 = Color(0xFF94A3B8)
    val TextSlate500 = Color(0xFF64748B)
    val TextSlate900 = Color(0xFF0F172A)
    val PrimaryBlue = Color(0xFF3B82F6)
    val BlueBg = Color(0xFF1E3A8A).copy(alpha = 0.1f)
    val BlueRing = Color(0xFF3B82F6).copy(alpha = 0.2f)
    val InputBg = Color(0xFF15161E)
    val InputBorder = Color.White.copy(alpha = 0.05f)
    val GrayBg = Color(0xFFF9FAFB)
    val GrayBorder = Color(0xFFF3F4F6)

    val surfaceColor = if (isDark) SurfaceDark else Color.White
    val borderColor = if (isDark) BorderDark else GrayBorder
    val textPrimary = if (isDark) TextWhite else TextSlate900
    val textSecondary = if (isDark) TextSlate500 else TextSlate400
    val inputBackground = if (isDark) InputBg else GrayBg
    val inputBorderColor = if (isDark) InputBorder else GrayBorder
    val iconBg = if (isDark) BlueBg else Color(0xFFDEEBFF)
    val iconRing = if (isDark) BlueRing else Color(0xFFBFDBFE)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                        .border(1.dp, iconRing, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart, // Replace with web icon
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title and Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.name,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.hsn,
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Input
            Row(
                modifier = Modifier
                    .weight(1.3f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(inputBackground)
                    .border(1.dp, inputBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qty",
                    color = TextSlate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                var qtyText by remember { mutableStateOf(item.quantity.toInt().toString()) }

                BasicTextField(
                    value = qtyText,
                    onValueChange = {
                        qtyText = it
                        it.toDoubleOrNull()?.takeIf { q -> q > 0 }?.let(onQtyChange)
                    },
                    textStyle = TextStyle(
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds()

                )


            }

            // Rate Input
            Row(
                modifier = Modifier
                    .weight(1.5f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(inputBackground)
                    .border(1.dp, inputBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rate",
                    color = TextSlate400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 0.dp)
                )

                var priceText by remember {
                    mutableStateOf(
                        if (item.price == 0.0) "" else "%.2f".format(
                            item.price
                        )
                    )
                }

                BasicTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        it.toDoubleOrNull()?.takeIf { p -> p >= 0 }?.let(onPriceChange)
                    },
                    textStyle = TextStyle(
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds(),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            inner()
                        }
                    }
                )
            }

            // Total Display
            Column(
                modifier = Modifier
                    .weight(1.2f)

                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TOTAL",
                    color = TextSlate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${"Rs %.2f".format(item.total)}",
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun TaxRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (isTotal) Color(0xFFCBD5E1) else TextGray,
            fontSize = 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = if (isTotal) TextWhite else TextGray,
            fontSize = 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null)
                    Modifier.clickable(onClick = onClick)
                else
                    Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(20.dp))
            Column {
                Text(title, color = TextWhite, fontSize = 14.sp)
                subtitle?.let {
                    Text(
                        it,
                        color = TextGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                badge != null -> {
                    Text(
                        badge,
                        color = PrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                onClear != null || subtitle != null -> {
                    // no arrow when value exists
                }

                else -> {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        null,
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }


            if (onClear != null) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Clear",
                    tint = TextGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onClear)
                )
            }
        }

    }
}

@Composable
fun BottomActionSection(totalAmount: String, onGenerate: () -> Unit, mode: InvoiceFormMode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
            .padding(bottom = 20.dp) // Handle system nav bar
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Amount", color = TextGray, fontSize = 12.sp)
                Text(totalAmount, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }


            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (mode is InvoiceFormMode.Edit) "Update" else "Generate", color = TextWhite)
            }

        }
    }
}