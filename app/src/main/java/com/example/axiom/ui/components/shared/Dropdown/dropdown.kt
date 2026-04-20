package com.example.axiom.ui.components.shared.Dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.theme.AxiomTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    label: String,
    items: List<String>,
    selectedValue: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select item",
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(selectedValue) {
        query = selectedValue ?: ""
    }
    val filteredItems = remember(query, items) {
        if (query.isBlank()) items
        else items.filter { it.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = modifier,
    ) {

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AxiomTheme.components.textInput.unfocusedLabel, // Using your existing theme color
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
        )

        // Native Compose Dropdown Wrapper
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            // The "TextField" acting as the clickable anchor
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                placeholder = {
                    Text(text = placeholder, fontSize = 16.sp)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                isError = isError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    // Text Colors
                    focusedTextColor = AxiomTheme.components.textInput.textColor,
                    unfocusedTextColor = AxiomTheme.components.textInput.textColor,
                    errorTextColor = AxiomTheme.components.textInput.errorColor,

                    // Container (Background) Colors
                    focusedContainerColor = AxiomTheme.components.textInput.focusedBg,
                    unfocusedContainerColor = AxiomTheme.components.textInput.unfocusedBg,
                    errorContainerColor = AxiomTheme.components.textInput.errorBg,

                    // Border Colors
                    focusedBorderColor = AxiomTheme.components.textInput.focusedBorder,
                    unfocusedBorderColor = AxiomTheme.components.textInput.unfocusedBorder,
                    errorBorderColor = AxiomTheme.components.textInput.errorColor,

                    // Label Colors
                    focusedLabelColor = AxiomTheme.components.textInput.focusedLabel,
                    unfocusedLabelColor = AxiomTheme.components.textInput.unfocusedLabel,
                    errorLabelColor = AxiomTheme.components.textInput.errorColor,

                    // Icon Colors
                    focusedLeadingIconColor = AxiomTheme.components.textInput.focusedBorder,
                    unfocusedLeadingIconColor = AxiomTheme.components.textInput.iconColorResting,
                    errorLeadingIconColor = AxiomTheme.components.textInput.errorColor,

                    // Cursor
                    cursorColor = AxiomTheme.components.textInput.focusedBorder,
                    errorCursorColor = AxiomTheme.components.textInput.errorColor
                )
            )

            // The Popup Menu (Renders above all other UI components)
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    onItemSelected(query) // custom value allowed
                },
                modifier = Modifier.background(AxiomTheme.components.card.background) // Matches the dark theme
            ) {
                if (items.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No data", color = AxiomTheme.components.card.subtitle) },
                        onClick = { expanded = false }
                    )
                } else if (filteredItems.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text("Use \"$query\"", color = AxiomTheme.components.card.subtitle)
                        },
                        onClick = {
                            onItemSelected(query)
                            expanded = false
                        }
                    )
                } else {
                    filteredItems.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(item, color = AxiomTheme.components.card.subtitle)
                            },
                            onClick = {
                                query = item
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


