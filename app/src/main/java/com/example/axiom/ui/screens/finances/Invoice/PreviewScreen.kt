package com.example.axiom.ui.screens.finances.Invoice

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.Invoice
import com.example.axiom.data.finances.InvoiceItem
import com.example.axiom.data.finances.InvoiceViewModel
import com.example.axiom.data.finances.InvoiceViewModelFactory
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Colors matched from your Design ---

private val SurfaceDarker = Color(0xFF111219)

private val TextDarkGray = Color(0xFF64748B)

private val GreenText = Color(0xFF22C55E)
private val GreenBg = Color(0xFF14532D) // Darker green for bg
private val BlueBg = Color(0xFF1E3A8A) // Darker blue for bg
private val PurpleText = Color(0xFFA855F7)
private val PurpleBg = Color(0xFF581C87) // Darker purple for bg

@Composable
fun InvoicePreviewScreen(
    invoiceId: String? = null, // ID parameter as requested
    onBack: () -> Unit,
    onNavigateToPdfViewer: (Uri) -> Unit
) {
    val context = LocalContext.current
    val invoiceViewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(context)
    )


    val currentInvoice by invoiceViewModel.invoiceById.collectAsStateWithLifecycle()
    val pdfUri by invoiceViewModel.pdfUri.collectAsStateWithLifecycle()

    LaunchedEffect(pdfUri) {
        pdfUri?.let {
            onNavigateToPdfViewer(it)
            invoiceViewModel.clearPdf()
        }
    }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            invoiceViewModel.getInvoiceById(invoiceId)
        }
    }

    fun deleteInvoice(id: String) {
        invoiceViewModel.deleteById(id)
        onBack()
    }


    val invoiceNo = currentInvoice?.invoiceNo ?: "---"
    val dateMillis = currentInvoice?.date?.toLongOrNull() ?: System.currentTimeMillis()
    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateMillis))
    val customerName = currentInvoice?.customerDetails?.name ?: "Unknown Customer"
    val grandTotal = "₹${String.format("%.2f", currentInvoice?.totalAmount ?: 0.0)}"
    val status = currentInvoice?.status?.name ?: "DRAFT"

    Scaffold(
        containerColor = Color(0xFF000000),
        topBar = {
            PreviewTopBar(onBack = onBack, onDelete = { deleteInvoice(invoiceId ?: "") })
        },
        bottomBar = {
            currentInvoice?.let { nonNullInvoice ->
                PreviewBottomBar(
                    invoice = nonNullInvoice,
                    logoUri = "",

                    onGetPdfClick = {
                        invoiceViewModel.generatePdf(nonNullInvoice, "")
                    }
                )
            }
        }
    ) { paddingValues ->
        val invoice = currentInvoice

        if (invoice == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            }
        } else {
            val date = remember(invoice.date) {
                SimpleDateFormat(
                    "MMM dd, yyyy",
                    Locale.getDefault()
                ).format(Date(invoice.date.toLongOrNull() ?: 0L))
            }
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp) // Extra space at bottom
            ) {
                // --- Header Card ---
                HeaderCard(
                    invoiceNo = invoice.invoiceNo,
                    date = date,
                    customer = invoice.customerDetails?.name ?: "Unknown Customer",
                    status = invoice.status.name
                )

                // --- Product Details ---
                ProductDetailsSection(items = invoice.items)

                // --- Payment Breakdown ---
                PaymentBreakdownCard(invoice = invoice)

                // --- Additional Info ---
                AdditionalInfoSection()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTIONS
// -----------------------------------------------------------------------------

@Composable
fun PreviewTopBar(onBack: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1D27))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        AppIconButton(
            icon = AppIcons.Back,
            contentDescription = "back button",
            onClick = onBack,
        )
        Text(
            text = "Invoice Preview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFFFFF)
        )

        Row() {
            // Hidden/Dummy Edit button to balance layout
            AppIconButton(
                icon = AppIcons.Edit,
                contentDescription = "edit button",
                onClick = onBack,
            )
            AppIconButton(
                icon = AppIcons.Delete,
                contentDescription = "delete button",
                onClick = onDelete,
            )
        }

    }
    HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
}

