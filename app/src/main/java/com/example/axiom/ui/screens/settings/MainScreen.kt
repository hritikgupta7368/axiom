package com.example.axiom.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.axiom.BuildConfig
import com.example.axiom.ui.components.shared.ThemeToggle
import com.example.axiom.ui.components.shared.button.AppIconButton
import com.example.axiom.ui.components.shared.button.AppIcons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenBackup: () -> Unit,
    onOpenRestore: () -> Unit
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
                }
            }
            item {
                ThemeToggle()
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
                    // BACKUP
                    SettingsRow(
                        icon = Icons.Default.Check,
                        title = "Backup",
                        subtitle = "Export vault data to file",
                        onClick = onOpenBackup
                    )

                    // RESTORE
                    SettingsRow(
                        icon = Icons.Default.Build,
                        title = "Restore",
                        subtitle = "Import vault data from file",
                        onClick = onOpenRestore
                    )

                }
            }
            item {
                SettingsSection(title = "About") {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = BuildConfig.VERSION_NAME
                    )
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "DB Version",
                        subtitle = (BuildConfig.DB_VERSION).toString()
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
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AppIconButton(AppIcons.ArrowForward, contentDescription = "settings item", onClick = {})

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }


    }
}
