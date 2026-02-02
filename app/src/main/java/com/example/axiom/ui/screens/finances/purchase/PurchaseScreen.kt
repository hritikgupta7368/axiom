package com.example.axiom.ui.screens.finances.purchase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.Product
import com.example.axiom.data.finances.PurchaseRecord
import com.example.axiom.data.finances.PurchaseRecordViewModel
import com.example.axiom.data.finances.PurchaseRecordViewModelFactory
import com.example.axiom.data.finances.PurchasedItem
import com.example.axiom.data.finances.SupplierFirm
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.AppButton
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


private data class PurchaseItemDraft(
    val product: Product,
    var quantity: Double,
    var costPrice: Double
) {
    val total: Double get() = quantity * costPrice
}


@Composable
fun PurchaseRecordCard(
    record: PurchaseRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = record.supplierId ?: "Unknown Supplier",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = dateFormatter.format(Date(record.purchaseDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row {
                    AppIconButton(
                        icon = AppIcons.Edit,
                        contentDescription = "Edit",
                        onClick = onEdit
                    )
                    AppIconButton(
                        icon = AppIcons.Delete,
                        contentDescription = "Delete",
                        onClick = onDelete
                    )
                }
            }

            Text(
                text = "Items: ${record.items.size}",
                style = MaterialTheme.typography.bodyMedium
            )

            val total = record.items.sumOf { it.total }
            Text(
                text = "Total: ₹%.2f".format(total),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(onBack: () -> Unit) {

    val context = LocalContext.current

    val purchaseViewModel: PurchaseRecordViewModel = viewModel(
        factory = PurchaseRecordViewModelFactory(context)
    )
    var searchQuery by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }

    val purchases by purchaseViewModel.purchases.collectAsState(
        initial = emptyList()
    )
    val suppliers by purchaseViewModel.suppliers.collectAsState(initial = emptyList())
    val products by purchaseViewModel.products.collectAsState(initial = emptyList())

    val filteredPurchases = remember(searchQuery, purchases) {
        if (searchQuery.isBlank()) purchases
        else purchases.filter {
            it.remarks?.contains(searchQuery, ignoreCase = true) == true ||
                    it.supplierId.contains(searchQuery, ignoreCase = true)
        }
    }
    var editingPurchase by remember { mutableStateOf<PurchaseRecord?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Purchase Screen") },
                navigationIcon = {
                    AppIconButton(
                        icon = AppIcons.Back,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true }
            ) {
                AppIconButton(
                    icon = AppIcons.Add,
                    contentDescription = "Back",
                    onClick = { showCreateSheet = true }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search purchases") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPurchases, key = { it.id }) { purchase ->
                    PurchaseRecordCard(purchase, { }, {})
                }
            }
        }
    }

    AppBottomSheet(
        showSheet = showCreateSheet || editingPurchase != null,
        onDismiss = {
            showCreateSheet = false
            editingPurchase = null
        }
    ) {
        PurchaseEditorSheet(
            suppliers = suppliers,
            products = products,
            existing = editingPurchase,
            onSave = {
                purchaseViewModel.insert(it)
                showCreateSheet = false
                editingPurchase = null
            },
            onDismiss = {
                showCreateSheet = false
                editingPurchase = null
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEditorSheet(
    suppliers: List<SupplierFirm>,
    products: List<Product>,
    existing: PurchaseRecord? = null,
    onSave: (PurchaseRecord) -> Unit,
    onDismiss: () -> Unit
) {
    val now = System.currentTimeMillis()

    var selectedSupplier by remember {
        mutableStateOf(
            suppliers.firstOrNull { it.id == existing?.supplierId }
        )
    }

    var remarks by remember { mutableStateOf(existing?.remarks ?: "") }

    val items = remember {
        mutableStateListOf<PurchaseItemDraft>().apply {
            existing?.items?.forEach { item ->
                products.find { it.id == item.productId }?.let { product ->
                    add(
                        PurchaseItemDraft(
                            product = product,
                            quantity = item.quantity,
                            costPrice = item.costPrice
                        )
                    )
                }
            }
        }
    }
    var showSupplierPicker by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = if (existing == null) "New Purchase" else "Edit Purchase",
            style = MaterialTheme.typography.titleLarge
        )


        Text(
            text = "Select Supplier",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suppliers.forEach { supplier ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSupplier = supplier
                            showSupplierPicker = false
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        supplier.gstin?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }


        /* ITEMS */
        Text("Items", style = MaterialTheme.typography.titleMedium)

        products.forEach { product ->
            val draft = items.find { it.product.id == product.id }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = product.name,
                    modifier = Modifier.weight(1f)
                )

                if (draft == null) {
                    AppButton(
                        text = "Add",
                        onClick = {
                            items.add(
                                PurchaseItemDraft(
                                    product = product,
                                    quantity = 1.0,
                                    costPrice = product.sellingPrice
                                )
                            )
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = draft.quantity.toInt().toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { q ->
                                if (q > 0) draft.quantity = q
                            }
                        },
                        label = { Text("Qty") },
                        modifier = Modifier.width(80.dp)
                    )

                    OutlinedTextField(
                        value = draft.costPrice.toString(),
                        onValueChange = {
                            it.toDoubleOrNull()?.let { p ->
                                if (p >= 0) draft.costPrice = p
                            }
                        },
                        label = { Text("Cost") },
                        modifier = Modifier.width(100.dp)
                    )

                    AppIconButton(
                        icon = AppIcons.Delete,
                        contentDescription = "Remove",
                        onClick = { items.remove(draft) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        /* REMARKS */
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks") },
            modifier = Modifier.fillMaxWidth()
        )

        /* SAVE */
        AppButton(
            text = "Save Purchase",
            enabled = selectedSupplier != null && items.isNotEmpty(),
            onClick = {
                onSave(
                    PurchaseRecord(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        supplierId = selectedSupplier!!.id,
                        purchaseDate = existing?.purchaseDate ?: now,
                        items = items.map {
                            PurchasedItem(
                                id = UUID.randomUUID().toString(),
                                productId = it.product.id,
                                name = it.product.name,
                                hsn = it.product.hsn,
                                unit = it.product.unit,
                                quantity = it.quantity,
                                costPrice = it.costPrice,
                                total = it.total
                            )
                        },
                        remarks = remarks.ifBlank { null },
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now
                    )
                )
            }
        )

        AppButton(
            text = "Cancel",
            onClick = onDismiss
        )
    }
}
