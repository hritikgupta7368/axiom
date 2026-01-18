package com.example.axiom.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding) // Apply padding from the scaffold
                .fillMaxSize(), // Ensure it takes up the available space
            contentPadding = PaddingValues(16.dp), // Inner padding for the content
            verticalArrangement = Arrangement.spacedBy(20.dp) // Space between items
        ) {
            item {
                SettingsSection(title = "Appearance") {
                    SettingsRow(
                        icon = if (isDarkTheme) AppIcons.Moon else AppIcons.Sun,
                        title = "Dark Mode",
                        subtitle = if (isDarkTheme) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onThemeToggle() }
                            )
                        }
                    )
                }
            }
            item {
                SettingsSection(title = "Account") {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = "Manage your account"
                    )
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        title = "Security",
                        subtitle = "Password, biometrics"
                    )
                }
            }
            item {
                SettingsSection(title = "Data") {
                    SettingsRow(
                        icon = Icons.Default.Menu,
                        title = "Backup",
                        subtitle = "Cloud backup settings"
                    )
                    SettingsRow(
                        icon = Icons.Default.Delete,
                        title = "Clear Cache",
                        subtitle = "Free local storage"
                    )
                }
            }
            item {
            SettingsSection(title = "About") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0"
                )
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "Terms & Privacy",
                    subtitle = "Legal information"
                )
            }
                }
        }
    }
}


@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: Any,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trailing == null) {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

//        when (icon) {
//            is ImageVector -> Icon(icon, null)
//            is AppIcons -> AppIconButton(icon = icon, contentDescription = null)
//        }
        AppIconButton(AppIcons.ArrowForward , contentDescription = "settings item" , onClick = {})

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailing?.invoke()
    }
}
