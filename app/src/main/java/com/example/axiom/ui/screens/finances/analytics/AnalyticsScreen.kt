package com.example.axiom.ui.screens.finances.analytics


//import com.example.axiom.data.finances.BusinessAnalyticsViewModelFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModel
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceViewModelFactory
import com.example.axiom.ui.theme.AxiomTheme
import java.text.DateFormatSymbols
import java.util.Locale

// -----------------------------------------------------------------------------
// 1. DATA MODELS
// -----------------------------------------------------------------------------
data class B2bInvoice(
    val id: String,
    val gstin: String,
    val invoiceNo: String,
    val date: String,
    val totalVal: Double,
    val taxableVal: Double,
    val igst: Double,
    val cgst: Double,
    val sgst: Double
)

data class HsnSummary(
    val id: String, val hsnCode: String, val uqc: String, val totalQty: Double,
    val taxableVal: Double, val rate: Double, val igst: Double, val cgst: Double, val sgst: Double
)


@Composable
fun MonthYearPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    onDismissRequest: () -> Unit,
    onDateSelected: (month: Int, year: Int) -> Unit
) {
    var currentYear by remember { mutableIntStateOf(initialYear) }
    var currentMonth by remember { mutableIntStateOf(initialMonth) }

    val months = remember { DateFormatSymbols().shortMonths.take(12) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Year Selector)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentYear-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Year")
                    }
                    Text(
                        text = currentYear.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { currentYear++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Months Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    itemsIndexed(months) { index, monthName ->
                        val isSelected = currentMonth == index
                        val bgColor =
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val textColor =
                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable { currentMonth = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthName.uppercase(),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onDateSelected(currentMonth, currentYear)
                        onDismissRequest()
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// 3. MAIN SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GSTAnalyticsScreen(
    onBack: () -> Unit, // Back navigation callback

) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDark = isSystemInDarkTheme()

    val context = LocalContext.current

    val viewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModelFactory(context)
    )

    val b2bInvoices by viewModel.b2bInvoices.collectAsState()
    val hsnSummaryList by viewModel.hsnSummaryList.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    // Format the date for the button (e.g., "Oct 2023")
    val monthName = remember(selectedMonth) {
        DateFormatSymbols().shortMonths[selectedMonth]
    }

    if (showDatePicker) {
        MonthYearPickerDialog(
            initialMonth = selectedMonth,
            initialYear = selectedYear,
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { month, year ->
                viewModel.setMonthAndYear(month, year) // This automatically triggers the DB fetch!
            }
        )
    }



    AnimatedHeaderScrollView(
        largeTitle = "Gst Analytics",
        subtitle = "$monthName $selectedYear",
        onBack = onBack,
        onHeaderClick = { showDatePicker = true }
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // --- Tab Layout ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE5E7EB),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "GSTR-1",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = 0 }
                    TabButton(
                        text = "GSTR-3B",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = 1 }
                }

                // --- Content ---
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        B2bInvoiceTable(b2bInvoices)
                        HsnSummaryTable(hsnSummaryList)
                        DocumentsIssuedSection()
                    }
                } else {
                    // Placeholder for 3B
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "GSTR-3B Dashboard Coming Soon...",
                            color = AxiomTheme.components.card.mutedText
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun B2bInvoiceTable(invoices: List<B2bInvoice>) {
    var ackedRows by remember { mutableStateOf(setOf<String>()) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(20.dp))
    ) {
        // Table Header
        Text(
            text = "1. B2B Invoices (Table 4A, 4B, 4C, 6B, 6C)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            modifier = Modifier.padding(20.dp)
        )
        Divider(color = AxiomTheme.components.card.border)

        // Sync Scrolling Table
        Row(modifier = Modifier.fillMaxWidth()) {

            // --- STICKY COLUMN (Ack) ---

            Column(
                modifier = Modifier
                    .width(50.dp)
                    .background(AxiomTheme.components.card.background)
            ) {
                // 1. Header Cell
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ACK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }

                // 2. Divider matching the right side header
                Divider(color = AxiomTheme.components.card.border)

                // 3. Data Cells (Checkboxes)
                invoices.forEach { invoice ->
                    val isAcked = ackedRows.contains(invoice.id)
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                            .background(
                                if (isAcked) AxiomTheme.components.card.background.copy(alpha = 0.05f)
                                else AxiomTheme.components.card.background
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = isAcked,
                            onCheckedChange = {
                                ackedRows =
                                    if (it) ackedRows + invoice.id else ackedRows - invoice.id
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                        )
                    }

                    // 4. Divider matching the right side data rows
                    Divider(color = AxiomTheme.components.card.border)
                }

                // 5. Total Row Space (Matching height and background)
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                )
            }


            // --- HORIZONTAL SCROLLABLE COLUMNS ---
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {

                // Helper to generate a fully dynamic column
                @Composable
                fun TableDataColumn(
                    header: String,
                    isRight: Boolean = false,
                    totalValue: String = "", // Empty string if no total for this column
                    isTotalBold: Boolean = true,
                    dataCellContent: @Composable (B2bInvoice, Boolean) -> Unit
                ) {
                    Column {
                        // Header Cell
                        DynamicTableCell(
                            text = header,
                            isHeader = true,
                            isRight = isRight,
                            backgroundColor = AxiomTheme.components.card.background
                        )
                        Divider(color = AxiomTheme.components.card.border)

                        // Data Cells
                        invoices.forEach { invoice ->
                            val isAcked = ackedRows.contains(invoice.id)
                            dataCellContent(invoice, isAcked)
                            Divider(color = AxiomTheme.components.card.border)
                        }

                        // Total Cell
                        DynamicTableCell(
                            text = totalValue,
                            isRight = isRight,
                            isBold = isTotalBold,
                            backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f),
                            textColor = if (totalValue.contains("TOTAL")) Color(0xFF3B82F6) else AxiomTheme.components.card.title
                        )
                    }
                }

                // 1. GSTIN Column
                TableDataColumn(
                    header = "GSTIN / UIN",
                    totalValue = "TOTAL FOR B2B"
                ) { invoice, isAcked ->
                    DynamicTableCell(text = invoice.gstin, isAcked = isAcked, isBold = true)
                }

                // 2. Invoice No Column
                TableDataColumn(header = "Invoice No.") { invoice, isAcked ->
                    DynamicTableCell(text = invoice.invoiceNo, isAcked = isAcked)
                }

                // 3. Date Column
                TableDataColumn(header = "Date") { invoice, isAcked ->
                    DynamicTableCell(text = invoice.date, isAcked = isAcked)
                }

                // 4. Total Invoice Value
                TableDataColumn(
                    header = "Total Inv Val",
                    isRight = true,
                    totalValue = invoices.sumOf { it.totalVal }.toInr()
                ) { invoice, isAcked ->
                    DynamicTableCell(
                        text = invoice.totalVal.toInr(),
                        isRight = true,
                        isAcked = isAcked,
                        isBold = true
                    )
                }

                // 5. Taxable Value
                TableDataColumn(
                    header = "Taxable Val",
                    isRight = true,
                    totalValue = invoices.sumOf { it.taxableVal }.toInr()
                ) { invoice, isAcked ->
                    DynamicTableCell(
                        text = invoice.taxableVal.toInr(),
                        isRight = true,
                        isAcked = isAcked
                    )
                }

                // 6. IGST
                TableDataColumn(
                    header = "IGST",
                    isRight = true,
                    totalValue = invoices.sumOf { it.igst }.toInr()
                ) { invoice, isAcked ->
                    DynamicTableCell(text = invoice.igst.toInr(), isRight = true, isAcked = isAcked)
                }

                // 7. CGST
                TableDataColumn(
                    header = "CGST",
                    isRight = true,
                    totalValue = invoices.sumOf { it.cgst }.toInr()
                ) { invoice, isAcked ->
                    DynamicTableCell(text = invoice.cgst.toInr(), isRight = true, isAcked = isAcked)
                }

                // 8. SGST
                TableDataColumn(
                    header = "SGST",
                    isRight = true,
                    totalValue = invoices.sumOf { it.sgst }.toInr()
                ) { invoice, isAcked ->
                    DynamicTableCell(text = invoice.sgst.toInr(), isRight = true, isAcked = isAcked)
                }
            }
        }
    }
}

