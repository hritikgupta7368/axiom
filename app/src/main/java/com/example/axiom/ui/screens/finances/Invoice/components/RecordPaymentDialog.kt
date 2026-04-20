package com.example.axiom.ui.screens.finances.Invoice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.DatePicker.DateFieldPicker
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.components.shared.dialog.AppDialog
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    invoice: InvoiceCardDto,
    onDismiss: () -> Unit,
    onSubmit: (Double, PaymentMode, String, Long) -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    // States
    var amountString by remember { mutableStateOf(invoice.grandTotal.toString()) } // Default to full amount
    var selectedMode by remember { mutableStateOf(PaymentMode.CASH) }
    var notes by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var hasSubmitted by remember { mutableStateOf(false) }

    val parsedAmount = amountString.toDoubleOrNull() ?: 0.0
    val isAmountError = hasSubmitted && (parsedAmount <= 0)

    val triggerClose = { isVisible = false }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300)
            onDismiss()
        }
    }

    // Replace AppDialog with your actual dialog wrapper (BasicAlertDialog/Dialog)
    AppDialog(
        visible = isVisible,
        onDismissRequest = triggerClose
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = AxiomTheme.components.card.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Record Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AxiomTheme.components.card.title
                )

                Text(
                    text = "Invoice: INV-${invoice.invoiceNumber}\nTotal: ₹${invoice.grandTotal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AxiomTheme.components.card.subtitle
                )

                // Input for Amount
                Input(
                    value = amountString,
                    onValueChange = {
                        // Only allow numbers and one decimal
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            amountString = it
                        }
                    },
                    label = "Received Amount (₹)",
                    placeholder = "0.00",
                    isError = isAmountError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Decimal,
                    singleLine = true,
                    imeAction = ImeAction.Next
                )

                // Payment Mode Scrollable Row (or dropdown)
                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = AxiomTheme.components.card.title
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.name) }
                        )
                    }
                }

                // transaction date
                Text(
                    text = "Transaction Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = AxiomTheme.components.card.title
                )

                DateFieldPicker(
                    dateMillis = transactionDate,
                    onDateChange = { newDate ->
                        // Optional: Prevent future dates
                        val today = System.currentTimeMillis()
                        if (newDate <= today) {
                            transactionDate = newDate
                        }
                    },
                    label = "Transaction Date",
                    isError = transactionDate == 0L
                )

                // Input for Notes/Reference
                Input(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Reference / Notes (Optional)",
                    placeholder = "e.g. UPI Ref #123456",
                    icon = Icons.Default.Info,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Text,
                    singleLine = true,
                    imeAction = ImeAction.Done
                )

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        text = "Cancel",
                        onClick = triggerClose,
                        variant = ButtonVariant.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        text = "Update",
                        onClick = {
                            hasSubmitted = true
                            if (parsedAmount > 0) {
                                onSubmit(parsedAmount, selectedMode, notes, transactionDate)
                                triggerClose()
                            }
                        },
                        icon = Icons.Default.Check,
                        variant = ButtonVariant.White,
                        modifier = Modifier.weight(1f)
                    )

                }
            }
        }
    }
}