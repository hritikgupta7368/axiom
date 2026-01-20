// BackupManager.kt
package com.example.axiom.Backup


import androidx.room.withTransaction
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import android.content.Context
import android.net.Uri

class BackupManager(
    private val db: AppDatabase
) {

    suspend fun exportVault(): AppBackup =
        withContext(Dispatchers.IO) {

            val vault = db.vaultDao().exportAll().map { it.toBackup() }
            val tasks = db.calendarDao().exportTasks().map { it.toBackup() }
            val events = db.calendarDao().exportEvents().map { it.toBackup() }

            AppBackup(
                meta = BackupMeta(
                    appVersion = 1,
                    dbVersion = AppDatabase.VERSION,
                    createdAt = System.currentTimeMillis()
                ),
                vaultEntries = vault,
                tasks = tasks,
                events = events
            )
        }

    suspend fun restoreVault(backup: AppBackup) =
        withContext(Dispatchers.IO) {

            db.withTransaction {
                db.vaultDao().restore(
                    backup.vaultEntries.map { it.toEntity() }
                )
                db.calendarDao()
                    .restoreTasks(backup.tasks.map { it.toEntity() })

                db.calendarDao()
                    .restoreEvents(backup.events.map { it.toEntity() })
            }
        }
}

// BackupJson
object BackupJson {

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

// BackupFileWriter
fun writeBackupToUri(
    context: Context,
    uri: Uri,
    backup: AppBackup
) {
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(
            BackupJson.json
                .encodeToString(AppBackup.serializer(), backup)
                .toByteArray()
        )
    }
}

//BackupFileReader
fun readBackupFromUri(
    context: Context,
    uri: Uri
): AppBackup {
    val text =
        context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.readText()
            ?: error("Invalid backup file")

    return BackupJson.json
        .decodeFromString(AppBackup.serializer(), text)
}