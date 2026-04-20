package com.example.axiom.ui.screens.finances.quotation


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.data.finances.dataStore.SelectedSellerPref
import com.example.axiom.ui.components.Accordion.Accordion
import com.example.axiom.ui.components.DatePicker.DateFieldPicker
import com.example.axiom.ui.components.shared.Switch.AnimatedSwitch
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.navigation.QuotationFormMode
import com.example.axiom.ui.screens.finances.Invoice.PrimaryBlue
import com.example.axiom.ui.screens.finances.Invoice.components.BillingCalculator
import com.example.axiom.ui.screens.finances.Invoice.components.extractStateCodeFromGst
import com.example.axiom.ui.screens.finances.Invoice.components.resolveSupplyType
import com.example.axiom.ui.screens.finances.customer.components.CustomerListSheetWrapper
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.product.components.ProductListSheetWrapper
import com.example.axiom.ui.screens.finances.purchase.SummaryRow
import com.example.axiom.ui.screens.finances.quotation.components.QuotationEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationItemEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationStatus
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModel
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModelFactory
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.ui.utils.Amount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


enum class SheetType {
    PRODUCT,
    CUSTOMER,
    NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuotationScreen(
    mode: QuotationFormMode,
    onBack: () -> Unit,
    onInvoicePreview: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: QuotationViewModel = viewModel(
        factory = QuotationViewModelFactory(context)
    )

    //seller details
    val financePreferences = remember { FinancePreferences(context) }
    val selectedSeller by financePreferences.selectedSeller.collectAsState(
        initial = SelectedSellerPref(null, null, null)
    )

    var formState by remember {
        mutableStateOf(QuotationEntity(id = UUID.randomUUID().toString()))
    }
    val quotationItems = remember { mutableStateListOf<QuotationItemEntity>() }
    var selectedCustomer by remember { mutableStateOf<PartyWithContacts?>(null) }
    var isRoundOffEnabled by remember { mutableStateOf(true) }
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }

    LaunchedEffect(mode) {
        if (mode is QuotationFormMode.Edit) {
            val existingData = viewModel.getFullQuotationById(mode.quotationId)
            existingData?.let { data ->

                formState = data.quotation

                // Populate Customer Data
                selectedCustomer = data.customer

                quotationItems.clear()
                quotationItems.addAll(data.items)
            }
        }
    }
    val currentSupplyType = resolveSupplyType(selectedSeller.stateCode, selectedCustomer?.party?.stateCode)

    LaunchedEffect(currentSupplyType) {
        // Check if the supply type actually needs updating to avoid unnecessary recomposition
        if (formState.supplyType != currentSupplyType) {
            formState = formState.copy(supplyType = currentSupplyType)
        }
    }

    val billingSummary by remember {
        derivedStateOf {
            BillingCalculator.calculate(
                itemSubTotal = quotationItems.sumOf { it.taxableAmount },
                discountAmount = formState.globalDiscountAmount,
                shippingCharges = 0.0,
                extraCharges = 0.0,
                globalGstRate = 0.0, // User assigns this (e.g., 18.0)
                supplyType = formState.supplyType,
                isRoundOffEnabled = isRoundOffEnabled
            )
        }
    }

