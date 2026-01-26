package com.example.axiom.ui.screens.finances.analytics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.CreateInvoiceViewModel
import com.example.axiom.data.finances.CreateInvoiceViewModelFactory
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.Invoice
import com.example.axiom.data.finances.InvoiceStatus
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter


private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun GSTSummaryScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: CreateInvoiceViewModel = viewModel(
        factory = CreateInvoiceViewModelFactory(context)
    )
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val invoices by viewModel.invoices.collectAsState(initial = emptyList())


    val customerMap = remember(customers) {
        customers.associateBy { it.id }
    }

    var selectedMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    /* ---------------- MONTH FILTER ---------------- */

//    val monthAllInvoices = remember(invoices, selectedMonth) {
//        invoices.filter { inv ->
//            val date = runCatching {
//                LocalDate.parse(inv.date, DATE_FMT)
//            }.getOrNull() ?: return@filter false
//
//            YearMonth.from(date) == selectedMonth
//        }
//    }
//
//    val monthFinalInvoices = remember(monthAllInvoices) {
//        monthAllInvoices.filter {
//            it.status == InvoiceStatus.FINAL && !it.deleted
//        }
//    }
//
//    val monthB2bInvoices = remember(monthFinalInvoices, customerMap) {
//        monthFinalInvoices.filter {
//            customerMap[it.customerId]?.gstin != null
//        }
//    }

    val monthAllInvoices = remember(invoices, selectedMonth) {
        val zoneId = ZoneId.systemDefault() // Get the system's default time zone
        invoices.filter { inv ->
            // Convert the 'createdAt' Long timestamp to a LocalDate
            val timestamp = inv.date.toLongOrNull() ?: return@filter false
            val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
            YearMonth.from(date) == selectedMonth
        }
    }

    val monthFinalInvoices = remember(monthAllInvoices) {
        monthAllInvoices.filter {
            it.status == InvoiceStatus.FINAL && !it.deleted
        }
    }

    val monthB2bInvoices = remember(monthFinalInvoices, customerMap) {
        monthFinalInvoices.filter {
            // Ensure customer exists and has a GSTIN
            customerMap[it.customerDetails!!.id]?.gstin?.isNotBlank() == true
        }
    }


    /* ---------------- UI ---------------- */

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            MonthSelector(
                selected = selectedMonth,
                onChange = { selectedMonth = it }
            )
        }

        item { SectionTitle("B2B Invoices (GSTR-1)") }


        if (monthB2bInvoices.isEmpty()) {
            item {
                Text(
                    "No B2B invoices found for ${selectedMonth.month.name} ${selectedMonth.year}.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                B2bInvoiceTable(
                    invoices = monthB2bInvoices,
                    customerMap = customerMap
                )
            }
        }



        item {
            Divider(thickness = 2.dp)
            SectionTitle("HSN-wise Summary")
        }

        item {
            HsnSummaryTable(monthFinalInvoices)
        }

        item {
            Divider(thickness = 2.dp)
            SectionTitle("Documents Issued")
        }

        item {
            DocumentsIssuedSummary(monthAllInvoices)
        }
    }
}

