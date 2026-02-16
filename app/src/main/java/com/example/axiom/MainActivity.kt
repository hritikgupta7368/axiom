package com.example.axiom

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.axiom.ui.navigation.RootScaffold
import com.example.axiom.ui.theme.AxiomTheme
import com.example.axiom.update.ApkDownloader
import com.example.axiom.update.UpdateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForUpdates()
        setContent {
            AxiomTheme {
                val navController = rememberNavController()
                RootScaffold(navController)
            }
        }

    }

    private fun checkForUpdates() {
        lifecycleScope.launch {

            val update = withContext(Dispatchers.IO) {
                UpdateRepository.check()
            }

            if (update != null) {
                showUpdateDialog(update.versionName, update.apkUrl)
            }
        }
    }

    private fun showUpdateDialog(versionName: String, apkUrl: String) {

        AlertDialog.Builder(this)
            .setTitle("Update Available")
            .setMessage("Version $versionName is available.")
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                ApkDownloader.download(this, apkUrl)
            }
            .setNegativeButton("Later", null)
            .show()
    }
}

