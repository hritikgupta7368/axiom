package com.example.axiom.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.axiom.data.finances.domain.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InvoicePdfRepository(private val context: Context) {

    suspend fun generate(invoice: Invoice, logoUri: String): Uri =
        withContext(Dispatchers.IO) {

            val html = InvoiceHtmlGenerator.generateInvoiceHtml(invoice, logoUri)

            val pdfFile = File(
                context.cacheDir,
                "invoice_${invoice.invoiceNo}.pdf"
            ).apply {
                if (exists()) delete()
            }

            InvoicePdfRenderer(context).render(html, pdfFile)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
        }
}
