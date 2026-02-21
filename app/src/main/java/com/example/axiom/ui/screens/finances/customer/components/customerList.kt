package com.example.axiom.ui.screens.finances.customer.components

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
import com.example.axiom.data.finances.CustomerFirm


@Composable
fun CustomerListSheet(
    customers: List<CustomerFirm>,
    onConfirmSelection: (CustomerFirm) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {

    var query by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<String?>(null) }

    val filteredCustomers = remember(query, customers) {
        if (query.isBlank()) customers
        else customers.filter {
            it.name.contains(query, true) ||
                    (it.gstin?.contains(query, true) == true) ||
                    (it.contactNumber?.contains(query, true) == true)
        }
    }

    val selectedCustomer = remember(selectedId, customers) {
        customers.firstOrNull { it.id == selectedId }
    }

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
                text = "Select Customer",
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
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search name, GSTIN or phone") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            }
        )

        Spacer(Modifier.height(12.dp))

        // Customer List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = filteredCustomers,
                key = { it.id }
            ) { customer ->

                val isSelected = selectedId == customer.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedId =
                                if (isSelected) null
                                else customer.id
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            selectedId =
                                if (isSelected) null
                                else customer.id
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(customer.name, fontWeight = FontWeight.Medium)

                        if (!customer.gstin.isNullOrBlank()) {
                            Text(
                                "GSTIN: ${customer.gstin}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!customer.contactNumber.isNullOrBlank()) {
                            Text(
                                "Phone: ${customer.contactNumber}",
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
                Text("Create Customer")
            }

            Button(
                onClick = {
                    selectedCustomer?.let { onConfirmSelection(it) }
                },
                enabled = selectedCustomer != null
            ) {
                Text("Select")
            }
        }
    }
}


enum class CustomerSheetMode {
    LIST,
    CREATE
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomerListSheetWrapper(
    onConfirmSelection: (CustomerFirm) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: CustomerViewModel = viewModel(
        factory = CustomerFirmViewModelFactory(context)
    )

    val customers by viewModel.customers.collectAsState()

    var mode by remember { mutableStateOf(CustomerSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "CustomerSheetSwitch"
    ) { currentMode ->

        when (currentMode) {

            CustomerSheetMode.LIST -> {

                CustomerListSheet(
                    customers = customers,
                    onConfirmSelection = {
                        onConfirmSelection(it)
                    },
                    onCreateClick = {
//                        mode = CustomerSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            CustomerSheetMode.CREATE -> {

                CustomerListSheet(
                    customers = customers,
                    onConfirmSelection = { customer ->
                        onConfirmSelection(customer)
                    },
                    onCreateClick = {
//                        mode = CustomerSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }
        }
    }
}
