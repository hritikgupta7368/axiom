package com.example.axiom.ui.screens.finances.suppliers.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity

@Composable
fun SupplierListSheet(
    suppliers: List<PartyEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onConfirmSelection: (PartyEntity) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selectedSupplier = suppliers.firstOrNull { it.id == selectedId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 700.dp)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }

            Text(
                text = "Select Supplier",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (selectedId != null) {
                Text(
                    text = "1 Selected",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search name, GSTIN or phone") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            }
        )

        Spacer(Modifier.height(12.dp))

        // Supplier List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = suppliers,
                key = { it.id }
            ) { supplier ->

                val isSelected = selectedId == supplier.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedId = if (isSelected) null else supplier.id
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            selectedId = if (isSelected) null else supplier.id
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(supplier.businessName, fontWeight = FontWeight.Medium)

                        if (!supplier.gstNumber.isNullOrBlank()) {
                            Text(
                                text = "GSTIN: ${supplier.gstNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!supplier.city.isNullOrBlank()) {
                            Text(
                                text = "${supplier.city}, ${supplier.state ?: ""}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Divider()
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onCreateClick) {
                Text("Create Supplier")
            }

            Button(
                onClick = {
                    selectedSupplier?.let { onConfirmSelection(it) }
                },
                enabled = selectedSupplier != null
            ) {
                Text("Select")
            }
        }
    }
}

enum class SupplierSheetMode {
    LIST,
    CREATE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SupplierListSheetWrapper(
    onConfirmSelection: (PartyEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: SupplierListViewModel = viewModel(
        factory = SupplierListViewModelFactory(context)
    )

    val suppliers by viewModel.suppliers.collectAsState()
    var mode by remember { mutableStateOf(SupplierSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "SupplierSheetSwitch"
    ) { currentMode ->

        when (currentMode) {
            SupplierSheetMode.LIST -> {
                SupplierListSheet(
                    suppliers = suppliers,
                    searchQuery = viewModel.searchQuery.collectAsState().value,
                    onSearchChange = viewModel::updateSearchQuery,
                    onConfirmSelection = {
                        onConfirmSelection(it)
                    },
                    onCreateClick = {
//                        mode = SupplierSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            SupplierSheetMode.CREATE -> {
//                SupplierListSheet(
//                    suppliers = suppliers,
//                    onConfirmSelection = { supplier ->
//                        onConfirmSelection(supplier)
//                    },
//                    onCreateClick = {
////                        mode = SupplierSheetMode.CREATE
//                    },
//                    onBack = onBack
//                )
            }
        }
    }
}