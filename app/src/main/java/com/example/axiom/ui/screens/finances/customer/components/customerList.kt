package com.example.axiom.ui.screens.finances.customer.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
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
import com.example.axiom.ui.theme.AxiomTheme
import java.util.UUID


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerListSheet(
    customers: List<PartyWithContacts>,
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
            SheetHeadingText("Select Customer", modifier = Modifier.weight(1f))

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
            placeholder = "Search Customers",
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = customers,
                key = { it.party.id }
            ) { customer ->

                val party = customer.party


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
                            onClick = { onSelect(customer) },
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


                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bottom buttons
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


@Composable
fun ContactCard(
    contact: PartyContactEntity,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {

    val (icon, label) = when (contact.contactType) {
        ContactType.PHONE -> Icons.Default.Person to "Mobile Number"
        ContactType.EMAIL -> Icons.Default.Email to "Email Address"
        ContactType.WEBSITE -> Icons.Default.Info to "Website"
    }

    val highlightColor = if (contact.isPrimary)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = highlightColor.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(highlightColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = highlightColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Section
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = contact.value,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (contact.isPrimary) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(highlightColor.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    highlightColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRIMARY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = highlightColor
                            )
                        }
                    }
                }

                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Button
            IconButton(
                onClick = {
                    if (onEdit != null) onEdit() else onDelete?.invoke()
                }
            ) {
                Icon(
                    imageVector = if (onEdit != null) Icons.Default.Edit else Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerForm(
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

    // Temp state for price text fields to handle string/double conversion smoothly
    var creditLimitText by remember { mutableStateOf("") }
    var openingBalanceText by remember { mutableStateOf("") }


    var currentData by remember {
        mutableStateOf(
            PartyEntity(
                id = UUID.randomUUID().toString(),
                partyType = PartyType.CUSTOMER
            )
        )
    }


    LaunchedEffect(partyWithContacts, isEditing) {
        if (isEditing && partyWithContacts != null) {
            currentData = partyWithContacts.party

            // Populate contacts
            contacts.clear()
            contacts.addAll(partyWithContacts.contacts)

            // Populate text fields securely
            creditLimitText = partyWithContacts.party.creditLimit
                .takeIf { it != 0.0 }
                ?.toString() ?: ""

            openingBalanceText = partyWithContacts.party.openingBalance
                .takeIf { it != 0.0 }
                ?.toString() ?: ""

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
            val creditLimit = creditLimitText.toDoubleOrNull() ?: 0.0
            val openingBalance = openingBalanceText.toDoubleOrNull() ?: 0.0
            val derivedStateCode = extractStateCodeFromGst(currentData.gstNumber)

            // Update the timestamp before saving
            val finalData = currentData.copy(
                stateCode = derivedStateCode ?: currentData.stateCode,
                creditLimit = creditLimit,
                openingBalance = openingBalance,
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
            .padding(15.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SheetHeadingText(
            text = if (isEditing) "Edit Customer" else "Add New Customer",
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

        //Credit Limit and opening Balance
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = creditLimitText,
                    onValueChange = { input ->
                        // Allows only one decimal point and restricts to 2 decimal places
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            creditLimitText = input
                        }
                    },
                    label = "Credit Limit",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    singleLine = true,
                    isError = creditLimitText.isNotEmpty() && creditLimitText.toDoubleOrNull() == null
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Input(
                    value = openingBalanceText,
                    onValueChange = { input ->
                        // Standard financial regex: digits, optional dot, max 2 decimals
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            openingBalanceText = input
                        }
                    },
                    label = "Opening Balance", // Keep it clean; handle '*' via validation logic
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    singleLine = true,
                    // Error if mandatory field is blank
                    isError = openingBalanceText.isBlank()
                )
            }
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

enum class CustomerSheetMode {
    LIST,
    CREATE
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomerListSheetWrapper(
    onConfirmSelection: (PartyWithContacts) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: CustomerListViewModel = viewModel(
        factory = CustomerListViewModelFactory(context)
    )

    val customers by viewModel.customersWithContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedId by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(CustomerSheetMode.LIST) }

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        label = "CustomerSheetSwitch"
    ) { currentMode ->
        when (currentMode) {
            CustomerSheetMode.LIST -> {
                CustomerListSheet(
                    customers = customers,
                    searchQuery = searchQuery,
                    selectedId = selectedId,
                    onSearchChange = viewModel::updateSearchQuery,
                    onSelect = { party ->
                        selectedId = party.party.id
                        onConfirmSelection(party)
                    },

                    onCreateClick = {
                        mode = CustomerSheetMode.CREATE
                    },
                    onBack = onBack
                )
            }

            CustomerSheetMode.CREATE -> {

                CustomerForm(
                    partyWithContacts = null,
                    isEditing = false,

                    onSave = { party, contacts ->

                        viewModel.saveCustomer(
                            party,
                            contacts
                        )

                        mode = CustomerSheetMode.LIST
                    },

                    onCancel = {
                        mode = CustomerSheetMode.LIST
                    }
                )
            }
        }
    }
}