    fun validateQuotation(): Boolean {
        // 1. Seller Check (Internal check for firm profile)
        if (selectedSeller.id == null) {
            Toast.makeText(context, "Please select your Business Profile first", Toast.LENGTH_LONG).show()
            return false
        }

        // 2. Customer Check
        if (selectedCustomer == null) {
            Toast.makeText(context, "Select a Customer to proceed", Toast.LENGTH_SHORT).show()
            return false
        }

        // 3. Quotation Number Check
        if (formState.quotationNumber.trim().isBlank()) {
            Toast.makeText(context, "Quotation Number is required", Toast.LENGTH_SHORT).show()
            return false
        }

        // 4. Items Check
        if (quotationItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one product to the quotation", Toast.LENGTH_SHORT).show()
            return false
        }

        // 5. Date Validation Check
        if (formState.validUntilDate < formState.quotationDate) {
            Toast.makeText(context, "Valid Until Date cannot be before Quotation Date", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // ─── 5. SUBMISSION LOGIC ───────────────────────────────────────────────
    fun saveRecord(isDraft: Boolean = false) {
        if (!validateQuotation()) return

        // 2. Apply calculated math to the final entity
        val finalRecord = formState.copy(
            customerId = selectedCustomer!!.party.id,
            sellerId = selectedSeller.id,

            // Output from BillingCalculator (Mapping to your QuotationEntity fields)
            itemSubTotal = billingSummary.itemSubTotal,
            globalDiscountAmount = formState.globalDiscountAmount,
            grandTotal = billingSummary.grandTotal,

            placeOfSupplyCode = extractStateCodeFromGst(selectedCustomer?.party?.gstNumber) ?: "",

            // Set status based on button clicked
            status = if (isDraft) QuotationStatus.DRAFT else QuotationStatus.SENT,

            updatedAt = System.currentTimeMillis()
        )

        val finalItems = quotationItems.map { it.copy(quotationId = finalRecord.id) }

        // 4. Execute Save
        scope.launch {
            if (mode is QuotationFormMode.Create) {
                viewModel.createQuotation(finalRecord, finalItems)
            } else {
                viewModel.updateQuotation(finalRecord, finalItems)
            }

            val msg = if (isDraft) "Quotation saved as Draft" else "Quotation Saved Successfully"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            delay(300)
            onInvoicePreview(finalRecord.id)
        }
    }
    AnimatedHeaderScrollView(
        largeTitle = if (mode is QuotationFormMode.Edit) "Edit Quotation" else "Create Quotation",
        onBack = onBack,
        isParentRoute = true,
    ) {

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1st row: Quote Number & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.5f)) {
                        Input(
                            value = formState.quotationNumber,
                            onValueChange = { formState = formState.copy(quotationNumber = it) },
                            label = "Quotation Number",
                            placeholder = "e.g. QT-2026-00",
                            singleLine = true,
                            isError = formState.quotationNumber.isEmpty()
                        )
                    }

                    Box(modifier = Modifier.weight(0.5f)) {
                        DateFieldPicker(
                            dateMillis = formState.quotationDate,
                            onDateChange = { newDate ->
                                formState = formState.copy(quotationDate = newDate)
                            },
                            label = "Quotation Date",
                            isError = formState.quotationDate == 0L
                        )
                    }
                }

                // 2nd row: Customer Selection
                Box {
                    Input(
                        label = "Customer / Billed To",
                        value = selectedCustomer?.party?.businessName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "Select Customer",
                    )
                    // The invisible shield that catches the click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { activeSheet = SheetType.CUSTOMER }
                    )
                }

