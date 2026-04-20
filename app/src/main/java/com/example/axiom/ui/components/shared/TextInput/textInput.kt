package com.example.axiom.ui.components.shared.TextInput


import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.theme.AxiomTheme

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    allCaps: Boolean = false,
    readOnly: Boolean = false,
    isError: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {

//    val containerModifier = modifier
//        .fillMaxWidth()
//        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)


    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AxiomTheme.components.textInput.unfocusedLabel, // Using your existing theme color
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            enabled = enabled,
            readOnly = onClick != null || readOnly,
            onValueChange = { if (allCaps) onValueChange(it.uppercase()) else onValueChange(it) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null)
                        Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                onClick()
                            }
                        }
                    else Modifier
                ),
            shape = RoundedCornerShape(14.dp), // Matched to HTML border-radius
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            isError = isError,
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
            ),

            )
    }
}