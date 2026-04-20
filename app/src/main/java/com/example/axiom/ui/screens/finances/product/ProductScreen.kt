package com.example.axiom.ui.screens.finances.product

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.axiom.ui.components.shared.cards.ProductCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.product.components.ProductDao
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import com.example.axiom.ui.screens.finances.product.components.ProductForm
import com.example.axiom.ui.screens.finances.product.components.ProductInvoiceUsage
import com.example.axiom.ui.screens.finances.product.components.ProductListViewModel
import com.example.axiom.ui.screens.finances.product.components.ProductListViewModelFactory
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current


    val viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(context)
    )

    val profitStats by viewModel.selectedProductStats.collectAsState()

    // all states
    val deletedItemIds = remember { mutableStateListOf<String>() }
    var showSheet by remember { mutableStateOf(false) }
    var showProductDetailsSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    val selectedProductIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedProductIds.isNotEmpty()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()

    BackHandler(enabled = isSelectionMode) {
        selectedProductIds.clear()
    }

    // DB FETCHING
    val products by viewModel.products.collectAsState()


    val invoices by selectedProduct
        ?.let { viewModel.getInvoicesForProduct(it.id) }
        ?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val categories by viewModel.categories.collectAsState()


    val groupedProducts = remember(products) {
        products
            .groupBy { it.category.ifBlank { "Uncategorized" } }
            .toSortedMap()
    }


    // --- Helpers ---
    fun openCreateSheet() {
        isEditing = false
        selectedProductIds.clear()
        viewModel.clearEditSelection()
        showSheet = true
    }

    fun openEditSheet() {
        val productId = selectedProductIds.firstOrNull() ?: return
        isEditing = true
        viewModel.loadProductForEdit(productId)
        showSheet = true
    }


    fun deleteSelected() {
        if (selectedProductIds.isNotEmpty()) {
            val ids = selectedProductIds.toList()
            selectedProductIds.clear()

            viewModel.deleteAll(ids)
        }
    }

    fun closeSheetSmoothly() {
        scope.launch {
            sheetState.hide() // Animates the sheet sliding down
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showSheet = false // Only remove it from composition AFTER animation
                selectedProductIds.clear()
            }
        }
    }

    fun saveProduct(updatedData: ProductEntity) {
        if (isEditing) {
            viewModel.updateProduct(updatedData)
        } else {
            viewModel.insertProduct(updatedData)
        }
        closeSheetSmoothly()
    }

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = products.isEmpty()



    AnimatedHeaderScrollView(
        largeTitle = "Products",
        onAddClick = { openCreateSheet() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
        onBack = onBack,
        isSelectionMode = isSelectionMode,
        selectionCount = selectedProductIds.size,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
        onToggleSelectionMode = { selectedProductIds.clear() }

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
                        title = "No products yet",
                        description = "Add products to start creating invoices",
                        buttonText = "Add Products",
                        onAdd = { openCreateSheet() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {

            groupedProducts.forEach { (category, itemsInCategory) ->
                // CATEGORY HEADER
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(
                    items = itemsInCategory,
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
                            ProductCard(
                                product = it,
                                isSelected = selectedProductIds.contains(it.id),
                                onClick = {
                                    if (isSelectionMode) {
                                        if (selectedProductIds.contains(it.id)) {
                                            selectedProductIds.remove(it.id)
                                        } else {
                                            selectedProductIds.add(it.id)
                                        }
                                    } else {
//                                        selectedProduct = it
                                        viewModel.selectProduct(it.id)
                                        showProductDetailsSheet = true
                                    }
                                },
                                // --- LONG PRESS ---
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isSelectionMode) {
                                        selectedProductIds.clear()
                                        selectedProductIds.add(it.id)
                                    } else {
                                        if (selectedProductIds.contains(it.id)) {
                                            selectedProductIds.remove(it.id)
                                        } else {
                                            selectedProductIds.add(it.id)
                                        }
                                    }
                                }
                            )
                        }
                    }

                }
            }
        }


    }

    // --- Bottom Sheets ---
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = { showSheet = false }
    ) {

        ProductForm(
            product = editingProduct,
            isEditing = isEditing,
            category = categories,
            onSave = { saveProduct(it) },
            onCancel = { closeSheetSmoothly() }
        )
    }

    AppBottomSheet(
        showSheet = showProductDetailsSheet,
        onDismiss = { showProductDetailsSheet = false }
    ) {
        selectedProduct?.let { product ->
            ProductInvoicesSheet(
                product = product,
                invoices = invoices,
                profitStats = profitStats
            )
        }
    }


}