@Composable
fun HeaderCard(invoiceNo: String, date: String, customer: String, status: String) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1D27))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
    ) {
        // Decorative Circle
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp)
                .size(96.dp)
                .clip(RoundedCornerShape(bottomStart = 50.dp))
                .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    LabelText("INVOICE NUMBER")
                    Text(
                        text = invoiceNo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    LabelText("STATUS")
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E3A8A).copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = status,
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Grid Row
            Row(modifier = Modifier.fillMaxWidth()) {
                // Date
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("DATE")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            date,
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
                // Customer
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("CUSTOMER")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBox,
                            null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            customer,
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsSection(items: List<InvoiceItem>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Product Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF)
            )
            Text(
                "${items.size} Items",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1D27))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("ITEM", modifier = Modifier.weight(6f), style = headerStyle())
                Text(
                    "QTY",
                    modifier = Modifier.weight(2f),
                    style = headerStyle(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "TOTAL",
                    modifier = Modifier.weight(4f),
                    style = headerStyle(),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = Color(0xFF1F2937)) // Dark gray border

            items.forEachIndexed { index, item ->
                ProductRow(
                    name = item.name,
                    sub = "₹${item.price} / ${item.unit}",
                    qty = "${item.quantity.toInt()}",
                    total = "₹${String.format("%.2f", item.total)}"
                )

                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentBreakdownCard(invoice: Invoice) {
    // Assuming shipping charges are part of taxable amount logic from previous screen,
    // but not explicitly stored in invoice domain model yet (as per provided domain file).
    // For now, we calculate based on fields we have.
    // If you add shipping to Invoice model, use that.
    // Currently relying on totalBeforeTax - itemsTotal logic if we want to infer, or just displaying available fields.
    // Based on create screen: totalBeforeTax = itemsTotal + shippingCharges.

    val itemsTotal = invoice.totalBeforeTax
    val shippingCharges = invoice.totalBeforeTax - itemsTotal

    // Note: This inference works if only shipping is added to itemsTotal.

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1D27))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            "Payment Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFFFFF),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(color = Color(0xFF1F2937))
        Spacer(modifier = Modifier.height(12.dp))

        BreakdownRow("Subtotal", "₹${String.format("%.2f", itemsTotal)}")
        if (shippingCharges > 0.01) {
            BreakdownRow("Delivery Charges", "₹${String.format("%.2f", shippingCharges)}")
        }

        // Tax Box
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(SurfaceDarker, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val gst = invoice.gst
            if (gst.igstAmount > 0) {
                BreakdownRowSmall(
                    "IGST (${gst.igstRate.toInt()}%)",
                    "₹${String.format("%.2f", gst.igstAmount)}"
                )
            } else {
                BreakdownRowSmall(
                    "CGST (${gst.cgstRate.toInt()}%)",
                    "₹${String.format("%.2f", gst.cgstAmount)}"
                )
                BreakdownRowSmall(
                    "SGST (${gst.sgstRate.toInt()}%)",
                    "₹${String.format("%.2f", gst.sgstAmount)}"
                )
            }

            HorizontalDivider(
                color = Color(0xFF334155),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Taxes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    "₹${String.format("%.2f", gst.totalTax)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
            }
        }

        HorizontalDivider(color = Color(0xFF1F2937), modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Grand Total",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF)
            )
            Text(
                "₹${String.format("%.2f", invoice.totalAmount)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = invoice.amountInWords,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun AdditionalInfoSection() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Additional Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFFFFF),
            modifier = Modifier.padding(start = 4.dp)
        )

        // Bank Details
        InfoCard(
            icon = Icons.Default.AccountBox,
            iconColor = Color(0xFF3B82F6),
            iconBg = BlueBg.copy(alpha = 0.2f),
            title = "Bank Details"
        ) {
            Text("HDFC Bank", fontSize = 14.sp, color = Color(0xFF94A3B8))
            Text("AC: **** **** 4589", fontSize = 12.sp, color = TextDarkGray)
        }

        // Notes
        InfoCard(
            icon = Icons.Default.Menu,
            iconColor = PurpleText,
            iconBg = PurpleBg.copy(alpha = 0.2f),
            title = "Notes & Terms"
        ) {
            Text("Payment due within 15 days.", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(
                "Please include invoice number on your check.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Signature
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1D27))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(GreenBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    // Use a generic pen icon as ink_pen isn't in default set
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = GreenText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Authorized Signature",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        "Attached",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GreenText
                    )
                }
            }
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint = TextDarkGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PreviewBottomBar(
    invoice: Invoice,
    logoUri: String,
    onGetPdfClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1D27))
            .shadow(elevation = 20.dp)
            .border(1.dp, Color(0xFF1F2937)) // Top border
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Print Button
            OutlinedButton(
                onClick = { /* Print Logic */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextDarkGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFFFFF))
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print", fontWeight = FontWeight.Bold)
            }

            // Export PDF Button
            Button(

                onClick = onGetPdfClick,
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get PDF",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// -----------------------------------------------------------------------------
// HELPER COMPONENTS
// -----------------------------------------------------------------------------

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF94A3B8),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun headerStyle() = androidx.compose.ui.text.TextStyle(
    color = TextDarkGray,
    fontSize = 12.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.5.sp
)

@Composable
fun ProductRow(name: String, sub: String, qty: String, total: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(6f)) {
            Text(
                name,
                color = Color(0xFFFFFFFF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(sub, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1F2937), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(qty, color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            total,
            modifier = Modifier.weight(4f),
            textAlign = TextAlign.End,
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 14.sp, color = Color(0xFFFFFFFF), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BreakdownRowSmall(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 12.sp, color = Color(0xFFCBD5E1))
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1D27))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(iconBg, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF))
            content()
        }
    }
}