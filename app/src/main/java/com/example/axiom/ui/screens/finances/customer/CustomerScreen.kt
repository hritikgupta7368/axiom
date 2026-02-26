package com.example.axiom.ui.screens.finances.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.customer.components.CustomerForm
import com.example.axiom.ui.screens.finances.customer.components.CustomerListViewModel
import com.example.axiom.ui.screens.finances.customer.components.CustomerListViewModelFactory
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: CustomerListViewModel = viewModel(
        factory = CustomerListViewModelFactory(context)
    )
    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    val deletedItemIds = remember { mutableStateListOf<String>() }

    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // --- Helpers ---
//    fun openCreateSheet() {
//        isEditing = false
//        selectedCustomerId = null
//        showSheet = true
//    }

    fun openCreateSheet() {
        isEditing = false
        selectedCustomerId = null
        viewModel.clearSelection()   // you must implement this
        showSheet = true
    }

//    fun openEditSheet() {
//        val selected = customers.find { it.id == selectedCustomerId }
//        if (selected != null) {
//            isEditing = true
//            showSheet = true
//        }
//    }

    fun openEditSheet() {
        val id = selectedCustomerId ?: return
        isEditing = true
        viewModel.loadCustomer(id)
        showSheet = true
    }

    fun deleteSelected() {
        val idToDelete = selectedCustomerId
        if (idToDelete != null) {
            selectedCustomerId = null

            scope.launch {
                deletedItemIds.add(idToDelete)
                delay(500)
                viewModel.deleteCustomer(idToDelete)
                deletedItemIds.remove(idToDelete)
            }
        }
    }

    val selectedParty by viewModel.selectedParty.collectAsState()

//    fun saveCustomer(updatedData: PartyEntity) {
//        if (isEditing) {
//            viewModel.updateCustomer(updatedData)
//        } else {
//            viewModel.insertCustomer(updatedData)
//        }
//        showSheet = false
//        selectedCustomerId = null
//    }


    fun saveCustomer(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        viewModel.saveCustomer(party, contacts)
        showSheet = false
        selectedCustomerId = null
    }
    AnimatedHeaderScrollView(
        largeTitle = "Customers",
        onAddClick = { openCreateSheet() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
    ) {
        if (customers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {

            // PRODUCTS UNDER CATEGORY
            items(
                customers,
                key = { it.id }
            ) { customer ->

                val isVisible = !deletedItemIds.contains(customer.id)

                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(animationSpec = tween(500)) +
                            fadeOut(animationSpec = tween(500)),
                    enter = expandVertically() + fadeIn()
                ) {
                    CustomerCard(
                        customer = customer,
                        isSelected = customer.id == selectedCustomerId,
                        onSelect = {
                            selectedCustomerId =
                                if (selectedCustomerId == customer.id) null else customer.id
                        }
                    )
                }
            }

        }


    }
    // --- Bottom Sheet ---
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = { showSheet = false }
    ) {
        CustomerForm(
            customerId = selectedCustomerId,
            isEditing = isEditing,
            partyWithContacts = selectedParty,
//            loadCustomer = { id -> viewModel.getCustomerById(id) },
            onSave = { party, contacts ->
                saveCustomer(party, contacts)
            },
            onCancel = { showSheet = false }
        )
    }


}


// --- List Item Component ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerCard(
    customer: PartyEntity,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = 0.5f
        )
    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {
                    if (isSelected) onSelect()
                },
                onLongClick = { onSelect() }
            )
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (customer.businessName.isNotEmpty()) customer.businessName.take(1)
                            .uppercase() else "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!customer.gstNumber.isNullOrBlank()) {
                    Text(
                        text = "GSTIN: ${customer.gstNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!customer.stateCode.isNullOrBlank()) {
                    Text(
                        text = customer.stateCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}



