package com.example.axiom.ui.components.DatePicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.axiom.ui.components.shared.TextInput.Input
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFieldPicker(
    dateMillis: Long,
    onDateChange: (Long) -> Unit,
    label: String = "Date",
    isError: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onDateChange)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    // The Overlay trick applied here
    Box {
        Input(
            value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMillis)),
            onValueChange = {},
            readOnly = true,
            label = label,
            icon = Icons.Default.Star,
            singleLine = true,
            isError = isError
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }
}