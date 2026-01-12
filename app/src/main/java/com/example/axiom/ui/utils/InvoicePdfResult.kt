package com.example.axiom.ui.utils


import java.io.File

sealed class InvoicePdfResult {

    data class Success(val file: File) : InvoicePdfResult()

    data class Error(val message: String) : InvoicePdfResult()
}

