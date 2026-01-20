package com.example.axiom.ui.components.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.*
import com.example.axiom.preferences.theme.ThemeMode
import com.example.axiom.preferences.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ThemeToggle() {
    val context = LocalContext.current
    val owner = LocalViewModelStoreOwner.current!!

    val themeViewModel: ThemeViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = ThemeViewModelFactory(context)
    )

    val themeMode by themeViewModel.themeMode.collectAsState(
        initial = ThemeMode.DARK
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (themeMode == ThemeMode.DARK)
                "Dark Mode"
            else
                "Light Mode"
        )

        Switch(
            checked = themeMode == ThemeMode.DARK,
            onCheckedChange = {
                themeViewModel.toggleTheme()
            }
        )
    }
}