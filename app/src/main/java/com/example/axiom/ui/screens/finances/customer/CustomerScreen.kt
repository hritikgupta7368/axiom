package com.example.axiom.ui.screens.finances.customer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.EmptyState.EmptyScreen
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.cards.CustomerCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerBusinessStats
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerInvoiceRow
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerProductUsage
import com.example.axiom.ui.screens.finances.Invoice.components.CustomerTopProduct
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import com.example.axiom.ui.screens.finances.customer.components.CustomerForm
import com.example.axiom.ui.screens.finances.customer.components.CustomerListViewModel
import com.example.axiom.ui.screens.finances.customer.components.CustomerListViewModelFactory
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.purchase.formatDate
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current


    val viewModel: CustomerListViewModel = viewModel(
        factory = CustomerListViewModelFactory(context)
    )

    // all states
    val deletedItemIds = remember { mutableStateListOf<String>() }
    var showSheet by remember { mutableStateOf(false) }
    var showCustomerDetailsSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedCustomerIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedCustomerIds.isNotEmpty()
    val selectedCustomer by viewModel.selectedParty.collectAsState()
    val editingParty by viewModel.editingParty.collectAsState()


    BackHandler(enabled = isSelectionMode) {
        selectedCustomerIds.clear()
    }

    // DB FETCHING
    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val products by viewModel.productsPurchased.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val stats by viewModel.businessStats.collectAsState()

    fun closeSheetSmoothly() {
        scope.launch {
            sheetState.hide() // Animates the sheet sliding down
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showSheet = false // Only remove it from composition AFTER animation
                selectedCustomerIds.clear()
            }
        }
    }


    // --- Helpers ---
    fun openCreateSheet() {
        isEditing = false
        selectedCustomerIds.clear()
        viewModel.clearEditSelection()
        showSheet = true
    }

    fun openEditSheet() {
        val customerId = selectedCustomerIds.firstOrNull() ?: return
        isEditing = true
        viewModel.loadCustomerForEdit(customerId)
        showSheet = true

    }


    fun deleteSelected() {
        if (selectedCustomerIds.isNotEmpty()) {
            val ids = selectedCustomerIds.toList()
            selectedCustomerIds.clear()

            viewModel.deleteAll(ids)
        }
    }


    fun saveCustomer(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        if (isEditing) {
            viewModel.updateCustomerWithContacts(party, contacts)
        } else {
            viewModel.insertCustomerWithContacts(party, contacts)
        }
        closeSheetSmoothly()
    }

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = customers.isEmpty()



    AnimatedHeaderScrollView(
        largeTitle = "Customers",
        onAddClick = { openCreateSheet() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
        onBack = onBack,
        isSelectionMode = isSelectionMode,
        selectionCount = selectedCustomerIds.size,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
        onToggleSelectionMode = { selectedCustomerIds.clear() }
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
                        title = "No customers yet",
                        description = "Add customers to start creating invoices",
                        buttonText = "Add Customers",
                        onAdd = { openCreateSheet() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {

            items(
                items = customers,
                key = { it.id }
            ) {
                val isVisible = !deletedItemIds.contains(it.id)

                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(animationSpec = tween(500)) +
                            fadeOut(animationSpec = tween(500)),
                    enter = expandVertically() + fadeIn()
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {

                        CustomerCard(
                            customer = it,
                            isSelected = selectedCustomerIds.contains(it.id),
                            onClick = {

                                if (isSelectionMode) {
                                    if (selectedCustomerIds.contains(it.id)) {
                                        selectedCustomerIds.remove(it.id)
                                    } else {
                                        selectedCustomerIds.add(it.id)
                                    }
                                } else {
                                    viewModel.selectCustomer(it.id)
                                    showCustomerDetailsSheet = true
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!isSelectionMode) {
                                    selectedCustomerIds.clear()
                                    selectedCustomerIds.add(it.id)
                                } else {
                                    if (selectedCustomerIds.contains(it.id)) {
                                        selectedCustomerIds.remove(it.id)
                                    } else {
                                        selectedCustomerIds.add(it.id)
                                    }
                                }
                            }
                        )
                    }
                }

            }
        }

    }

    // --- Bottom Sheet ---
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = {
            showSheet = false
        }
    ) {
        CustomerForm(
            partyWithContacts = editingParty,
            isEditing = isEditing,

            onSave = { party, contacts ->
                saveCustomer(party, contacts)
            },
            onCancel = {
                closeSheetSmoothly()
            }
        )
    }

    AppBottomSheet(
        showSheet = showCustomerDetailsSheet,
        onDismiss = { showCustomerDetailsSheet = false }
    ) {

        selectedCustomer?.let {

            CustomerInsightsSheet(
                customer = it,
                stats = stats,
                topProducts = topProducts,
                products = products,
                invoices = invoices
            )
        }
    }


}


