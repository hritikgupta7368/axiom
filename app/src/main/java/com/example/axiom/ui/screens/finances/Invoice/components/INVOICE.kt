package com.example.axiom.ui.screens.finances.Invoice.components

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
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import com.example.axiom.ui.screens.finances.product.components.ProductInvoiceUsage
import kotlinx.coroutines.flow.Flow

enum class SupplyType { INTRA_STATE, INTER_STATE }
enum class PaymentStatus { UNPAID, PARTIAL, PAID }
enum class InvoiceStatus { DRAFT, ACTIVE, CANCELLED }

// Granular Payment Modes
enum class PaymentMode {
    CASH, UPI, NEFT, RTGS,
}

enum class TransactionType {
    CREDIT, // Money received
    DEBIT   // Money refunded / reversed
}


@Entity(
    tableName = "invoices",
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
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String = "",
    val invoiceNumber: String = "",
    val customerId: String? = null,
    val sellerId: String? = null,

    val invoiceDate: Long = System.currentTimeMillis(),

    // Logistics & Shipping
    val vehicleNumber: String? = null,
    val shippedToAddress: String? = null,
    val placeOfSupplyCode: String = "",
    val supplyType: SupplyType = SupplyType.INTRA_STATE,
    val eWayBillNumber: String? = null,  // Added!
    val eWayBillDate: Long? = null,      // Added!

    // Financials
    val itemSubTotal: Double = 0.0, // Sum of all item taxable amounts
    val deliveryCharge: Double = 0.0, // Taxable delivery charge
    val extraCharges: Double = 0.0,
    val globalDiscountAmount: Double = 0.0,

    // --- Financials (Calculated Totals) ---
    val totalTaxableAmount: Double = 0.0,       // final amount on amount on which tax will be applied
    val globalGstRate: Double = 0.0,  // either it will be igst rate or will split in cgst and sgst
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val amountInWords: String = "",


    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val status: InvoiceStatus = InvoiceStatus.ACTIVE,
    val isEdited: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
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
        Index("invoiceId"),
        Index("productId")
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey val id: String = "",
    val invoiceId: String = "",
    val productId: String = "",

    // Links to the purchase record to track profit margins / FIFO inventory
    val linkedPurchaseItemId: String? = null,

    val productNameSnapshot: String = "",
    val hsnSnapshot: String = "",
    val unitSnapshot: String = "",

    val quantity: Double = 0.0,
    val sellingPriceAtTime: Double = 0.0,

    val costPriceAtTime: Double = 0.0,

    val itemDiscountAmount: Double = 0.0,

    // (sellingPriceAtTime * quantity) - itemDiscountAmount
    val taxableAmount: Double = 0.0
)

//payments

@Entity(
    tableName = "payment_transactions",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("partyId"),
        Index("documentId")
    ]
)
data class PaymentTransactionEntity(
    @PrimaryKey val id: String = "",

    val partyId: String = "",          // Customer or Supplier
    val documentId: String? = null,    // InvoiceId (nullable for advance)

    val type: TransactionType = TransactionType.CREDIT,
    val amount: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,

    val transactionDate: Long = System.currentTimeMillis(),
    val referenceId: String? = null,
    val notes: String? = null
)

// DTO for your UI List / Cards
data class InvoiceCardDto(
    val id: String,
    val invoiceNumber: String,
    val customerName: String, // Joined from PartyEntity
    val grandTotal: Double,
    val invoiceDate: Long,
    val updatedAt: Long,
    val supplyType: SupplyType,
    val paymentStatus: PaymentStatus,
    val status: InvoiceStatus,
    val isEdited: Boolean
)

// Full Invoice with Items for Create/Edit screens
data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>,

    // 2. NEW: Customer Details (Nested Relation)
    @Relation(
        entity = PartyEntity::class,
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: PartyWithContacts?,

    // 3. NEW: Seller Details (Nested Relation)
    @Relation(
        entity = PartyEntity::class,
        parentColumn = "sellerId",
        entityColumn = "id"
    )
    val seller: PartyWithContacts?,

    // 4. NEW: Payment Transactions
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val payments: List<PaymentTransactionEntity>

)

// DTO for HSN Summary (GSTR-1)
data class HsnSummaryDto(
    val hsnCode: String,
    val uqc: String,           // Added this
    val taxRate: Double,       // Added this
    val totalQty: Double,
    val totalTaxableValue: Double,
    val igst: Double,
    val cgst: Double,
    val sgst: Double
)


