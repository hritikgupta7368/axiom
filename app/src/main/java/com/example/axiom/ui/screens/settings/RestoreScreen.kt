package com.example.axiom.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.axiom.Backup.AppBackup
import com.example.axiom.Backup.BackupManager
import com.example.axiom.Backup.readBackupFromUri
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.get(context) }
    val backupManager = remember { BackupManager(db) }

    var backup by remember { mutableStateOf<AppBackup?>(null) }


    // Pick backup file
    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult

            scope.launch {
                try {
                    val loaded = readBackupFromUri(context, uri)
                    val success = backupManager.restore(loaded)

                    if (success) {
                        Toast.makeText(
                            context,
                            "Restore completed successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBack()
                    } else {
                        Toast.makeText(
                            context,
                            "Restore failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Invalid backup file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restore Backup") },
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

            if (backup == null) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text("Select Backup File")
                }

            } else {


                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            try {
                                backupManager.restore(
                                    backup = backup!!,
                                )
                                Toast.makeText(
                                    context,
                                    "Restore completed successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBack()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Restore failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Restore Selected")
                }
            }
        }
    }
}


