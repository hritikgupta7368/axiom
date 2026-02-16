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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.Product
import com.example.axiom.data.finances.ProductViewModel
import com.example.axiom.data.finances.ProductViewModelFactory
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Standard GST Unit List
val GST_UNITS = listOf(
    "BAG" to "Bag",
    "BAL" to "Bale",
    "BDL" to "Bundle",
    "BKL" to "Buckles",
    "BOU" to "Billions of Units",
    "BOX" to "Box",
    "BTL" to "Bottle",
    "BUN" to "Bunch",
    "CAN" to "Can",
    "CBM" to "Cubic Meter",
    "CCM" to "Cubic Centimeter",
    "CMS" to "Centimeter",
    "CTN" to "Carton",
    "DOZ" to "Dozen",
    "DRM" to "Drum",
    "GGR" to "Great Gross",
    "GMS" to "Gram",
    "GRS" to "Gross",
    "GYD" to "Gross Yard",
    "KGS" to "Kilogram",
    "KLR" to "Kilolitre",
    "KME" to "Kilometre",
    "MLT" to "Millilitre",
    "MTR" to "Metre",
    "MTS" to "Metric Tonne",
    "NOS" to "Number",
    "PAC" to "Pack",
    "PCS" to "Piece",
    "PRS" to "Pair",
    "QTL" to "Quintal",
    "ROL" to "Roll",
    "SET" to "Set",
    "SQF" to "Square Foot",
    "SQM" to "Square Metre",
    "SQY" to "Square Yard",
    "TBS" to "Tablets",
    "TGM" to "Ten Gross",
    "THD" to "Thousand",
    "TON" to "Tonne",
    "TUB" to "Tube",
    "UGS" to "US Gallon",
    "UNT" to "Unit",
    "YDS" to "Yard",
    "OTH" to "Other"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(context)
    )

    val products by viewModel.products.collectAsState(initial = emptyList())

    val groupedProducts = remember(products) {
        products
            .filter { it.active }
            .groupBy { product ->
                product.category.ifBlank { "Uncategorized" }
            }
            .toSortedMap(compareBy { it }) // optional: alphabetical categories
    }

    // Selection State
    var selectedProductId by remember { mutableStateOf<String?>(null) }

    // Animation State
    val deletedItemIds = remember { mutableStateListOf<String>() }

    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Form State Holder
    var formData by remember { mutableStateOf(Product()) }

    // --- Helpers ---
    fun openCreateSheet() {
        formData = Product() // Reset form
        isEditing = false
        selectedProductId = null
        showSheet = true
    }

    fun openEditSheet() {
        val selected = products.find { it.id == selectedProductId }
        if (selected != null) {
            formData = selected
            isEditing = true
            showSheet = true
        }
    }

    fun deleteSelected() {
        val idToDelete = selectedProductId
        if (idToDelete != null) {
            // Clear selection immediately for UI feedback
            selectedProductId = null

            scope.launch {
                // Trigger animation
                deletedItemIds.add(idToDelete)

                // Wait for animation to complete
                delay(500)

                // Perform actual soft delete in DB
                viewModel.deleteById(idToDelete)

                // Cleanup ID from tracking
                deletedItemIds.remove(idToDelete)
            }
        }
    }

    fun saveProduct(updatedData: Product) {
        if (isEditing) {
            viewModel.update(updatedData)
        } else {
            viewModel.insert(updatedData)
        }
        showSheet = false
        selectedProductId = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selectedProductId == null) {
                // Default Top Bar
                TopAppBar(
                    title = { Text("Products", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { openCreateSheet() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Product",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            } else {
                // Selection Mode Top Bar
                TopAppBar(
                    title = { Text("1 Selected", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedProductId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { openEditSheet() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { deleteSelected() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->

        // --- Content ---
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp // Forced to zero
                    )
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                // new data rendering
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
                formData = formData,
                isEditing = isEditing,
                onSave = { saveProduct(it) },
                onCancel = { showSheet = false }
            )
        }
    }
}

// --- List Item Component ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: Product,
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

@Composable
fun ProductForm(
    formData: Product,
    isEditing: Boolean,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit
) {
    // Local state for form fields to handle validation updates immediately
    var currentData by remember { mutableStateOf(formData) }

    // Temp state for price text field to handle string/double conversion smoothly
    var priceText by remember(formData.sellingPrice) {
        mutableStateOf(if (formData.sellingPrice == 0.0 && !isEditing) "" else formData.sellingPrice.toString())
    }

    // Validation Errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var hsnError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var unitError by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showUnitDialog by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var isValid = true

        // Name Validation
        if (currentData.name.isBlank()) {
            nameError = "Name is required"
            isValid = false
        } else {
            nameError = null
        }

        // HSN Validation
        if (currentData.hsn.isBlank()) {
            hsnError = "HSN Code is required"
            isValid = false
        } else {
            hsnError = null
        }

        // Price Validation
        if (priceText.isBlank()) {
            priceError = "Selling Price is required"
            isValid = false
        } else {
            val price = priceText.toDoubleOrNull()
            if (price == null || price < 0) {
                priceError = "Invalid Price"
                isValid = false
            } else {
                priceError = null
                currentData = currentData.copy(sellingPrice = price)
            }
        }

        // Unit Validation
        if (currentData.unit.isBlank()) {
            unitError = "Unit is required"
            isValid = false
        } else {
            unitError = null
        }

        return isValid
    }

    // Unit Selection Dialog
    if (showUnitDialog) {
        UnitSelectionDialog(
            onDismiss = { showUnitDialog = false },
            onUnitSelected = { code ->
                currentData = currentData.copy(unit = code)
                if (unitError != null) unitError = null
                showUnitDialog = false
            }
        )
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = screenHeight * 0.85f // Max 85% of screen height

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditing) "Edit Product" else "Add New Product",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Name
        ProductInput(
            value = currentData.name,
            onValueChange = {
                currentData = currentData.copy(name = it); if (nameError != null) nameError = null
            },
            label = "Product Name *",
            icon = Icons.Default.Info,
            isError = nameError != null,
            errorMessage = nameError
        )

        // HSN Code
        ProductInput(
            value = currentData.hsn,
            onValueChange = {
                currentData = currentData.copy(hsn = it); if (hsnError != null) hsnError = null
            },
            label = "HSN Code *",
            icon = Icons.Default.Info,
            keyboardType = KeyboardType.Number,
            isError = hsnError != null,
            errorMessage = hsnError
        )

        // Selling Price
        ProductInput(
            value = priceText,
            onValueChange = {
                if (it.all { char -> char.isDigit() || char == '.' }) {
                    priceText = it
                    if (priceError != null) priceError = null
                }
            },
            label = "Selling Price *",
            icon = Icons.Default.Info,
            keyboardType = KeyboardType.Decimal,
            isError = priceError != null,
            errorMessage = priceError
        )

        // Unit Selection (Clickable Field)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = currentData.unit,
                onValueChange = {}, // Read-only
                readOnly = true,
                label = { Text("Unit *") },
                placeholder = { Text("Select Unit") },
                leadingIcon = { Icon(Icons.Default.Info, null) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = unitError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )
            // Overlay to capture clicks
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showUnitDialog = true }
            )
        }
        if (unitError != null) {
            Text(
                text = unitError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Category (Optional)
        ProductInput(
            value = currentData.category,
            onValueChange = { currentData = currentData.copy(category = it) },
            label = "Category (Optional)",
            icon = Icons.Default.Menu
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (validate()) {
                        onSave(currentData)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Update" else "Save")
            }
        }
    }
}

@Composable
fun UnitSelectionDialog(onDismiss: () -> Unit, onUnitSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUnits = remember(searchQuery) {
        GST_UNITS.filter {
            it.first.contains(searchQuery, ignoreCase = true) ||
                    it.second.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Unit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search unit (e.g., KGS, Pieces)") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredUnits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(24.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No matching units found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(filteredUnits) { (code, name) ->
                            ListItem(
                                headlineContent = { Text(name, fontWeight = FontWeight.Medium) },
                                supportingContent = {
                                    Text(
                                        "Code: $code",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier
                                    .clickable { onUnitSelected(code) }
                                    .fillMaxWidth()
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun ProductInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    allCaps: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (allCaps) onValueChange(it.uppercase()) else onValueChange(it) },
            label = { Text(label) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            trailingIcon = if (isError) {
                {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}