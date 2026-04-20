package com.example.axiom.Backup


import com.example.axiom.data.notes.NoteEntity
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.Priority
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.temp.TaskStatus
import com.example.axiom.data.vault.VaultEntryEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceEntity
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceItemEntity
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentTransactionEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyContactEntity
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.product.components.ProductEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseItemEntity
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseRecordEntity
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val meta: BackupMeta,

    // Vault
    val vaultEntries: List<VaultEntryBackup>,

    // Calendar
    val tasks: List<TaskBackup>,
    val events: List<EventBackup>,

    // Notes
    val notes: List<NoteBackup>,

// Finances (UPDATED)
    val parties: List<PartyBackup>,
    val partyContacts: List<PartyContactBackup>,
    val products: List<ProductBackup>,
    val invoices: List<InvoiceBackup>,
    val invoiceItems: List<InvoiceItemBackup>,
    val purchases: List<PurchaseRecordBackup>,
    val purchaseItems: List<PurchaseItemBackup>,
    val payments: List<PaymentTransactionBackup>
)


@Serializable
data class BackupMeta(
    val appVersion: Int,
    val dbVersion: Int,
    val createdAt: Long
)


//Backup Mapper                 2. add mapper funxtions
fun VaultEntryEntity.toBackup(): VaultEntryBackup =
    VaultEntryBackup(
        id = id,
        serviceIcon = serviceIcon,
        serviceName = serviceName,
        username = username,
        password = password,
        note = note,
        expiryDate = expiryDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun VaultEntryBackup.toEntity(): VaultEntryEntity =
    VaultEntryEntity(
        id = id,
        serviceIcon = serviceIcon,
        serviceName = serviceName,
        username = username,
        password = password,
        note = note,
        expiryDate = expiryDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

//Calender
fun TaskEntity.toBackup(): TaskBackup =
    TaskBackup(
        id, title, note, date,
        startTime, endTime, allDay,
        status.name,
        priority.name,
        color,
        recurrenceRule,
        sortIndex,
        timeZone,
        createdAt,
        updatedAt
    )

fun TaskBackup.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        note = note,
        date = date,
        startTime = startTime,
        endTime = endTime,
        allDay = allDay,
        status = TaskStatus.valueOf(status),
        priority = Priority.valueOf(priority),
        color = color,
        recurrenceRule = recurrenceRule,
        sortIndex = sortIndex,
        timeZone = timeZone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun EventEntity.toBackup(): EventBackup =
    EventBackup(
        id, title, description,
        date, startTime, endTime,
        allDay, importance, pinned,
        color, timeZone,
        createdAt, updatedAt
    )

fun EventBackup.toEntity(): EventEntity =
    EventEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        allDay = allDay,
        importance = importance,
        pinned = pinned,
        color = color,
        timeZone = timeZone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

//Notes
fun NoteEntity.toBackup(): NoteBackup =
    NoteBackup(
        id,
        title,
        content,
        color,
        createdAt,
        updatedAt
    )

fun NoteBackup.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        content = content,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// new code
// --- PARTY MAPPERS ---
fun PartyEntity.toBackup() = PartyBackup(
    id, partyType, businessName, logoUrl, registrationType, gstNumber, stateCode, address,
    createdAt, updatedAt, isDeleted, billingAddress, defaultShippingAddress, creditLimit,
    openingBalance, bankName, branchName, accountNumber, ifscCode, signatureUrl, stampUrl
)

fun PartyBackup.toEntity() = PartyEntity(
    id, partyType, businessName, logoUrl, registrationType, gstNumber, stateCode, address,
    createdAt, updatedAt, isDeleted, billingAddress, defaultShippingAddress, creditLimit,
    openingBalance, bankName, branchName, accountNumber, ifscCode, signatureUrl, stampUrl
)

fun PartyContactEntity.toBackup() = PartyContactBackup(id, partyId, contactType, value, isPrimary)
fun PartyContactBackup.toEntity() = PartyContactEntity(id, partyId, contactType, value, isPrimary)

// --- PRODUCT MAPPERS ---
fun ProductEntity.toBackup() = ProductBackup(
    id, name, description, hsn, category, brand, costPrice, sellingPrice, lastSellingPrice,
    peakPrice, floorPrice, unit,
    imageUrl, productLink, createdAt, updatedAt, isDeleted
)

fun ProductBackup.toEntity() = ProductEntity(
    id, name, description, hsn, category, brand, costPrice, sellingPrice, lastSellingPrice,
    peakPrice, floorPrice, unit,
    imageUrl, productLink, createdAt, updatedAt, isDeleted
)

// --- INVOICE MAPPERS ---
fun InvoiceEntity.toBackup() = InvoiceBackup(
    id, invoiceNumber, customerId, sellerId, invoiceDate, vehicleNumber, shippedToAddress,
    placeOfSupplyCode, supplyType, eWayBillNumber, eWayBillDate, itemSubTotal, deliveryCharge,
    extraCharges, globalDiscountAmount, totalTaxableAmount, globalGstRate, cgstAmount,
    sgstAmount, igstAmount, roundOff, grandTotal, amountInWords, paymentStatus, status,
    isEdited, createdAt, updatedAt
)

fun InvoiceBackup.toEntity() = InvoiceEntity(
    id, invoiceNumber, customerId, sellerId, invoiceDate, vehicleNumber, shippedToAddress,
    placeOfSupplyCode, supplyType, eWayBillNumber, eWayBillDate, itemSubTotal, deliveryCharge,
    extraCharges, globalDiscountAmount, totalTaxableAmount, globalGstRate, cgstAmount,
    sgstAmount, igstAmount, roundOff, grandTotal, amountInWords, paymentStatus, status,
    isEdited, createdAt, updatedAt
)

fun InvoiceItemEntity.toBackup() = InvoiceItemBackup(
    id, invoiceId, productId, linkedPurchaseItemId, productNameSnapshot, hsnSnapshot,
    unitSnapshot, quantity, sellingPriceAtTime, itemDiscountAmount, taxableAmount
)

fun InvoiceItemBackup.toEntity() = InvoiceItemEntity(
    id, invoiceId, productId, linkedPurchaseItemId, productNameSnapshot, hsnSnapshot,
    unitSnapshot, quantity, sellingPriceAtTime, itemDiscountAmount, taxableAmount
)

// --- PURCHASE MAPPERS ---
fun PurchaseRecordEntity.toBackup() = PurchaseRecordBackup(
    id, customerId, supplierId, supplierInvoiceNumber, purchaseDate, placeOfSupplyCode,
    reverseChargeApplicable, eWayBillNumber, eWayBillDate, vehicleNumber, shippedToAddress,
    supplyType, deliveryCharge, extraCharges, globalDiscountAmount, itemSubTotal, isEdited,
    totalTaxableAmount, globalGstRate, cgstAmount, sgstAmount, igstAmount, roundOff, grandTotal,
    isItcEligible, createdAt, updatedAt
)

fun PurchaseRecordBackup.toEntity() = PurchaseRecordEntity(
    id, customerId, supplierId, supplierInvoiceNumber, purchaseDate, placeOfSupplyCode,
    reverseChargeApplicable, eWayBillNumber, eWayBillDate, vehicleNumber, shippedToAddress,
    supplyType, deliveryCharge, extraCharges, globalDiscountAmount, itemSubTotal, isEdited,
    totalTaxableAmount, globalGstRate, cgstAmount, sgstAmount, igstAmount, roundOff, grandTotal,
    isItcEligible, createdAt, updatedAt
)

fun PurchaseItemEntity.toBackup() = PurchaseItemBackup(
    id, purchaseId, productId, productNameSnapshot, hsnCode, unit, quantity, costPrice, taxableAmount
)

fun PurchaseItemBackup.toEntity() = PurchaseItemEntity(
    id, purchaseId, productId, productNameSnapshot, hsnCode, unit, quantity, costPrice, taxableAmount
)

// --- PAYMENT MAPPERS ---
fun PaymentTransactionEntity.toBackup() = PaymentTransactionBackup(
    id = id,
    partyId = partyId,
    documentId = documentId,
    type = type,
    amount = amount,
    paymentMode = paymentMode,
    transactionDate = transactionDate,
    referenceId = referenceId,
    notes = notes
)

fun PaymentTransactionBackup.toEntity() = PaymentTransactionEntity(
    id = id,
    partyId = partyId,
    documentId = documentId,
    type = type,
    amount = amount,
    paymentMode = paymentMode,
    transactionDate = transactionDate,
    referenceId = referenceId,
    notes = notes
)