@Composable
fun DynamicTableCell(
    text: String,
    isHeader: Boolean = false,
    isRight: Boolean = false,
    isBold: Boolean = false,
    isAcked: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = AxiomTheme.components.card.title
) {
    val textAlpha = if (isAcked && !isHeader) 0.5f else 1f
    val decoration = if (isAcked && !isHeader) TextDecoration.LineThrough else TextDecoration.None

    // Resolve row background color if not explicitly passed
    val resolvedBg = if (backgroundColor == Color.Transparent) {
        if (isAcked) AxiomTheme.components.card.background.copy(alpha = 0.05f)
        else AxiomTheme.components.card.background
    } else backgroundColor

    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .defaultMinSize(minWidth = 100.dp) // Prevents empty columns (like Total row gaps) from shrinking to 0
            .background(resolvedBg)
            .padding(horizontal = 16.dp),
        contentAlignment = if (isRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text.takeIf { it != "₹ 0" } ?: "-",
            fontSize = if (isHeader) 11.sp else 13.sp,
            fontWeight = if (isHeader || isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isHeader) Color.Gray else textColor.copy(alpha = textAlpha),
            textDecoration = decoration,
            maxLines = 1,
            softWrap = false // CRITICAL: This prevents text from wrapping and forces the column to stretch dynamically
        )
    }
}