@Composable
fun ProductInvoicesSheet(
    product: ProductEntity,
    invoices: List<ProductInvoiceUsage>,
    profitStats: ProductDao.ProductProfitStats? // Allow nullability
) {
    // 1. Crash-Proof Fallback Data
    val safeStats = profitStats ?: ProductDao.ProductProfitStats(product.id, 0.0, 0.0, 0.0, 0.0)

    val profitPerUnit = product.sellingPrice - product.costPrice
    val margin = if (product.costPrice > 0) ((profitPerUnit / product.costPrice) * 100) else 0.0
    val isLoss = profitPerUnit < 0

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // ---------- HEADER ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AxiomTheme.components.card.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AxiomTheme.components.card.background)
                                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 12.sp,
                                color = AxiomTheme.components.card.subtitle
                            )
                        }
                        Text(
                            text = "  •  ${product.unit}",
                            fontSize = 12.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "HSN: ${product.hsn}",
                        fontSize = 12.sp,
                        color = AxiomTheme.components.card.mutedText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(product.sellingPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.components.card.title
                    )
                    if (product.lastSellingPrice > 0) {
                        Text(
                            text = "Last ${formatCurrency(product.lastSellingPrice)}",
                            fontSize = 12.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            }
        }

        // ---------- FINANCIAL HIGHLIGHTS ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HighlightCard(
                    modifier = Modifier.weight(1f),
                    label = "COST PRICE",
                    value = formatCurrency(product.costPrice),
                    labelColor = AxiomTheme.components.card.subtitle,
                    valueColor = AxiomTheme.components.card.title,
                    bgColor = AxiomTheme.components.card.background,
                    borderColor = AxiomTheme.components.card.border
                )

                HighlightCard(
                    modifier = Modifier.weight(1f),
                    label = if (isLoss) "LOSS / UNIT" else "PROFIT / UNIT",
                    value = formatCurrency(profitPerUnit.absoluteValue),
                    labelColor = if (isLoss) AxiomTheme.colors.red else AxiomTheme.colors.green,
                    valueColor = if (isLoss) AxiomTheme.colors.red else AxiomTheme.colors.green,
                    bgColor = if (isLoss) AxiomTheme.colors.mutedRed else AxiomTheme.colors.mutedGreen,
                    borderColor = if (isLoss) AxiomTheme.colors.red.copy(alpha = 0.3f) else AxiomTheme.colors.green.copy(alpha = 0.3f)
                )

                HighlightCard(
                    modifier = Modifier.weight(1f),
                    label = "MARGIN",
                    value = "${margin.toInt()}%",
                    labelColor = AxiomTheme.components.card.subtitle,
                    valueColor = AxiomTheme.components.card.title,
                    bgColor = AxiomTheme.components.card.background,
                    borderColor = AxiomTheme.components.card.border
                )
            }
        }

        // ---------- ANALYTICS GRID ----------
        item {
            Column {
                Text(
                    text = "Analytics Overview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(
                        Modifier.weight(1f),
                        "Total Units Sold",
                        safeStats.totalUnitsSold.toString()
                    )
                    AnalyticsCard(
                        Modifier.weight(1f),
                        "Total Profit Earned",
                        formatCurrency(safeStats.totalProfit),
                        valueColor = if (safeStats.totalProfit < 0) AxiomTheme.colors.red else AxiomTheme.colors.green
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(Modifier.weight(1f), "Peak Price", formatCurrency(product.peakPrice))
                    AnalyticsCard(Modifier.weight(1f), "Floor Price", formatCurrency(product.floorPrice))
                }
            }
        }

        // ---------- INVOICE LIST ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Recent Usage",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title
                )
                Text(
                    "${invoices.size} Invoices",
                    fontSize = 12.sp,
                    color = AxiomTheme.components.card.subtitle
                )
            }
        }

        if (invoices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AxiomTheme.components.card.background)
                        .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No usage history",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AxiomTheme.components.card.subtitle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "This product hasn't been added to any invoices yet.",
                            fontSize = 12.sp,
                            color = AxiomTheme.components.card.mutedText
                        )
                    }
                }
            }
        } else {
            items(invoices, key = { it.invoiceId }) { invoice ->
                InvoiceListItem(invoice)
            }
        }
    }
}

// --- Sub-components & Helpers ---

@Composable
private fun HighlightCard(
    modifier: Modifier, label: String, value: String,
    labelColor: Color, valueColor: Color, bgColor: Color, borderColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = labelColor, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AnalyticsCard(
    modifier: Modifier,
    title: String,
    value: String,
    valueColor: Color = AxiomTheme.components.card.title
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 11.sp, color = AxiomTheme.components.card.subtitle)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun InvoiceListItem(invoice: ProductInvoiceUsage) {
    val isCancelled = invoice.invoiceStatus.name == "CANCELLED"
    val isDraft = invoice.invoiceStatus.name == "DRAFT"

    val textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(14.dp))
            .alpha(if (isCancelled) 0.6f else 1f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceNumber,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = AxiomTheme.components.card.title,
                        textDecoration = textDecoration
                    )

                    if (isCancelled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AxiomTheme.colors.error.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("CANCELLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.colors.error)
                        }
                    } else if (isDraft) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AxiomTheme.components.card.border)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("DRAFT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.components.card.subtitle)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = invoice.customerName ?: "Unknown Customer",
                    fontSize = 12.sp, color = AxiomTheme.components.card.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(invoice.taxableAmount),
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title,
                    textDecoration = textDecoration
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = formatDate(invoice.invoiceDate), fontSize = 10.sp, color = AxiomTheme.components.card.mutedText)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val qtyDisplay = if (invoice.quantity % 1 == 0.0) invoice.quantity.toInt() else invoice.quantity
            SmallTag("Qty $qtyDisplay")
            SmallTag("@ ${formatCurrency(invoice.sellingPrice)}")
        }
    }
}

@Composable
private fun SmallTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(6.dp))
            .background(AxiomTheme.colors.background) // Uses the main screen background color for contrast
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = AxiomTheme.components.card.subtitle
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}