package com.example.axiom.ui.screens.finances.quotation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.Product
import com.example.axiom.data.finances.dataStore.FinancePreferences
import com.example.axiom.data.finances.dataStore.SelectedSellerPref
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.button.AppIcons
import com.example.axiom.ui.components.shared.header.HeaderActionSpec
import com.example.axiom.ui.components.shared.header.ListHeader
import com.example.axiom.ui.components.shared.header.SearchSpec
import com.example.axiom.ui.screens.finances.customer.components.CustomerListSheetWrapper
import com.example.axiom.ui.screens.finances.product.components.ProductListSheetWrapper
import com.example.axiom.ui.screens.finances.quotation.components.QuotationEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationForm
import com.example.axiom.ui.screens.finances.quotation.components.QuotationFull
import com.example.axiom.ui.screens.finances.quotation.components.QuotationItemEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModel
import com.example.axiom.ui.screens.finances.quotation.components.QuotationViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


enum class QuotationSheetMode {
    DETAILS,
    CREATE
}

enum class CreateQuotationMode {
    FORM,
    PRODUCT_SELECTION,
    CUSTOMER_SELECTION
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationRoute(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: QuotationViewModel = viewModel(
        factory = QuotationViewModelFactory(context)
    )


    val quotations by viewModel.quotations.collectAsState()                 //all quotation
    val completeQuotation by viewModel.selectedQuotation.collectAsState()   //detailed quotation

    // states
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val selectedIds = rememberSaveable { mutableStateListOf<String>() }     // for deletion
    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
    val selectedCount = selectedIds.size


    var showSheet by remember { mutableStateOf(false) }
    var sheetMode by remember { mutableStateOf<QuotationSheetMode?>(null) }

    var selectedQuotationId by remember { mutableStateOf<String?>(null) }

    val financePreferences = remember { FinancePreferences(context) }
    val selectedSeller by financePreferences.selectedSeller.collectAsState(
        initial = SelectedSellerPref(null, null, null)
    )


    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ListHeader(
                title = "Quotations",
                back = HeaderActionSpec(
                    icon = AppIcons.Back,
                    contentDescription = "Back",
                    onClick = onBack
                ),
                add = HeaderActionSpec(             // ← add this if you have a + button
                    icon = AppIcons.Add,
                    contentDescription = "New Quotation",
                    onClick = {
                        sheetMode = QuotationSheetMode.CREATE
                        showSheet = true
                    }
                ),
                edit = if (selectedCount == 1) HeaderActionSpec(
                    icon = AppIcons.Edit,
                    contentDescription = "Edit",
                    onClick = {
                        val id = selectedIds.first()
                        selectedIds.clear()
                        // Optional: exit selection after edit
                        // selectedIds.clear()
                    }
                ) else null,
                delete = if (selectedCount >= 1) HeaderActionSpec(
                    icon = AppIcons.Delete,
                    contentDescription = "Delete",
                    onClick = {
                        viewModel.deleteMany(selectedIds.toList())
                        selectedIds.clear()
                    }
                ) else null,
                search = SearchSpec.Inline(
                    expanded = searchExpanded,
                    onExpandedChange = { shouldExpand ->
                        searchExpanded = shouldExpand
                        if (!shouldExpand) {
                            query = ""          // ← clear query when collapsing
                        }
                    },
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.updateSearchQuery(it)
                    },
                    placeholder = "Search by customer",
                    onSearchImeAction = null,
                    onClear = { query = "" }
                ),
                isSelectionMode = isSelectionMode,
                selectedCount = selectedCount,
                onCancelSelection = {
                    selectedIds.clear()
                },
            )


            if (quotations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (query.isNotBlank())
                            "No results found"
                        else
                            "No quotations yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    items(quotations, key = { it.id }) { row ->

                        val isSelected = row.id in selectedIds

                        QuotationCard(
                            row = row,
                            isSelected = isSelected,

                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedIds.remove(row.id)
                                    else selectedIds.add(row.id)
                                } else {
                                    selectedQuotationId = row.id
                                    viewModel.loadQuotation(row.id)
                                    sheetMode = QuotationSheetMode.DETAILS
                                    showSheet = true
                                }
                            },

                            onLongClick = {
                                if (isSelected) selectedIds.remove(row.id)
                                else selectedIds.add(row.id)
                            }
                        )
                    }


                }
            }
        }
        AppBottomSheet(
            showSheet = showSheet,
            onDismiss = {
                showSheet = false
                sheetMode = null
                selectedQuotationId = null
            }
        ) {

            when (sheetMode) {

                QuotationSheetMode.DETAILS -> {
                    completeQuotation?.let { row ->
                        QuotationDetailContent(
                            row = row,
                            onPrintClick = {},
                            onShareClick = {}
                        )
                    }
                }

                QuotationSheetMode.CREATE -> {
                    CreateQuotationSheetContent(
                        sellerId = selectedSeller.id ?: return@AppBottomSheet,
                        onCreate = { quotation, items ->
                            viewModel.createQuotation(quotation, items)
                            showSheet = false
                        }
                    )
                }

                null -> {}
            }
        }

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuotationCard(
    row: QuotationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val borderColor =
        if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val containerColor =
        if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else
            MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
//            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${row.quotationNo}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = row.issueDate.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = row.totalAmount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(6.dp))


        }
    }
}


