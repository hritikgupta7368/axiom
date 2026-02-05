package com.example.axiom.ui.screens.finances.quotation

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.button.AppIcons
import com.example.axiom.ui.components.shared.header.HeaderActionSpec
import com.example.axiom.ui.components.shared.header.ListHeader
import com.example.axiom.ui.components.shared.header.SearchSpec

data class QuotationRow(
    val id: Int,
    val customer: String,
    val amount: String,
    val status: String
)

@Composable
fun QuotationRoute(
    onBack: () -> Unit
) {

    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }


    val selectedIds: SnapshotStateSet<Int> = rememberSaveable { mutableStateSetOf() }
    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
    val selectedCount = selectedIds.size

    val all = remember {
        listOf(
            QuotationRow(101, "Acme Corp", "$1,250.00", "Draft"),
            QuotationRow(102, "Nova Retail", "$980.00", "Sent"),
            QuotationRow(103, "BlueSky Labs", "$4,120.00", "Approved"),
            QuotationRow(104, "Orchard Foods", "$2,340.00", "Draft"),
            QuotationRow(105, "Summit Health", "$6,890.00", "Rejected"),
            QuotationRow(106, "Kite Logistics", "$1,430.00", "Sent"),
            QuotationRow(107, "Beacon Media", "$3,210.00", "Approved"),
            QuotationRow(108, "Vertex Studio", "$760.00", "Draft"),
            QuotationRow(109, "Summit Health", "$6,890.00", "Rejected"),
            QuotationRow(110, "Kite Logistics", "$1,430.00", "Sent"),
            QuotationRow(111, "Beacon Media", "$3,210.00", "Approved"),
            QuotationRow(112, "Vertex Studio", "$760.00", "Draft"),
            QuotationRow(113, "Summit Health", "$6,890.00", "Rejected"),
            QuotationRow(114, "Kite Logistics", "$1,430.00", "Sent"),

            )
    }

    val filtered = remember(query, all) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) all
        else all.filter {
            it.customer.lowercase().contains(q) ||
                    it.status.lowercase().contains(q) ||
                    it.id.toString().contains(q)
        }
    }

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
                    onClick = {}
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
                        selectedIds.clear()
                        // In real app: remove from database + clear selection
                        // selectedIds.clear()
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
                    onQueryChange = { query = it },
                    placeholder = "Search by customer, id, status...",
                    onSearchImeAction = null,
                    onClear = { query = "" }
                ),
                isSelectionMode = isSelectionMode,
                selectedCount = selectedCount,
                onCancelSelection = {
                    selectedIds.clear()
                },
            )


            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { row ->
                    val isSelected = row.id in selectedIds

                    QuotationListItem(
                        row = row,
                        selected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                // Toggle selection in selection mode
                                if (isSelected) selectedIds.remove(row.id)
                                else selectedIds.add(row.id)
                            } else {
                                // TODO: Implement view details / navigate to new page for quotation #${row.id}
                            }
                        },
                        onLongClick = {
                            // Start or toggle selection on long press
                            if (isSelected) selectedIds.remove(row.id)
                            else selectedIds.add(row.id)
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun QuotationListItem(
    row: QuotationRow,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val bg = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${row.id}",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = row.customer,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = row.amount,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Status: ${row.status}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