@Composable
fun HsnSummaryTable(hsnList: List<HsnSummary>) {
    var ackedRows by remember { mutableStateOf(setOf<String>()) }




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(20.dp))
    ) {
        Text(
            text = "2. HSN Wise Summary (Table 12)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title,
            modifier = Modifier.padding(20.dp)
        )
        Divider(color = AxiomTheme.components.card.border)

        Row(modifier = Modifier.fillMaxWidth()) {

            // --- STICKY COLUMN (Ack) ---
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .background(AxiomTheme.components.card.background)
            ) {
                TableHeaderCell("Ack", isCenter = true)
                hsnList.forEach { hsn ->
                    val isAcked = ackedRows.contains(hsn.id)
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()

                            .border(1.dp, AxiomTheme.components.card.border),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = isAcked,
                            onCheckedChange = {
                                ackedRows = if (it) ackedRows + hsn.id else ackedRows - hsn.id
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                )
            }

            // --- HORIZONTAL SCROLLABLE COLUMNS ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                Row {
                    TableHeaderCell("HSN Code", width = 100.dp)
                    TableHeaderCell("UQC", width = 60.dp, isCenter = true)
                    TableHeaderCell("Total Qty", width = 100.dp, isRight = true)
                    TableHeaderCell("Taxable Val", width = 120.dp, isRight = true)
                    TableHeaderCell("Rate (%)", width = 80.dp, isCenter = true)
                    TableHeaderCell("IGST", width = 100.dp, isRight = true)
                    TableHeaderCell("CGST", width = 100.dp, isRight = true)
                    TableHeaderCell("SGST", width = 100.dp, isRight = true)
                }

                hsnList.forEach { hsn ->
                    val isAcked = ackedRows.contains(hsn.id)
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .background(
                                if (isAcked) AxiomTheme.components.card.background.copy(
                                    alpha = 0.05f
                                ) else AxiomTheme.components.card.background
                            )
                    ) {
                        TableCell(hsn.hsnCode, 100.dp, isAcked = isAcked, isBold = true)
                        TableCell(hsn.uqc, 60.dp, isCenter = true, isAcked = isAcked)
                        TableCell(
                            hsn.totalQty.toString(),
                            100.dp,
                            isRight = true,
                            isAcked = isAcked
                        )
                        TableCell(
                            hsn.taxableVal.toInr(),
                            120.dp,
                            isRight = true,
                            isAcked = isAcked,
                            isBold = true
                        )
                        TableCell(hsn.rate.toString(), 80.dp, isCenter = true, isAcked = isAcked)
                        TableCell(hsn.igst.toInr(), 100.dp, isRight = true, isAcked = isAcked)
                        TableCell(hsn.cgst.toInr(), 100.dp, isRight = true, isAcked = isAcked)
                        TableCell(hsn.sgst.toInr(), 100.dp, isRight = true, isAcked = isAcked)
                    }
                    Divider(color = AxiomTheme.components.card.border)
                }

                // Total Row
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                ) {
                    TableCell("TOTAL FOR HSN", 260.dp, isBold = true)
                    TableCell(
                        hsnList.sumOf { it.taxableVal }.toInr(),
                        120.dp,
                        isRight = true,
                        isBold = true
                    )
                    TableCell("", 80.dp)
                    TableCell(
                        hsnList.sumOf { it.igst }.toInr(),
                        100.dp,
                        isRight = true,
                        isBold = true
                    )
                    TableCell(
                        hsnList.sumOf { it.cgst }.toInr(),
                        100.dp,
                        isRight = true,
                        isBold = true
                    )
                    TableCell(
                        hsnList.sumOf { it.sgst }.toInr(),
                        100.dp,
                        isRight = true,
                        isBold = true
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentsIssuedSection() {
    val isDark = isSystemInDarkTheme()



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AxiomTheme.components.card.background)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "3. Documents Issued (Table 13)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AxiomTheme.components.card.title
        )

        // Only doing Outward Supplies for brevity
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Invoices for Outward Supply",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AxiomTheme.components.card.title
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Serial No. From",
                    fontSize = 12.sp,
                    color = AxiomTheme.components.card.subtitle
                )
                Text(
                    "INV-001",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AxiomTheme.components.card.title
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Serial No. To", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                Text(
                    "INV-003",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AxiomTheme.components.card.title
                )
            }
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = AxiomTheme.components.card.border
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Number", fontSize = 12.sp, color = AxiomTheme.components.card.subtitle)
                Text(
                    "3",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AxiomTheme.components.card.title
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Net Issued",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AxiomTheme.components.card.title
                )
                Text("3", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
            }
        }
    }
}

// --- Helper Composables & Extensions ---

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isSelected) (if (isDark) Color(0xFF18181A) else Color.White) else Color.Transparent
    val textColor = if (isSelected) (if (isDark) Color.White else Color(0xFF111827)) else Color.Gray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp? = null,
    isRight: Boolean = false,
    isCenter: Boolean = false
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .padding(horizontal = 12.dp),
        contentAlignment = if (isRight) Alignment.CenterEnd else if (isCenter) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AxiomTheme.components.card.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TableCell(
    text: String, width: androidx.compose.ui.unit.Dp,
    isRight: Boolean = false, isCenter: Boolean = false,
    isAcked: Boolean = false, isBold: Boolean = false,

    ) {

    val decoration = if (isAcked) TextDecoration.LineThrough else TextDecoration.None

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(width)
            .padding(horizontal = 12.dp),
        contentAlignment = if (isRight) Alignment.CenterEnd else if (isCenter) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = text.takeIf { it != "₹ 0" } ?: "-", // Display dash for 0 value
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = AxiomTheme.components.card.title,
            textDecoration = decoration,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Simple INR formatter
fun Double.toInr(): String = "₹ ${String.format(Locale.US, "%,.0f", this)}"