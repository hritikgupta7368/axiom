package com.example.axiom.ui.screens.finances.suppliers

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.EmptyState.EmptyScreen
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import com.example.axiom.ui.components.shared.cards.SupplierCard
import com.example.axiom.ui.components.shared.header.AnimatedHeaderScrollView
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.suppliers.components.SupplierForm
import com.example.axiom.ui.screens.finances.suppliers.components.SupplierListViewModel
import com.example.axiom.ui.screens.finances.suppliers.components.SupplierListViewModelFactory
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Replace with your actual Factory implementation
    val viewModel: SupplierListViewModel = viewModel(
        factory = SupplierListViewModelFactory(context)
    )

    // all states
    val deletedItemIds = remember { mutableStateListOf<String>() }
    var showSheet by remember { mutableStateOf(false) }
    var showSupplierDetailsSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedSupplierIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedSupplierIds.isNotEmpty()
    val selectedSupplier by viewModel.selectedParty.collectAsState()
    val editingParty by viewModel.editingParty.collectAsState()



    BackHandler(enabled = isSelectionMode) {
        selectedSupplierIds.clear()
    }

    // DB FETCHING
    val suppliers by viewModel.suppliers.collectAsState()


    fun closeSheetSmoothly() {
        scope.launch {
            sheetState.hide() // Animates the sheet sliding down
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showSheet = false // Only remove it from composition AFTER animation
                selectedSupplierIds.clear()
            }
        }
    }


    // --- Helpers ---
    fun openCreateSheet() {
        isEditing = false
        selectedSupplierIds.clear()
        viewModel.clearEditSelection()
        showSheet = true
    }

    fun openEditSheet() {
        val supplierId = selectedSupplierIds.firstOrNull() ?: return

        isEditing = true
        viewModel.loadSupplierForEdit(supplierId)
        showSheet = true

    }


    fun deleteSelected() {
        if (selectedSupplierIds.isNotEmpty()) {
            val ids = selectedSupplierIds.toList()
            selectedSupplierIds.clear()

            viewModel.deleteAll(ids)
        }
    }


    fun saveSupplier(
        party: PartyEntity,
        contacts: List<PartyContactEntity>
    ) {
        if (isEditing) {
            viewModel.updateSupplierWithContacts(party, contacts)
        } else {
            viewModel.insertSupplierWithContacts(party, contacts)
        }
        closeSheetSmoothly()
    }

    val isSearching = searchQuery.isNotEmpty()
    val isEmpty = suppliers.isEmpty()



    AnimatedHeaderScrollView(
        largeTitle = "Suppliers",
        onAddClick = { openCreateSheet() },
        onEditClick = { openEditSheet() },
        onDeleteClick = { deleteSelected() },
        onBack = onBack,
        isSelectionMode = isSelectionMode,
        selectionCount = selectedSupplierIds.size,
        query = searchQuery,
        updateQuery = viewModel::updateSearchQuery,
        onToggleSelectionMode = { selectedSupplierIds.clear() }
    ) {

        if (isEmpty) {
            if (isSearching) {
                // CASE A: Searching, but no results
                item(key = "no_results_state") {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AxiomTheme.components.card.mutedText.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AxiomTheme.components.card.title
                        )
                        Text(
                            text = "Try adjusting your search for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = AxiomTheme.components.card.subtitle
                        )
                    }
                }
            } else {
                // CASE B: Database is completely empty (No search active)
                // We show the empty state INSIDE the scroll view so the header stays!
                item(key = "database_empty_state") {

                    EmptyScreen(
                        title = "No Suppliers yet",
                        description = "Add suppliers to start creating invoices",
                        buttonText = "Add Supplier",
                        onAdd = { openCreateSheet() },
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                    )
                }
            }
        } else {

            items(
                items = suppliers,
                key = { it.id }
            ) {
                val isVisible = !deletedItemIds.contains(it.id)

                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(animationSpec = tween(500)) + fadeOut(
                        animationSpec = tween(
                            500
                        )
                    ),
                    enter = expandVertically() + fadeIn()
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {


                        SupplierCard(
                            supplier = it,
                            isSelected = selectedSupplierIds.contains(it.id),
                            onClick = {

                                if (isSelectionMode) {
                                    if (selectedSupplierIds.contains(it.id)) {
                                        selectedSupplierIds.remove(it.id)
                                    } else {
                                        selectedSupplierIds.add(it.id)
                                    }
                                } else {
                                    viewModel.selectSupplier(it.id)
                                    showSupplierDetailsSheet = true
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!isSelectionMode) {
                                    selectedSupplierIds.clear()
                                    selectedSupplierIds.add(it.id)
                                } else {
                                    if (selectedSupplierIds.contains(it.id)) {
                                        selectedSupplierIds.remove(it.id)
                                    } else {
                                        selectedSupplierIds.add(it.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    // --- Bottom Sheet ---
    AppBottomSheet(
        showSheet = showSheet,
        onDismiss = { showSheet = false }
    ) {
        SupplierForm(
            isEditing = isEditing,
            partyWithContacts = editingParty,
            onSave = { party, contacts -> saveSupplier(party, contacts) },
            onCancel = { closeSheetSmoothly() }
        )
    }
}

// --- List Item Component ---





