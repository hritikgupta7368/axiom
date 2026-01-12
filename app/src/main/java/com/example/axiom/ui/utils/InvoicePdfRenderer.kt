package com.example.axiom.ui.utils

import android.content.Context
import com.uttampanchasara.pdfgenerator.CreatePdf
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class InvoicePdfRenderer(private val context: Context) {

    suspend fun render(html: String, outputFile: File) =
        suspendCancellableCoroutine<Unit> { cont ->

            CreatePdf(context)
                .setPdfName(outputFile.nameWithoutExtension)
                .setFilePath(outputFile.parent!!) // REQUIRED by lib
                .setContent(html)
                .openPrintDialog(false)
                .setCallbackListener(object : CreatePdf.PdfCallbackListener {

                    override fun onSuccess(filePath: String) {
                        if (cont.isActive) cont.resume(Unit)
                    }

                    override fun onFailure(errorMsg: String) {
                        if (cont.isActive) {
                            cont.resumeWithException(RuntimeException(errorMsg))
                        }
                    }
                })
                .create()
        }
}
