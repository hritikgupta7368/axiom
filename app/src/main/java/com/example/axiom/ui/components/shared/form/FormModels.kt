package com.example.axiom.ui.components.shared.form

import androidx.compose.ui.text.input.KeyboardType

typealias Validator = (String) -> String?

sealed class FormField(
    val key: String,
    val label: String,
    val required: Boolean = false
)

class TextFormField(
    key: String,
    label: String,
    required: Boolean = false,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    val validator: Validator = { null }
) : FormField(key, label, required)

class DropdownFormField<T>(
    key: String,
    label: String,
    val options: List<T>,
    val display: (T) -> String,
    required: Boolean = false
) : FormField(key, label, required)

class DateFormField(
    key: String,
    label: String,
    required: Boolean = false
) : FormField(key, label, required)

class TimeFormField(
    key: String,
    label: String,
    required: Boolean = false
) : FormField(key, label, required)

class RadioFormField<T>(
    key: String,
    label: String,
    val options: List<T>,
    val display: (T) -> String,
    required: Boolean = false
) : FormField(key, label, required)

class CheckboxFormField(
    key: String,
    label: String
) : FormField(key, label, false)
