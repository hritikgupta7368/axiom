package com.example.axiom.ui.utils

import android.content.Context
import android.net.Uri
import java.io.File

object PdfCacheCleaner {

    fun delete(context: Context, uri: Uri) {
        runCatching {
            val fileName = uri.lastPathSegment ?: return
            val file = File(context.cacheDir, fileName)
            if (file.exists()) file.delete()
        }
    }
}
