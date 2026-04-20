package com.example.axiom

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.axiom.preferences.theme.ThemeMode
import com.example.axiom.preferences.theme.ThemeViewModel
import com.example.axiom.preferences.theme.ThemeViewModelFactory
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

        // --- ADD THIS BLOCK TO HIDE THE SYSTEM NAV BAR ---
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // This ensures it only shows temporarily if the user swipes up from the bottom
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the navigation bar (3 buttons)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        // -------------------------------------------------


        enableEdgeToEdge()

        checkForUpdates()
        setContent {

            val context = LocalContext.current

            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(context)
            )

            val themeMode by themeViewModel.themeMode.collectAsState(
                initial = ThemeMode.DARK
            )
            val isDarkTheme = themeMode == ThemeMode.DARK

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window

                    // 1. Force the status bar background to be completely transparent
                    window.statusBarColor = android.graphics.Color.TRANSPARENT

                    // 2. Flip the icon colors based on YOUR app's theme, not the system theme
                    // If Light Theme -> Dark Icons (true). If Dark Theme -> Light Icons (false).
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        !isDarkTheme
                }
            }

            AxiomTheme(
                darkTheme = isDarkTheme
            ) {
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

