package com.example.axiom.ui.screens.finances.purchase.components

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.product.components.ProductEntity


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
    val supplierId: String = "",
    val supplierInvoiceNumber: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),

    val totalTaxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,

    val isItcEligible: Boolean = true
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
    val quantity: Double = 0.0,
    val costPrice: Double = 0.0,
    val taxableAmount: Double = 0.0
)

@Dao
interface PurchaseDao {}