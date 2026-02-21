package com.example.axiom.ui.components.shared.form.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun <T> AppDropdown(
    value: T?,
    options: List<T>,
    display: (T) -> String,
    onSelect: (T) -> Unit,
    label: String,
    error: String?
) {

    var expanded by remember { mutableStateOf(false) }

    AppTextField(
        value = if (value != null) display(value) else "",
        onValueChange = {},
        placeholder = label,
        error = error,
        onClick = { expanded = true }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach {
            DropdownMenuItem(
                text = { Text(display(it)) },
                onClick = {
                    onSelect(it)
                    expanded = false
                }
            )
        }
    }
}

