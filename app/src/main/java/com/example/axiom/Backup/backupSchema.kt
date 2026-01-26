// backupSchema.kt

package com.example.axiom.Backup

import kotlinx.serialization.Serializable

@Serializable
data class VaultEntryBackup(
    val id: Long,
    val serviceIcon: String?,
    val serviceName: String,
    val username: String,
    val password: String,
    val note: String?,
    val expiryDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class TaskBackup(
    val id: Long,
    val title: String,
    val note: String?,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val status: String,
    val priority: String,
    val color: Int,
    val recurrenceRule: String?,
    val sortIndex: Int,
    val timeZone: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class EventBackup(
    val id: Long,
    val title: String,
    val description: String?,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val importance: Int,
    val pinned: Boolean,
    val color: Int,
    val timeZone: String,
    val createdAt: Long,
    val updatedAt: Long
)

//Notes
@Serializable
data class NoteBackup(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val color: Int,
    val createdAt: Long,
    val updatedAt: Long
)
// Finances - product , customer firm , seller firm , supplier firm , purchasing records , invoices


@Serializable
enum class SupplyType {
    INTRA_STATE,
    INTER_STATE
}

@Serializable
enum class InvoiceStatus {
    DRAFT,
    FINAL,
    CANCELLED
}

@Serializable
data class ProductBackup(
    val id: String,
    val name: String,
    val hsn: String,
    val sellingPrice: Double,
    val unit: String,
    val category: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long? = null
)

@Serializable
data class CustomerFirmBackup(
    val id: String,
    val name: String,
    val gstin: String? = null,
    val address: String,
    val contactNumber: String? = null,
    val email: String? = null,
    val stateCode: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val active: Boolean,
    val image: String? = null
)

@Serializable
data class SellerFirmBackup(
    val id: String,
    val stateCode: String,
    val name: String,
    val gstin: String,
    val address: String,
    val contactNumber: String,
    val email: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val isActive: Boolean = true
)

@Serializable
data class SupplierFirmBackup(
    val id: String,
    val name: String,
    val gstin: String?,
    val address: String,
    val contactNumber: String? = null,
    val email: String? = null,
    val stateCode: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val isActive: Boolean = true
)

@Serializable
data class PurchasedItemBackup(
    val id: String,
    val productId: String? = null,
    val name: String,
    val hsn: String? = null,
    val unit: String,
    val quantity: Double,
    val costPrice: Double
)

@Serializable
data class PurchaseRecordBackup(
    val id: String,
    val supplierId: String,
    val purchaseDate: Long,
    val items: List<PurchasedItemBackup>,
    val remarks: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null
)

@Serializable
data class InvoiceItemBackup(
    val id: String,
    val productId: String,
    val name: String,
    val unit: String,
    val price: Double,
    val quantity: Double,
    val hsn: String,
    val total: Double
)

@Serializable
data class GstBreakdownBackup(
    val cgstRate: Double,
    val sgstRate: Double,
    val igstRate: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalTax: Double
)

@Serializable
data class InvoiceBackup(
    val id: String,
    val invoiceNo: String,
    val date: String,
    val sellerId: String,
    val customerDetails: CustomerFirmBackup?,
    val supplyType: String,
    val vehicleNumber: String?,
    val shippedTo: String?,
    val items: List<InvoiceItemBackup> = emptyList(),
    val totalBeforeTax: Double,
    val gst: GstBreakdownBackup,
    val shippingCharge: Double?,
    val totalAmount: Double,
    val amountInWords: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val cancelledAt: Long?,
    val cancelReason: String?,
    val deleted: Boolean,
    val deletedAt: Long?,
    val version: Int
)



