package com.example.axiom.ui.screens.finances.customer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.TextInput.Input
import com.example.axiom.ui.components.shared.button.Button
import com.example.axiom.ui.components.shared.button.ButtonVariant
import com.example.axiom.ui.components.shared.dialog.AppDialog
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.delay
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiContactDialog(
    partyId: String,
    onDismiss: () -> Unit,
    onAdd: (PartyContactEntity) -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    var selectedType by remember { mutableStateOf(ContactType.PHONE) }
    var value by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val keyboardType = when (selectedType) {
        ContactType.PHONE -> KeyboardType.Phone
        ContactType.EMAIL -> KeyboardType.Email
        ContactType.WEBSITE -> KeyboardType.Uri
    }

    val validationError = when {
        !hasSubmitted -> false
        value.isBlank() -> true
        selectedType == ContactType.PHONE && value.length < 10 -> true
        selectedType == ContactType.EMAIL && !value.contains("@") -> true
        else -> false
    }

    // Define the triple first to remove ambiguity
    val uiConfig: Triple<String, String, ImageVector> = when (selectedType) {
        ContactType.PHONE -> Triple("Phone Number", "e.g. 9876543210", Icons.Default.Phone)
        ContactType.EMAIL -> Triple("Email Address", "e.g. name@company.com", Icons.Default.Email)
        ContactType.WEBSITE -> Triple("Website URL", "e.g. www.company.com", Icons.Default.Person)
    }

// Now destructure safely
    val (label, placeholder, icon) = uiConfig


    val isError = hasSubmitted && value.isBlank()

    val triggerClose = { isVisible = false }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(300)
            onDismiss()
        }
    }

    AppDialog(
        visible = isVisible,
        onDismissRequest = triggerClose
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = AxiomTheme.components.card.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Add Contact",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AxiomTheme.components.card.title
                )

                // Contact Type Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContactType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) }
                        )
                    }
                }


                Input(
                    value = value,
                    onValueChange = { value = it },
                    label = label,
                    placeholder = placeholder,
                    icon = icon,
                    isError = validationError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = keyboardType,
                    singleLine = true,
                    imeAction = ImeAction.Done
                )

                // Primary Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
//                        colors = CheckboxDefaults.colors(
//                            checkedColor =AxiomTheme.components.card.title,
//                            uncheckedColor = AxiomTheme.components.card.
//                        )
                    )
                    Text("Set as Primary", color = AxiomTheme.components.card.subtitle)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {


                    Button(
                        text = "Cancel",
                        onClick = triggerClose,
                        variant = ButtonVariant.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        text = "Add",
                        onClick = {
                            hasSubmitted = true
                            if (value.isNotBlank()) {
                                onAdd(
                                    PartyContactEntity(
                                        id = UUID.randomUUID().toString(),
                                        partyId = partyId,
                                        contactType = selectedType,
                                        value = value,
                                        isPrimary = isPrimary
                                    )
                                )
                                triggerClose()
                            }
                        },
                        icon = Icons.Default.Check,
                        variant = ButtonVariant.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}