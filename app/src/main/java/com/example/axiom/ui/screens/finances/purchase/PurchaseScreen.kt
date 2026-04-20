package com.example.axiom.ui.screens.finances.purchase

//import com.example.axiom.data.finances.PurchaseRecordViewModelFactory

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.EmptyState.EmptyScreen
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.cards.PurchaseRecordCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.PrimaryBlue
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseRecordEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseViewModel
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseViewModelFactory
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseWithItems
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.ui.utils.Amount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(onBack: () -> Unit, onCreatePurchase: () -> Unit, onEditPurchase: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val viewModel: PurchaseViewModel = viewModel(
        factory = PurchaseViewModelFactory(context)
    )

    // all states
    val deletedItemIds = remember { mutableStateListOf<String>() }
    var showSheet by remember { mutableStateOf(false) }
    var showPurchaseDetailsSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPurchaseIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedPurchaseIds.isNotEmpty()
    var selectedPurchaseForDetails by remember { mutableStateOf<PurchaseWithItems?>(null) }

    var selectedPurchase by remember { mutableStateOf<PurchaseRecordEntity?>(null) }
    var selectedPurchaseId = selectedPurchase?.id

    BackHandler(enabled = isSelectionMode) {
        selectedPurchaseIds.clear()
    }


    // DB FETCHING
    val purchases by viewModel.allPurchases.collectAsState()        // List<PurchaseWithItems>
    val groupedPurchases = remember(purchases) {
        purchases
            .sortedByDescending { it.record.purchaseDate }   // recent first
            .groupBy { purchase ->
                val date = Date(purchase.record.purchaseDate)
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            }
            // Map each month's list into a Pair holding the (Total Sum, List of Purchases)
            .mapValues { (_, purchasesInMonth) ->
                val monthTotal = purchasesInMonth.sumOf { it.record.grandTotal }
                Pair(monthTotal, purchasesInMonth)
            }
    }

    val purchaseToEdit by viewModel.selectedPurchase.collectAsState()

    val purchaseWithItems by viewModel
        .selectedPurchase
        .collectAsState()


    // --- Helpers ---
    fun openEditSheet() {
        // Take the first selected item to edit
        val idToEdit = selectedPurchaseIds.firstOrNull()
        if (idToEdit != null) {
            viewModel.loadPurchase(idToEdit) // load full details for the form
            isEditing = true
            showSheet = true
        }
    }

    fun deleteSelected() {
        if (selectedPurchaseIds.isNotEmpty()) {
            val idsToDelete = selectedPurchaseIds.toList() // Copy the list
            selectedPurchaseIds.clear()

            scope.launch {
                deletedItemIds.addAll(idsToDelete)
                delay(520) // wait for exit animation
                idsToDelete.forEach { viewModel.deletePurchase(it) }
                deletedItemIds.removeAll(idsToDelete)
            }
        }
    }

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = purchases.isEmpty()


    AnimatedHeaderScrollView(
        largeTitle = "Purchases",
        onAddClick = { onCreatePurchase() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
        onBack = onBack,
        isSelectionMode = isSelectionMode,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
    ) {
        if (isEmpty) {
            if (isSearching) {
                // CASE A: Searching, but no results
                item(key = "no_results_state") {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AxiomTheme.components.card.mutedText.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AxiomTheme.components.card.title
                        )
                        Text(
                            text = "Try adjusting your search for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            } else {
                // CASE B: Database is completely empty (No search active)
                // We show the empty state INSIDE the scroll view so the header stays!
                item(key = "database_empty_state") {

                    EmptyScreen(
                        title = "No purchases yet",
                        description = "Your purchase history will appear here.",
                        buttonText = "Add Purchase",
                        onAdd = { onCreatePurchase() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {
            groupedPurchases.forEach { (monthYear, summaryData) ->

                val (monthTotal, purchasesInMonth) = summaryData

                // 1. Group Header (Month/Year)
                item(key = "header_$monthYear") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = monthYear, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Text(text = Amount.format(monthTotal), fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }

                // 2. The Items for that Month
                items(
                    items = purchasesInMonth,
                    key = { it.record.id } // Requires stable ID for animations
                ) { purchase ->

                    val record = purchase.record
                    val isDeleting = deletedItemIds.contains(record.id)
                    val isSelected = selectedPurchaseIds.contains(record.id)

                    // 3. Deletion Animation Wrapper
                    AnimatedVisibility(
                        visible = !isDeleting,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            PurchaseRecordCard(
                                record = record,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) {
                                        if (selectedPurchaseIds.contains(record.id)) {
                                            selectedPurchaseIds.remove(record.id)
                                        } else {
                                            selectedPurchaseIds.add(record.id)
                                        }
                                    } else {
                                        // FIX 3: Assign the full 'purchase' (PurchaseWithItems) to state
                                        selectedPurchaseForDetails = purchase
                                        showPurchaseDetailsSheet = true
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isSelectionMode) {
                                        selectedPurchaseIds.clear()
                                        selectedPurchaseIds.add(record.id)
                                    } else {
                                        if (selectedPurchaseIds.contains(record.id)) {
                                            selectedPurchaseIds.remove(record.id)
                                        } else {
                                            selectedPurchaseIds.add(record.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }


        // Bottom padding to ensure last item isn't hidden by system navigation
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }

    }

    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = {
            showSheet = false
            isEditing = false
            selectedPurchaseIds.clear() // Clear selection when sheet closes
            viewModel.clearSelection()
        }
    ) {

    }

    AppBottomSheet(
        showSheet = showPurchaseDetailsSheet,
        onDismiss = {
            showPurchaseDetailsSheet = false
            selectedPurchaseForDetails = null
        }
    ) {

        selectedPurchaseForDetails?.let { details ->
            PurchaseInsightsSheet(record = details)
        }
    }
}


@Composable
fun PurchaseInsightsSheet(
    record: PurchaseWithItems,
    modifier: Modifier = Modifier
) {
    val entity = record.record
    val items = record.items
    val supplier = record.supplier?.party // Assuming PartyWithContacts has a 'party' property with a 'businessName'

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateString = dateFormatter.format(Date(entity.purchaseDate))

    val eWayBillDateString = entity.eWayBillDate?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "N/A"

    val metaDateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AxiomTheme.components.card.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // --- Header Section ---
        Text(
            text = supplier?.businessName ?: "Unknown Supplier",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title
        )
        Text(
            text = "Invoice: ${entity.supplierInvoiceNumber.ifBlank { "N/A" }}  •  $dateString",
            fontSize = 13.sp,
            color = AxiomTheme.components.card.subtitle,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Status Tags ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (entity.isItcEligible) StatusChip("ITC Eligible", Color(0xFF3B82F6)) // Standard Blue
            if (entity.reverseChargeApplicable) StatusChip("Reverse Charge", Color(0xFFF59E0B)) // Amber
            StatusChip(
                text = if (entity.supplyType == SupplyType.INTER_STATE) "Inter-State" else "Intra-State",
                color = AxiomTheme.components.card.title.copy(alpha = 0.6f) // Theme-adaptive gray
            )
            if (entity.isEdited) StatusChip("Edited", Color(0xFF10B981)) // Emerald
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. Logistics & Supply ---
        SectionTitle("Logistics & Supply")
        DetailCard {
            DetailRow("Place of Supply", entity.placeOfSupplyCode ?: "N/A")
            DetailRow("Shipped To", entity.shippedToAddress ?: "N/A")

            if (!entity.eWayBillNumber.isNullOrBlank() || !entity.vehicleNumber.isNullOrBlank()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = AxiomTheme.components.card.subtitle.copy(alpha = 0.2f))
                if (!entity.eWayBillNumber.isNullOrBlank()) {
                    DetailRow("E-Way Bill No.", entity.eWayBillNumber)
                    DetailRow("E-Way Bill Date", eWayBillDateString)
                }
                if (!entity.vehicleNumber.isNullOrBlank()) {
                    DetailRow("Vehicle No.", entity.vehicleNumber)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. Item Breakdown ---
        SectionTitle("Items (${items.size})")
        DetailCard {
            if (items.isEmpty()) {
                Text("No items recorded.", fontSize = 14.sp, color = AxiomTheme.components.card.subtitle)
            } else {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productNameSnapshot.ifBlank { "Unknown Item" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AxiomTheme.components.card.title
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            val hsnString = if (item.hsnCode.isNotBlank()) " | HSN: ${item.hsnCode}" else ""
                            Text(
                                text = "Qty: ${item.quantity} ${item.unit} @ ₹${item.costPrice}$hsnString",
                                fontSize = 12.sp,
                                color = AxiomTheme.components.card.subtitle
                            )
                        }
                        Text(
                            text = "₹ ${String.format(Locale.US, "%,.2f", item.taxableAmount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AxiomTheme.components.card.title
                        )
                    }
                    if (index < items.lastIndex) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = AxiomTheme.components.card.subtitle.copy(alpha = 0.2f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Financial Waterfall ---
        SectionTitle("Amount Breakdown")
        DetailCard {
            AmountRow("Item Subtotal", entity.itemSubTotal)

            if (entity.deliveryCharge > 0) AmountRow("Shipping Charges", entity.deliveryCharge)
            if (entity.extraCharges > 0) AmountRow("Extra Charges", entity.extraCharges)
            if (entity.globalDiscountAmount > 0) AmountRow("Discount", -entity.globalDiscountAmount, isDiscount = true)

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = AxiomTheme.components.card.subtitle.copy(alpha = 0.2f))

            AmountRow("Taxable Amount", entity.totalTaxableAmount, isBold = true)

            if (entity.cgstAmount > 0) AmountRow("CGST", entity.cgstAmount)
            if (entity.sgstAmount > 0) AmountRow("SGST", entity.sgstAmount)
            if (entity.igstAmount > 0) AmountRow("IGST", entity.igstAmount)
            if (entity.roundOff != 0.0) AmountRow("Round Off", entity.roundOff)

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = AxiomTheme.components.card.subtitle.copy(alpha = 0.2f))

            AmountRow("Grand Total", entity.grandTotal, isBold = true, color = Color(0xFF0D9488)) // Teal accent for total
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Record Metadata ---
        Text(
            text = "Record created on ${metaDateFormatter.format(Date(entity.createdAt))}",
            fontSize = 11.sp,
            color = AxiomTheme.components.card.subtitle,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        entity.updatedAt?.let {
            Text(
                text = "Last updated on ${metaDateFormatter.format(Date(it))}",
                fontSize = 11.sp,
                color = AxiomTheme.components.card.subtitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- Helper Composables ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = AxiomTheme.components.card.title,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.title.copy(alpha = 0.04f)) // Uses theme title color for subtle contrast
            .padding(16.dp),
        content = content
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AxiomTheme.components.card.subtitle,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AxiomTheme.components.card.title,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun AmountRow(
    label: String,
    amount: Double,
    isBold: Boolean = false,
    isDiscount: Boolean = false,
    color: Color = Color.Unspecified
) {
    val displayAmount = if (isDiscount) "- ₹ ${String.format(Locale.US, "%,.2f", Math.abs(amount))}"
    else "₹ ${String.format(Locale.US, "%,.2f", amount)}"

    val textColor = if (color != Color.Unspecified) color
    else if (isDiscount) Color(0xFFEF4444) // Red for negative
    else AxiomTheme.components.card.title

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 15.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isBold) textColor else AxiomTheme.components.card.subtitle
        )
        Text(
            text = displayAmount,
            fontSize = if (isBold) 15.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}