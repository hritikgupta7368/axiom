// BackupManager.kt
package com.example.axiom.Backup


import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.axiom.BuildConfig
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


data class BackupScope(
    val vault: Boolean = true,
    val tasks: Boolean = true,
    val events: Boolean = true,
    val notes: Boolean = true,
    //finances
    val finances: Boolean = true
)

class BackupManager(
    private val db: AppDatabase
) {

    suspend fun export(scope: BackupScope): AppBackup = withContext(Dispatchers.IO) {

        val vault: List<VaultEntryBackup> =
            if (scope.vault) db.vaultDao().exportAll().map { it.toBackup() } else emptyList()
        val tasks =
            if (scope.tasks) db.calendarDao().exportTasks().map { it.toBackup() } else emptyList()
        val events =
            if (scope.events) db.calendarDao().exportEvents().map { it.toBackup() } else emptyList()
        val notes = if (scope.notes) db.noteDao().exportAll().map { it.toBackup() } else emptyList()

// Finances - Use your actual DAO names here
        val parties = if (scope.finances) db.partyDao().exportAllParties().map { it.toBackup() } else emptyList()
        val partyContacts = if (scope.finances) db.partyDao().exportAllContacts().map { it.toBackup() } else emptyList()
        val products = if (scope.finances) db.productDao().exportAll().map { it.toBackup() } else emptyList()

        val invoices = if (scope.finances) db.invoiceDao().exportAllInvoices().map { it.toBackup() } else emptyList()
        val invoiceItems = if (scope.finances) db.invoiceDao().exportAllItems().map { it.toBackup() } else emptyList()
        val payments = if (scope.finances) db.invoiceDao().exportAllPayments().map { it.toBackup() } else emptyList()

        val purchases = if (scope.finances) db.purchaseDao().exportAllRecords().map { it.toBackup() } else emptyList()
        val purchaseItems = if (scope.finances) db.purchaseDao().exportAllItems().map { it.toBackup() } else emptyList()




        AppBackup(
            meta = BackupMeta(
                appVersion = BuildConfig.VERSION_CODE,
                dbVersion = BuildConfig.DB_VERSION,
                createdAt = System.currentTimeMillis()
            ),
            vaultEntries = vault,
            tasks = tasks,
            events = events,
            notes = notes,

            // Finances
            parties = parties,
            partyContacts = partyContacts,
            products = products,
            invoices = invoices,
            invoiceItems = invoiceItems,
            purchases = purchases,
            purchaseItems = purchaseItems,
            payments = payments
        )
    }

    suspend fun restore(backup: AppBackup): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (backup.meta.dbVersion > BuildConfig.DB_VERSION) {
                    return@withContext false
                }
                db.withTransaction {
                    // Restore Vault
                    db.vaultDao().restore(backup.vaultEntries.map { it.toEntity() })

                    // Restore Calendar
                    db.calendarDao().restoreTasks(backup.tasks.map { it.toEntity() })
                    db.calendarDao().restoreEvents(backup.events.map { it.toEntity() })

                    // Restore Notes
                    db.noteDao().restore(backup.notes.map { it.toEntity() })

                    // RESTORE FINANCES IN STRICT ORDER DUE TO FOREIGN KEYS

                    // 1. Independent Entities
                    db.partyDao().restoreParties(backup.parties.map { it.toEntity() })
                    db.productDao().restore(backup.products.map { it.toEntity() })

                    // 2. Weakly dependent entities (depend on Party)
                    db.partyDao().restoreContacts(backup.partyContacts.map { it.toEntity() })

                    // 3. Document headers (depend on Party)
                    db.purchaseDao().restoreRecords(backup.purchases.map { it.toEntity() })
                    db.invoiceDao().restoreInvoices(backup.invoices.map { it.toEntity() })

                    // 4. Document Items (depend on Document + Product)
                    db.purchaseDao().restoreItems(backup.purchaseItems.map { it.toEntity() })
                    db.invoiceDao().restoreItems(backup.invoiceItems.map { it.toEntity() })

                    // 5. Payments (depend on Party + Document)
                    db.invoiceDao().restorePayments(backup.payments.map { it.toEntity() })
                }
                true
            } catch (e: Exception) {
                e.printStackTrace() // Good for debugging foreign key constraint failures
                false
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