@Composable
private fun TableRow(
    cells: List<String>,
    columnWidths: List<Dp>,
    header: Boolean = false
) {
    Row {
        cells.forEachIndexed { index, text ->
            Text(
                text = text,
                modifier = Modifier
                    .width(columnWidths[index])
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                maxLines = 1,
                softWrap = false,
                style = if (header)
                    MaterialTheme.typography.labelMedium
                else
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
private fun B2bInvoiceTable(
    invoices: List<Invoice>,
    customerMap: Map<String, CustomerFirm>
) {
    val scrollState = rememberScrollState()

    val columnWidths = listOf(
        160.dp, // GSTIN
        120.dp, // Invoice No
        110.dp, // Date
        140.dp, // Invoice Value
        140.dp, // Place
        110.dp, // Supply
        120.dp, // Taxable
        120.dp, // IGST
        120.dp, // CGST
        120.dp  // SGST
    )

    Column(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {

        TableRow(
            header = true,
            columnWidths = columnWidths,
            cells = listOf(
                "GSTIN",
                "Invoice No",
                "Date",
                "Invoice Value",
                "Place",
                "Supply",
                "Taxable",
                "IGST",
                "CGST",
                "SGST"
            )
        )

        HorizontalDivider(thickness = 1.dp)

        invoices.forEach { inv ->
            val customer = customerMap[inv.customerDetails!!.id] ?: return@forEach

            val date = inv.date.toLongOrNull()?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DATE_FMT)
            } ?: inv.date

            TableRow(
                columnWidths = columnWidths,
                cells = listOf(
                    customer.gstin ?: "-",
                    inv.invoiceNo,
                    date,
                    "₹${inv.totalAmount}",
                    customer.stateCode ?: "-",
                    inv.supplyType.name,
                    "₹${inv.totalBeforeTax}",
                    "₹${inv.gst.igstAmount}",
                    "₹${inv.gst.cgstAmount}",
                    "₹${inv.gst.sgstAmount}"
                )
            )

            HorizontalDivider()
        }
    }
}


@Composable
private fun HsnSummaryTable(invoices: List<Invoice>) {

    val rows = remember(invoices) {

        val expanded = invoices.flatMap { inv ->
            val invoiceTaxable = inv.totalBeforeTax.takeIf { it > 0.0 } ?: 1.0

            inv.items.map { item ->
                val ratio = item.total / invoiceTaxable

                HsnRow(
                    hsn = item.hsn,
                    description = item.name,
                    unit = item.unit,
                    quantity = item.quantity,
                    taxable = item.total,
                    igst = inv.gst.igstAmount * ratio,
                    cgst = inv.gst.cgstAmount * ratio,
                    sgst = inv.gst.sgstAmount * ratio
                )
            }
        }

        expanded.groupBy { it.hsn }.map { (_, list) ->
            HsnRow(
                hsn = list.first().hsn,
                description = list.first().description,
                unit = list.first().unit,
                quantity = list.sumOf { it.quantity },
                taxable = list.sumOf { it.taxable },
                igst = list.sumOf { it.igst },
                cgst = list.sumOf { it.cgst },
                sgst = list.sumOf { it.sgst }
            )
        }
    }

    if (rows.isEmpty()) {
        Text("No HSN data available for this month.")
        return
    }

    val scrollState = rememberScrollState()

    val columnWidths = listOf(
        120.dp, // HSN
        220.dp, // Description
        90.dp,  // UQC
        90.dp,  // Qty
        130.dp, // Taxable
        120.dp, // IGST
        120.dp, // CGST
        120.dp  // SGST
    )

    Column(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        TableRow(
            header = true,
            columnWidths = columnWidths,
            cells = listOf(
                "HSN",
                "Description",
                "UQC",
                "Qty",
                "Taxable",
                "IGST",
                "CGST",
                "SGST"
            )
        )
        HorizontalDivider()
        rows.forEach {
            TableRow(
                columnWidths = columnWidths,
                cells = listOf(
                    it.hsn,
                    it.description,
                    it.unit,
                    it.quantity.toString(),
                    "₹${"%.2f".format(it.taxable)}",
                    "₹${"%.2f".format(it.igst)}",
                    "₹${"%.2f".format(it.cgst)}",
                    "₹${"%.2f".format(it.sgst)}"
                )
            )
            HorizontalDivider()
        }
    }


}

private data class HsnRow(
    val hsn: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val taxable: Double,
    val igst: Double,
    val cgst: Double,
    val sgst: Double
)


@Composable
private fun DocumentsIssuedSummary(invoices: List<Invoice>) {

    val issued = invoices.count { it.status == InvoiceStatus.FINAL && !it.deleted }
    val cancelled = invoices.count { it.status == InvoiceStatus.CANCELLED }
    val net = issued

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val seriesStart = invoices.filter { it.status == InvoiceStatus.FINAL && !it.deleted }
                .minOfOrNull { it.invoiceNo.toLongOrNull() ?: Long.MAX_VALUE } ?: 0
            val seriesEnd = invoices.filter { it.status == InvoiceStatus.FINAL && !it.deleted }
                .maxOfOrNull { it.invoiceNo.toLongOrNull() ?: 0 } ?: 0

            Text("From Serial No: $seriesStart")
            Text("To Serial No: $seriesEnd")
            Text("Total Number: $issued")
            Text("Cancelled: $cancelled")
            Text("Net Issued: ${issued - cancelled}")
        }
    }
}


@Composable
fun MonthSelector(
    selected: YearMonth,
    onChange: (YearMonth) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { onChange(selected.minusMonths(1)) }) { Text("◀") }
        Text("${selected.month.name} ${selected.year}")
        Button(onClick = { onChange(selected.plusMonths(1)) }) { Text("▶") }
    }
}



