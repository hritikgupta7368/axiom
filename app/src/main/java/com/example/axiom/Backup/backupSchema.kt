// backupSchema.kt

package com.example.axiom.Backup

import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentMode
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentStatus
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.Invoice.components.TransactionType
import com.example.axiom.ui.screens.finances.customer.components.ContactType
import com.example.axiom.ui.screens.finances.customer.components.PartyType
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


// --- PARTIES ---
@Serializable
data class PartyBackup(  // used for seller , supplier and customer table
    val id: String,
    val partyType: PartyType,
    val businessName: String,
    val logoUrl: String?,
    val registrationType: String,
    val gstNumber: String?,
    val stateCode: String?,
    val address: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val isDeleted: Boolean,
    val billingAddress: String?,
    val defaultShippingAddress: String?,
    val creditLimit: Double,
    val openingBalance: Double,
    val bankName: String?,
    val branchName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
    val signatureUrl: String?,
    val stampUrl: String?
)

@Serializable
data class PartyContactBackup(
    val id: String,
    val partyId: String,
    val contactType: ContactType,
    val value: String,
    val isPrimary: Boolean
)


// --- PRODUCTS ---
@Serializable
data class ProductBackup(
    val id: String,
    val name: String,
    val description: String?,
    val hsn: String,
    val category: String,
    val brand: String?,
    val costPrice: Double,
    val sellingPrice: Double,
    val lastSellingPrice: Double,
    val peakPrice: Double,
    val floorPrice: Double,
    val unit: String,
    val imageUrl: String?,
    val productLink: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val isDeleted: Boolean
)

// --- INVOICES ---
@Serializable
data class InvoiceBackup(
    val id: String,
    val invoiceNumber: String,
    val customerId: String?,
    val sellerId: String?,
    val invoiceDate: Long,
    val vehicleNumber: String?,
    val shippedToAddress: String?,
    val placeOfSupplyCode: String,
    val supplyType: SupplyType,
    val eWayBillNumber: String?,
    val eWayBillDate: Long?,
    val itemSubTotal: Double,
    val deliveryCharge: Double,
    val extraCharges: Double,
    val globalDiscountAmount: Double,
    val totalTaxableAmount: Double,
    val globalGstRate: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val roundOff: Double,
    val grandTotal: Double,
    val amountInWords: String,
    val paymentStatus: PaymentStatus,
    val status: InvoiceStatus,
    val isEdited: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class InvoiceItemBackup(
    val id: String,
    val invoiceId: String,
    val productId: String,
    val linkedPurchaseItemId: String?,
    val productNameSnapshot: String,
    val hsnSnapshot: String,
    val unitSnapshot: String,
    val quantity: Double,
    val sellingPriceAtTime: Double,
    val itemDiscountAmount: Double,
    val taxableAmount: Double
)

// --- PURCHASES ---
@Serializable
data class PurchaseRecordBackup(
    val id: String,
    val customerId: String?,
    val supplierId: String?,
    val supplierInvoiceNumber: String,
    val purchaseDate: Long,
    val placeOfSupplyCode: String?,
    val reverseChargeApplicable: Boolean,
    val eWayBillNumber: String?,
    val eWayBillDate: Long?,
    val vehicleNumber: String?,
    val shippedToAddress: String?,
    val supplyType: SupplyType,
    val deliveryCharge: Double,
    val extraCharges: Double,
    val globalDiscountAmount: Double,
    val itemSubTotal: Double,
    val isEdited: Boolean,
    val totalTaxableAmount: Double,
    val globalGstRate: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val roundOff: Double,
    val grandTotal: Double,
    val isItcEligible: Boolean,
    val createdAt: Long,
    val updatedAt: Long?
)

@Serializable
data class PurchaseItemBackup(
    val id: String,
    val purchaseId: String,
    val productId: String,
    val productNameSnapshot: String,
    val hsnCode: String,
    val unit: String,
    val quantity: Double,
    val costPrice: Double,
    val taxableAmount: Double
)

// --- PAYMENTS ---
@Serializable
data class PaymentTransactionBackup(
    val id: String,
    val partyId: String,
    val documentId: String?,
    val type: TransactionType,
    val amount: Double,
    val paymentMode: PaymentMode,
    val transactionDate: Long,
    val referenceId: String?,
    val notes: String?
)

// Quotation