@Composable
fun MetricCard(
    title: String,
    value: String
) {

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AxiomTheme.components.card.background)
            .border(
                1.dp,
                Color(0xFF222222),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {

        Text(
            value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = AxiomTheme.components.card.title
        )

        Text(
            title,
            fontSize = 11.sp,
            color = AxiomTheme.components.card.subtitle
        )
    }
}

@Composable
fun CustomerInsightsSheet(
    customer: PartyWithContacts,
    stats: CustomerBusinessStats?,
    topProducts: List<CustomerTopProduct>,
    products: List<CustomerProductUsage>,
    invoices: List<CustomerInvoiceRow>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            // Kept padding at the top, removed bottom padding to allow scrolling flush
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Slightly larger gap between sections
    ) {

        // --- 1. HEADER SECTION ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Avatar Initial
                val initial = customer.party.businessName.take(1).uppercase()
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AxiomTheme.components.avatar.background)
                        .border(1.dp, AxiomTheme.components.avatar.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.colors.accentBlue
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name & GST
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.party.businessName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AxiomTheme.components.card.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.party.gstNumber ?: "Unregistered Customer",
                        fontSize = 13.sp,
                        color = AxiomTheme.components.card.subtitle
                    )
                }

                // State Code Badge
                Box(
                    modifier = Modifier
                        .background(AxiomTheme.colors.accentBlueBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "State: ${customer.party.stateCode ?: "N/A"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AxiomTheme.colors.accentBlue
                    )
                }
            }
        }

        // --- 2. BUSINESS SUMMARY ---
        item {
            Column {
                SectionHeader("Business Summary", Icons.Filled.Info)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weighted equally so they fill the screen perfectly
                    InsightMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Sales",
                        value = "₹${stats?.totalSales ?: 0}"
                    )
                    InsightMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Invoices",
                        value = "${stats?.totalInvoices ?: 0}"
                    )
                    InsightMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Value",
                        value = "₹${stats?.avgInvoiceValue ?: 0}"
                    )
                }
            }
        }

        // --- 3. TOP PRODUCTS (Only show if data exists) ---
        if (topProducts.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("Top Products", Icons.Outlined.Star)

                    DataCardContainer {
                        topProducts.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${product.productName}",
                                    color = AxiomTheme.components.card.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Qty: ${product.totalQty}",
                                    color = AxiomTheme.components.card.subtitle,
                                    fontSize = 13.sp
                                )
                            }
                            // Add divider unless it's the last item
                            if (index < topProducts.lastIndex) {
                                Divider(color = AxiomTheme.components.card.border, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. ALL PRODUCTS PURCHASED ---
        if (products.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("Products Purchased", Icons.Outlined.ShoppingCart)

                    DataCardContainer {
                        products.forEachIndexed { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.productName,
                                        color = AxiomTheme.components.card.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "HSN: ${product.hsn}",
                                        fontSize = 12.sp,
                                        color = AxiomTheme.components.card.mutedText
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${product.totalSales}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AxiomTheme.components.card.title
                                    )
                                    Text(
                                        text = "Qty: ${product.totalQty}",
                                        color = AxiomTheme.components.card.subtitle,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (index < products.lastIndex) {
                                Divider(color = AxiomTheme.components.card.border, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // --- 5. RECENT INVOICES ---
        if (invoices.isNotEmpty()) {
            item {
                Column {
                    SectionHeader("Recent Invoices", Icons.Outlined.DateRange)

                    DataCardContainer {
                        invoices.forEachIndexed { index, invoice ->

                            val isCancelled = invoice.status == InvoiceStatus.CANCELLED
                            val isDraft = invoice.status == InvoiceStatus.DRAFT

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (isCancelled) 0.6f else 1f) // Dim the entire row if cancelled
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = invoice.invoiceNumber,
                                            color = AxiomTheme.components.card.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                        )

                                        // Inline Status Badges
                                        if (isCancelled) {
                                            Text(
                                                text = "CANCELLED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AxiomTheme.colors.error,
                                                modifier = Modifier
                                                    .background(AxiomTheme.colors.error.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        } else if (isDraft) {
                                            Text(
                                                text = "DRAFT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AxiomTheme.components.card.subtitle,
                                                modifier = Modifier
                                                    .background(AxiomTheme.components.card.border, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = formatDate(invoice.invoiceDate),
                                        fontSize = 12.sp,
                                        color = AxiomTheme.components.card.subtitle
                                    )
                                }

                                Text(
                                    text = "₹${invoice.grandTotal}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None,
                                    color = when {
                                        isCancelled -> AxiomTheme.components.card.mutedText
                                        isDraft -> AxiomTheme.components.card.title
                                        else -> AxiomTheme.colors.accentGreen // ACTIVE gets the green highlight
                                    }
                                )
                            }
                            if (index < invoices.lastIndex) {
                                Divider(color = AxiomTheme.components.card.border, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AxiomTheme.components.card.mutedText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AxiomTheme.components.card.subtitle,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun InsightMetricCard(modifier: Modifier = Modifier, title: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = AxiomTheme.components.card.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DataCardContainer(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}






