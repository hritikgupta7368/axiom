package com.example.axiom.ui.screens.finances.suppliers

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.data.finances.SupplierFirm
import com.example.axiom.data.finances.SupplierFirmViewModel
import com.example.axiom.data.finances.SupplierFirmViewModelFactory
import com.example.axiom.ui.components.shared.bottomSheet.AppBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(onBack: () -> Unit) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: SupplierFirmViewModel = viewModel(
        factory = SupplierFirmViewModelFactory(context)
    )

    val suppliers by viewModel.suppliers.collectAsState(initial = emptyList())


    // Selection State
    var selectedSupplierId by remember { mutableStateOf<String?>(null) }

    // Animation State
    val deletedItemIds = remember { mutableStateListOf<String>() }

    // Bottom Sheet State
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Form State Holder
    var formData by remember { mutableStateOf(SupplierFirm()) }

    // --- Helpers ---
    fun openCreateSheet() {
        formData = SupplierFirm() // Reset form
        isEditing = false
        selectedSupplierId = null
        showSheet = true
    }

    fun openEditSheet() {
        val selected = suppliers.find { it.id == selectedSupplierId }
        if (selected != null) {
            formData = selected
            isEditing = true
            showSheet = true
        }
    }

    fun deleteSelected() {
        val idToDelete = selectedSupplierId
        if (idToDelete != null) {
            // Clear selection immediately for UI feedback
            selectedSupplierId = null

            scope.launch {
                // Trigger animation
                deletedItemIds.add(idToDelete)

                // Wait for animation to complete
                delay(500)

                // Perform actual soft delete in DB
                viewModel.deleteById(idToDelete)

                // Cleanup ID from tracking
                deletedItemIds.remove(idToDelete)
            }
        }
    }

    fun saveSupplier(updatedData: SupplierFirm) {
        if (isEditing) {
            viewModel.update(updatedData)
        } else {
            viewModel.insert(updatedData)
        }
        showSheet = false
        selectedSupplierId = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selectedSupplierId == null) {
                // Default Top Bar
                TopAppBar(
                    title = { Text("SupplierFirms", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { openCreateSheet() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Supplier",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            } else {
                // Selection Mode Top Bar
                TopAppBar(
                    title = { Text("1 Selected", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedSupplierId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { openEditSheet() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { deleteSelected() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->

        // --- Content ---
        if (suppliers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No suppliers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp // Forced to zero
                    )
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suppliers, key = { it.id }) { supplier ->
                    val isVisible = !deletedItemIds.contains(supplier.id)
                    AnimatedVisibility(
                        visible = isVisible,
                        exit = shrinkVertically(animationSpec = tween(500)) + fadeOut(
                            animationSpec = tween(
                                500
                            )
                        ),
                        enter = expandVertically() + fadeIn()
                    ) {
                        SupplierCard(
                            supplier = supplier,
                            isSelected = supplier.id == selectedSupplierId,
                            onSelect = {
                                selectedSupplierId =
                                    if (selectedSupplierId == supplier.id) null else supplier.id
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
            SupplierForm(
                formData = formData,
                isEditing = isEditing,
                onSave = { saveSupplier(it) },
                onCancel = { showSheet = false }
            )
        }
    }
}

// --- List Item Component ---


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupplierCard(
    supplier: SupplierFirm,
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
                        text = if (supplier.name.isNotEmpty()) supplier.name.take(1)
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
                    text = supplier.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!supplier.gstin.isNullOrBlank()) {
                    Text(
                        text = "GSTIN: ${supplier.gstin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!supplier.contactNumber.isNullOrBlank()) {
                    Text(
                        text = supplier.contactNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


// --- Form Content Component ---

@Composable
fun SupplierForm(
    formData: SupplierFirm,
    isEditing: Boolean,
    onSave: (SupplierFirm) -> Unit,
    onCancel: () -> Unit
) {
    // Local state for form fields to handle validation updates immediately
    var currentData by remember(formData.id) {
        mutableStateOf(formData)
    }


    // Validation Errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var gstinError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var stateCodeError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true

        // Name Validation
        if (currentData.name.isBlank()) {
            nameError = "Name is required"
            isValid = false
        } else {
            nameError = null
        }

        // GSTIN Validation (Mandatory)
        if (currentData.gstin.isNullOrBlank()) {
            gstinError = "GSTIN is required"
            isValid = false
        } else if (!currentData.gstin!!.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"))) {
            gstinError = "Invalid GSTIN format"
            isValid = false
        } else {
            gstinError = null
        }

        // Phone Validation (Indian: 10 digits starting with 6-9)
        if (currentData.contactNumber.isNullOrBlank()) {
            phoneError = "Phone number is required"
            isValid = false
        } else if (!currentData.contactNumber!!.matches(Regex("^[6-9]\\d{9}$"))) {
            phoneError = "Invalid Indian phone number"
            isValid = false
        } else {
            phoneError = null
        }

        // State Code Validation (Mandatory, 2 digits)
        if (currentData.stateCode.isNullOrBlank()) {
            stateCodeError = "State code is required"
            isValid = false
        } else if (currentData.stateCode!!.length != 2 || !currentData.stateCode!!.all { it.isDigit() }) {
            stateCodeError = "Invalid Code (2 digits)"
            isValid = false
        } else {
            stateCodeError = null
        }

        // Address Validation (Mandatory)
        if (currentData.address.isBlank()) {
            addressError = "Address is required"
            isValid = false
        } else {
            addressError = null
        }

        // Email Validation (Optional)
        if (!currentData.email.isNullOrBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                currentData.email!!
            ).matches()
        ) {
            emailError = "Invalid email format"
            isValid = false
        } else {
            emailError = null
        }

        return isValid
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()

            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditing) "Edit Supplier" else "Add New Supplier",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)


        // Name
        CustomerInput(
            value = currentData.name,
            onValueChange = {
                currentData = currentData.copy(name = it); if (nameError != null) nameError = null
            },
            label = "Firm / Supplier Name *",
            icon = Icons.Default.Person,
            isError = nameError != null,
            errorMessage = nameError
        )

        // GSTIN
        CustomerInput(
            value = currentData.gstin ?: "",
            onValueChange = {
                currentData = currentData.copy(gstin = it); if (gstinError != null) gstinError =
                null
            },
            label = "GSTIN *",
            placeholder = "29ABCDE1234F1Z5",
            allCaps = true,
            isError = gstinError != null,
            errorMessage = gstinError
        )

        // Contact
        CustomerInput(
            value = currentData.contactNumber ?: "",
            onValueChange = {
                // Only allow numeric input
                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                    currentData = currentData.copy(contactNumber = it)
                    if (phoneError != null) phoneError = null
                }
            },
            label = "Contact Number",
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            placeholder = "9876543210",
            isError = phoneError != null,
            errorMessage = phoneError
        )

        // Email
        CustomerInput(
            value = currentData.email ?: "",
            onValueChange = {
                currentData = currentData.copy(email = it); if (emailError != null) emailError =
                null
            },
            label = "Email Address",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError
        )

        // State Code
        CustomerInput(
            value = currentData.stateCode ?: "",
            onValueChange = {
                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                    currentData = currentData.copy(stateCode = it)
                    if (stateCodeError != null) stateCodeError = null
                }
            },
            label = "State Code *",
            placeholder = "29",
            keyboardType = KeyboardType.Number,
            isError = stateCodeError != null,
            errorMessage = stateCodeError
        )

        // Address
        CustomerInput(
            value = currentData.address,
            onValueChange = {
                currentData =
                    currentData.copy(address = it); if (addressError != null) addressError = null
            },
            label = "Address *",
            singleLine = false,
            minLines = 3,
            isError = addressError != null,
            errorMessage = addressError
        )

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
                        onSave(currentData)
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

@Composable
fun CustomerInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    allCaps: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (allCaps) onValueChange(it.uppercase()) else onValueChange(it) },
            label = { Text(label) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            trailingIcon = if (isError) {
                {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}