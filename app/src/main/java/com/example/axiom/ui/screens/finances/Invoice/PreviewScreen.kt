package com.example.axiom.ui.screens.finances.Invoice

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceItemEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModel
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModelFactory
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceWithItems
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentStatus
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentTransactionEntity
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.theme.AxiomTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// --- Color Palette ---
private val BgDark = Color(0xFF0F172A)

private val TextDarkGray = Color(0xFF64748B)

private val AccentBlue = Color(0xFF3B82F6)
private val AccentBlueBg = Color(0xFF1E3A8A)
private val AccentGreen = Color(0xFF10B981)
private val AccentGreenBg = Color(0xFF064E3B)
private val AccentOrange = Color(0xFFF59E0B)
private val AccentOrangeBg = Color(0xFF78350F)


@Composable
fun InvoicePreviewScreen(
    invoiceId: String? = null,
    onBack: () -> Unit,
    onEditInvoice: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModelFactory(context))

    var currentInvoiceWithItems by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val pdfUri by viewModel.pdfUri.collectAsStateWithLifecycle()

    LaunchedEffect(pdfUri) {
        pdfUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice"))
            viewModel.clearPdf()
        }
    }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            isLoading = true
            currentInvoiceWithItems = viewModel.getInvoiceByIdSync(invoiceId)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    fun deleteInvoiceAction(id: String) {
        viewModel.cancelInvoice(id)
        onBack()
    }

    val customerName = currentInvoiceWithItems?.customer?.party?.businessName ?: "Unknown Customer"
    val isCancelled = currentInvoiceWithItems?.invoice?.status == InvoiceStatus.CANCELLED

    AnimatedHeaderScrollView(
        largeTitle = "Invoice Preview",
        subtitle = customerName,
        onBack = onBack,
        selectionCount = 1,
        showBack = true,
        isSelectionMode = !isCancelled,
        onDeleteClick = { invoiceId?.let { deleteInvoiceAction(it) } },
        onEditClick = { invoiceId?.let { onEditInvoice(it) } },
        onThirdOptionClick = {
            currentInvoiceWithItems?.let { viewModel.generatePdf(it, "") }
        }
    ) {
        val invoiceData = currentInvoiceWithItems

        if (isLoading || invoiceData == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AxiomTheme.colors.accentBlue)
                }
            }
        } else {
            val invoice = invoiceData.invoice
            val customer = invoiceData.customer?.party
            val seller = invoiceData.seller?.party
            val payments = invoiceData.payments

            item { HeaderCard(invoice = invoice) }
            item { AddressSection(invoice = invoice, customer = customer) }
            item { ProductDetailsSection(items = invoiceData.items, isCancelled = isCancelled) }
            item { PaymentBreakdownCard(invoice = invoice, isCancelled = isCancelled) }

            // NEW: Payment History Section
            if (payments.isNotEmpty()) {
                item { PaymentHistorySection(payments = payments) }
            }

            item { AdditionalInfoSection(invoice = invoice, seller = seller) }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTIONS
// -----------------------------------------------------------------------------

@Composable
fun HeaderCard(invoice: InvoiceEntity) {
    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(invoice.invoiceDate))
    val isCancelled = invoice.status == InvoiceStatus.CANCELLED

    // Dynamic coloring based on AxiomTheme
    val statusColor = when {
        isCancelled -> AxiomTheme.colors.error
        invoice.paymentStatus == PaymentStatus.PAID -> AxiomTheme.colors.accentGreen
        invoice.paymentStatus == PaymentStatus.PARTIAL -> AxiomTheme.colors.accentBlue
        else -> AxiomTheme.components.card.subtitle
    }

    val statusBg = when {
        isCancelled -> AxiomTheme.colors.error.copy(alpha = 0.15f)
        invoice.paymentStatus == PaymentStatus.PAID -> AxiomTheme.colors.accentGreenBg
        invoice.paymentStatus == PaymentStatus.PARTIAL -> AxiomTheme.colors.accentBlueBg
        else -> AxiomTheme.colors.background
    }

    val displayStatus = if (isCancelled) "CANCELLED" else invoice.paymentStatus.name

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    LabelText("INVOICE NO.")
                    Text(
                        text = invoice.invoiceNumber,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.components.card.title,
                        textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    LabelText("STATUS")
                    Box(
                        modifier = Modifier
                            .background(statusBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = displayStatus,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("INVOICE DATE")
                    Text(date, color = AxiomTheme.components.card.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("PLACE OF SUPPLY")
                    Text(
                        invoice.placeOfSupplyCode.ifBlank { "N/A" },
                        color = AxiomTheme.components.card.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddressSection(invoice: InvoiceEntity, customer: PartyEntity?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(AxiomTheme.components.card.background)
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            LabelText("BILLED TO")
            Spacer(modifier = Modifier.height(4.dp))
            Text(customer?.businessName ?: "N/A", color = AxiomTheme.components.card.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!customer?.gstNumber.isNullOrBlank()) {
                Text("GSTIN: ${customer?.gstNumber}", color = AxiomTheme.colors.accentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                customer?.billingAddress ?: customer?.address ?: "No address provided",
                color = AxiomTheme.components.card.subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(AxiomTheme.components.card.background)
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            LabelText("SHIPPED TO")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                invoice.shippedToAddress ?: "Same as billing address",
                color = AxiomTheme.components.card.subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (!invoice.vehicleNumber.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                LabelText("VEHICLE NO.")
                Text(invoice.vehicleNumber, color = AxiomTheme.components.card.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ProductDetailsSection(items: List<InvoiceItemEntity>, isCancelled: Boolean) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(if (isCancelled) 0.6f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.components.card.title)
            Text("${items.size} Total", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AxiomTheme.components.card.subtitle)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AxiomTheme.components.card.background)
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("ITEM & HSN", modifier = Modifier.weight(5f), style = headerStyle())
                Text("QTY", modifier = Modifier.weight(2f), style = headerStyle(), textAlign = TextAlign.Center)
                Text("TOTAL", modifier = Modifier.weight(3f), style = headerStyle(), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = AxiomTheme.components.card.border)

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(5f)) {
                        Text(
                            item.productNameSnapshot,
                            color = AxiomTheme.components.card.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("HSN: ${item.hsnSnapshot}", color = AxiomTheme.components.card.subtitle, fontSize = 11.sp)
                            Text("₹${item.sellingPriceAtTime}/${item.unitSnapshot}", color = AxiomTheme.components.card.mutedText, fontSize = 11.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .background(AxiomTheme.colors.background, RoundedCornerShape(4.dp))
                                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}" else "${item.quantity}",
                                color = AxiomTheme.components.card.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        formatCurrency(item.taxableAmount),
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.End,
                        color = AxiomTheme.components.card.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = AxiomTheme.components.card.border, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
fun PaymentBreakdownCard(invoice: InvoiceEntity, isCancelled: Boolean) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .alpha(if (isCancelled) 0.6f else 1f)
    ) {
        Text(
            "Payment Summary",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        BreakdownRow("Item Subtotal", formatCurrency(invoice.itemSubTotal))
        if (invoice.deliveryCharge > 0) BreakdownRow("Delivery Charges", "+ ${formatCurrency(invoice.deliveryCharge)}")
        if (invoice.extraCharges > 0) BreakdownRow("Extra Charges", "+ ${formatCurrency(invoice.extraCharges)}")
        if (invoice.globalDiscountAmount > 0) BreakdownRow(
            "Discount",
            "- ${formatCurrency(invoice.globalDiscountAmount)}",
            valueColor = AxiomTheme.colors.accentGreen
        )

        HorizontalDivider(color = AxiomTheme.components.card.border, modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Taxable Amount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AxiomTheme.components.card.title)
            Text(
                formatCurrency(invoice.totalTaxableAmount),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AxiomTheme.components.card.title
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(AxiomTheme.colors.background, RoundedCornerShape(8.dp))
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (invoice.supplyType == SupplyType.INTER_STATE || invoice.igstAmount > 0) {
                BreakdownRowSmall("IGST", formatCurrency(invoice.igstAmount))
            } else {
                BreakdownRowSmall("CGST", formatCurrency(invoice.cgstAmount))
                BreakdownRowSmall("SGST", formatCurrency(invoice.sgstAmount))
            }
        }

        if (invoice.roundOff != 0.0) {
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRowSmall("Round Off", formatCurrency(invoice.roundOff))
        }

        HorizontalDivider(color = AxiomTheme.components.card.border, modifier = Modifier.padding(vertical = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("Grand Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.components.card.title)
            Text(formatCurrency(invoice.grandTotal), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.colors.accentBlue)
        }

        if (invoice.amountInWords.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${invoice.amountInWords} Rupees Only",
                fontSize = 12.sp,
                color = AxiomTheme.components.card.subtitle,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun PaymentHistorySection(payments: List<PaymentTransactionEntity>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Payment History",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AxiomTheme.components.card.background)
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
        ) {
            payments.forEachIndexed { index, payment ->
                val date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(payment.transactionDate))

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = AxiomTheme.colors.accentGreen, modifier = Modifier.size(16.dp))
                            Text(payment.paymentMode.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AxiomTheme.components.card.title)
                        }
                        Text(formatCurrency(payment.amount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AxiomTheme.colors.accentGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(payment.notes ?: "Payment received", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                        Text(date, fontSize = 11.sp, color = AxiomTheme.components.card.mutedText)
                    }
                }
                if (index < payments.lastIndex) {
                    HorizontalDivider(color = AxiomTheme.components.card.border)
                }
            }
        }
    }
}

@Composable
fun AdditionalInfoSection(invoice: InvoiceEntity, seller: PartyEntity?) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Additional Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            modifier = Modifier.padding(start = 4.dp)
        )

        if (!seller?.bankName.isNullOrBlank() || !seller?.accountNumber.isNullOrBlank()) {
            InfoCard(
                icon = Icons.Default.AccountBox,
                iconColor = AxiomTheme.colors.accentBlue,
                iconBg = AxiomTheme.colors.accentBlueBg,
                title = "Bank Details"
            ) {
                Text(seller?.bankName ?: "N/A", fontSize = 14.sp, color = AxiomTheme.components.card.title)
                Text("A/C: ${seller?.accountNumber ?: "N/A"}", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                Text("IFSC: ${seller?.ifscCode ?: "N/A"}", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
            }
        }

        if (!invoice.eWayBillNumber.isNullOrBlank()) {
            InfoCard(
                icon = Icons.Default.ShoppingCart,
                iconColor = AxiomTheme.colors.error,
                iconBg = AxiomTheme.colors.error.copy(alpha = 0.15f),
                title = "E-Way Bill Details"
            ) {
                Text("E-Way Bill No: ${invoice.eWayBillNumber}", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                invoice.eWayBillDate?.let {
                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    Text("Generated On: $date", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                }
            }
        }

        InfoCard(
            icon = Icons.Default.Info,
            iconColor = AxiomTheme.colors.accentGreen,
            iconBg = AxiomTheme.colors.accentGreenBg,
            title = "Terms & Conditions"
        ) {
            Text("1. Goods once sold will not be taken back.", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
            Text("2. Interest @ 18% p.a. will be charged if payment is delayed.", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
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
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = AxiomTheme.components.card.mutedText,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun headerStyle() = androidx.compose.ui.text.TextStyle(
    color = AxiomTheme.components.card.subtitle,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.5.sp
)

@Composable
fun BreakdownRow(label: String, value: String, valueColor: Color = AxiomTheme.components.card.title) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = AxiomTheme.components.card.subtitle)
        Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BreakdownRowSmall(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = AxiomTheme.components.card.subtitle)
        Text(value, fontSize = 13.sp, color = AxiomTheme.components.card.title)
    }
}

@Composable
fun InfoCard(icon: ImageVector, iconColor: Color, iconBg: Color, title: String, content: @Composable ColumnScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AxiomTheme.components.card.title)
            content()
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 2 // Keeps decimals for invoice precision
    return format.format(amount)
}