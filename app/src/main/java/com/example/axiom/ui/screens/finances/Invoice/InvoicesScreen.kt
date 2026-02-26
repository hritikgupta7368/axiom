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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.GstBreakdown
import com.example.axiom.data.finances.Invoice
import com.example.axiom.data.finances.InvoiceItem
import com.example.axiom.data.finances.InvoiceStatus
import com.example.axiom.data.finances.SupplyType
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
    val context = LocalContext.current
//    val viewModel: InvoiceViewModel = viewModel(
//        factory = InvoiceViewModelFactory(context)
//    )
//    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    val invoices = listOf(

        Invoice(
            id = "INV_ID_001",
            invoiceNo = "INV-2026-0001",
            date = "01-02-2026",
            sellerId = "SELLER_001",
            customerDetails = CustomerFirm(
                id = "CUST_001",
                name = "Sharma Traders",
                gstin = "22AAAAA0000A1Z5",
                address = "MG Road, Raipur, Chhattisgarh - 492001",
                contactNumber = "9876543210",
                email = "sharma.traders@gmail.com",
                stateCode = "22",
                image = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                active = true
            ),
            supplyType = SupplyType.INTRA_STATE,
            vehicleNumber = "CG04AB1234",
            shippedTo = "Raipur Warehouse",
            items = listOf(
                InvoiceItem(
                    id = "ITEM_001",
                    productId = "PROD_001",
                    name = "Cement Bag",
                    unit = "BAG",
                    price = 500.0,
                    quantity = 100.0,
                    hsn = "2523",
                    total = 50000.0
                ),
            ),
            totalBeforeTax = 50000.0,
            gst = GstBreakdown(
                cgstRate = 9.0,
                sgstRate = 9.0,
                cgstAmount = 4500.0,
                sgstAmount = 4500.0,
                totalTax = 9000.0
            ),
            shippingCharge = 1000.0,
            totalAmount = 60000.0,
            amountInWords = "Sixty Thousand Rupees Only",
            status = InvoiceStatus.DRAFT,
            createdAt = System.currentTimeMillis(),
            version = 1
        ),

        Invoice(
            id = "INV_ID_002",
            invoiceNo = "INV-2026-0002",
            date = "05-02-2026",
            sellerId = "SELLER_001",
            customerDetails = CustomerFirm(
                id = "CUST_002",
                name = "Gupta Hardware",
                gstin = "22BBBBB1111B2Z6",
                address = "Link Road, Bilaspur, Chhattisgarh - 495001",
                contactNumber = "9123456780",
                email = "guptahardware@yahoo.com",
                stateCode = "22",
                image = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                active = true
            ),
            supplyType = SupplyType.INTER_STATE,
            vehicleNumber = "CG10XY5678",
            shippedTo = "Bilaspur Site",
            items = listOf(
                InvoiceItem(
                    id = "ITEM_002",
                    productId = "PROD_002",
                    name = "Steel Rod 12mm",
                    unit = "PCS",
                    price = 800.0,
                    quantity = 50.0,
                    hsn = "7214",
                    total = 40000.0
                ),
            ),
            totalBeforeTax = 40000.0,
            gst = GstBreakdown(
                igstRate = 18.0,
                igstAmount = 7200.0,
                totalTax = 7200.0
            ),
            shippingCharge = 500.0,
            totalAmount = 47700.0,
            amountInWords = "Forty Seven Thousand Seven Hundred Rupees Only",
            status = InvoiceStatus.DRAFT,
            createdAt = System.currentTimeMillis(),
            version = 1
        ),

        Invoice(
            id = "INV_ID_003",
            invoiceNo = "INV-2026-0003",
            date = "10-02-2026",
            sellerId = "SELLER_001",
            customerDetails = CustomerFirm(
                id = "CUST_003",
                name = "Verma Constructions",
                gstin = "22CCCCC2222C3Z7",
                address = "Station Road, Durg, Chhattisgarh - 491001",
                contactNumber = "9988776655",
                email = "vermaconstructions@outlook.com",
                stateCode = "22",
                image = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = null,
                active = true
            ),
            supplyType = SupplyType.INTRA_STATE,
            vehicleNumber = "CG07MN4321",
            shippedTo = "Durg Project",
            items = listOf(
                InvoiceItem(
                    id = "ITEM_003",
                    productId = "PROD_003",
                    name = "Red Bricks",
                    unit = "PCS",
                    price = 8.0,
                    quantity = 1000.0,
                    hsn = "6901",
                    total = 8000.0
                ),
            ),
            totalBeforeTax = 8000.0,
            gst = GstBreakdown(
                cgstRate = 6.0,
                sgstRate = 6.0,
                cgstAmount = 480.0,
                sgstAmount = 480.0,
                totalTax = 960.0
            ),
            shippingCharge = 200.0,
            totalAmount = 9160.0,
            amountInWords = "Nine Thousand One Hundred Sixty Rupees Only",
            status = InvoiceStatus.CANCELLED,
            createdAt = System.currentTimeMillis(),
            cancelledAt = System.currentTimeMillis(),
            cancelReason = "Order Cancelled by Customer",
            version = 1
        )
    )

//    val isLoading = invoices.isEmpty() && viewModel.invoices.replayCache.isEmpty()
    val isLoading = false

    // Group invoices by Month-Year
    val groupedInvoices = remember(invoices) {
        invoices
            .sortedByDescending { it.date.toLongOrNull() ?: 0L }
            .groupBy {
                val dateMillis = it.date.toLongOrNull() ?: 0L
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(dateMillis))
            }
    }

//    val groupedInvoices = remember(dummyInvoices) {
//        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
//
//        dummyInvoices
//            .sortedByDescending { invoice ->
//                inputFormat.parse(invoice.date)?.time ?: 0L
//            }
//            .groupBy { invoice ->
//                val dateMillis = inputFormat.parse(invoice.date)?.time ?: 0L
//                outputFormat.format(Date(dateMillis))
//            }
//    }

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
        } else if (invoices.isEmpty()) {

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No invoices found.", style = MaterialTheme.typography.bodyLarge)
            }

        } else {
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    text = "$customerName • ${
                        SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                        ).format(Date(invoice.date.toLongOrNull() ?: 0L))
                    }",
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

