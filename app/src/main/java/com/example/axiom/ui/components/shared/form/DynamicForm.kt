package com.example.axiom.ui.components.shared.form


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.form.components.AppDropdown
import com.example.axiom.ui.components.shared.form.components.AppTextField

@Composable
fun DynamicForm(
    fields: List<FormField>,
    formState: FormState
) {

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        fields.forEach { field ->

            Text(field.label)

            when (field) {

                is TextFormField -> {
                    AppTextField(
                        value = formState.values[field.key] as? String ?: "",
                        onValueChange = {
                            formState.setValue(field.key, it)
                        },
                        placeholder = field.placeholder,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = field.keyboardType
                        ),
                        error = formState.errors[field.key]
                    )
                }

                is DropdownFormField<*> -> {
                    RenderDropdown(field, formState)
                }

                is DateFormField -> {
                    // render date picker field
                }

                is TimeFormField -> {
                    // render time picker field
                }

                is RadioFormField<*> -> {
                    RenderRadio(field, formState)
                }

                is CheckboxFormField -> {
                    Row {
                        Checkbox(
                            checked = formState.values[field.key] as? Boolean ?: false,
                            onCheckedChange = {
                                formState.setValue(field.key, it)
                            }
                        )
                        Text(field.label)
                    }
                }
            }
        }
    }
}

@Composable
fun RenderRadio(x0: RadioFormField<*>, x1: FormState) {
    TODO("Not yet implemented")
}

@Composable
private fun <T> RenderDropdown(
    field: DropdownFormField<T>,
    formState: FormState
) {
    val selected = formState.values[field.key] as? T

    AppDropdown(
        value = selected,
        options = field.options,
        display = field.display,
        onSelect = { formState.setValue(field.key, it) },
        label = field.label,
        error = formState.errors[field.key]
    )
}

