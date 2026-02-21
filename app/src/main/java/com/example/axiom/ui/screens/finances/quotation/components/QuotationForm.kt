package com.example.axiom.ui.screens.finances.quotation.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.ui.screens.finances.quotation.CreateQuotationMode


@Composable
fun QuotationForm(
    quotationNumber: String,
    issueDate: String,
    selectedCustomer: CustomerFirm?,
    items: List<QuotationItemEntity>,
    discountPercent: Double,
    onQuotationNumberChange: (String) -> Unit,
    onIssueDateChange: (String) -> Unit,
    onSelectCustomerClick: () -> Unit,
    onSelectProductClick: () -> Unit,
    onQtyChange: (String, Double) -> Unit,
    onRateChange: (String, Double) -> Unit,
    onItemDelete: (String) -> Unit,
    onDiscountChange: (Double) -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text("Quotation", style = MaterialTheme.typography.titleLarge)
        }

        // Quotation Number
        item {
            OutlinedTextField(
                value = quotationNumber,
                onValueChange = onQuotationNumberChange,
                label = { Text("Quotation Number") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Issue Date
        item {
            OutlinedTextField(
                value = issueDate,
                onValueChange = onIssueDateChange,
                label = { Text("Issue Date") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Customer Selection
        item {
            OutlinedButton(
                onClick = onSelectCustomerClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedCustomer?.name ?: "Select Customer"
                )
            }
        }

        // Product Selection Button
        item {
            Button(
                onClick = onSelectProductClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Products")
            }
        }

        // Items Section
        if (items.isNotEmpty()) {
            item {
                Text(
                    "Items",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(
                items = items,
                key = { it.id }
            ) { item ->

                QuotationItemCard(
                    item = item,
                    onDelete = { onItemDelete(item.id) },
                    onQtyChange = { onQtyChange(item.id, it) },
                    onRateChange = { onRateChange(item.id, it) }
                )
            }
        }

        // Discount Input
        item {
            OutlinedTextField(
                value = discountPercent.toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let(onDiscountChange)
                },
                label = { Text("Overall Discount %") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Summary
        item {
            val subTotal = items.sumOf { it.taxableAmount }
            val taxTotal = items.sumOf { it.taxableAmount }
            val grandTotal = items.sumOf { it.taxableAmount }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Subtotal: ₹%.2f".format(subTotal))
                Text("Tax: ₹%.2f".format(taxTotal))
                Text(
                    "Grand Total: ₹%.2f".format(grandTotal),
                    fontWeight = FontWeight.Bold
                )
            }
        }


    }
}

@Composable
fun QuotationItemCard(
    item: QuotationItemEntity,
    onDelete: () -> Unit,
    onQtyChange: (Double) -> Unit,
    onRateChange: (Double) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("HSN: ${item.hsn}", fontSize = 12.sp)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Qty
            OutlinedTextField(
                value = item.quantity.toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let(onQtyChange)
                },
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.weight(1f)
            )

            // Rate
            OutlinedTextField(
                value = item.rate.toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let(onRateChange)
                },
                label = { Text("Rate") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            "Total: ₹%.2f".format(item.taxableAmount),
            fontWeight = FontWeight.Bold
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuotationSheetWrapper(
    onCreateQuotation: (QuotationEntity) -> Unit
) {
    var mode by remember { mutableStateOf(CreateQuotationMode.FORM) }

    var quotationNumber by remember { mutableStateOf("Q-${System.currentTimeMillis()}") }
    var issueDate by remember { mutableStateOf("19/02/2026") }

    var selectedCustomer by remember { mutableStateOf<CustomerFirm?>(null) }
    var items by remember { mutableStateOf<List<QuotationItemEntity>>(emptyList()) }
    var discountPercent by remember { mutableStateOf(0.0) }
}
