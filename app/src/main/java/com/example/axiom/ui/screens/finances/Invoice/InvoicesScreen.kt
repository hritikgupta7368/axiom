package com.example.axiom.ui.screens.finances.Invoice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.domain.Invoice
import com.example.axiom.ui.screens.finances.FinancesViewModel
import com.example.axiom.ui.screens.finances.FinancesViewModelFactory
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
    val viewModel: FinancesViewModel = viewModel(factory = FinancesViewModelFactory())
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    val isLoading = invoices.isEmpty() && viewModel.invoices.replayCache.isEmpty()

    // Group invoices by Month-Year
    val groupedInvoices = remember(invoices) {
        invoices
            .sortedByDescending { it.date.toLongOrNull() ?: 0L }
            .groupBy {
                val dateMillis = it.date.toLongOrNull() ?: 0L
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(dateMillis))
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Invoices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCreateInvoice) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else if (invoices.isEmpty()) {

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No invoices found.", style = MaterialTheme.typography.bodyLarge)
            }

        }
        else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Iterate through the groups
                groupedInvoices.forEach { (monthYear, invoiceList) ->
                    // Sticky Header for the Month
                    stickyHeader {
                        HeaderMonth(monthYear)
                    }

                    // Items for that month

                    items(invoiceList, key = { it.id }) { invoice ->
                        // The 'find' logic is removed. We directly and safely access the name.
                        InvoiceCard(
                            invoice = invoice,
                            customerName = invoice.customerDetails?.name ?: "Unknown Customer",
                            onClick = { onInvoiceClick(invoice.id) }
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun HeaderMonth(monthYear: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // Match background to hide content behind when scrolling
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthYear,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, customerName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${invoice.invoiceNo}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$customerName • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(invoice.date.toLongOrNull() ?: 0L))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹${String.format("%.2f", invoice.totalAmount)} • ${invoice.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Right side icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// -- PREVIEWS --

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun InvoicesScreenPreviewDark() {
    // Explicitly wrapping in a dark theme for preview purposes
    MaterialTheme(colorScheme = darkColorScheme()) {
        InvoicesScreen(onBack = {}, onCreateInvoice = {}, onInvoiceClick = {})
    }
}