@Composable
fun QuotationDetailContent(
    row: QuotationFull,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 140.dp
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            /* ---------------- HEADER ---------------- */

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Column(Modifier.weight(1f)) {

                        Text(
                            text = "Quotation #${row.quotation.quotationNo}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = row.quotation.issueDate.toString(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }


                }
            }

            /* ---------------- BILL TO ---------------- */

            item {

                SectionLabel("Bill To")

                GlassCard {

                    Row(verticalAlignment = Alignment.Top) {

                        CircleAvatar(row.customer.name)

                        Spacer(Modifier.width(16.dp))

                        Column(Modifier.weight(1f)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {

                                Column(Modifier.weight(1f)) {

                                    Text(
                                        row.customer.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        "client@email.com",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }

                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.05f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {

                                SmallMeta("Due Date", row.quotation.issueDate.toString())
                                SmallMeta("ID", "#${row.quotation.quotationNo}")
                            }
                        }
                    }
                }
            }

            /* ---------------- ITEMS ---------------- */

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionLabel("Items Breakdown", false)
                    Text(
                        "${row.items.size} Items",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                GlassCard(noPadding = true) {

                    Column {

                        TableHeader()

                        row.items.forEach { item ->
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            ItemRow(item)
                        }

                        Divider(color = Color.White.copy(alpha = 0.05f))

                        Column(Modifier.padding(16.dp)) {
                            MiniRow("Subtotal", row.quotation.totalTaxableAmount)
                            MiniRow("Discount", row.quotation.totalDiscountAmount)
                            MiniRow("Tax", row.quotation.totalAmount)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                TotalHighlight(row.quotation.totalAmount)
            }

            /* ---------------- TERMS ---------------- */

            item {

                SectionLabel("Terms & Conditions")

                GlassCard {

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Payment within 15 days", "Late fee 14% applicable").forEach {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    it,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        /* ---------------- BOTTOM ACTION BAR ---------------- */

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xCC050505))
                .border(1.dp, Color.White.copy(alpha = 0.1f))
                .padding(20.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedButton(
                    onClick = onPrintClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Print")
                }

                Button(
                    onClick = onShareClick,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Share PDF")
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            )
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

@Composable
private fun GlassCard(
    noPadding: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.03f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (noPadding) 0.dp else 20.dp),
            content = content
        )
    }
}

@Composable
private fun SectionLabel(text: String, spaced: Boolean = true) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = if (spaced) Modifier.padding(start = 4.dp) else Modifier
    )
}

@Composable
private fun CircleAvatar(name: String) {
    val initials = name.split(" ")
        .take(2)
        .joinToString("") { it.first().toString() }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFF6366F1)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallMeta(label: String, value: String) {
    Column {
        Text(
            label.uppercase(),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

//@Composable
//private fun StatusChip(status: QuotationStatus) {
//    val approved = status == QuotationStatus.APPROVED
//
//    Row(
//        modifier = Modifier
//            .background(
//                if (approved) Color(0x1422C55E)
//                else Color.White.copy(alpha = 0.06f),
//                RoundedCornerShape(50)
//            )
//            .border(
//                1.dp,
//                if (approved) Color(0x3322C55E)
//                else Color.White.copy(alpha = 0.1f),
//                RoundedCornerShape(50)
//            )
//            .padding(horizontal = 12.dp, vertical = 6.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            Icons.Default.CheckCircle,
//            null,
//            tint = if (approved) Color(0xFF22C55E) else Color.White,
//            modifier = Modifier.size(16.dp)
//        )
//        Spacer(Modifier.width(6.dp))
//        Text(
//            status.name,
//            color = if (approved) Color(0xFF22C55E) else Color.White,
//            fontSize = 11.sp,
//            fontWeight = FontWeight.Bold,
//            letterSpacing = 1.sp
//        )
//    }
//}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "DESCRIPTION",
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "QTY",
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "TOTAL",
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ItemRow(item: QuotationItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {

        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            item.hsn?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("${item.quantity}", color = Color.White)
        }

        Text(
            "₹ ${item.discountPercent}",
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun MiniRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Text("₹ %.2f".format(value), color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun TotalHighlight(total: Double) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "TOTAL QUOTATION AMOUNT",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "₹",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "%.2f".format(total),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedProductDropdown(
    products: List<Product>,
    selected: Product?,
    onSelected: (Product) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Product") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.filter { it.active }.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onSelected(product)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateQuotationSheetContent(
    sellerId: String,
    onCreate: (QuotationEntity, List<QuotationItemEntity>) -> Unit
) {

    var mode by remember { mutableStateOf(CreateQuotationMode.FORM) }

    var quotationNumber by remember { mutableStateOf("Q-${System.currentTimeMillis()}") }
    var issueDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var selectedCustomer by remember { mutableStateOf<CustomerFirm?>(null) }
    var items by remember { mutableStateOf<List<QuotationItemEntity>>(emptyList()) }
    var discountPercent by remember { mutableStateOf(0.0) }

    val formattedDate = remember(issueDateMillis) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.format(Date(issueDateMillis))
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                slideInHorizontally { it } togetherWith
                        slideOutHorizontally { -it }
            },
            label = "CreateQuotationSheetAnimation",
            modifier = Modifier
                .fillMaxSize()
              
        ) { target ->

            when (target) {

                CreateQuotationMode.FORM -> {

                    QuotationForm(
                        quotationNumber = quotationNumber,
                        issueDate = formattedDate,
                        selectedCustomer = selectedCustomer,
                        items = items,
                        discountPercent = discountPercent,
                        onQuotationNumberChange = { quotationNumber = it },
                        onIssueDateChange = { newString ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            issueDateMillis = formatter.parse(newString)?.time ?: issueDateMillis
                        },
                        onSelectCustomerClick = {
                            mode = CreateQuotationMode.CUSTOMER_SELECTION
                        },
                        onSelectProductClick = {
                            mode = CreateQuotationMode.PRODUCT_SELECTION
                        },
                        onQtyChange = { id, qty ->
                            items = items.map {
                                if (it.id == id) it.copy(quantity = qty)
                                else it
                            }
                        },
                        onRateChange = { id, rate ->
                            items = items.map {
                                if (it.id == id) it.copy(rate = rate)
                                else it
                            }
                        },
                        onItemDelete = { id ->
                            items = items.filterNot { it.id == id }
                        },
                        onDiscountChange = { discountPercent = it }
                    )
                }

                CreateQuotationMode.PRODUCT_SELECTION -> {

                    ProductListSheetWrapper(
                        onConfirmSelection = { selectedProducts ->

                            val newItems = selectedProducts.map { product ->
                                QuotationItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    quotationId = "",
                                    productId = product.id,
                                    name = product.name,
                                    hsn = product.hsn,
                                    unit = product.unit,
                                    quantity = 1.0,
                                    rate = product.sellingPrice,
                                    discountPercent = 0.0,
                                    taxableAmount = product.sellingPrice
                                )
                            }

                            items = items + newItems
                            mode = CreateQuotationMode.FORM
                        },
                        onBack = {
                            mode = CreateQuotationMode.FORM
                        }
                    )
                }


                CreateQuotationMode.CUSTOMER_SELECTION -> {
                    CustomerListSheetWrapper(
                        onConfirmSelection = {
                            selectedCustomer = it
                            mode = CreateQuotationMode.FORM
                        },
                        onBack = {
                            mode = CreateQuotationMode.FORM
                        }
                    )

                }
            }
        }

        // Bottom Save Button (Fixed)
        if (mode == CreateQuotationMode.FORM) {

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                Divider()

                Button(
                    onClick = {
                        val quotationId = UUID.randomUUID().toString()

                        val updatedItems = items.map { item ->
                            val gross = item.quantity * item.rate
                            val discountAmount = gross * item.discountPercent / 100
                            val taxable = gross - discountAmount

                            item.copy(
                                quotationId = quotationId,
                                taxableAmount = taxable
                            )
                        }
                        val gstRate = 18.0

                        val totalTaxable = updatedItems.sumOf { it.taxableAmount }
                        val totalTax = totalTaxable * gstRate / 100
                        val totalAmount = totalTaxable + totalTax

                        val quotation = QuotationEntity(
                            id = quotationId,
                            quotationNo = quotationNumber,
                            sellerId = sellerId,
                            customerId = selectedCustomer?.id ?: return@Button,
                            issueDate = issueDateMillis,
                            placeOfSupply = selectedCustomer?.stateCode ?: "",
                            totalTaxableAmount = totalTaxable,
                            taxRate = gstRate,
                            totalTax = totalTax,
                            totalAmount = totalAmount,
                            totalDiscountAmount = updatedItems.sumOf {
                                it.quantity * it.rate * it.discountPercent / 100
                            },
                            amountInWords = totalAmount.toString()

                        )

                        onCreate(quotation, updatedItems)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedCustomer != null && items.isNotEmpty()
                ) {
                    Text("Save Quotation")
                }
            }
        }
    }
}



