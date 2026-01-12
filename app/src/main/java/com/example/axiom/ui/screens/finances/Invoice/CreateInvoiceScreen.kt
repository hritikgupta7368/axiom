package com.example.axiom.ui.screens.finances.Invoice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.data.finances.domain.*
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.ui.screens.finances.FinancesViewModel
import com.example.axiom.ui.screens.finances.FinancesViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.ui.text.style.TextAlign
import com.example.axiom.MainActivity
import com.example.axiom.data.finances.dataStore.SelectedSellerPref

import com.example.axiom.ui.utils.NetworkMonitor
import com.example.axiom.ui.utils.numberToWords
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// --- Colors from your React Native Design ---
val BgDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF1C1D27)
val BorderDark = Color(0xFF334155)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF94A3B8)
val PrimaryBlue = Color(0xFF3B82F6)


enum class SheetType {
    NONE, CUSTOMER, PRODUCT
}

fun calculateGst(
    taxableAmount: Double,
    supplyType: SupplyType
): GstBreakdown {
    val rate = 0.18

    return if (supplyType == SupplyType.INTRA_STATE) {
        val halfRate = rate / 2
        val cgst = taxableAmount * halfRate
        val sgst = taxableAmount * halfRate

        GstBreakdown(
            cgstRate = halfRate * 100,
            sgstRate = halfRate * 100,
            cgstAmount = cgst,
            sgstAmount = sgst,
            totalTax = cgst + sgst
        )
    } else {
        val igst = taxableAmount * rate

        GstBreakdown(
            igstRate = rate * 100,
            igstAmount = igst,
            totalTax = igst
        )
    }
}

