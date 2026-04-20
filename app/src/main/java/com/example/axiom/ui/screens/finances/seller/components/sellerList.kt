package com.example.axiom.ui.screens.finances.seller.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.Dropdown.Dropdown
import com.example.axiom.ui.components.shared.ImagePicker.CompactImagePicker
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.bottomSheet.SearchBar
import com.example.axiom.ui.components.shared.bottomSheet.SheetHeadingText
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.screens.finances.Invoice.components.extractStateCodeFromGst
import com.example.axiom.ui.screens.finances.customer.components.ContactCard
import com.example.axiom.ui.screens.finances.customer.components.MultiContactDialog
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyType
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.theme.AxiomTheme
import java.util.UUID

@Composable
fun SellerListSheet(
    sellers: List<PartyEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedId: String,
    onToggleSelection: (String) -> Unit,
    onConfirmSelection: () -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AxiomTheme.components.card.background)
            .heightIn(min = 400.dp, max = 700.dp)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AxiomTheme.components.card.title)
            }

            Text(
                text = "Select Seller",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = AxiomTheme.components.card.title
            )

            IconButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Seller", tint = AxiomTheme.components.card.title)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search
        SearchBar(
            containerWidth = 350.dp,
            tint = Color.White,
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search Sellers",
        )


        Spacer(Modifier.height(12.dp))

        // Seller List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = sellers,
                key = { it.id }
            ) { seller ->

                val isSelected = selectedId.contains(seller.id)
                val borderColor =
                    if (isSelected) AxiomTheme.components.card.selectedBorder else AxiomTheme.components.card.border
                val backgroundColor =
                    if (isSelected) AxiomTheme.components.card.selectedBackground else AxiomTheme.components.card.background

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { onToggleSelection(seller.id) }
                        )
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = seller.businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = AxiomTheme.components.card.title
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "GST: ${seller.gstNumber ?: "N/A"} • ${seller.stateCode ?: "Unknown State code"}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = AxiomTheme.components.card.subtitle
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                text = "Cancel",
                onClick = onBack,
                variant = ButtonVariant.Gray,
                modifier = Modifier.weight(1f)
            )

            Button(
                text = if (selectedId.isNotEmpty()) "Add Selected" else "Select Seller",
                onClick = onConfirmSelection,
                icon = if (selectedId.isNotEmpty()) Icons.Default.Check else null,
                variant = ButtonVariant.White,
                modifier = Modifier.weight(1f)
            )

        }
    }
}


