package com.example.axiom.ui.screens.finances.product.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.bottomSheet.SearchBar
import com.example.axiom.ui.components.shared.bottomSheet.SheetHeadingText
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.util.UUID


// for product selection
@Composable
fun ProductListSheet(
    products: List<ProductBasic>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedIds: Set<String>,
    onToggleSelection: (String) -> Unit,
    onConfirmSelection: () -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SheetHeadingText("Select Product", modifier = Modifier.weight(1f))

            AppIconButton(
                icon = AppIcons.Add,
                contentDescription = "Add",
                onClick = onCreateClick,
                enclosedInCircle = true
            )
        }

        Spacer(Modifier.height(12.dp))


        SearchBar(
            containerWidth = 350.dp,
            tint = Color.White,
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search Products",

            )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
            items(
                items = products,
                key = { it.id }
            ) { product ->

                val isSelected = selectedIds.contains(product.id)
                val borderColor =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline

                val backgroundColor =
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface


                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { onToggleSelection(product.id) },
                            onLongClick = { }
                        )
                        .background(backgroundColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
//                                if (product.category.isNotEmpty()) {
//                                    Text(
//                                        text = " • ${product.category}",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
//                                            alpha = 0.7f
//                                        ),
//                                        maxLines = 1,
//                                        overflow = TextOverflow.Ellipsis,
//                                        modifier = Modifier.weight(1f, fill = false)
//                                    )
//                                }
                            }

                        }

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
        }

        Spacer(Modifier.height(12.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onConfirmSelection,
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Add (${selectedIds.size})")
            }
        }
    }
}


@Composable
fun ProductForm(
    productId: String? = null,
    isEditing: Boolean,
    loadProduct: (suspend (String) -> ProductEntity?)? = null,
    onSave: (ProductEntity) -> Unit,
    onCancel: () -> Unit
) {

    // Local state for form fields
    var currentData by remember {
        mutableStateOf(
            ProductEntity(id = productId ?: UUID.randomUUID().toString())
        )
    }
    var isLoaded by remember { mutableStateOf(!isEditing) }

    LaunchedEffect(productId) {
        if (isEditing && productId != null && loadProduct != null) {
            val loaded = loadProduct(productId)
            if (loaded != null) {
                currentData = loaded
            }
            isLoaded = true
        }
    }


    // Temp state for price text fields to handle string/double conversion smoothly
    var sellingPriceText by remember(currentData.sellingPrice) {
        mutableStateOf(
            if (currentData.sellingPrice == 0.0 && !isEditing)
                ""
            else
                currentData.sellingPrice.toString()
        )
    }
    var costPriceText by remember(currentData.costPrice) {
        mutableStateOf(if (currentData.costPrice == 0.0 && !isEditing) "" else currentData.costPrice.toString())
    }

    // Validation Errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var hsnError by remember { mutableStateOf<String?>(null) }
    var sellingPriceError by remember { mutableStateOf<String?>(null) }
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

        // Selling Price Validation
        if (sellingPriceText.isBlank()) {
            sellingPriceError = "Selling Price is required"
            isValid = false
        } else {
            val price = sellingPriceText.toDoubleOrNull()
            if (price == null || price < 0) {
                sellingPriceError = "Invalid Price"
                isValid = false
            } else {
                sellingPriceError = null
                currentData = currentData.copy(sellingPrice = price)
            }
        }

        // Cost Price (Optional, but validate if entered)
        if (costPriceText.isNotBlank()) {
            val cPrice = costPriceText.toDoubleOrNull()
            if (cPrice != null && cPrice >= 0) {
                currentData = currentData.copy(costPrice = cPrice)
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
    val maxHeight = screenHeight * 0.85f

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

        // Basic Info
        Input(
            value = currentData.name,
            onValueChange = {
                currentData = currentData.copy(name = it); if (nameError != null) nameError = null
            },
            label = "Product Name *",
            icon = Icons.Default.Info,
            isError = nameError != null,

            )

        Input(
            value = currentData.description ?: "",
            onValueChange = { currentData = currentData.copy(description = it) },
            label = "Description (Optional)",
            icon = Icons.Default.Menu,
            singleLine = false,
            minLines = 2
        )


        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.hsn,
                    onValueChange = {
                        currentData = currentData.copy(hsn = it); if (hsnError != null) hsnError =
                        null
                    },
                    label = "HSN Code *",
                    icon = Icons.Default.Info,
                    keyboardType = KeyboardType.Number,
                    isError = hsnError != null,

                    )
            }


            // Unit Selection (Clickable Field)
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = currentData.unit,
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    label = { Text("Unit *") },
                    placeholder = { Text("Select") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = unitError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showUnitDialog = true }
                )
            }
        }
        if (unitError != null) {
            Text(
                text = unitError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Categorization
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.category,
                    onValueChange = { currentData = currentData.copy(category = it) },
                    label = "Category",
                    icon = Icons.Default.List
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.brand ?: "",
                    onValueChange = { currentData = currentData.copy(brand = it) },
                    label = "Brand",
                    icon = Icons.Default.Star
                )
            }
        }

        // Pricing
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = costPriceText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            costPriceText = it
                        }
                    },
                    label = "Cost Price",
                    keyboardType = KeyboardType.Decimal
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = sellingPriceText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            sellingPriceText = it
                            if (sellingPriceError != null) sellingPriceError = null
                        }
                    },
                    label = "Selling Price *",
                    keyboardType = KeyboardType.Decimal,
                    isError = sellingPriceError != null,

                    )
            }
        }

        // Media / Links
        Input(
            value = currentData.imageUrl ?: "",
            onValueChange = { currentData = currentData.copy(imageUrl = it) },
            label = "Image URL (Optional)",
            keyboardType = KeyboardType.Uri
        )

        Input(
            value = currentData.productLink ?: "",
            onValueChange = { currentData = currentData.copy(productLink = it) },
            label = "Product Link (Optional)",
            keyboardType = KeyboardType.Uri
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
                        // Update the timestamp before saving
                        val finalData = if (isEditing) {
                            currentData.copy(updatedAt = System.currentTimeMillis())
                        } else {
                            currentData // createdAt is already handled by default val
                        }
                        onSave(finalData)
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


// wrapper
enum class ProductSheetMode {
    LIST,
    CREATE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProductListSheetWrapper(
    onConfirmSelection: (List<ProductBasic>) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(context)
    )

    val products by viewModel.products.collectAsState()

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var mode by remember { mutableStateOf(ProductSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "ProductSheetSwitch"
    ) { currentMode ->

        when (currentMode) {

            ProductSheetMode.LIST -> {

                ProductListSheet(
                    products = products,
                    searchQuery = viewModel.searchQuery.collectAsState().value,
                    selectedIds = selectedIds,
                    onSearchChange = viewModel::updateSearchQuery,
                    onToggleSelection = { id ->
                        selectedIds =
                            if (selectedIds.contains(id))
                                selectedIds - id
                            else
                                selectedIds + id
                    },
                    onConfirmSelection = {
                        val selected =
                            products.filter { selectedIds.contains(it.id) }

                        onConfirmSelection(selected)
                        selectedIds = emptySet()
                    },
                    onCreateClick = {
                        mode = ProductSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            ProductSheetMode.CREATE -> {


                ProductForm(
                    productId = "",
                    isEditing = false,
                    onSave = { product ->
                        viewModel.insertProduct(product)
                        mode = ProductSheetMode.LIST
                    },
                    onCancel = { mode = ProductSheetMode.LIST }
                )
            }
        }
    }
}

