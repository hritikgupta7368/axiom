package com.example.axiom.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.axiom.Backup.BackupManager
import com.example.axiom.Backup.BackupScope
import com.example.axiom.Backup.writeBackupToUri
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.get(context) }
    val backupManager = remember { BackupManager(db) }

    var vault by remember { mutableStateOf(true) }
    var tasks by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf(true) }


    // CREATE backup file
    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult

            scope.launch {
                try {
                    val backup = backupManager.export(
                        BackupScope(
                            vault = vault,
                            tasks = tasks,
                            events = events
                        )
                    )
                    writeBackupToUri(context, uri, backup)
                    Toast
                        .makeText(context, "Backup saved successfully", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    Toast
                        .makeText(context, "Backup failed", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            BackupToggle("Vault Entries", vault) { vault = it }
            BackupToggle("Tasks", tasks) { tasks = it }
            BackupToggle("Events", events) { events = it }

            Button(
                onClick = {
                    exportLauncher.launch("axiom_backup.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun BackupToggle(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
