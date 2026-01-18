package com.example.axiom.data.finances.domain

data class Product(
    val id: String = "",
    val name: String = "",
    val hsn: String = "",
    val sellingPrice: Double = 0.0,
    val unit: String = "",
    val category: String = "",
    val active: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null
)

data class SupplierFirm(
    val id: String = "",
    val name: String = "",
    val gstin: String? = null,
    val address: String = "",
    val contactNumber: String? = null,
    val email: String? = null,
    val stateCode: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isActive: Boolean = true
)

data class PurchasedItem(
    val id: String = "",
    val productId: String? = null,
    val name: String = "",
    val hsn: String? = null,
    val unit: String = "",
    val quantity: Double = 0.0,
    val costPrice: Double = 0.0,
    val total: Double = 0.0
)

data class PurchaseRecord(
    val id: String = "",
    val supplierId: String = "",
    val purchaseDate: Long = 0L,
    val items: List<PurchasedItem> = emptyList(),
    val remarks: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null
)

data class SellerFirm(
    val id: String = "",
    val stateCode: String = "",
    val name: String = "",
    val gstin: String = "",
    val address: String = "",
    val contactNumber: String = "",
    val email: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val isActive: Boolean = true
)

data class CustomerFirm(
    val id: String = "",
    val name: String = "",
    val gstin: String? = null,
    val address: String = "",
    val contactNumber: String? = null,
    val email: String? = null,
    val stateCode: String? = null,
    val image: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val active: Boolean = true
)

enum class SupplyType { INTRA_STATE, INTER_STATE }
enum class InvoiceStatus { DRAFT, FINAL, CANCELLED }

data class InvoiceItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val unit: String = "",
    val price: Double = 0.0,
    val quantity: Double = 0.0,
    val hsn: String = "",
    val total: Double = 0.0
)

data class GstBreakdown(
    val cgstRate: Double = 0.0,
    val sgstRate: Double = 0.0,
    val igstRate: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalTax: Double = 0.0
)


data class Invoice(
    val id: String = "",
    val invoiceNo: String = "",
    val date: String = "",
    val sellerId: String = "",
    val customerDetails: CustomerFirm? = null,
    val supplyType: SupplyType = SupplyType.INTRA_STATE,
    val vehicleNumber: String? = null,
    val shippedTo: String? = null,
    val items: List<InvoiceItem> = emptyList(),
    val totalBeforeTax: Double = 0.0,
    val gst: GstBreakdown = GstBreakdown(),
    val shippingCharge: Double? = 0.0,
    val totalAmount: Double = 0.0,
    val amountInWords: String = "",
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val createdAt: Long = 0L,
    val updatedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancelReason: String? = null,
    val deleted: Boolean = false,
    val deletedAt: Long? = null,
    val version: Int = 1
)