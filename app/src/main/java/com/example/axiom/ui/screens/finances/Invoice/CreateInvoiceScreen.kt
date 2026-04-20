package com.example.axiom.ui.screens.finances.Invoice

//import com.example.axiom.data.finances.CreateInvoiceViewModelFactory
//import com.example.axiom.ui.screens.finances.product.UnitSelectionDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.axiom.ui.components.shared.Switch.SwitchSize
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.navigation.InvoiceFormMode
import com.example.axiom.ui.screens.finances.Invoice.components.BillingCalculator
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceItemEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModel
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModelFactory
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentMode
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentStatus
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentTransactionEntity
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.Invoice.components.TransactionType
import com.example.axiom.ui.screens.finances.Invoice.components.extractStateCodeFromGst
import com.example.axiom.ui.screens.finances.Invoice.components.resolveSupplyType
import com.example.axiom.ui.screens.finances.customer.components.CustomerListSheetWrapper
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.product.components.ProductListSheetWrapper
import com.example.axiom.ui.screens.finances.purchase.SummaryRow
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.ui.utils.Amount
import com.example.axiom.ui.utils.numberToWords
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


val SurfaceDark = Color(0xFF1C1D27)
val BorderDark = Color(0xFF334155)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF94A3B8)
val PrimaryBlue = Color(0xFF3B82F6)


enum class SheetType {
    PRODUCT,
    CUSTOMER,
    NONE
}


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


