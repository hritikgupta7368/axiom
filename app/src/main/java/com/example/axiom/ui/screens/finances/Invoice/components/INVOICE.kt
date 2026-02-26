package com.example.axiom.ui.screens.finances.Invoice.components

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.product.components.ProductEntity

enum class SupplyType { INTRA_STATE, INTER_STATE }
enum class PaymentStatus { UNPAID, PARTIAL, PAID }
enum class InvoiceStatus { DRAFT, ACTIVE, CANCELLED }

// Granular Payment Modes
enum class PaymentMode {
    CASH, UPI, NEFT, RTGS, IMPS, CHEQUE, CREDIT_CARD, WALLET
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

    // Financials
    val itemSubTotal: Double = 0.0, // Sum of all item taxable amounts
    val deliveryCharge: Double = 0.0, // Taxable delivery charge
    val extraCharges: Double = 0.0,
    val globalDiscountAmount: Double = 0.0,

    // totalTaxableAmount = (itemSubTotal + deliveryCharge + extraCharges) - globalDiscountAmount
    val totalTaxableAmount: Double = 0.0,

    // GST Breakdown
    val globalGstRate: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,

    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val amountInWords: String = "",

    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val status: InvoiceStatus = InvoiceStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
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
    val linkedPurchaseItemId: String? = null,

    val productNameSnapshot: String = "",
    val hsnSnapshot: String = "",
    val quantity: Double = 0.0,
    val sellingPriceAtTime: Double = 0.0,
    val itemDiscountAmount: Double = 0.0,
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

@Dao
interface InvoiceDao {}