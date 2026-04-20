package com.example.axiom.ui.screens.finances.purchase.components

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import kotlinx.coroutines.flow.Flow


@Entity(
    tableName = "purchase_records",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("supplierId"),
        Index(value = ["supplierInvoiceNumber"])
    ]
)
data class PurchaseRecordEntity(
    @PrimaryKey val id: String = "",
    val customerId: String? = "",
    val supplierId: String? = "",
    val supplierInvoiceNumber: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),

    val placeOfSupplyCode: String? = null,
    val reverseChargeApplicable: Boolean = false,
    val eWayBillNumber: String? = null,
    val eWayBillDate: Long? = null,
    val vehicleNumber: String? = null,
    val shippedToAddress: String? = null,
    val supplyType: SupplyType = SupplyType.INTRA_STATE,
    val deliveryCharge: Double = 0.0,
    val extraCharges: Double = 0.0,
    val globalDiscountAmount: Double = 0.0,
    val itemSubTotal: Double = 0.0,


    val isEdited: Boolean = true,


    val totalTaxableAmount: Double = 0.0,
    val globalGstRate: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,

    val isItcEligible: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)


@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("purchaseId"),
        Index("productId")
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey val id: String = "",
    val purchaseId: String = "",
    val productId: String = "",
    val productNameSnapshot: String = "",
    val hsnCode: String = "",
    val unit: String = "",
    val quantity: Double = 0.0,
    val costPrice: Double = 0.0,
    val taxableAmount: Double = 0.0
)

data class PurchaseWithItems(
    @Embedded val record: PurchaseRecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItemEntity>,
    @Relation(
        entity = PartyEntity::class,
        parentColumn = "supplierId",
        entityColumn = "id"
    )
    val supplier: PartyWithContacts?,
)

@Dao
interface PurchaseDao {

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PurchaseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItemEntity>)

    // ---------- UPSERT (ADD / UPDATE) ----------
    @Transaction
    suspend fun upsertPurchaseWithItems(
        record: PurchaseRecordEntity,
        items: List<PurchaseItemEntity>
    ) {
        insertRecord(record)

        deleteItemsByPurchaseId(record.id)

        insertItems(
            items.map { it.copy(purchaseId = record.id) }
        )
    }

    // ---------- DELETE ----------
    @Query("DELETE FROM purchase_records WHERE id = :id")
    suspend fun deletePurchase(id: String)
    // CASCADE handles purchase_items automatically

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsByPurchaseId(purchaseId: String)

    // ---------- UPDATE ----------
    @Update
    suspend fun updateRecord(record: PurchaseRecordEntity)

    // ---------- GET ALL (latest first, based on createdAt) ----------
    @Transaction
    @Query("SELECT * FROM purchase_records ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<PurchaseWithItems>>


    // ---------- SEARCH ----------
    @Transaction
    @Query(
        """
        SELECT * FROM purchase_records
        WHERE supplierInvoiceNumber LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchPurchases(query: String): Flow<List<PurchaseWithItems>>

    // ---------- GET SINGLE ----------
    @Transaction
    @Query("SELECT * FROM purchase_records WHERE id = :id LIMIT 1")
    suspend fun getPurchaseWithItems(id: String): PurchaseWithItems?

    // --- EXPORT ---
    @Query("SELECT * FROM purchase_records")
    suspend fun exportAllRecords(): List<PurchaseRecordEntity>

    @Query("SELECT * FROM purchase_items")
    suspend fun exportAllItems(): List<PurchaseItemEntity>

    // --- RESTORE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreRecords(records: List<PurchaseRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreItems(items: List<PurchaseItemEntity>)

}