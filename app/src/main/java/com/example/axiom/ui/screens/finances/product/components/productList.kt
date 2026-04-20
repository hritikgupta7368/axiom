package com.example.axiom.ui.screens.finances.product.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.Dropdown.Dropdown
import com.example.axiom.ui.components.shared.ImagePicker.CompactImagePicker
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.bottomSheet.SearchBar
import com.example.axiom.ui.components.shared.bottomSheet.SheetHeadingText
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.theme.AxiomTheme
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
                enclosedInCircle = true,
                circleColor = AxiomTheme.components.card.title,
                tint = AxiomTheme.components.card.background
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
                    if (isSelected) AxiomTheme.components.card.selectedBorder
                    else AxiomTheme.components.card.border

                val backgroundColor =
                    if (isSelected) AxiomTheme.components.card.selectedBackground
                    else AxiomTheme.components.card.background


                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onToggleSelection(product.id) },
                            onLongClick = { }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
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
                                    imageVector = Icons.Default.ShoppingCart,
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
                                color = AxiomTheme.components.card.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HSN: ${product.hsn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AxiomTheme.components.card.subtitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(0.4f, fill = false)
                                )
//
                            }

                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${product.sellingPrice}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AxiomTheme.components.card.selectedBorder
                            )
                            Text(
                                text = "per ${product.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AxiomTheme.components.card.subtitle
                            )
                        }

                    }
                }

            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                text = "Cancel",
                onClick = onBack,
                variant = ButtonVariant.Gray,
                modifier = Modifier.weight(1f)
            )

            Button(
                text = "Add (${selectedIds.size})",
                onClick = onConfirmSelection,
                icon = Icons.Default.Check,
                variant = ButtonVariant.White,
                modifier = Modifier.weight(1f)
            )

        }


    }
}


