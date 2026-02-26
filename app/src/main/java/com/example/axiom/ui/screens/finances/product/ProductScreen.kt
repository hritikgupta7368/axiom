package com.example.axiom.ui.screens.finances.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.product.components.ProductBasic
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import com.example.axiom.ui.screens.finances.product.components.ProductForm
import com.example.axiom.ui.screens.finances.product.components.ProductListViewModel
import com.example.axiom.ui.screens.finances.product.components.ProductListViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(context)
    )
    val products by viewModel.products.collectAsState()

    val groupedProducts = remember(products) {
        products
            .groupBy { product ->
                product.category.ifBlank { "Uncategorized" }
            }
            .toSortedMap(compareBy { it }) // optional: alphabetical categories
    }

    var selectedProductId by remember { mutableStateOf<String?>(null) }
    val deletedItemIds = remember { mutableStateListOf<String>() }

    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }


    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSelectionMode = selectedProductId != null


    // --- Helpers ---
    fun openCreateSheet() {
        isEditing = false
        selectedProductId = null
        showSheet = true
    }

    fun openEditSheet() {
        val selected = products.find { it.id == selectedProductId }
        if (selected != null) {
            isEditing = true
            showSheet = true
        }
       
    }

    fun deleteSelected() {
        val idToDelete = selectedProductId
        if (idToDelete != null) {
            selectedProductId = null

            scope.launch {
                deletedItemIds.add(idToDelete)
                delay(500)
                viewModel.deleteProduct(idToDelete)
                deletedItemIds.remove(idToDelete)
                selectedProductId = null
            }
        }
    }

    fun saveProduct(updatedData: ProductEntity) {
        if (isEditing) {
            viewModel.updateProduct(updatedData)
        } else {
            viewModel.insertProduct(updatedData)
        }
        showSheet = false
        selectedProductId = null
    }

    AnimatedHeaderScrollView(
        largeTitle = "Products",
        onAddClick = { openCreateSheet() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
        onBack = onBack,
        isSelectionMode = isSelectionMode,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
    ) {
        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            groupedProducts.forEach { (category, itemsInCategory) ->

                // CATEGORY HEADER
                item(key = "header_$category") {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }

                // PRODUCTS UNDER CATEGORY
                items(
                    itemsInCategory,
                    key = { it.id }
                ) { product ->

                    val isVisible = !deletedItemIds.contains(product.id)

                    AnimatedVisibility(
                        visible = isVisible,
                        exit = shrinkVertically(animationSpec = tween(500)) +
                                fadeOut(animationSpec = tween(500)),
                        enter = expandVertically() + fadeIn()
                    ) {
                        ProductCard(
                            product = product,
                            isSelected = product.id == selectedProductId,
                            onSelect = {
                                selectedProductId =
                                    if (selectedProductId == product.id) null else product.id
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
        onDismiss = { showSheet = false }
    ) {
        ProductForm(
            productId = selectedProductId,
            isEditing = isEditing,
            loadProduct = { id -> viewModel.getProductById(id) },
            onSave = { saveProduct(it) },
            onCancel = { showSheet = false }
        )
    }


}


// --- List Item Component ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: ProductBasic,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // Dynamic colors based on selection
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = 0.5f
        )
    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { if (isSelected) onSelect() },
                onLongClick = { onSelect() }
            )
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer, // Different accent than customers
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HSN: ${product.hsn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.4f, fill = false)
                    )
                    if (product.category.isNotEmpty()) {
                        Text(
                            text = " • ${product.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            // Price Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${product.sellingPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "per ${product.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Form Content Component ---