fun resolveSupplyType(
    sellerStateCode: String?,
    customerStateCode: String?
): SupplyType {
    return if (
        !sellerStateCode.isNullOrBlank() &&
        !customerStateCode.isNullOrBlank() &&
        sellerStateCode == customerStateCode
    ) {
        SupplyType.INTRA_STATE
    } else {
        SupplyType.INTER_STATE
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(onBack: () -> Unit, onInvoicePreview: (String) -> Unit) {
    val context = LocalContext.current
    val viewModel: FinancesViewModel = viewModel(factory = FinancesViewModelFactory())


    val networkMonitor = remember { NetworkMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()
    val invoiceId = remember { UUID.randomUUID().toString() }

    val isCreating by viewModel.isCreatingInvoice.collectAsState()


    // DataStore Integration
    val financePreferences = remember { FinancePreferences(context) }
    val lastInvoiceNo by financePreferences.lastInvoiceNumber.collectAsState(initial = 0L)



    var invoiceNo by remember { mutableStateOf("") }
    var suggestedInvoiceNo by remember { mutableStateOf("") }
    var userEditedInvoiceNo by remember { mutableStateOf(false) }

    LaunchedEffect(lastInvoiceNo) {
        val next = (lastInvoiceNo + 1).toString()
        suggestedInvoiceNo = next

        if (!userEditedInvoiceNo) {
            invoiceNo = next
        }
    }


    val selectedSeller by financePreferences.selectedSeller
        .collectAsState(initial = SelectedSellerPref(null, null, null))
    val sellerStateCode = selectedSeller.stateCode

    val savedSellerId by financePreferences.selectedSellerFirmId.collectAsState(initial = null)
    val savedSellerName by financePreferences.selectedSellerFirmName.collectAsState(initial = null)


    val scope = rememberCoroutineScope()
    
    // Fetched Data
    val customers by viewModel.customerFirms.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    // State

    var isInvoiceNoEditable by remember { mutableStateOf(false) }
    var isRoundOffEnabled by remember { mutableStateOf(true) }



    // Date Picker State
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(selectedDateMillis))
    }


    var selectedCustomer by remember { mutableStateOf<CustomerFirm?>(null) }
    
    // Placeholder logic for supplyType until seller stateCode is fully integrated
    val supplyType = resolveSupplyType(sellerStateCode , selectedCustomer?.stateCode)


    var shippingCharges by remember { mutableStateOf(0.0) }


    // Bottom Sheet State
    var activeSheet by remember { mutableStateOf(SheetType.NONE) }

    // Items
    val invoiceItems = remember { mutableStateListOf<InvoiceItem>() }


    val itemsTotal = invoiceItems.sumOf { it.total }
    val taxableAmount = itemsTotal + shippingCharges

    val gst = calculateGst(
        taxableAmount = taxableAmount,
        supplyType = supplyType
    )

    val totalBeforeTax = taxableAmount
    val totalWithTax = totalBeforeTax + gst.totalTax
    
    val totalAmount = if (isRoundOffEnabled) {
        totalWithTax.roundToInt().toDouble()
    } else {
        totalWithTax
    }
    
    val roundOffDifference = totalAmount - totalWithTax

    val invoiceStatus = InvoiceStatus.FINAL

    fun generateInvoice() {

        if (selectedSeller.id == null || selectedCustomer == null || invoiceItems.isEmpty()) {
            Toast.makeText(context, "Missing required fields", Toast.LENGTH_SHORT).show()
            return
        }
//        isLocked = true
        val invoice = Invoice(
            id = invoiceId,
            invoiceNo = invoiceNo,
            date = selectedDateMillis.toString(),
            sellerId = selectedSeller.id.toString(),
            customerDetails = selectedCustomer,
            supplyType = supplyType,
            items = invoiceItems.toList(),
            totalBeforeTax = totalBeforeTax,
            gst = gst,
            totalAmount = totalAmount,
            amountInWords = numberToWords(totalAmount),
            status = invoiceStatus
        )

        // OFFLINE CASE
        if (!isOnline) {
            Toast.makeText(
                context,
                "Offline. Invoice will sync when internet is available.",
                Toast.LENGTH_LONG
            ).show()
        }

        // SINGLE SOURCE OF TRUTH: always create
        viewModel.addInvoice(invoice) {
            scope.launch {
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

    }


    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("New GST Invoice", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    TextButton(onClick = { /* Save Draft */ }) {
                        Text("Save as Draft", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        bottomBar = {
            BottomActionSection(
                totalAmount = "₹ ${String.format("%.2f", totalAmount)}",
                enabled = !isCreating,
                onGenerate = {generateInvoice()}
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
                        trailingIcon = {
                            IconButton(onClick = { isInvoiceNoEditable = !isInvoiceNoEditable }) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                }

                // --- Date & Customer Row ---
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Date", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        CustomTextField(
                            value = formattedDate,
                            onValueChange = {},
                            enabled = false, // Read only, click triggers picker
                            trailingIcon = { Icon(Icons.Default.DateRange, null, tint = TextGray, modifier = Modifier.size(18.dp)) },
                            onClick = { showDatePicker = true }
                        )
                    }

                    // Customer
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Customer", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        CustomTextField(
                            value = selectedCustomer?.name ?: "",
                            onValueChange = {},
                            placeholder = "Select Customer",
                            enabled = false,
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray, modifier = Modifier.size(22.dp)) },
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
                        Text("Products", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Badge(containerColor = PrimaryBlue.copy(alpha = 0.2f), contentColor = PrimaryBlue) {
                            Text("${invoiceItems.size} Items", modifier = Modifier.padding(4.dp))
                        }
                    }

                    // Product List
                    invoiceItems.forEachIndexed { index, item ->
                        ProductItemCard(
                            item = item, 
                            onDelete = { invoiceItems.remove(item) },
                            onQtyChange = { newQty ->
                                if (newQty > 0) {
                                    val updatedItem = item.copy(
                                        quantity = newQty, 
                                        total = newQty * item.price
                                    )
                                    invoiceItems[index] = updatedItem
                                }
                            }
                        )
                    }

                    // Add Product Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp)) // Dashed border simulated
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeSheet = SheetType.PRODUCT }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Text("Add Product", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // --- Additional Charges ---
                SectionGroup(title = "Additional Charges") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Delivery
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Delivery", color = TextGray, fontSize = 12.sp)
                            CustomTextField(
                                value =if (shippingCharges == 0.0) "" else shippingCharges.toString(),
                                onValueChange = { shippingCharges = it.toDoubleOrNull() ?: 0.0 },
                                placeholder = "0.00",
                                prefix = { Text("₹ ", color = Color(0xFF64748B), fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        // Extra Charges
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Extra Charges", color = TextGray, fontSize = 12.sp)
                            CustomTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = "0.00",
                                prefix = { Text("Rs ", color = Color(0xFF64748B), fontSize = 14.sp) },
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
                                        text = "${if (roundOffDifference > 0) "+" else ""}${"%.2f".format(roundOffDifference)}",
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
                    SettingItem(icon = Icons.Outlined.Edit, title = "Select Signature", badge = "Selected")
                    HorizontalDivider(color = BorderDark)
                    SettingItem(icon = Icons.Outlined.Info, title = "Notes & Terms")
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
        onDismiss = { activeSheet = SheetType.NONE }
    ) {
        when(activeSheet) {
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
                SelectionSheetContent(
                    title = "Select Product",
                    items = products,
                    searchPredicate = { p, q ->
                        p.name.contains(q, true) || p.hsn.contains(q, true)
                    },
                    rowContent = { ProductCard(it) },
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
            else -> {}
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
        Text(title, color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
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
    onClick: (() -> Unit)? = null
) {
    val containerModifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(SurfaceDark)
        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
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
            textStyle = TextStyle(color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium),
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
fun ProductItemCard(item: InvoiceItem, onDelete: () -> Unit, onQtyChange: (Double) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Yellow.copy(alpha = 0.2f)), // Dummy color
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ShoppingCart, null, tint = Color.Yellow)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Rate: ${item.price}", color = TextGray, fontSize = 12.sp)
        }

        // Price & Qty
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("₹${"%.2f".format(item.total)}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            // Qty Controls
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .clip(RoundedCornerShape(4.dp))
//                    .background(Color(0xFF334155))
//            ) {
//                IconButton(
//                    onClick = { onQtyChange(item.quantity - 1) },
//                    modifier = Modifier.size(24.dp)
//                ) {
//                    Icon(Icons.Default.Delete, null, tint = TextWhite, modifier = Modifier.size(14.dp))
//                }
//
//                Text(
//                    "${item.quantity.toInt()}",
//                    color = TextWhite,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(horizontal = 4.dp)
//                )
//
//                IconButton(
//                    onClick = { onQtyChange(item.quantity + 1) },
//                    modifier = Modifier.size(24.dp)
//                ) {
//                    Icon(Icons.Default.Add, null, tint = TextWhite, modifier = Modifier.size(14.dp))
//                }
//            }


            QuantityEditor(
                quantity = item.quantity,
                onQuantityChange = onQtyChange
            )

        }

        // Delete
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Outlined.Delete, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuantityEditor(
    quantity: Double,
    onQuantityChange: (Double) -> Unit
) {
    var text by remember { mutableStateOf(quantity.toInt().toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.toDoubleOrNull()?.takeIf { q -> q > 0 }?.let(onQuantityChange)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.width(64.dp),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    )
}


@Composable
fun TaxRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if(isTotal) Color(0xFFCBD5E1) else TextGray,
            fontSize = 14.sp,
            fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = if(isTotal) TextWhite else TextGray,
            fontSize = 14.sp,
            fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, badge: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(20.dp))
            Text(title, color = TextWhite, fontSize = 14.sp)
        }
        if (badge != null) {
            Text(badge, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun BottomActionSection(totalAmount: String, enabled: Boolean, onGenerate: () -> Unit) {
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
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Generate", color = TextWhite)
                }

        }
    }
}