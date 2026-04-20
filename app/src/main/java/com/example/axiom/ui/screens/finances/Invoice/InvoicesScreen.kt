package com.example.axiom.ui.screens.finances.Invoice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.EmptyState.EmptyScreen
import com.example.axiom.ui.components.shared.cards.InvoiceCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceCardDto
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModel
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModelFactory
import com.example.axiom.ui.screens.finances.Invoice.components.RecordPaymentDialog
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.ui.utils.Amount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InvoicesScreen(
    onBack: () -> Unit,
    onCreateInvoice: () -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current


    val viewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(context)
    )

    // all states
    val invoices by viewModel.invoiceCards.collectAsState() // List<InvoiceCardDto>
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Group invoices by Month-Year
    val groupedInvoices = remember(invoices) {

        invoices
            .sortedByDescending { it.invoiceDate }
            .groupBy { invoice ->
                val date = Date(invoice.invoiceDate)
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            }
            // Map each month's list into a Pair holding the (Total Sum, List of Purchases)
            .mapValues { (_, salesInMonth) ->
                val monthTotal = salesInMonth.sumOf { it.grandTotal }
                Pair(monthTotal, salesInMonth)
            }
    }

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = groupedInvoices.isEmpty()
    var selectedInvoiceForPayment by remember { mutableStateOf<InvoiceCardDto?>(null) }


    selectedInvoiceForPayment?.let { invoice ->
        RecordPaymentDialog(
            invoice = invoice,
            onDismiss = { selectedInvoiceForPayment = null },
            onSubmit = { amount, mode, notes, date ->
                viewModel.recordStandalonePayment(
                    invoiceId = invoice.id,
                    amount = amount,
                    paymentMode = mode,
                    notes = notes,
                    date = date
                )
                // Optionally show a toast here
            }
        )
    }
    AnimatedHeaderScrollView(
        largeTitle = "Invoices",
        onAddClick = { onCreateInvoice() },
        onBack = onBack,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
    ) {
        if (isEmpty) {
            if (isSearching) {
                // CASE A: Searching, but no results
                item(key = "no_results_state") {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AxiomTheme.components.card.mutedText.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AxiomTheme.components.card.title
                        )
                        Text(
                            text = "Try adjusting your search for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            } else {
                // CASE B: Database is completely empty (No search active)
                // We show the empty state INSIDE the scroll view so the header stays!
                item(key = "database_empty_state") {

                    EmptyScreen(
                        title = "No invoices are there",
                        description = "No invoices found",
                        buttonText = "Add Invoice",
                        onAdd = { onCreateInvoice() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {

            // Iterate through the groups
            groupedInvoices.forEach { (monthYear, summaryData) ->

                val (monthTotal, salesInMonth) = summaryData

                // 1. Group Header (Month/Year)
                item(key = "header_$monthYear") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = monthYear, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Text(text = Amount.format(monthTotal), fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }

                // 2. The Items for that Month
                items(
                    items = salesInMonth,
                    key = { it.id } // Requires stable ID for animations
                ) { invoice ->

                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        InvoiceCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) },
                            onLongClick = { selectedInvoiceForPayment = invoice }
                        )

                    }


                }
            }
        }
    }
}







