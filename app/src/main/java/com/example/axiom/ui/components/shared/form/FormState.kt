package com.example.axiom.ui.components.shared.form


import androidx.compose.runtime.mutableStateMapOf

class FormState(private val fields: List<FormField>) {

    val values = mutableStateMapOf<String, Any?>()
    val errors = mutableStateMapOf<String, String?>()

    init {
        fields.forEach {
            values[it.key] = null
            errors[it.key] = null
        }
    }

    fun setValue(key: String, value: Any?) {
        values[key] = value
        errors[key] = null
    }

    fun validate(): Boolean {
        var valid = true

        fields.forEach { field ->
            val value = values[field.key]

            if (field.required && (value == null || value.toString().isBlank())) {
                errors[field.key] = "Required"
                valid = false
                return@forEach
            }

            if (field is TextFormField) {
                val text = value as? String ?: ""
                val error = field.validator(text)
                errors[field.key] = error
                if (error != null) valid = false
            }
        }

        return valid
    }
}
