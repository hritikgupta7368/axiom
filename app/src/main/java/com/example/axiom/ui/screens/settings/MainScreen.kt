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
import com.example.axiom.ui.components.shared.ThemeToggle

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.axiom.Backup.*
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.get(context) }
    val backupManager = remember { BackupManager(db) }


    // CREATE backup file
    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val backup = backupManager.exportVault()
                    writeBackupToUri(context, uri, backup)
                }
            }
        }

    // PICK backup file
    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val backup = readBackupFromUri(context, uri)
                    backupManager.restoreVault(backup)
                }
            }
        }


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
                        title = "Backup Vault",
                        subtitle = "Export vault data to file"
                    ) {
                        exportLauncher.launch("axiom_backup_vault.json")
                    }
                    // RESTORE
                    SettingsRow(
                        icon = Icons.Default.Build,
                        title = "Restore Vault",
                        subtitle = "Import vault data from file"
                    ) {
                        importLauncher.launch(arrayOf("application/json"))
                    }
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


    }
}