                // 3rd row: Products Header
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Products / Services",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AxiomTheme.colors.textPrimary
                        )
                        Badge(
                            containerColor = PrimaryBlue.copy(alpha = 0.2f),
                            contentColor = PrimaryBlue
                        ) {
                            Text("${quotationItems.size} Items", modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }

        // Product List (Mapped for QuotationItems)
        items(quotationItems, key = { it.id }) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ProductItemCard(
                    item = item, // Ensure your ProductItemCard supports QuotationItemEntity or map it to a generic DTO
                    onDelete = { quotationItems.remove(item) },
                    onQtyChange = { newQty ->
                        val index = quotationItems.indexOf(item)
                        if (index != -1) {
                            quotationItems[index] = item.copy(
                                quantity = newQty,
                                taxableAmount = newQty * item.quotationPriceAtTime
                            )
                        }
                    },
                    onPriceChange = { newPrice ->
                        val index = quotationItems.indexOf(item)
                        if (index != -1) {
                            quotationItems[index] = item.copy(
                                quotationPriceAtTime = newPrice,
                                taxableAmount = newPrice * item.quantity
                            )
                        }
                    }
                )
            }
        }

        // Add Product Button
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeSheet = SheetType.PRODUCT }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = AxiomTheme.components.card.title, modifier = Modifier.size(20.dp))
                        Text("Add Product/Service", color = AxiomTheme.components.card.title, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Validity & Notes
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Accordion(
                    title = "Validity & Terms",
                ) {
                    // Valid Until Date
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            DateFieldPicker(
                                dateMillis = formState.validUntilDate,
                                onDateChange = { newDate ->
                                    formState = formState.copy(validUntilDate = newDate)
                                },
                                label = "Valid Until"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Terms and Conditions
                    Input(
                        value = formState.termsAndConditions,
                        onValueChange = { formState = formState.copy(termsAndConditions = it) },
                        label = "Terms & Conditions",
                        placeholder = "e.g. 50% Advance Required...",
                        singleLine = false,
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Additional Notes
                    Input(
                        value = formState.additionalNotes,
                        onValueChange = { formState = formState.copy(additionalNotes = it) },
                        label = "Additional Notes",
                        placeholder = "Timeline, Scope details...",
                        singleLine = false,
                        minLines = 3
                    )
                }
            }
        }

        // Discounts
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Accordion(
                    title = "Discounts",
                ) {
                    Input(
                        value = if (formState.globalDiscountAmount == 0.0) "" else formState.globalDiscountAmount.toString(),
                        onValueChange = { input ->
                            formState = formState.copy(globalDiscountAmount = input.toDoubleOrNull() ?: 0.0)
                        },
                        label = "Overall Discount Amount",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                }
            }
        }

        // Summary and Breakdown
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                    .background(AxiomTheme.components.card.background, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("QUOTATION SUMMARY", color = AxiomTheme.colors.textPrimary, fontSize = 15.sp)

                SummaryRow("Items Total", billingSummary.itemSubTotal, "gray")

                if (formState.globalDiscountAmount > 0) {
                    SummaryRow("Discount", -formState.globalDiscountAmount, "red")
                }

                Divider(color = Color(0xFF333333))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text("Round Off", color = AxiomTheme.colors.textPrimary, fontSize = 13.sp)
                        AnimatedSwitch(checked = isRoundOffEnabled, onCheckedChange = { isRoundOffEnabled = it })
                    }
                    Text(Amount.format(-billingSummary.roundOff), color = Color.Gray, fontSize = 13.sp)
                }

                Divider(color = Color(0xFF333333))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(Amount.format(billingSummary.grandTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        // Submit Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    text = "Save as Draft",
                    onClick = { saveRecord(isDraft = true) },
                    variant = ButtonVariant.Gray,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    text = "Save & Send",
                    onClick = { saveRecord(isDraft = false) },
                    icon = Icons.Default.Check,
                    variant = ButtonVariant.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    AppBottomSheet(
        showSheet = activeSheet != null,
        onDismiss = { activeSheet = null }
    ) {
        when (activeSheet) {
            SheetType.PRODUCT -> {
                ProductListSheetWrapper(
                    onConfirmSelection = { selectedProducts ->
                        selectedProducts.forEach { product ->
                            quotationItems.add(
                                QuotationItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    productId = product.id,
                                    productNameSnapshot = product.name,
                                    hsnSnapshot = product.hsn,
                                    quantity = 1.0,
                                    quotationPriceAtTime = product.sellingPrice,
                                    taxableAmount = product.sellingPrice,
                                    unitSnapshot = product.unit,
                                )
                            )
                        }
                        activeSheet = null
                    },
                    onBack = { activeSheet = null }
                )
            }

            SheetType.CUSTOMER -> { // Replaced SUPPLIER with CUSTOMER
                CustomerListSheetWrapper( // Ensure you have this wrapper for selecting customers
                    onConfirmSelection = { party ->
                        selectedCustomer = party
                        activeSheet = null
                    },
                    onBack = { activeSheet = null }
                )
            }

            null, SheetType.NONE -> { /* Safe empty state */
            }
        }
    }
}


@Composable
fun ProductItemCard(
    item: QuotationItemEntity,
    onDelete: () -> Unit,
    onQtyChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
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
                        .background(AxiomTheme.components.textInput.unfocusedBg)
                        .border(1.dp, AxiomTheme.components.textInput.unfocusedBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart, // Replace with web icon
                        contentDescription = null,
                        tint = AxiomTheme.components.card.selectedBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title and Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.productNameSnapshot,
                        color = AxiomTheme.components.card.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.hsnSnapshot,
                        color = AxiomTheme.components.card.subtitle,
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
                    tint = AxiomTheme.components.card.title,
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
                    .background(AxiomTheme.components.textInput.unfocusedBg)
                    .border(1.dp, AxiomTheme.components.textInput.unfocusedBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qty",
                    color = AxiomTheme.components.card.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                var qtyText by remember(item.id) {
                    mutableStateOf(
                        if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString()
                        else item.quantity.toString()
                    )
                }

                BasicTextField(
                    value = qtyText,
                    onValueChange = {
                        qtyText = it
                        it.toDoubleOrNull()?.takeIf { q -> q > 0 }?.let(onQtyChange)
                    },
                    textStyle = TextStyle(
                        color = AxiomTheme.components.card.title,
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
                    .background(AxiomTheme.components.textInput.unfocusedBg)
                    .border(1.dp, AxiomTheme.components.textInput.unfocusedBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rate",
                    color = AxiomTheme.components.card.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 0.dp)
                )

                var priceText by remember(item.id) {
                    mutableStateOf(
                        if (item.taxableAmount == 0.0) ""
                        else "%.2f".format(item.taxableAmount)
                    )
                }

                BasicTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        it.toDoubleOrNull()?.takeIf { p -> p >= 0 }?.let(onPriceChange)
                    },
                    textStyle = TextStyle(
                        color = AxiomTheme.components.card.title,
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
                    color = AxiomTheme.components.card.subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${"Rs %.2f".format(item.taxableAmount)}",
                    color = AxiomTheme.components.card.selectedBorder,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
