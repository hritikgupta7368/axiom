package com.example.axiom.DB


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.axiom.BuildConfig
import com.example.axiom.data.notes.NoteEntity
import com.example.axiom.data.notes.NotesDao
import com.example.axiom.data.temp.CalendarDao
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.vault.VaultDao
import com.example.axiom.data.vault.VaultEntryEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceDao
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceItemEntity
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentTransactionEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyDao
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.product.components.ProductDao
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseDao
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseItemEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseRecordEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationDao
import com.example.axiom.ui.screens.finances.quotation.components.QuotationEntity
import com.example.axiom.ui.screens.finances.quotation.components.QuotationItemEntity


@Database(
    entities = [
        ProductEntity::class,
        PartyEntity::class,
        PartyContactEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentTransactionEntity::class,
        PurchaseRecordEntity::class,
        PurchaseItemEntity::class,
        QuotationEntity::class,
        QuotationItemEntity::class,


        VaultEntryEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        EventEntity::class,

        // add more entityies
    ],
    version = BuildConfig.DB_VERSION,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun partyDao(): PartyDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun purchaseDao(): PurchaseDao


    abstract fun vaultDao(): VaultDao
    abstract fun noteDao(): NotesDao
    abstract fun calendarDao(): CalendarDao


    abstract fun quotationDao(): QuotationDao


    // and daos here

    companion object {
        const val VERSION = 2

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "axiom_db"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
