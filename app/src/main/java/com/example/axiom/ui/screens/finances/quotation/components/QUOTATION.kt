package com.example.axiom.ui.screens.finances.quotation.components

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

enum class QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    CONVERTED_TO_INVOICE
}

@Entity(
    tableName = "quotation_items",
    foreignKeys = [
        ForeignKey(
            entity = QuotationEntity::class,
            parentColumns = ["id"],
            childColumns = ["quotationId"],
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
        Index("quotationId"),
        Index("productId")
    ]
)
data class QuotationItemEntity(
    @PrimaryKey val id: String = "",
    val quotationId: String = "",
    val productId: String = "",

    // Snapshots (in case product details change later)
    val productNameSnapshot: String = "",
    val hsnSnapshot: String = "", // Maps to <span class="item-subtext">
    val unitSnapshot: String = "",
    val quantity: Double = 0.0,
    val quotationPriceAtTime: Double = 0.0,
    val taxableAmount: Double = 0.0,

    )


@Entity(
    tableName = "quotations",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("customerId"),
        Index("sellerId"),
        Index(value = ["quotationNumber"], unique = true)
    ]
)
data class QuotationEntity(
    @PrimaryKey val id: String = "",
    val quotationNumber: String = "",
    val customerId: String? = null,
    val sellerId: String? = null, // "Quotation By"

    // Dates
    val quotationDate: Long = System.currentTimeMillis(),
    val validUntilDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Default 30 days

    // Supply Info
    val placeOfSupplyCode: String = "",
    val countryOfSupply: String = "India",

    // Financials (Mirroring your HTML summary)
    val itemSubTotal: Double = 0.0,
    val globalDiscountAmount: Double = 0.0, // If you apply a flat discount on the whole quote
    val grandTotal: Double = 0.0,
    val amountInWords: String = "",
    val supplyType: SupplyType = SupplyType.INTRA_STATE,

    // Text Blocks (Lower left of HTML)
    val termsAndConditions: String = "",
    val additionalNotes: String = "",

    // Signature (Bottom right of HTML)
    val signatureText: String = "Authorized Signature",
    val signatureImageUri: String? = null, // Path to the signature image file

    val status: QuotationStatus = QuotationStatus.DRAFT,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)


data class FullQuotation(
    @Embedded val quotation: QuotationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "quotationId"
    )
    val items: List<QuotationItemEntity>,


    // Customer Details
    @Relation(
        entity = PartyEntity::class,
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: PartyWithContacts?,

    // Seller/Company Details
    @Relation(
        entity = PartyEntity::class,
        parentColumn = "sellerId",
        entityColumn = "id"
    )
    val seller: PartyWithContacts?
)

data class QuotationCardDto(
    val id: String,
    val quotationNumber: String,
    val customerName: String?, // Joined from PartyEntity
    val quotationDate: Long,
    val grandTotal: Double,
    val status: QuotationStatus
)


@Dao
interface QuotationDao {

    // =========================================================================
    // READ OPERATIONS (UI Lists & Details)
    // =========================================================================

    // Gets all active quotations for the card list
    @Query(
        """
        SELECT q.id, q.quotationNumber, p.businessName AS customerName, 
               q.quotationDate, q.grandTotal, q.status 
        FROM quotations q 
        LEFT JOIN party p ON q.customerId = p.id 
        WHERE q.isDeleted = 0 
        ORDER BY q.createdAt DESC
    """
    )
    fun getAllQuotationCards(): Flow<List<QuotationCardDto>>

    // Searches active quotations by quote number or customer name
    @Query(
        """
        SELECT q.id, q.quotationNumber, p.businessName AS customerName, 
               q.quotationDate, q.grandTotal, q.status 
        FROM quotations q 
        LEFT JOIN party p ON q.customerId = p.id 
        WHERE q.isDeleted = 0 AND (
            q.quotationNumber LIKE '%' || :searchQuery || '%' OR 
            p.businessName LIKE '%' || :searchQuery || '%'
        )
        ORDER BY q.createdAt DESC
    """
    )
    fun searchQuotationCards(searchQuery: String): Flow<List<QuotationCardDto>>

    // Gets the complete quotation with all nested items and parties for Edit/View screens
    @Transaction
    @Query("SELECT * FROM quotations WHERE id = :id AND isDeleted = 0")
    suspend fun getFullQuotationById(id: String): FullQuotation?

    // =========================================================================
    // WRITE OPERATIONS (Create, Update, Soft Delete)
    // =========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<QuotationItemEntity>)

    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    @Query("DELETE FROM quotation_items WHERE quotationId = :quotationId")
    suspend fun deleteItemsByQuotationId(quotationId: String)

    // TRANSACTION: Safely create a quote and its items together
    @Transaction
    suspend fun createFullQuotation(quotation: QuotationEntity, items: List<QuotationItemEntity>) {
        insertQuotation(quotation)
        insertItems(items)
    }

    // TRANSACTION: Update a quote by refreshing the header and replacing items
    @Transaction
    suspend fun updateFullQuotation(quotation: QuotationEntity, items: List<QuotationItemEntity>) {
        updateQuotation(quotation.copy(updatedAt = System.currentTimeMillis()))
        deleteItemsByQuotationId(quotation.id) // Clear old items
        insertItems(items) // Insert updated items
    }

    // Soft delete: Just flags it as deleted so it hides from lists
    @Query("UPDATE quotations SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteQuotation(id: String, timestamp: Long = System.currentTimeMillis())

    // Status update helper (e.g. converting to invoice, or marking as accepted)
    @Query("UPDATE quotations SET status = :newStatus, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateQuotationStatus(id: String, newStatus: QuotationStatus, timestamp: Long = System.currentTimeMillis())
}