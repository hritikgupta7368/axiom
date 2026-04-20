package com.example.axiom.ui.screens.finances.purchase

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
import androidx.compose.material.icons.filled.Star
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
import com.example.axiom.ui.navigation.PurchaseFormMode
import com.example.axiom.ui.screens.finances.Invoice.BorderDark
import com.example.axiom.ui.screens.finances.Invoice.PrimaryBlue
import com.example.axiom.ui.screens.finances.Invoice.SurfaceDark
import com.example.axiom.ui.screens.finances.Invoice.TextGray
import com.example.axiom.ui.screens.finances.Invoice.TextWhite
import com.example.axiom.ui.screens.finances.Invoice.components.BillingCalculator
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.Invoice.components.extractStateCodeFromGst
import com.example.axiom.ui.screens.finances.Invoice.components.resolveSupplyType
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.product.components.ProductListSheetWrapper
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseItemEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseRecordEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseViewModel
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseViewModelFactory
import com.example.axiom.ui.screens.finances.suppliers.components.SupplierListSheetWrapper
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.ui.utils.Amount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

enum class SheetType {
    PRODUCT,
    SUPPLIER,
    NONE
}

@Composable
fun SummaryRow(
    label: String,
    value: Double,
    tone: String = "primary",
    isBold: Boolean = false
) {
    val color = when (tone) {
        "primary" -> AxiomTheme.colors.textPrimary
        "red" -> Color.Red
        "gray" -> AxiomTheme.colors.textSecondary
        "blue" -> PrimaryBlue
        else -> AxiomTheme.colors.textPrimary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = color, fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp)
        Text(
            Amount.format(value),
            color = color,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseScreen(mode: PurchaseFormMode, onBack: () -> Unit) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: PurchaseViewModel = viewModel(
        factory = PurchaseViewModelFactory(context)
    )

    val financePreferences = remember { FinancePreferences(context) }
    val selectedCustomer by financePreferences.selectedSeller.collectAsState(
        initial = SelectedSellerPref(null, null, null)
    )


    var formState by remember {
        mutableStateOf(
            PurchaseRecordEntity(
                id = UUID.randomUUID().toString(),
                globalGstRate = 18.0,
                eWayBillDate = null
            ),
        )
    }
    val purchaseItems = remember { mutableStateListOf<PurchaseItemEntity>() }
    var selectedSupplier by remember { mutableStateOf<PartyWithContacts?>(null) }
    var isRoundOffEnabled by remember { mutableStateOf(true) }

    // ─── 3. EDIT MODE POPULATION ───────────────────────────────────────────
    LaunchedEffect(mode) {
        if (mode is PurchaseFormMode.Edit) {
            val existingData = viewModel.getPurchaseById(mode.purchaseId)
            existingData?.let { data ->


                formState = data.record

                // Populate Customer Data (Assuming you have a fetch method)
                selectedSupplier = data.supplier// here full object will be there

                purchaseItems.clear()
                purchaseItems.addAll(data.items)
            }
        }
    }

    val currentSupplyType = resolveSupplyType(selectedCustomer.stateCode, selectedSupplier?.party?.stateCode)

    LaunchedEffect(currentSupplyType) {
        // Check if the supply type actually needs updating to avoid unnecessary recomposition
        if (formState.supplyType != currentSupplyType) {
            formState = formState.copy(supplyType = currentSupplyType)
        }
    }

    val billingSummary by remember {
        derivedStateOf {
            BillingCalculator.calculate(
                itemSubTotal = purchaseItems.sumOf { it.taxableAmount },
                discountAmount = formState.globalDiscountAmount,
                shippingCharges = formState.deliveryCharge,
                extraCharges = formState.extraCharges,
                globalGstRate = formState.globalGstRate, // User assigns this (e.g., 18.0)
                supplyType = formState.supplyType,
                isRoundOffEnabled = isRoundOffEnabled
            )
        }
    }

    val isEWayBillRequired = billingSummary.grandTotal > 50000.0
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }


    fun validateInvoice(): Boolean {
        // 1. Seller Check (Internal check for firm profile)
        if (selectedCustomer.id == null) {
            Toast.makeText(context, "Please select your Business Profile / Customer first", Toast.LENGTH_LONG).show()
            return false
        }

        // 2. Customer Check
        if (selectedSupplier == null) {
            Toast.makeText(context, "Select a Supplier to proceed", Toast.LENGTH_SHORT).show()
            // Optional: Trigger a state to highlight the customer input in red
            return false
        }

        // 3. Invoice Number Check
        if (formState.supplierInvoiceNumber.trim().isBlank()) {
            Toast.makeText(context, "Purchase Number is required", Toast.LENGTH_SHORT).show()
            return false
        }

        // 4. Items Check
        if (purchaseItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one product to the record", Toast.LENGTH_SHORT).show()
            return false
        }


        // 6. E-Way Bill Strict Validation (GST Rules)
        if (isEWayBillRequired) {
            if (formState.eWayBillNumber.isNullOrBlank()) {
                Toast.makeText(context, "E-Way Bill No. is mandatory for transactions over ₹50,000", Toast.LENGTH_LONG).show()
                return false
            }

            if (formState.eWayBillDate == null) {
                Toast.makeText(context, "E-Way Bill Date is required", Toast.LENGTH_LONG).show()
                return false
            }


        }


        return true
    }

    // ─── 5. SUBMISSION LOGIC ───────────────────────────────────────────────
    fun saveRecord() {
        if (!validateInvoice()) return
        val hasEwayBill = !formState.eWayBillNumber.isNullOrBlank()

        // 2. Apply calculated math to the final entity
        val finalRecord = formState.copy(
            // invoice number added
            customerId = selectedCustomer.id,
            supplierId = selectedSupplier!!.party.id,

            vehicleNumber = if (hasEwayBill) formState.vehicleNumber else null,
            deliveryCharge = formState.deliveryCharge,
            shippedToAddress = formState.shippedToAddress,
            eWayBillNumber = if (hasEwayBill) formState.eWayBillNumber else null,
            eWayBillDate = if (hasEwayBill) formState.eWayBillDate else null,


            // Output from BillingCalculator
            itemSubTotal = billingSummary.itemSubTotal,
            totalTaxableAmount = billingSummary.totalTaxableAmount,
            cgstAmount = billingSummary.cgstAmount,
            sgstAmount = billingSummary.sgstAmount,
            igstAmount = billingSummary.igstAmount,
            roundOff = billingSummary.roundOff,
            grandTotal = billingSummary.grandTotal,

            // ... inside finalRecord copy block
            placeOfSupplyCode = extractStateCodeFromGst(selectedSupplier?.party?.gstNumber) ?: "",


            isEdited = mode is PurchaseFormMode.Edit,
            updatedAt = System.currentTimeMillis()
        )

        val finalItems = purchaseItems.map { it.copy(purchaseId = finalRecord.id) }


        // 4. Execute Save
        scope.launch {
            viewModel.savePurchase(finalRecord, finalItems)

            Toast.makeText(context, "Purchase Saved Successfully", Toast.LENGTH_SHORT).show()
            delay(300)
            onBack()

        }
    }





    AnimatedHeaderScrollView(
        largeTitle = if (mode is InvoiceFormMode.Edit) "Edit Purchase" else "Create Purchase",
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
                            value = formState.supplierInvoiceNumber ?: "",
                            onValueChange = {
                                formState = formState.copy(supplierInvoiceNumber = it)

                            },
                            label = "Purchase Number",
                            placeholder = "e.g. INV-2024-00",
                            singleLine = true,
                            isError = formState.supplierInvoiceNumber.isEmpty()
                        )
                    }

                    Box(modifier = Modifier.weight(0.5f)) {
                        //date block
                        DateFieldPicker(
                            dateMillis = formState.purchaseDate,
                            onDateChange = { newDate ->
                                formState = formState.copy(
                                    purchaseDate = newDate
                                )
                            },
                            label = "Purchase Date",
                            isError = formState.purchaseDate == 0L
                        )
                    }

                }

                Box {
                    Input(
                        label = "Supplier / Party",
                        value = selectedSupplier?.party?.businessName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "Select Supplier",

                        )

                    // The invisible shield that catches the click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { activeSheet = SheetType.SUPPLIER }
                    )
                }


                // 3rd row
                //Products
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
                            containerColor = PrimaryBlue.copy(alpha = 0.2f),
                            contentColor = PrimaryBlue
                        ) {
                            Text("${purchaseItems.size} Items", modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }

        // Product List
        items(purchaseItems, key = { it.id }) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ProductItemCard(
                    item = item,
                    onDelete = { purchaseItems.remove(item) },
                    onQtyChange = { newQty ->
                        val index = purchaseItems.indexOf(item)
                        if (index != -1) {
                            purchaseItems[index] = item.copy(
                                quantity = newQty,
                                taxableAmount = newQty * item.costPrice
                            )
                        }
                    },
                    onPriceChange = { newPrice ->
                        val index = purchaseItems.indexOf(item)
                        if (index != -1) {
                            purchaseItems[index] = item.copy(
                                costPrice = newPrice,
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
                                value = formState.eWayBillNumber ?: "",
                                onValueChange = { formState = formState.copy(eWayBillNumber = it) },
                                label = "E-Way Bill Number",
                                placeholder = "Optional",
                                icon = Icons.Default.Star,
                                singleLine = true,
                            )
                        }
                        Box(modifier = Modifier.weight(0.4f)) {
                            DateFieldPicker(
                                dateMillis = formState.eWayBillDate ?: System.currentTimeMillis(),
                                onDateChange = { newDate ->
                                    formState = formState.copy(eWayBillDate = newDate)
                                }
                            )
                        }
                    }

                    //2nd row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            Input(
                                value = formState.vehicleNumber ?: "",
                                onValueChange = { formState = formState.copy(vehicleNumber = it) },
                                label = "Vehicle Number",
                                placeholder = "e.g DL01AB123",
                                singleLine = true,
                            )
                        }

                    }

                    //3rd row
                    Input(
                        value = formState.shippedToAddress ?: "",
                        onValueChange = { formState = formState.copy(shippedToAddress = it) },
                        label = "Shipped To",
                        placeholder = "Delivery address if different",

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
                                value = if (formState.deliveryCharge == 0.0) "" else formState.deliveryCharge.toString(),
                                onValueChange = { input ->
                                    formState = formState.copy(deliveryCharge = input.toDoubleOrNull() ?: 0.0)
                                },
                                label = "Shipping Charges",
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Input(
                                value = if (formState.extraCharges == 0.0) "" else formState.extraCharges.toString(),
                                onValueChange = { input ->
                                    formState = formState.copy(extraCharges = input.toDoubleOrNull() ?: 0.0)
                                },
                                label = "Extra Charges",
                                placeholder = "0.00",
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                    }


                    //3rd row
                    Input(
                        value = if (formState.globalDiscountAmount == 0.0) "" else formState.globalDiscountAmount.toString(),
                        onValueChange = { input ->
                            formState = formState.copy(globalDiscountAmount = input.toDoubleOrNull() ?: 0.0)
                        },
                        label = "Discount Amount",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                }
            }


        }


        //summary and breakdown
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

                if (formState.globalDiscountAmount > 0) {
                    SummaryRow("Discount", -formState.globalDiscountAmount, "red")
                }
                if (formState.deliveryCharge > 0) {
                    SummaryRow("Shipping Charges", formState.deliveryCharge)
                }
                if (formState.extraCharges > 0) {
                    SummaryRow("Extra Charges", formState.extraCharges)
                }

                Divider(color = Color(0xFF333333))

                SummaryRow("Total Taxable Amount", billingSummary.totalTaxableAmount, isBold = true)

                val globalRate = formState.globalGstRate

                if (formState.supplyType == SupplyType.INTRA_STATE) {
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


                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Text("Round Off", color = AxiomTheme.colors.textPrimary, fontSize = 13.sp)
                        AnimatedSwitch(checked = isRoundOffEnabled, onCheckedChange = { isRoundOffEnabled = it }, size = SwitchSize.SM)
                    }
                    Text(
                        Amount.format(-billingSummary.roundOff),
                        color = if (billingSummary.roundOff != 0.0) PrimaryBlue else TextGray,
                        fontSize = 13.sp
                    )
                }

                Divider(color = Color(0xFF333333))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    onClick = {},
                    variant = ButtonVariant.Gray,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    text = "Submit",
                    onClick = { saveRecord() },
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

                            purchaseItems.add(
                                PurchaseItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    productId = product.id,
                                    productNameSnapshot = product.name,
                                    hsnCode = product.hsn,
                                    quantity = 1.0,
                                    costPrice = product.costPrice,
                                    unit = product.unit,
                                    taxableAmount = product.costPrice * 1.0
                                )

                            )

                        }
                        activeSheet = null
                    },
                    onBack = { activeSheet = null }
                )
            }

            SheetType.SUPPLIER -> {

                SupplierListSheetWrapper(

                    onConfirmSelection = { party ->
                        selectedSupplier = party
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
    item: PurchaseItemEntity,
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
                        text = item.productNameSnapshot,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.hsnCode,
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
                        if (item.costPrice == 0.0) "" else "%.2f".format(
                            item.costPrice
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
                    text = "${"Rs %.2f".format(item.taxableAmount)}",
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