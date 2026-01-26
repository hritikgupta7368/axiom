// BackupManager.kt
package com.example.axiom.Backup


import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
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

        val products: List<ProductBackup> =
            if (scope.finances) db.productDao().exportAll().map { it.toBackup() } else emptyList()
        val customers: List<CustomerFirmBackup> =
            if (scope.finances) db.customerFirmDao().exportAll()
                .map { it.toBackup() } else emptyList()
        val sellers: List<SellerFirmBackup> =
            if (scope.finances) db.sellerFirmDao().exportAll()
                .map { it.toBackup() } else emptyList()
        val suppliers: List<SupplierFirmBackup> =
            if (scope.finances) db.supplierFirmDao().exportAll()
                .map { it.toBackup() } else emptyList()
        val purchases: List<PurchaseRecordBackup> =
            if (scope.finances) db.purchaseRecordDao().exportAll()
                .map { it.toBackup() } else emptyList()
        val invoices =
            if (scope.finances) db.invoiceDao().exportAll().map { it.toBackup() } else emptyList()


        AppBackup(
            meta = BackupMeta(
                appVersion = 1, // You can pull this from BuildConfig.VERSION_CODE
                dbVersion = AppDatabase.VERSION,
                createdAt = System.currentTimeMillis()
            ),
            vaultEntries = vault,
            tasks = tasks,
            events = events,
            notes = notes,

            // Finances
            products = products,
            customers = customers,
            sellers = sellers,
            suppliers = suppliers,
            purchases = purchases,
            invoices = invoices
        )
    }

    suspend fun restore(backup: AppBackup) =
        withContext(Dispatchers.IO) {

            db.withTransaction {
                // Restore Vault
                db.vaultDao().restore(backup.vaultEntries.map { it.toEntity() })

                // Restore Calendar
                db.calendarDao().restoreTasks(backup.tasks.map { it.toEntity() })
                db.calendarDao().restoreEvents(backup.events.map { it.toEntity() })

                // Restore Notes
                db.noteDao().restore(backup.notes.map { it.toEntity() })

                // Restore Finances
                db.productDao().restore(backup.products.map { it.toEntity() })
                db.customerFirmDao().restore(backup.customers.map { it.toEntity() })
                db.sellerFirmDao().restore(backup.sellers.map { it.toEntity() })
                db.supplierFirmDao().restore(backup.suppliers.map { it.toEntity() })
                db.purchaseRecordDao().restore(backup.purchases.map { it.toEntity() })
                db.invoiceDao().restore(backup.invoices.map { it.toEntity() })
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