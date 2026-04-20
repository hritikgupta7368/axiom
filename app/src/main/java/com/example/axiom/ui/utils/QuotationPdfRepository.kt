package com.example.axiom.ui.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.axiom.ui.screens.finances.quotation.components.FullQuotation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class QuotationPdfRepository(private val context: Context) {

    suspend fun generate(quotation: FullQuotation, logoUri: String): Uri =
        withContext(Dispatchers.IO) {

            // Assuming you have a corresponding HTML generator for quotations
            val html = QuotationHtmlGenerator.generateQuotationHtml(quotation)

            val pdfFile = File(
                context.cacheDir,
                // Adjusted to use a quotation number (update the property name if yours differs)
                "quotation_${quotation.quotation.quotationNumber}.pdf"
            ).apply {
                if (exists()) delete()
            }

            // Assuming you have a corresponding PDF renderer
            QuotationPdfRenderer(context).render(html, pdfFile)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
        }
}