@Composable
fun SellerForm(
    sellerId: String? = null,
    partyWithContacts: PartyWithContacts?,
    isEditing: Boolean,
    onSave: (PartyEntity, List<PartyContactEntity>) -> Unit,
    onCancel: () -> Unit
) {
    var showMultiDialog by remember { mutableStateOf(false) }
    val contacts = remember { mutableStateListOf<PartyContactEntity>() }

    var currentData by remember {
        mutableStateOf(
            partyWithContacts?.party ?: PartyEntity(
                id = UUID.randomUUID().toString(),
                partyType = PartyType.SELLER
            )
        )
    }

    LaunchedEffect(partyWithContacts, isEditing) {
        if (isEditing && partyWithContacts != null) {
            currentData = partyWithContacts.party
            contacts.clear()
            contacts.addAll(partyWithContacts.contacts)
        } else if (!isEditing) {
            contacts.clear()
        }
    }

    fun onSubmit() {
        if (currentData.businessName.isNotBlank()) {

            val derivedStateCode = extractStateCodeFromGst(currentData.gstNumber)

            val finalData = currentData.copy(
                stateCode = derivedStateCode ?: currentData.stateCode,
                updatedAt = if (isEditing) System.currentTimeMillis() else null
            )
            onSave(finalData, contacts)
        }
    }
    // Unit Selection Dialog
    if (showMultiDialog) {
        MultiContactDialog(
            partyId = currentData.id,
            onDismiss = { showMultiDialog = false },
            onAdd = { newContact ->
                if (newContact.isPrimary) {
                    contacts.replaceAll { it.copy(isPrimary = false) }
                }
                contacts.add(newContact)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SheetHeadingText(
            text = if (isEditing) "Edit Seller" else "Add New Seller",
        )

        //1st row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CompactImagePicker(
                onClick = { /* TODO: Open Image Picker */ },
                label = "Logo"
            )

            Box(modifier = Modifier.weight(0.75f)) {
                Input(
                    value = currentData.businessName ?: "",
                    onValueChange = { currentData = currentData.copy(businessName = it) },
                    label = "Business Name",
                    placeholder = "e.g. Acme Corp",
                    singleLine = true,
                    keyboardType = KeyboardType.Text,
                    isError = currentData.businessName.isEmpty(),
                    imeAction = ImeAction.Next,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            //Gst / non gst
            Box(modifier = Modifier.weight(0.4f)) {
                Dropdown(
                    label = "Registration Type",
                    items = listOf("GST", "No GST"),
                    selectedValue = currentData.registrationType,
                    placeholder = "Select type",
                    onItemSelected = { currentData = currentData.copy(registrationType = it) },
                    isError = currentData.registrationType.isEmpty()
                )
            }
            Box(modifier = Modifier.weight(0.6f)) {
                if (currentData.registrationType == "GST") {
                    Input(
                        value = currentData.gstNumber ?: "",
                        onValueChange = { input ->
                            // GSTIN is always 15 chars, alphanumeric, uppercase
                            val formatted = input.uppercase().filter { it.isLetterOrDigit() }.take(15)
                            currentData = currentData.copy(gstNumber = formatted)
                        },
                        label = "GSTIN",
                        placeholder = "22AAAAA0000A1Z5",
                        allCaps = true,
                        singleLine = true,
                        keyboardType = KeyboardType.Ascii,
                        isError = currentData.gstNumber?.let { it.isNotEmpty() && it.length != 15 } ?: false,
                        imeAction = ImeAction.Next,

                        )
                }
            }
        }

        Input(
            value = currentData.billingAddress ?: "",
            onValueChange = { currentData = currentData.copy(billingAddress = it) },
            label = "Billing Address",
            placeholder = "e.g. 123 Street Name,\nCity, State, 110001",
            imeAction = ImeAction.Default,
            singleLine = false,
            minLines = 2,
            keyboardType = KeyboardType.Text,
            isError = currentData.billingAddress.isNullOrBlank(),

            )


        // Bank Details
        Text(
            text = "Bank Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AxiomTheme.components.card.title
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.bankName ?: "",
                    onValueChange = { currentData = currentData.copy(bankName = it) },
                    label = "Bank Name",
                    placeholder = "e.g. HDFC Bank",
                    singleLine = true,
                    keyboardType = KeyboardType.Text,
                    isError = currentData.bankName.isNullOrBlank()
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = currentData.ifscCode ?: "",
                    onValueChange = { input ->
                        // IFSC is 11 chars: 4 letters, 0, then 6 alphanumeric
                        val formatted = input.uppercase().filter { it.isLetterOrDigit() }.take(11)
                        currentData = currentData.copy(ifscCode = formatted)
                    },
                    label = "IFSC Code",
                    placeholder = "e.g. HDFC0001234",
                    allCaps = true,
                    singleLine = true,
                    keyboardType = KeyboardType.Ascii,
                    isError = !currentData.ifscCode.isNullOrEmpty() && currentData.ifscCode!!.length != 11
                )
            }
        }

        Input(
            value = currentData.accountNumber ?: "",
            onValueChange = { input ->
                // Bank accounts are digits only, typically 9-18 digits
                val digits = input.filter { it.isDigit() }.take(18)
                currentData = currentData.copy(accountNumber = digits)
            },
            label = "Account Number",
            placeholder = "Enter bank account no.",
            keyboardType = KeyboardType.Number,
            singleLine = true,
            isError = !currentData.accountNumber.isNullOrEmpty() && currentData.accountNumber!!.length < 9
        )

        //Contact Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Contact Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AxiomTheme.components.card.title
            )

            AppIconButton(
                icon = AppIcons.Add,
                contentDescription = "Add",
                onClick = { showMultiDialog = true },
                iconSize = 20.dp,          // control visual weight
                modifier = Modifier.size(36.dp), // touch target
                tint = AxiomTheme.components.card.title
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contacts added",
                        style = MaterialTheme.typography.bodySmall,
                        color = AxiomTheme.components.card.subtitle
                    )
                }
            } else {
                contacts.forEach { contact ->
                    ContactCard(
                        contact = contact,
                        onDelete = {
                            contacts.remove(contact)
                        }
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                text = "Cancel",
                onClick = onCancel,
                variant = ButtonVariant.Gray,
                modifier = Modifier.weight(1f)
            )

            Button(
                text = if (isEditing) "Update" else "Save",
                onClick = { onSubmit() },
                icon = Icons.Default.Check,
                variant = ButtonVariant.White,
                modifier = Modifier.weight(1f)
            )

        }
    }
}

enum class SellerSheetMode {
    LIST,
    CREATE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SellerListSheetWrapper(
    onConfirmSelection: (PartyEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: SellerListViewModel = viewModel(
        factory = SellerListViewModelFactory(context)
    )

    val sellers by viewModel.sellers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedId by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(SellerSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "SellerSheetSwitch"
    ) { currentMode ->

        when (currentMode) {
            SellerSheetMode.LIST -> {
                SellerListSheet(
                    sellers = sellers,
                    searchQuery = searchQuery,
                    selectedId = selectedId,
                    onSearchChange = viewModel::updateSearchQuery,
                    onToggleSelection = { id ->
                        selectedId = if (selectedId == id) "" else id
                    },
                    onConfirmSelection = {
                        sellers.find { it.id == selectedId }?.let { selectedSeller ->
                            onConfirmSelection(selectedSeller)
                            selectedId = "" // Clear selection after confirming
                        }
                    },
                    onCreateClick = {
//                        viewModel.clearSelection()
                        mode = SellerSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            SellerSheetMode.CREATE -> {
                SellerForm(
                    sellerId = null,
                    partyWithContacts = viewModel.selectedParty.collectAsState().value,
                    isEditing = false,
                    onSave = { party, contacts ->
                        viewModel.saveSeller(party, contacts)
                        mode = SellerSheetMode.LIST // Return to list after saving
                    },
                    onCancel = { mode = SellerSheetMode.LIST }
                )
            }
        }
    }
}