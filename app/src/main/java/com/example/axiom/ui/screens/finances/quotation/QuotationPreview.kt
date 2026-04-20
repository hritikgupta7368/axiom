package com.example.axiom.ui.screens.finances.quotation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.LabelText
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.quotation.components.FullQuotation
import com.example.axiom.ui.screens.finances.quotation.components.QuotationEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationItemEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationStatus
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModel
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModelFactory
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
private val AccentRed = Color(0xFFEF4444)
private val AccentRedBg = Color(0xFF7F1D1D)
private val AccentGray = Color(0xFF94A3B8)
private val AccentGrayBg = Color(0xFF334155)

@Composable
fun QuotationPreviewScreen(
    quotationId: String? = null,
    onBack: () -> Unit,
    onEditQuotation: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: QuotationViewModel = viewModel(
        factory = QuotationViewModelFactory(context)
    )

    // Local state to hold the one-shot fetched quotation
    var currentQuotationWithItems by remember { mutableStateOf<FullQuotation?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Assuming you have similar PDF logic in QuotationViewModel
    val pdfUri by viewModel.pdfUri.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(pdfUri) {
        pdfUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(intent, "Share Quotation")
            )
            viewModel.clearPdf()
        }
    }

    LaunchedEffect(quotationId) {
        if (quotationId != null) {
            isLoading = true
            currentQuotationWithItems = viewModel.getFullQuotationById(quotationId)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    fun deleteQuotationAction(id: String) {
        viewModel.deleteQuotation(id)
        onBack()
    }

    val customerName = currentQuotationWithItems?.customer?.party?.businessName ?: "Unknown Customer"

    AnimatedHeaderScrollView(
        largeTitle = "Quotation Preview",
        subtitle = currentQuotationWithItems?.customer?.party?.businessName,
        onBack = onBack,
        selectionCount = 1,
        showBack = true,
        isSelectionMode = true,
        onDeleteClick = { quotationId?.let { deleteQuotationAction(it) } },
        onEditClick = { quotationId?.let { onEditQuotation(it) } },
        onThirdOptionClick = {
            currentQuotationWithItems?.let {
                // Assuming you add this to your ViewModel
                viewModel.generatePdf(it, "")
            }
        }
    ) {
        val quotationData = currentQuotationWithItems

        if (isLoading || quotationData == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        } else {
            val quotation = quotationData.quotation
            val customer = quotationData.customer?.party
            val seller = quotationData.seller?.party

            // 1. Header & Status
            item {
                HeaderCard(quotation = quotation)
            }
            // 2. Addresses (Quoted To & Quoted By)
            item {
                AddressSection(customer = customer, seller = seller)
            }

            // 3. Products Table
            item {
                ProductDetailsSection(items = quotationData.items)
            }

            // 4. Financial Breakdown
            item {
                QuotationBreakdownCard(quotation = quotation)
            }

            // 5. Additional Info (Terms & Notes)
            item {
//                AdditionalInfoSection(quotation = quotation)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTIONS
// -----------------------------------------------------------------------------

@Composable
fun HeaderCard(quotation: QuotationEntity) {
    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val date = df.format(Date(quotation.quotationDate))
    val validUntil = df.format(Date(quotation.validUntilDate))

    // Status Color Logic mapping to your QuotationStatus Enum
    val statusColor = when (quotation.status) {
        QuotationStatus.ACCEPTED, QuotationStatus.CONVERTED_TO_INVOICE -> AccentGreen
        QuotationStatus.SENT -> AccentBlue
        QuotationStatus.EXPIRED, QuotationStatus.REJECTED -> AccentRed
        QuotationStatus.DRAFT -> AccentGray
    }
    val statusBg = when (quotation.status) {
        QuotationStatus.ACCEPTED, QuotationStatus.CONVERTED_TO_INVOICE -> AccentGreenBg
        QuotationStatus.SENT -> AccentBlueBg
        QuotationStatus.EXPIRED, QuotationStatus.REJECTED -> AccentRedBg
        QuotationStatus.DRAFT -> AccentGrayBg
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    LabelText("QUOTATION NO.")
                    Text(
                        text = quotation.quotationNumber,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    LabelText("STATUS")
                    Box(
                        modifier = Modifier
                            .background(statusBg.copy(alpha = 0.8f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quotation.status.name.replace("_", " "),
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Info Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("DATE")
                    Text(date, color = Color(0xFFF8FAFC), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("VALID UNTIL")
                    Text(validUntil, color = Color(0xFFF8FAFC), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AddressSection(customer: PartyEntity?, seller: PartyEntity?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quoted To
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            LabelText("QUOTED TO")
            Spacer(modifier = Modifier.height(4.dp))
            Text(customer?.businessName ?: "N/A", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!customer?.gstNumber.isNullOrBlank()) {
                Text("GSTIN: ${customer?.gstNumber}", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                customer?.billingAddress ?: customer?.address ?: "No address provided",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        // Quoted By
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            LabelText("QUOTED BY")
            Spacer(modifier = Modifier.height(4.dp))
            Text(seller?.businessName ?: "N/A", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!seller?.gstNumber.isNullOrBlank()) {
                Text("GSTIN: ${seller?.gstNumber}", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                seller?.address ?: "No address provided",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ProductDetailsSection(items: List<QuotationItemEntity>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Services & Products", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
            Text("${items.size} Total", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("ITEM & HSN", modifier = Modifier.weight(5f), style = headerStyle())
                Text("QTY", modifier = Modifier.weight(2f), style = headerStyle(), textAlign = TextAlign.Center)
                Text("TOTAL", modifier = Modifier.weight(3f), style = headerStyle(), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = Color(0xFF334155))

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item Name & Sub-details
                    Column(modifier = Modifier.weight(5f)) {
                        Text(
                            item.productNameSnapshot,
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (item.hsnSnapshot.isNotBlank()) {
                                Text("HSN: ${item.hsnSnapshot}", color = TextDarkGray, fontSize = 11.sp)
                            }
                            Text("₹${item.quotationPriceAtTime}/${item.unitSnapshot.ifBlank { "UNIT" }}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    // Qty Badge
                    Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${item.quantity.toInt()}", color = Color(0xFFF8FAFC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Taxable Amount (Item Total)
                    Text(
                        "₹${String.format("%.2f", item.taxableAmount)}",
                        modifier = Modifier.weight(3f),
                        textAlign = TextAlign.End,
                        color = Color(0xFFF8FAFC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
fun QuotationBreakdownCard(quotation: QuotationEntity) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334))
    ) {}
}

@Composable
private fun headerStyle() =
    androidx.compose.ui.text.TextStyle(color = TextDarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)