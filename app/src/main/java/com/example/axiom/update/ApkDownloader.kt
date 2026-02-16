package com.example.axiom.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment

object ApkDownloader {

    fun download(context: Context, url: String) {

        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "axiom_update.apk"
        )

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadId = manager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val uri = manager.getUriForDownloadedFile(downloadId)
                if (uri != null) {
                    install(ctx, uri)
                }
               
                ctx.unregisterReceiver(this)
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    private fun install(context: Context, uri: Uri) {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            uri,
            "application/vnd.android.package-archive"
        )
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }
}