data class InvoiceFormState(
    // The core database entity
    val entity: InvoiceEntity,

    // UI-specific payment fields (which will be converted to PaymentTransactionEntity on save)
    val receivedAmount: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val paymentNotes: String = ""
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    mode: InvoiceFormMode,
    onBack: () -> Unit,
    onInvoicePreview: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(context)
    )

    val financePreferences = remember { FinancePreferences(context) }
    val lastInvoiceNo by financePreferences.lastInvoiceNumber.collectAsState(initial = 0L)
    val selectedSeller by financePreferences.selectedSeller.collectAsState(
        initial = SelectedSellerPref(null, null, null)
    )


    var formState by remember {
        mutableStateOf(
            InvoiceFormState(
                entity = InvoiceEntity(
                    id = if (mode is InvoiceFormMode.Edit) mode.invoiceId else UUID.randomUUID().toString(),
                    invoiceDate = System.currentTimeMillis(),
                    status = InvoiceStatus.ACTIVE,
                    globalGstRate = 18.0, // Default GST Rate
                    eWayBillDate = null
                )
            )
        )
    }

    // Relational/Complex UI States
    val invoiceItems = remember { mutableStateListOf<InvoiceItemEntity>() }
    var selectedCustomer by remember { mutableStateOf<PartyWithContacts?>(null) }
    var isRoundOffEnabled by remember { mutableStateOf(true) }

    var suggestedInvoiceNo by remember { mutableStateOf("") }
    var userEditedInvoiceNo by remember { mutableStateOf(false) }

    // ─── 2. AUTO-INVOICE GENERATION ────────────────────────────────────────
    LaunchedEffect(lastInvoiceNo) {
        if (mode is InvoiceFormMode.Create) {
            val nextNumber = (lastInvoiceNo + 1).coerceIn(INVOICE_NUMBER_MIN, INVOICE_NUMBER_MAX)
            val padded = nextNumber.toPaddedInvoiceNumber()
            suggestedInvoiceNo = padded
            if (!userEditedInvoiceNo) {
                // Update the entity inside our single state
                formState = formState.copy(
                    entity = formState.entity.copy(invoiceNumber = padded)
                )
            }
        }
    }

    // ─── 3. EDIT MODE POPULATION ───────────────────────────────────────────
    LaunchedEffect(mode) {
        if (mode is InvoiceFormMode.Edit) {
            val existingData = viewModel.getInvoiceByIdSync(mode.invoiceId)
            existingData?.let { data ->

                val existingPayment = data.payments.firstOrNull()

                // Boom. The entire form and entity populates in one go.
                formState = formState.copy(
                    entity = data.invoice,
                    receivedAmount = existingPayment?.amount ?: 0.0,
                    paymentMode = existingPayment?.paymentMode ?: PaymentMode.CASH,
                    paymentNotes = existingPayment?.notes ?: ""
                )
                userEditedInvoiceNo = true

                // Populate Customer Data (Assuming you have a fetch method)
                selectedCustomer = data.customer

                invoiceItems.clear()
                invoiceItems.addAll(data.items)
            }
        }
    }

    // ─── 4. REACTIVE BUSINESS LOGIC ────────────────────────────────────────
    // Supply type updates automatically based on seller vs customer states
    val currentSupplyType = resolveSupplyType(selectedSeller.stateCode, selectedCustomer?.party?.stateCode)

    LaunchedEffect(currentSupplyType) {
        if (formState.entity.supplyType != currentSupplyType) {
            formState = formState.copy(
                entity = formState.entity.copy(supplyType = currentSupplyType)
            )
        }
    }

    // Shared Math engine (Zero manual math in this file!)
    val billingSummary by remember {
        derivedStateOf {
            BillingCalculator.calculate(
                itemSubTotal = invoiceItems.sumOf { it.taxableAmount },
                discountAmount = formState.entity.globalDiscountAmount,
                shippingCharges = formState.entity.deliveryCharge,
                extraCharges = formState.entity.extraCharges,
                globalGstRate = formState.entity.globalGstRate,
                supplyType = formState.entity.supplyType,
                isRoundOffEnabled = isRoundOffEnabled
            )
        }
    }

    // Dynamic Payment Status tracking
    val currentPaymentStatus by remember(formState.receivedAmount, billingSummary.grandTotal) {
        derivedStateOf {
            when {
                formState.receivedAmount <= 0.0 -> PaymentStatus.UNPAID
                formState.receivedAmount >= billingSummary.grandTotal -> PaymentStatus.PAID
                else -> PaymentStatus.PARTIAL
            }
        }
    }

    val isEWayBillRequired = billingSummary.grandTotal > 50000.0
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }


    fun validateInvoice(): Boolean {
        // 1. Seller Check (Internal check for firm profile)
        if (selectedSeller.id == null) {
            Toast.makeText(context, "Please select your Business Profile / Seller first", Toast.LENGTH_LONG).show()
            return false
        }

        // 2. Customer Check
        if (selectedCustomer == null) {
            Toast.makeText(context, "Select a Customer to proceed", Toast.LENGTH_SHORT).show()
            // Optional: Trigger a state to highlight the customer input in red
            return false
        }

        // 3. Invoice Number Check
        if (formState.entity.invoiceNumber.trim().isBlank()) {
            Toast.makeText(context, "Invoice Number is required", Toast.LENGTH_SHORT).show()
            return false
        }

        // 4. Items Check
        if (invoiceItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one product to the invoice", Toast.LENGTH_SHORT).show()
            return false
        }


        // 6. E-Way Bill Strict Validation (GST Rules)
        if (isEWayBillRequired) {
            if (formState.entity.eWayBillNumber.isNullOrBlank()) {
                Toast.makeText(context, "E-Way Bill No. is mandatory for transactions over ₹50,000", Toast.LENGTH_LONG).show()
                return false
            }
            if (formState.entity.eWayBillDate == null) {
                Toast.makeText(context, "E-Way Bill Date is required", Toast.LENGTH_LONG).show()
                return false
            }


            // Case B: Missing Vehicle Number (Part B of E-Way Bill)
            if (formState.entity.vehicleNumber.isNullOrBlank()) {
                Toast.makeText(context, "Vehicle Number is required for E-Way Bill generation", Toast.LENGTH_LONG).show()
                return false
            }
        }

        // 7. Payment Validation (Optional: Prevent negative received amounts)
        if (formState.receivedAmount < 0) {
            Toast.makeText(context, "Received amount cannot be negative", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // ─── 5. SUBMISSION LOGIC ───────────────────────────────────────────────
    fun saveInvoice() {
        if (!validateInvoice()) return

        val hasEwayBill = !formState.entity.eWayBillNumber.isNullOrBlank()

        // 2. Apply calculated math to the final entity
        val finalRecord = formState.entity.copy(
            // invoice number added
            sellerId = selectedSeller.id,
            customerId = selectedCustomer!!.party.id,

            vehicleNumber = if (hasEwayBill) formState.entity.vehicleNumber else null,
            deliveryCharge = formState.entity.deliveryCharge,
            shippedToAddress = formState.entity.shippedToAddress,
            eWayBillNumber = if (hasEwayBill) formState.entity.eWayBillNumber else null,
            eWayBillDate = if (hasEwayBill) formState.entity.eWayBillDate else null,


            // Output from BillingCalculator
            itemSubTotal = billingSummary.itemSubTotal,
            totalTaxableAmount = billingSummary.totalTaxableAmount,
            cgstAmount = billingSummary.cgstAmount,
            sgstAmount = billingSummary.sgstAmount,
            igstAmount = billingSummary.igstAmount,
            roundOff = billingSummary.roundOff,
            grandTotal = billingSummary.grandTotal,

            // ... inside finalRecord copy block
            placeOfSupplyCode = extractStateCodeFromGst(selectedCustomer?.party?.gstNumber) ?: "",


            amountInWords = numberToWords(billingSummary.grandTotal),
            paymentStatus = currentPaymentStatus,
            isEdited = mode is InvoiceFormMode.Edit,
            updatedAt = System.currentTimeMillis()
        )

        val finalItems = invoiceItems.map { it.copy(invoiceId = finalRecord.id) }

        // 3. Draft Payment Transaction
        val paymentTransaction = if (formState.receivedAmount > 0) {
            PaymentTransactionEntity(
                id = UUID.randomUUID().toString(),
                partyId = selectedCustomer!!.party.id,
                documentId = finalRecord.id,
                type = TransactionType.CREDIT,
                amount = formState.receivedAmount,
                paymentMode = formState.paymentMode,
                transactionDate = System.currentTimeMillis(),
                notes = formState.paymentNotes.takeIf { it.isNotBlank() } ?: "Payment for ${finalRecord.invoiceNumber}"
            )
        } else null

        // 4. Execute Save
        scope.launch {
            if (mode is InvoiceFormMode.Create) {
                viewModel.createInvoice(finalRecord, finalItems, paymentTransaction) // -> add payment details later

                // Advance the global invoice counter only if the user didn't overwrite the auto-generated number
                if (!userEditedInvoiceNo && finalRecord.invoiceNumber == suggestedInvoiceNo) {
                    financePreferences.saveLastInvoiceNumber((lastInvoiceNo + 1).coerceIn(INVOICE_NUMBER_MIN, INVOICE_NUMBER_MAX))
                }
            } else {
                viewModel.editInvoice(finalRecord, finalItems, paymentTransaction)
            }

            Toast.makeText(context, "Invoice Saved Successfully", Toast.LENGTH_SHORT).show()
            delay(300)
            onInvoicePreview(finalRecord.id)
        }
    }


    AnimatedHeaderScrollView(
        largeTitle = if (mode is InvoiceFormMode.Edit) "Edit Invoice" else "Create Invoice",
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

                // 1st row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.5f)) {
                        Input(
                            label = "Invoice Number",
                            value = formState.entity.invoiceNumber,
                            onValueChange = {
                                formState = formState.copy(
                                    entity = formState.entity.copy(invoiceNumber = it)
                                )
                                userEditedInvoiceNo = true
                            },
                            placeholder = "e.g. INV/24-25/001",
                            singleLine = true,
                            allCaps = true,
                            isError = formState.entity.invoiceNumber.isEmpty()
                        )
                    }


                    Box(modifier = Modifier.weight(0.5f)) {
                        //date block
                        DateFieldPicker(
                            dateMillis = formState.entity.invoiceDate,
                            onDateChange = { newDate ->
                                // Constraint: Generally, Invoice Date shouldn't be in the future
                                val today = System.currentTimeMillis()
                                if (newDate <= today) {
                                    formState = formState.copy(
                                        entity = formState.entity.copy(invoiceDate = newDate)
                                    )
                                }
                            },
                            label = "Invoice Date",
                            isError = formState.entity.invoiceDate == 0L
                        )
                    }

                }

// 2nd row
                Box {
                    Input(
                        label = "Customer / Party",
                        value = selectedCustomer?.party?.businessName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "Select Customer",
//                        isError = supplierError,
                    )

                    // The invisible shield that catches the click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { activeSheet = SheetType.CUSTOMER }
                    )
                }

                // Product Section
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
                            color = AxiomTheme.colors.textPrimary
                        )
                        Badge(
                            containerColor = AxiomTheme.components.card.selectedBorder.copy(alpha = 0.2f),
                            contentColor = AxiomTheme.components.card.selectedBorder
                        ) {
                            Text("${invoiceItems.size} Items", modifier = Modifier.padding(4.dp))
                        }
                    }
                }

            }
        }

        // Product List
        items(invoiceItems, key = { it.id }) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ProductItemCard(
                    item = item,
                    onDelete = { invoiceItems.remove(item) },
                    onQtyChange = { newQty ->
                        val index = invoiceItems.indexOf(item)
                        if (index != -1) {
                            invoiceItems[index] = item.copy(
                                quantity = newQty,
                                taxableAmount = newQty * item.sellingPriceAtTime
                            )
                        }
                    },
                    onPriceChange = { newPrice ->
                        val index = invoiceItems.indexOf(item)
                        if (index != -1) {
                            invoiceItems[index] = item.copy(
                                sellingPriceAtTime = newPrice,
                                taxableAmount = newPrice * item.quantity
                            )
                        }
                    }
                )
            }
        }

        // button to add products
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add Product Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activeSheet = SheetType.PRODUCT } // Just set the enum
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = AxiomTheme.components.card.title, modifier = Modifier.size(20.dp))
                        Text("Add Product", color = AxiomTheme.components.card.title, fontWeight = FontWeight.SemiBold)
                    }
                }


            }
        }

        //Transport & E-Way Bill
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Accordion(
                    title = "Transport & E-Way Bill",
                ) {
                    // 1st row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(0.6f)) {
                            Input(
                                value = formState.entity.eWayBillNumber ?: "",
                                onValueChange = { input ->
                                    // Constraint: Only allow numbers and limit to 12 digits
                                    if (input.length <= 12 && input.all { it.isDigit() }) {
                                        formState = formState.copy(
                                            entity = formState.entity.copy(eWayBillNumber = input.takeIf { it.isNotBlank() })
                                        )
                                    }
                                },
                                label = "E-Way Bill Number",
                                placeholder = "12-digit number",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                singleLine = true,
                                isError = formState.entity.eWayBillNumber?.length in 1..11
                            )
                        }
                        Box(modifier = Modifier.weight(0.4f)) {
                            DateFieldPicker(
                                dateMillis = formState.entity.eWayBillDate ?: 0L,
                                onDateChange = { newDate ->
                                    // FIX: Always update the state so the UI reflects the user's choice
                                    formState = formState.copy(
                                        entity = formState.entity.copy(eWayBillDate = newDate)
                                    )
                                },
                                label = "E-Way Bill Date",
                            )
                        }
                    }

                    //2nd row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            Input(
                                value = formState.entity.vehicleNumber ?: "",
                                onValueChange = { input ->
                                    // 1. Force Uppercase
                                    // 2. Remove spaces/special chars (sanitization)
                                    // 3. Limit to 10-12 characters
                                    val sanitized = input.uppercase().filter { it.isLetterOrDigit() }
                                    if (sanitized.length <= 10) {
                                        formState = formState.copy(
                                            entity = formState.entity.copy(vehicleNumber = sanitized.takeIf { it.isNotBlank() })
                                        )
                                    }
                                },
                                label = "Vehicle Number",
                                placeholder = "e.g DL01AB123",
                                allCaps = true,
                                singleLine = true,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                                isError = formState.entity.vehicleNumber?.let {
                                    it.length > 0 && !it.matches(Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,2}[0-9]{4}$"))
                                } ?: false
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
//                            Input(
//                                value = formState.entity.placeOfSupplyCode ?: "",
//                                onValueChange = {
//                                    formState = formState.copy(
//                                        entity = formState.entity.copy(
//                                            placeOfSupplyCode = it.trim()
//                                        )
//                                    )
//                                },
//                                label = "Place of Supply",
//                                placeholder = "State/City",
//                                keyboardType = KeyboardType.Number
//                            )
                        }
                    }

                    //3rd row
                    Input(
                        value = formState.entity.shippedToAddress ?: "",
                        onValueChange = {
                            formState = formState.copy(
                                entity = formState.entity.copy(shippedToAddress = it.takeIf { it.isNotBlank() })
                            )
                        },
                        label = "Shipped To",
                        placeholder = "Delivery address if different",
                        imeAction = ImeAction.Default,
                        singleLine = false,
                    )
                }
            }


        }

        //Charges & Discounts
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Accordion(
                    title = "Charges & Discounts",
                ) {
                    // 1st row
                    // Shipping / Extra charges
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            Input(
                                value = if (formState.entity.deliveryCharge == 0.0) "" else formState.entity.deliveryCharge.toString(),
                                onValueChange = { input ->
                                    // Allow empty, digits, and a single decimal point
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                        formState = formState.copy(
                                            entity = formState.entity.copy(
                                                deliveryCharge = input.toDoubleOrNull() ?: 0.0
                                            )
                                        )
                                    }
                                },
                                label = "Shipping Charges",
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Input(
                                value = if (formState.entity.extraCharges == 0.0) "" else formState.entity.extraCharges.toString(),
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                        formState = formState.copy(
                                            entity = formState.entity.copy(
                                                extraCharges = input.toDoubleOrNull() ?: 0.0
                                            )
                                        )
                                    }
                                },
                                label = "Extra Charges",
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            )
                        }
                    }


                    //3rd row
                    Input(
                        value = if (formState.entity.globalDiscountAmount == 0.0) "" else formState.entity.globalDiscountAmount.toString(),
                        onValueChange = { input ->
                            // Constraint 1: Only allow numbers and a single decimal point
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                val discountValue = input.toDoubleOrNull() ?: 0.0

                                // Constraint 2: Prevent discount from being higher than the subtotal (Optional but recommended)
                                // val maxAllowed = formState.entity.subTotal ?: Double.MAX_VALUE

                                formState = formState.copy(
                                    entity = formState.entity.copy(
                                        globalDiscountAmount = discountValue
                                    )
                                )
                            }
                        },
                        label = "Discount Amount",
                        placeholder = "0.00",
                        singleLine = true,
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                        isError = formState.entity.globalDiscountAmount < 0
                    )
                }
            }


        }


        //fields needed
        // Signature
        // Bank Details
        // Notes & Terms
        // Bank Details

        // Payment Status
        // Payment Mode
        // Received Amount
        // Payment Date

        // --- Tax Breakdown ---
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
                Text("TAX BREAKDOWN", color = AxiomTheme.colors.textPrimary, fontSize = 15.sp)

                SummaryRow("Items Total", billingSummary.itemSubTotal, "gray")

                if (formState.entity.globalDiscountAmount > 0) {
                    SummaryRow("Discount", -formState.entity.globalDiscountAmount, "red")
                }

                if (formState.entity.deliveryCharge > 0) {
                    SummaryRow("Shipping Charges", formState.entity.deliveryCharge)
                }

                if (formState.entity.extraCharges > 0) {
                    SummaryRow("Extra Charges", formState.entity.extraCharges)
                }


                Divider(color = Color(0xFF333333))

                SummaryRow(
                    "Total Taxable Amount",
                    billingSummary.totalTaxableAmount,
                    isBold = true
                )

                val globalRate = formState.entity.globalGstRate


                if (formState.entity.supplyType == SupplyType.INTRA_STATE) {
                    val halfRate = (globalRate / 2).toInt()

                    if (billingSummary.cgstAmount > 0) {
                        SummaryRow("CGST ($halfRate%)", billingSummary.cgstAmount, "blue")
                    }
                    if (billingSummary.sgstAmount > 0) {
                        SummaryRow("SGST ($halfRate%)", billingSummary.sgstAmount, "blue")
                    }
                } else {
                    if (billingSummary.igstAmount > 0) {
                        SummaryRow("IGST (${globalRate.toInt()}%)", billingSummary.igstAmount, "blue")
                    }
                }

                Divider(color = Color(0xFF333333))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("Round Off", color = AxiomTheme.colors.textPrimary, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
                        AnimatedSwitch(
                            checked = isRoundOffEnabled,
                            onCheckedChange = { isRoundOffEnabled = it },
                            size = SwitchSize.SM,
                        )
                    }

                    Text(
                        Amount.format(-billingSummary.roundOff),
                        color = if (billingSummary.roundOff != 0.0) PrimaryBlue else TextGray,
                        fontSize = 13.sp
                    )
                }

                Divider(color = Color(0xFF333333))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AxiomTheme.components.card.title)
                    Text(
                        Amount.format(billingSummary.grandTotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AxiomTheme.components.card.title
                    )
                }
            }

        }

        //save button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    text = "Save as Draft",
                    onClick = {},
                    variant = ButtonVariant.Gray,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    text = if (mode is InvoiceFormMode.Create) "Submit" else "Update",
                    onClick = { saveInvoice() },
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

                            invoiceItems.add(
                                InvoiceItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    productId = product.id,
                                    productNameSnapshot = product.name,
                                    hsnSnapshot = product.hsn,
                                    quantity = 1.0,
                                    sellingPriceAtTime = product.sellingPrice,
                                    taxableAmount = product.sellingPrice,
                                    unitSnapshot = product.unit,
                                    costPriceAtTime = product.costPrice

                                )
                            )

                        }
                        activeSheet = null
                    },
                    onBack = { activeSheet = null }
                )
            }

            SheetType.CUSTOMER -> {

                CustomerListSheetWrapper(

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
    item: InvoiceItemEntity,
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
                        if (item.sellingPriceAtTime == 0.0) ""
                        else "%.2f".format(item.sellingPriceAtTime)
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