@Composable
fun ProductForm(
    product: ProductEntity?,
    isEditing: Boolean,
    category: List<String>,
    onSave: (ProductEntity) -> Unit,
    onCancel: () -> Unit
) {

    val haptic = LocalHapticFeedback.current
    // Dialog state
    var showUnitDialog by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    // Temp state for price text fields to handle string/double conversion smoothly
    var sellingPriceText by remember { mutableStateOf("") }
    var costPriceText by remember { mutableStateOf("") }

    // Local state for form fields
    var currentData by remember {
        mutableStateOf(
            ProductEntity(
                id = UUID.randomUUID().toString()
            )
        )
    }




    LaunchedEffect(product) {
        if (isEditing && product != null) {

            currentData = product

            sellingPriceText =
                product.sellingPrice.takeIf { it != 0.0 }?.toString() ?: ""

            costPriceText =
                product.costPrice.takeIf { it != 0.0 }?.toString() ?: ""
        }
    }


    fun validate(): Boolean {
        hasSubmitted = true

        return currentData.name.isNotBlank() &&
                currentData.hsn.isNotBlank() &&
                currentData.unit.isNotBlank() &&
                sellingPriceText.toDoubleOrNull() != null
    }

    fun onSubmit() {
        if (validate()) {

            val selling = sellingPriceText.toDoubleOrNull() ?: 0.0
            val cost = costPriceText.toDoubleOrNull() ?: 0.0

            // Update the timestamp before saving
            val finalData = currentData.copy(
                sellingPrice = selling,
                costPrice = cost,
                updatedAt = if (isEditing) System.currentTimeMillis() else null
            )

            onSave(finalData)
        } else {
            // ADD THIS: The user will feel a distinct vibration telling them they missed a field
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }


    // Unit Selection Dialog
    if (showUnitDialog) {
        UnitSelectionDialog(
            onDismiss = { showUnitDialog = false },
            onUnitSelected = { code ->
                currentData = currentData.copy(unit = code)
                showUnitDialog = false
            }
        )
    }

    val focusManager = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    val hsnFocus = remember { FocusRequester() }
    val costPriceFocus = remember { FocusRequester() }
    val sellingPriceFocus = remember { FocusRequester() }
    val linkFocus = remember { FocusRequester() }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SheetHeadingText(
            text = if (isEditing) "Edit Product" else "Add Product"
        )


        //1st row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CompactImagePicker(
                onClick = { /* TODO: Open Image Picker */ },
                label = "Logo"
            )

            Box(modifier = Modifier.weight(0.75f)) {

                Input(
                    value = currentData.name,
                    onValueChange = {
                        currentData = currentData.copy(name = it)
                    },
                    label = "Product Name *",
                    placeholder = "e.g. Wireless Headphones",
                    icon = Icons.Outlined.ShoppingCart,
                    isError = hasSubmitted && currentData.name.isBlank(),
                    modifier = Modifier.focusRequester(nameFocus),
                    keyboardActions = KeyboardActions(onNext = { hsnFocus.requestFocus() })
                )
            }
        }

        // 2nd row


        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                //hsn code
                Input(
                    value = currentData.hsn,
                    onValueChange = {
                        currentData = currentData.copy(hsn = it)
                    },
                    label = "HSN Code *",
                    placeholder = "(e.g. 8518 3000)",
                    icon = Icons.Outlined.AccountBox,
                    keyboardType = KeyboardType.Number,
                    isError = hasSubmitted && currentData.hsn.isBlank(),
                    modifier = Modifier.focusRequester(hsnFocus),
                    keyboardActions = KeyboardActions(onNext = { costPriceFocus.requestFocus() })
                )
            }

            // Unit Selection (Clickable Field)
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    label = "Unit *",
                    value = currentData.unit,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = "Select Unit",
                    isError = hasSubmitted && currentData.unit.isBlank(),
                    // Remove the onClick and modifier from inside Input
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showUnitDialog = true }
                )
            }
        }


        // Categorization
        Dropdown(
            label = "Category",
            items = category,
            selectedValue = currentData.category,
            placeholder = "Select Category",
            onItemSelected = { currentData = currentData.copy(category = it) }
        )

        // Pricing
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = costPriceText,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            costPriceText = it
                        }
                    },
                    label = "Cost Price",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.focusRequester(costPriceFocus),
                    keyboardActions = KeyboardActions(onNext = { sellingPriceFocus.requestFocus() })
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = sellingPriceText,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            sellingPriceText = it
                        }
                    },
                    label = "Selling Price *",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    isError = hasSubmitted && (sellingPriceText.isBlank() || sellingPriceText.toDoubleOrNull() == null),
                    modifier = Modifier.focusRequester(sellingPriceFocus),
                    keyboardActions = KeyboardActions(onNext = { linkFocus.requestFocus() })

                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.brand ?: "",
                    onValueChange = { currentData = currentData.copy(brand = it) },
                    label = "Brand",
                    placeholder = "e.g. Apple",
                    icon = Icons.Default.Star,
                    keyboardActions = KeyboardActions(onNext = { nameFocus.requestFocus() })
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.productLink ?: "",
                    onValueChange = { currentData = currentData.copy(productLink = it) },
                    label = "Link (Optional)",
                    placeholder = "e.g. ://store.com",
                    keyboardType = KeyboardType.Uri,
                    modifier = Modifier.focusRequester(linkFocus),
                    // --- 3. FINAL ACTION IS 'DONE' ---
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus() // Closes the keyboard
                            onSubmit() // Automatically attempts to save the form
                        }
                    )
                )
            }
        }


        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // ADD THIS: Pushes the buttons up so they don't overlap the system home swipe bar
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                text = "Cancel",
                onClick = onCancel,
                variant = ButtonVariant.Gray,
                modifier = Modifier.weight(1f)
            )

            Button(
                text = if (isEditing) "Update" else "Save",
                onClick = { onSubmit() },
                icon = Icons.Default.Check,
                variant = ButtonVariant.White,
                modifier = Modifier.weight(1f)
            )

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
    val categories by viewModel.categories.collectAsState()

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
                    product = null,
                    isEditing = false,
                    onSave = { product ->
                        viewModel.insertProduct(product)
                        mode = ProductSheetMode.LIST
                    },
                    category = categories,
                    onCancel = { mode = ProductSheetMode.LIST }
                )
            }
        }
    }
}

