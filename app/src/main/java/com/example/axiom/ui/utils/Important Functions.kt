package com.example.axiom.ui.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Amount {

    private val formatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale("en", "IN"))
        DecimalFormat("#,##,##0.00", symbols).apply {
            isGroupingUsed = true
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    fun format(value: Double): String {
        return "₹ ${formatter.format(value)}"
    }

    fun format(value: Int): String {
        return "₹ ${formatter.format(value)}"
    }
}