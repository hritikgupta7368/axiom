package com.example.axiom.ui.screens.finances.suppliers.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.axiom.ui.components.shared.Switch.AnimatedSwitch
import com.example.axiom.ui.components.shared.Switch.SwitchSize
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
fun SupplierListSheet(
    suppliers: List<PartyWithContacts>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedId: String?,
    onSelect: (PartyWithContacts) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SheetHeadingText("Select Supplier", modifier = Modifier.weight(1f))

            AppIconButton(
                icon = AppIcons.Add,
                contentDescription = "Add",
                onClick = onCreateClick,
                enclosedInCircle = true,
                circleColor = AxiomTheme.components.card.title,
                tint = AxiomTheme.components.card.background
            )
        }

        Spacer(Modifier.height(12.dp))

        SearchBar(
            containerWidth = 350.dp,
            tint = Color.White,
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = "Search Supplier",
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = suppliers,
                key = { it.party.id }
            ) { supplier ->

                val party = supplier.party


                val isSelected = selectedId == party.id
                val borderColor =
                    if (isSelected) AxiomTheme.components.card.selectedBorder
                    else AxiomTheme.components.card.border

                val backgroundColor =
                    if (isSelected) AxiomTheme.components.card.selectedBackground
                    else AxiomTheme.components.card.background

                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onSelect(supplier) },
                            onLongClick = { }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = party.businessName.ifBlank { "Unknown Business" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AxiomTheme.components.card.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (!party.gstNumber.isNullOrBlank()) "GST: ${party.gstNumber}" else "Unregistered",
                                style = MaterialTheme.typography.bodySmall,
                                color = AxiomTheme.components.card.subtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = party.stateCode ?: "N/A",
                                style = MaterialTheme.typography.titleSmall,
                                color = AxiomTheme.components.card.subtitle,
                            )

                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                text = "Cancel",
                onClick = onBack,
                variant = ButtonVariant.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierForm(
    partyWithContacts: PartyWithContacts?,
    isEditing: Boolean,
    onSave: (PartyEntity, List<PartyContactEntity>) -> Unit,
    onCancel: () -> Unit
) {

    // Dialog state
    var showMultiDialog by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var isSameAsShipping by remember { mutableStateOf(false) }

    val contacts = remember { mutableStateListOf<PartyContactEntity>() }

    var currentData by remember {
        mutableStateOf(
            PartyEntity(
                id = UUID.randomUUID().toString(),
                partyType = PartyType.SUPPLIER
            )
        )
    }

    LaunchedEffect(partyWithContacts, isEditing) {
        if (isEditing && partyWithContacts != null) {
            currentData = partyWithContacts.party
            contacts.clear()
            contacts.addAll(partyWithContacts.contacts)
        }
    }

    // for check box
    LaunchedEffect(isSameAsShipping, currentData.defaultShippingAddress) {
        if (isSameAsShipping) {
            currentData = currentData.copy(
                billingAddress = currentData.defaultShippingAddress
            )
        }
    }

    fun validate(): Boolean {
        hasSubmitted = true

        return currentData.businessName.isNotBlank() &&
                currentData.billingAddress?.isNotBlank() == true
    }

    fun onSubmit() {
        if (validate()) {
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
            text = if (isEditing) "Edit Supplier" else "Add New Supplier",
        )


        //1st row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
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


        //Billing address same as shipping address
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Same as Shipping Address",
                color = AxiomTheme.components.card.title
            )

            AnimatedSwitch(
                checked = isSameAsShipping,
                onCheckedChange = {
                    isSameAsShipping = it
                },
                size = SwitchSize.SM,
            )
        }


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

enum class SupplierSheetMode {
    LIST,
    CREATE
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SupplierListSheetWrapper(
    onConfirmSelection: (PartyWithContacts) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: SupplierListViewModel = viewModel(
        factory = SupplierListViewModelFactory(context)
    )

    val suppliers by viewModel.suppliersWithContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(SupplierSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith
                    slideOutHorizontally { -it }
        },
        label = "SupplierSheetSwitch"
    ) { currentMode ->

        when (currentMode) {
            SupplierSheetMode.LIST -> {
                SupplierListSheet(
                    suppliers = suppliers,
                    searchQuery = searchQuery,
                    selectedId = selectedId,
                    onSearchChange = viewModel::updateSearchQuery,
                    onSelect = { party ->
                        selectedId = party.party.id
                        onConfirmSelection(party)
                    },
                    onCreateClick = {
                        mode = SupplierSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            SupplierSheetMode.CREATE -> {
                SupplierForm(
                    partyWithContacts = null,
                    isEditing = false,

                    onSave = { party, contacts ->

                        viewModel.saveSupplier(
                            party,
                            contacts
                        )

                        mode = SupplierSheetMode.LIST
                    },

                    onCancel = {
                        mode = SupplierSheetMode.LIST
                    }
                )
            }
        }
    }
}