data class B2bInvoiceRow(
    val id: String,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val grandTotal: Double,
    val totalTaxableAmount: Double,
    val igstAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val gstNumber: String?
)


data class CustomerInvoiceRow(
    val invoiceId: String,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val grandTotal: Double,
    val totalTaxableAmount: Double,
    val paymentStatus: PaymentStatus,
    val status: InvoiceStatus
)

data class CustomerProductUsage(
    val productId: String,
    val productName: String,
    val hsn: String,
    val unit: String,
    val totalQty: Double,
    val totalSales: Double
)

data class CustomerTopProduct(
    val productId: String,
    val productName: String,
    val totalQty: Double
)

data class CustomerBusinessStats(
    val totalInvoices: Int,
    val totalSales: Double,
    val totalTaxable: Double,
    val avgInvoiceValue: Double
)


@Dao
interface InvoiceDao {

    // --- Basic CRUD ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsByInvoiceId(invoiceId: String)

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun deleteInvoiceById(invoiceId: String)

    // Transaction to safely Save/Edit a full invoice with items
    @Transaction
    suspend fun upsertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        insertInvoice(invoice)
        deleteItemsByInvoiceId(invoice.id) // Clear old items if editing
        insertInvoiceItems(items)
    }

    // Alternatively, if you just want a one-time fetch for an Edit screen without observing:
    // One-shot fetch for Edit/Preview screens
    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceWithItemsById(invoiceId: String): InvoiceWithItems?

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceWithItemsByIdSync(invoiceId: String): InvoiceWithItems?


    @Query(
        """
UPDATE invoices
SET status = :status,
    updatedAt = :updatedAt
WHERE id = :invoiceId
"""
    )
    suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: InvoiceStatus,
        updatedAt: Long
    )

    // --- Card & Search Queries ---

    // Sorts by invoiceNumber descending (latest at the top)
    @Query(
        """
        SELECT i.id, i.invoiceNumber, p.businessName AS customerName, i.grandTotal, 
               i.invoiceDate, i.updatedAt, i.supplyType, i.paymentStatus, i.status, i.isEdited
        FROM invoices i 
        LEFT JOIN party p ON i.customerId = p.id 
        ORDER BY i.invoiceNumber DESC
    """
    )
    fun getAllInvoiceCards(): Flow<List<InvoiceCardDto>>

    @Query(
        """
        SELECT i.id, i.invoiceNumber, p.businessName AS customerName, i.grandTotal, 
               i.invoiceDate, i.updatedAt, i.supplyType, i.paymentStatus, i.status, i.isEdited 
        FROM invoices i 
        LEFT JOIN party p ON i.customerId = p.id 
        WHERE i.invoiceNumber LIKE '%' || :query || '%' 
           OR p.businessName LIKE '%' || :query || '%'
        ORDER BY i.invoiceNumber DESC
    """
    )
    fun searchInvoiceCards(query: String): Flow<List<InvoiceCardDto>>


    // --- Analytics & GST Reporting Queries ---

    @Query("SELECT SUM(grandTotal) FROM invoices WHERE invoiceDate BETWEEN :start AND :end AND status != 'CANCELLED'")
    suspend fun getTotalSalesBetween(start: Long, end: Long): Double?

    // For Metric Card Bar Chart (Groups sales by time chunks if needed, but easier to calculate in ViewModel by querying daily ranges)
    @Query("SELECT SUM(grandTotal) FROM invoices WHERE invoiceDate >= :start AND invoiceDate < :end AND status != 'CANCELLED'")
    suspend fun getSalesForTimeframe(start: Long, end: Long): Double?

    // GSTR-1: B2B Invoices (Customers with GST Reg)
    @Query(
        """
SELECT 
i.id,
i.invoiceNumber,
i.invoiceDate,
i.grandTotal,
i.totalTaxableAmount,
i.igstAmount,
i.cgstAmount,
i.sgstAmount,
p.gstNumber
FROM invoices i
INNER JOIN party p ON i.customerId = p.id
WHERE p.registrationType LIKE '%Gst%'
AND i.invoiceDate BETWEEN :start AND :end
AND i.status != 'CANCELLED'
"""
    )
    suspend fun getB2bInvoiceRows(start: Long, end: Long): List<B2bInvoiceRow>

    // GSTR-1: HSN Summary
    @Query(
        """
        SELECT 
            ii.hsnSnapshot AS hsnCode, 
            ii.unitSnapshot AS uqc,
            i.globalGstRate AS taxRate,
            SUM(ii.quantity) AS totalQty, 
            
            -- 1. Apportioned Taxable Value: (Item Value / Invoice Item Subtotal) * Final Invoice Taxable
            SUM(ii.taxableAmount * (i.totalTaxableAmount / CASE WHEN i.itemSubTotal > 0 THEN i.itemSubTotal ELSE 1.0 END)) AS totalTaxableValue,
            
            -- 2. IGST: Only apply if INTER_STATE
            SUM(
                CASE WHEN i.supplyType = 'INTER_STATE' 
                THEN ii.taxableAmount * (i.totalTaxableAmount / CASE WHEN i.itemSubTotal > 0 THEN i.itemSubTotal ELSE 1.0 END) * (i.globalGstRate / 100.0) 
                ELSE 0.0 END
            ) AS igst, 
            
            -- 3. CGST: Only apply if INTRA_STATE
            SUM(
                CASE WHEN i.supplyType = 'INTRA_STATE' 
                THEN ii.taxableAmount * (i.totalTaxableAmount / CASE WHEN i.itemSubTotal > 0 THEN i.itemSubTotal ELSE 1.0 END) * (i.globalGstRate / 200.0) 
                ELSE 0.0 END
            ) AS cgst, 
            
            -- 4. SGST: Only apply if INTRA_STATE
            SUM(
                CASE WHEN i.supplyType = 'INTRA_STATE' 
                THEN ii.taxableAmount * (i.totalTaxableAmount / CASE WHEN i.itemSubTotal > 0 THEN i.itemSubTotal ELSE 1.0 END) * (i.globalGstRate / 200.0) 
                ELSE 0.0 END
            ) AS sgst 
            
        FROM invoice_items ii 
        INNER JOIN invoices i ON ii.invoiceId = i.id 
        WHERE i.invoiceDate BETWEEN :start AND :end AND i.status != 'CANCELLED'
        -- Group by HSN, UQC, and Tax Rate to avoid mixing different items/rates under one HSN
        GROUP BY ii.hsnSnapshot, ii.unitSnapshot, i.globalGstRate
    """
    )
    suspend fun getHsnSummary(start: Long, end: Long): List<HsnSummaryDto>


    @Query(
        """
SELECT 
    i.id AS invoiceId,
    i.invoiceNumber,
    i.invoiceDate,
    p.businessName AS customerName,
    ii.quantity,
    ii.sellingPriceAtTime AS sellingPrice,
    ii.taxableAmount,
    i.status AS invoiceStatus
FROM invoice_items ii
INNER JOIN invoices i ON ii.invoiceId = i.id
LEFT JOIN party p ON i.customerId = p.id
WHERE ii.productId = :productId
ORDER BY i.invoiceDate DESC
"""
    )
    fun getInvoicesForProduct(productId: String): Flow<List<ProductInvoiceUsage>>


    @Query(
        """
SELECT 
    id AS invoiceId,
    invoiceNumber,
    invoiceDate,
    grandTotal,
    totalTaxableAmount,
    paymentStatus,
    status
FROM invoices
WHERE customerId = :customerId
ORDER BY invoiceDate DESC
"""
    )
    fun getInvoicesForCustomer(customerId: String): Flow<List<CustomerInvoiceRow>>


    @Query(
        """
SELECT 
    ii.productId AS productId,
    ii.productNameSnapshot AS productName,
    ii.hsnSnapshot AS hsn,
    ii.unitSnapshot AS unit,
    SUM(ii.quantity) AS totalQty,
    SUM(ii.taxableAmount) AS totalSales
FROM invoice_items ii
INNER JOIN invoices i ON ii.invoiceId = i.id
WHERE i.customerId = :customerId
AND i.status != 'CANCELLED'
GROUP BY ii.productId
ORDER BY totalQty DESC
"""
    )
    fun getProductsUsedByCustomer(customerId: String): Flow<List<CustomerProductUsage>>

    @Query(
        """
SELECT 
    ii.productId AS productId,
    ii.productNameSnapshot AS productName,
    SUM(ii.quantity) AS totalQty
FROM invoice_items ii
INNER JOIN invoices i ON ii.invoiceId = i.id
WHERE i.customerId = :customerId
AND i.status != 'CANCELLED'
GROUP BY ii.productId
ORDER BY totalQty DESC
LIMIT 3
"""
    )
    fun getTopProductsForCustomer(customerId: String): Flow<List<CustomerTopProduct>>

    @Query(
        """
SELECT
    COUNT(id) AS totalInvoices,
    COALESCE(SUM(grandTotal),0) AS totalSales,
    COALESCE(SUM(totalTaxableAmount),0) AS totalTaxable,
    COALESCE(AVG(grandTotal),0) AS avgInvoiceValue
FROM invoices
WHERE customerId = :customerId
AND status != 'CANCELLED'
"""
    )
    fun getCustomerBusinessStats(customerId: String): Flow<CustomerBusinessStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTransaction(payment: PaymentTransactionEntity)

    @Transaction
    suspend fun upsertInvoiceWithItemsAndPayment(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        payment: PaymentTransactionEntity? // Nullable!
    ) {
        insertInvoice(invoice)
        deleteItemsByInvoiceId(invoice.id)
        insertInvoiceItems(items)

        // If a payment was made during this save, insert it
        if (payment != null) {
            insertPaymentTransaction(payment)
        }
    }


    @Query(
        """
SELECT 
    i.id AS invoiceId,
    i.invoiceNumber,
    i.invoiceDate,
    p.businessName AS customerName,
    ii.quantity,
    ii.sellingPriceAtTime AS sellingPrice,
    ii.taxableAmount,
    i.status AS invoiceStatus
FROM invoice_items ii
INNER JOIN invoices i ON i.id = ii.invoiceId
LEFT JOIN party p ON p.id = i.customerId
WHERE ii.productId = :productId
"""
    )
    suspend fun getInvoicesForProductSync(productId: String): List<ProductInvoiceUsage>


    @Query(
        """
    UPDATE invoice_items 
    SET costPriceAtTime = :actualCostPrice, 
        linkedPurchaseItemId = :purchaseItemId 
    WHERE productId = :productId 
      AND (linkedPurchaseItemId IS NULL OR costPriceAtTime = 0.0)
"""
    )
    suspend fun applyRetroactiveCostToUnlinkedItems(
        productId: String,
        actualCostPrice: Double,
        purchaseItemId: String
    )

    @Query(
        """
    SELECT SUM(taxableAmount) - SUM(quantity * costPriceAtTime) 
    FROM invoice_items ii
    INNER JOIN invoices i ON ii.invoiceId = i.id
    WHERE i.status = 'ACTIVE'
"""
    )
    fun getTotalBusinessProfitFlow(): Flow<Double?>

    // Payments related
    // 1. Insert the standalone payment - already have


    // 2. Quick fetches for calculation
    @Query("SELECT grandTotal FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceGrandTotal(invoiceId: String): Double

    @Query("SELECT customerId FROM invoices WHERE id = :invoiceId")
    suspend fun getCustomerIdForInvoice(invoiceId: String): String?

    @Query("SELECT SUM(amount) FROM payment_transactions WHERE documentId = :invoiceId AND type = 'CREDIT'")
    suspend fun getTotalPaidForInvoice(invoiceId: String): Double?

    // 3. Update the status and bump the updatedAt timestamp
    @Query("UPDATE invoices SET paymentStatus = :status, updatedAt = :timestamp WHERE id = :invoiceId")
    suspend fun updateInvoicePaymentStatus(invoiceId: String, status: PaymentStatus, timestamp: Long)

    // 4. THE TRANSACTION: Wraps it all together safely
    @Transaction
    suspend fun recordStandalonePayment(payment: PaymentTransactionEntity) {
        insertPaymentTransaction(payment)

        val invoiceId = payment.documentId ?: return

        val grandTotal = getInvoiceGrandTotal(invoiceId)
        val totalPaid = getTotalPaidForInvoice(invoiceId) ?: 0.0

        val newStatus = when {
            totalPaid <= 0.0 -> PaymentStatus.UNPAID
            totalPaid >= grandTotal -> PaymentStatus.PAID
            else -> PaymentStatus.PARTIAL
        }

        updateInvoicePaymentStatus(invoiceId, newStatus, System.currentTimeMillis())
    }


    // --- EXPORT ---
    @Query("SELECT * FROM invoices")
    suspend fun exportAllInvoices(): List<InvoiceEntity>

    @Query("SELECT * FROM invoice_items")
    suspend fun exportAllItems(): List<InvoiceItemEntity>

    @Query("SELECT * FROM payment_transactions")
    suspend fun exportAllPayments(): List<PaymentTransactionEntity>

    // --- RESTORE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreInvoices(invoices: List<InvoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreItems(items: List<InvoiceItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restorePayments(payments: List<PaymentTransactionEntity>)
}