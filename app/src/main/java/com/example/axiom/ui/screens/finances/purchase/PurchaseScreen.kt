package com.example.axiom.ui.screens.finances.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.PurchaseRecord
import com.example.axiom.data.finances.PurchaseRecordViewModel
import com.example.axiom.data.finances.PurchaseRecordViewModelFactory
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.AppButton
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@Composable
private fun PurchaseCard(purchase: PurchaseRecord) {


    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Supplier: ${purchase.supplierId}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Items: ${purchase.items.size}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Date: ${dateFormatter.format(Date(purchase.purchaseDate))}",
                style = MaterialTheme.typography.bodySmall
            )

            purchase.remarks?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Remarks: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
    val filteredPurchases = remember(searchQuery, purchases) {
        if (searchQuery.isBlank()) purchases
        else purchases.filter {
            it.remarks?.contains(searchQuery, ignoreCase = true) == true ||
                    it.supplierId.contains(searchQuery, ignoreCase = true)
        }
    }
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
                    PurchaseCard(purchase)
                }
            }
        }
    }

    AppBottomSheet(
        showSheet = showCreateSheet,
        onDismiss = { showCreateSheet = false }
    ) {
        CreatePurchaseSheet(
            onCreate = {
                purchaseViewModel.insert(it)
                showCreateSheet = false
            }
        )
    }
}

@Composable
fun CreatePurchaseSheet(
    onCreate: (PurchaseRecord) -> Unit
) {
    var supplierId by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val now = remember { System.currentTimeMillis() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "New Purchase",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = supplierId,
            onValueChange = { supplierId = it },
            label = { Text("Supplier ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks") },
            modifier = Modifier.fillMaxWidth()
        )

        AppButton(
            text = "Create Purchase",
            enabled = supplierId.isNotBlank(),
            onClick = {
                onCreate(
                    PurchaseRecord(
                        id = UUID.randomUUID().toString(),
                        supplierId = supplierId,
                        purchaseDate = now,
                        items = emptyList(),
                        remarks = remarks.ifBlank { null },
                        createdAt = now,
                        updatedAt = null
                    )
                )
            }
        )
    }
}
