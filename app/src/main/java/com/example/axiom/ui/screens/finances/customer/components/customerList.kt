package com.example.axiom.ui.screens.finances.customer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.ui.components.shared.bottomSheet.SearchBar
import com.example.axiom.ui.components.shared.bottomSheet.SheetHeadingText
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons
import java.util.UUID


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerListSheet(
    customers: List<PartyEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedIds: Set<String>,
    onToggleSelection: (String) -> Unit,
    onConfirmSelection: () -> Unit,
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
                enclosedInCircle = true
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
                key = { it.id }
            ) { customer ->
                val isSelected = selectedIds.contains(customer.id)
                val borderColor =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline

                val backgroundColor =
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface

                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { onToggleSelection(customer.id) },
                            onLongClick = { }
                        )
                        .background(backgroundColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
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
                                text = customer.businessName.ifBlank { "Unknown Business" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (!customer.gstNumber.isNullOrBlank()) "GST: ${customer.gstNumber}" else "Unregistered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = customer.city ?: "N/A",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if ((customer.openingBalance) > 0) {
                                Text(
                                    text = "Bal: ₹${customer.openingBalance}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onConfirmSelection,
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Add (${selectedIds.size})")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerForm(
    customerId: String? = null,
    partyWithContacts: PartyWithContacts?,
    isEditing: Boolean,
    loadCustomer: (suspend (String) -> PartyEntity?)? = null,
    onSave: (PartyEntity, List<PartyContactEntity>) -> Unit,
    onCancel: () -> Unit
) {


    // Local state for form fields
//    var currentData by remember {
//        mutableStateOf(
//            PartyEntity(
//                id = customerId ?: UUID.randomUUID().toString(),
//                partyType = PartyType.CUSTOMER // Defaulting strictly to CUSTOMER
//            )
//        )
//    }
//    var contacts by remember {
//        mutableStateOf(
//            mutableListOf<PartyContactEntity>()
//        )
//    }
//
//
//    LaunchedEffect(customerId) {
//        if (isEditing && customerId != null && loadCustomer != null) {
//            val loaded = loadCustomer(customerId)
//            if (loaded != null) {
//                currentData = loaded
//            }
//        }
//    }

    var currentData by remember {
        mutableStateOf(
            partyWithContacts?.party
                ?: PartyEntity(
                    id = UUID.randomUUID().toString(),
                    partyType = PartyType.CUSTOMER
                )
        )
    }

    val contacts = remember { mutableStateListOf<PartyContactEntity>() }

    LaunchedEffect(partyWithContacts) {
        if (partyWithContacts != null) {
            currentData = partyWithContacts.party
            contacts.clear()
            contacts.addAll(partyWithContacts.contacts)
        }
    }
    // Double conversion states
    var creditLimitText by remember(currentData.creditLimit) {
        mutableStateOf(if (currentData.creditLimit == 0.0 && !isEditing) "" else currentData.creditLimit.toString())
    }
    var openingBalanceText by remember(currentData.openingBalance) {
        mutableStateOf(if (currentData.openingBalance == 0.0 && !isEditing) "" else currentData.openingBalance.toString())
    }


    // Validation
    var nameError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true

        if (currentData.businessName.isBlank()) {
            nameError = "Business Name is required"
            isValid = false
        } else {
            nameError = null
        }

        // Apply string-to-double conversions before saving
        val credit = creditLimitText.toDoubleOrNull() ?: 0.0
        val opening = openingBalanceText.toDoubleOrNull() ?: 0.0
        currentData = currentData.copy(creditLimit = credit, openingBalance = opening)

        return isValid
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = screenHeight * 0.85f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditing) "Edit Customer" else "Add New Customer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Business Name
        OutlinedTextField(
            value = currentData.businessName,
            onValueChange = {
                currentData =
                    currentData.copy(businessName = it); if (nameError != null) nameError = null
            },
            label = { Text("Business Name *") },
            isError = nameError != null,
            supportingText = { if (nameError != null) Text(nameError!!) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row {
            FilterChip(
                selected = currentData.registrationType == GstRegistrationType.REGISTERED,
                onClick = {
                    currentData = currentData.copy(
                        registrationType = GstRegistrationType.REGISTERED
                    )
                },
                label = { Text("Registered (B2B)") }
            )

            FilterChip(
                selected = currentData.registrationType == GstRegistrationType.UNREGISTERED,
                onClick = {
                    currentData = currentData.copy(
                        registrationType = GstRegistrationType.UNREGISTERED,
                        gstNumber = null
                    )
                },
                label = { Text("Unregistered (B2C)") }
            )
        }

        // GST Number
        OutlinedTextField(
            value = currentData.gstNumber ?: "",
            onValueChange = { currentData = currentData.copy(gstNumber = it) },
            label = { Text("GST Number (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // ---------------- CONTACT SECTION ----------------

        Text(
            text = "Contacts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = {
                contacts.add(
                    PartyContactEntity(
                        id = UUID.randomUUID().toString(),
                        partyId = currentData.id,
                        contactType = ContactType.PHONE,
                        value = "",
                        isPrimary = contacts.isEmpty()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Contact")
        }

        Spacer(modifier = Modifier.height(8.dp))

        contacts.forEachIndexed { index, contact ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Contact Type Dropdown
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = contact.contactType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Contact Type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            ContactType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        contacts[index] =
                                            contact.copy(contactType = type)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Contact Value Field
                    OutlinedTextField(
                        value = contact.value,
                        onValueChange = {
                            contacts[index] =
                                contact.copy(value = it)
                        },
                        label = { Text("Contact Value") },
                        keyboardOptions = when (contact.contactType) {
                            ContactType.PHONE ->
                                KeyboardOptions(keyboardType = KeyboardType.Phone)

                            ContactType.EMAIL ->
                                KeyboardOptions(keyboardType = KeyboardType.Email)

                            ContactType.WEBSITE ->
                                KeyboardOptions(keyboardType = KeyboardType.Uri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = contact.isPrimary,
                            onCheckedChange = { checked ->
                                contacts.replaceAll {
                                    it.copy(isPrimary = false)
                                }
                                contacts[index] =
                                    contacts[index].copy(isPrimary = checked)
                            }
                        )
                        Text("Primary Contact")
                    }

                    TextButton(
                        onClick = {
                            contacts.removeAt(index)
                        }
                    ) {
                        Text("Remove Contact")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Location Info
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = currentData.city ?: "",
                onValueChange = { currentData = currentData.copy(city = it) },
                label = { Text("City") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = currentData.state ?: "",
                onValueChange = { currentData = currentData.copy(state = it) },
                label = { Text("State") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Address
        OutlinedTextField(
            value = currentData.billingAddress ?: "",
            onValueChange = { currentData = currentData.copy(billingAddress = it) },
            label = { Text("Billing Address") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Balances
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = openingBalanceText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() || char == '.' }) openingBalanceText = it
                },
                label = { Text("Opening Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = creditLimitText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() || char == '.' }) creditLimitText = it
                },
                label = { Text("Credit Limit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (validate()) {
                        val finalData = if (isEditing) {
                            currentData.copy(updatedAt = System.currentTimeMillis())
                        } else {
                            currentData
                        }
                        onSave(currentData, contacts.toList())
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Update" else "Save")
            }
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
    onConfirmSelection: (List<PartyEntity>) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val viewModel: CustomerListViewModel = viewModel(
        factory = CustomerListViewModelFactory(context)
    )

    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
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
                    selectedIds = selectedIds,
                    onSearchChange = viewModel::updateSearchQuery,
                    onToggleSelection = { id ->
                        selectedIds = if (selectedIds.contains(id)) {
                            selectedIds - id
                        } else {
                            selectedIds + id
                        }
                    },
                    onConfirmSelection = {
                        val selected = customers.filter { selectedIds.contains(it.id) }
                        onConfirmSelection(selected)
                        selectedIds = emptySet()
                    },
                    onCreateClick = { mode = CustomerSheetMode.CREATE },
                    onBack = onBack
                )
            }

            CustomerSheetMode.CREATE -> {
//                CustomerForm(
//                    isEditing = false,
//                    onSave = { customer ->
//                        viewModel.insertCustomer(customer)
//                        mode = CustomerSheetMode.LIST
//                    },
//                    onCancel = { mode = CustomerSheetMode.LIST }
//                )
            }
        }
    }
}
