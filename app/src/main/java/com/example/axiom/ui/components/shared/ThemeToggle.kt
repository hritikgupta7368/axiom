package com.example.axiom.ui.components.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.preferences.theme.ThemeMode
import com.example.axiom.preferences.theme.ThemeViewModel
import com.example.axiom.preferences.theme.ThemeViewModelFactory

@Composable
fun ThemeToggle(showText: Boolean = true) {
    val context = LocalContext.current

    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModelFactory(context)
    )

    val themeMode by themeViewModel.themeMode.collectAsState(
        initial = ThemeMode.DARK
    )

    val isDark = themeMode == ThemeMode.DARK

    // Extract the switch into a reusable variable so we don't duplicate code
    val switchComponent = @Composable {
        Switch(
            checked = isDark,
            onCheckedChange = { themeViewModel.toggleTheme() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF30D158), // iOS Green
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF39393D)
            )
        )
    }

    if (showText) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isDark) "Dark Mode" else "Light Mode",
                color = Color.White
            )
            switchComponent()
        }
    } else {
        switchComponent()
    }
}