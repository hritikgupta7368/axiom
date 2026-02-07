package com.example.axiom.ui.components.shared.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDialog(
    show: Boolean,
    onDismiss: () -> Unit,

    title: String? = null,
    message: String? = null,

    // INPUT (OPTIONAL)
    showInput: Boolean = false,
    inputLabel: String = "",
    initialInput: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,

    confirmText: String = "Confirm",
    dismissText: String = "Cancel",

    onConfirm: (input: String?) -> Unit
) {
    if (!show) return

    var input by remember { mutableStateOf(initialInput) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = title?.let {
            { Text(it, style = MaterialTheme.typography.titleMedium) }
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()               // keyboard aware
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (showInput) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(inputLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = keyboardOptions
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(if (showInput) input else null)
                }
            ) {
                Text(confirmText)
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
