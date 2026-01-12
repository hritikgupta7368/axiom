package com.example.axiom.ui.screens.finances.Invoice



import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.axiom.ui.utils.PdfCacheCleaner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    pdfUri: Uri,
    onBack: () -> Unit
) {
    val context = LocalContext.current



    DisposableEffect(Unit) {
        onDispose {
            PdfCacheCleaner.delete(context, pdfUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice PDF") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, "Share Invoice")
                        )
                    }) {
                        Icon(Icons.Default.Share, null)
                    }
                }
            )
        }
    ) {

    }
}

