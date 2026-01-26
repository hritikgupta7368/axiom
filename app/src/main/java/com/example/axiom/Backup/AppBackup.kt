package com.example.axiom.Backup

import com.example.axiom.data.finances.CustomerFirm
import com.example.axiom.data.finances.CustomerFirmEntity
import com.example.axiom.data.finances.GstBreakdown
import com.example.axiom.data.finances.InvoiceEntity
import com.example.axiom.data.finances.InvoiceItem
import com.example.axiom.data.finances.InvoiceStatus
import com.example.axiom.data.finances.ProductEntity
import com.example.axiom.data.finances.PurchaseRecordEntity
import com.example.axiom.data.finances.PurchasedItem
import com.example.axiom.data.finances.SellerFirmEntity
import com.example.axiom.data.finances.SupplierFirmEntity
import com.example.axiom.data.finances.SupplyType
import com.example.axiom.data.notes.NoteEntity
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.Priority
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.temp.TaskStatus
import com.example.axiom.data.vault.VaultEntryEntity
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

    // Finances
    val products: List<ProductBackup>,
    val customers: List<CustomerFirmBackup>,
    val sellers: List<SellerFirmBackup>,
    val suppliers: List<SupplierFirmBackup>,
    val purchases: List<PurchaseRecordBackup>,
    val invoices: List<InvoiceBackup>
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

//Finances
fun ProductEntity.toBackup(): ProductBackup = ProductBackup(
    id, name, hsn, sellingPrice, unit,
    category, active, createdAt, updatedAt
)

fun ProductBackup.toEntity(): ProductEntity = ProductEntity(
    id, name, hsn, sellingPrice, unit,
    category, active, createdAt, updatedAt
)

// Standalone mapper for the DAO
fun CustomerFirmEntity.toBackup(): CustomerFirmBackup =
    CustomerFirmBackup(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, active = active, image = image
    )

// CustomerFirm
fun CustomerFirm.toBackup(): CustomerFirmBackup =
    CustomerFirmBackup(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, active = active, image = image
    )

fun CustomerFirmBackup.toModel(): CustomerFirm = // Changed name to toModel to avoid confusion
    CustomerFirm(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, active = active, image = image
    )

// SellerFirm
fun SellerFirmEntity.toBackup(): SellerFirmBackup =
    SellerFirmBackup(
        id = id, stateCode = stateCode, name = name, gstin = gstin,
        address = address, contactNumber = contactNumber, email = email,
        createdAt = createdAt, updatedAt = updatedAt, isActive = isActive
    )

fun SellerFirmBackup.toEntity(): SellerFirmEntity =
    SellerFirmEntity(
        id = id, stateCode = stateCode, name = name, gstin = gstin,
        address = address, contactNumber = contactNumber, email = email,
        createdAt = createdAt, updatedAt = updatedAt, isActive = isActive
    )

// PurchasedItem
fun PurchasedItem.toBackup(): PurchasedItemBackup =
    PurchasedItemBackup(
        id = id, productId = productId, name = name, hsn = hsn,
        unit = unit, quantity = quantity, costPrice = costPrice
    )

fun PurchasedItemBackup.toEntity(): PurchasedItem =
    PurchasedItem(
        id = id, productId = productId, name = name, hsn = hsn,
        unit = unit, quantity = quantity, costPrice = costPrice
    )

// PurchaseRecord
fun PurchaseRecordEntity.toBackup(): PurchaseRecordBackup =
    PurchaseRecordBackup(
        id = id, supplierId = supplierId, purchaseDate = purchaseDate,
        items = items.map { it.toBackup() },
        remarks = remarks, createdAt = createdAt, updatedAt = updatedAt
    )

fun PurchaseRecordBackup.toEntity(): PurchaseRecordEntity =
    PurchaseRecordEntity(
        id = id, supplierId = supplierId, purchaseDate = purchaseDate,
        items = items.map { it.toEntity() },
        remarks = remarks, createdAt = createdAt, updatedAt = updatedAt
    )


// --- SUPPLIER MAPPERS (Was missing from your candidate list) ---
fun SupplierFirmEntity.toBackup(): SupplierFirmBackup =
    SupplierFirmBackup(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, isActive = isActive
    )

fun SupplierFirmBackup.toEntity(): SupplierFirmEntity =
    SupplierFirmEntity(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, isActive = isActive
    )

// --- CUSTOMER ENTITY MAPPER (For Restore) ---
// Add this so backup.customers.map { it.toEntity() } works for the DAO
fun CustomerFirmBackup.toEntity(): CustomerFirmEntity =
    CustomerFirmEntity(
        id = id, name = name, gstin = gstin, address = address,
        contactNumber = contactNumber, email = email, stateCode = stateCode,
        createdAt = createdAt, updatedAt = updatedAt, active = active, image = image
    )

// InvoiceItem
fun InvoiceItem.toBackup(): InvoiceItemBackup =
    InvoiceItemBackup(
        id = id, productId = productId, name = name, unit = unit,
        price = price, quantity = quantity, hsn = hsn, total = total
    )

fun InvoiceItemBackup.toEntity(): InvoiceItem =
    InvoiceItem(
        id = id, productId = productId, name = name, unit = unit,
        price = price, quantity = quantity, hsn = hsn, total = total
    )

// Gst Breakdown
fun GstBreakdown.toBackup(): GstBreakdownBackup =
    GstBreakdownBackup(
        cgstRate, sgstRate, igstRate, cgstAmount, sgstAmount, igstAmount, totalTax
    )

fun GstBreakdownBackup.toEntity(): GstBreakdown =
    GstBreakdown(
        cgstRate, sgstRate, igstRate, cgstAmount, sgstAmount, igstAmount, totalTax
    )

// Invoice (Renamed from InvoiceEntity to Invoice)
fun InvoiceEntity.toBackup(): InvoiceBackup =
    InvoiceBackup(
        id = id,
        invoiceNo = invoiceNo,
        date = date,
        sellerId = sellerId,
        customerDetails = customerDetails?.toBackup(), // Uses CustomerFirm mapper
        supplyType = supplyType.name, // Enum mapping
        vehicleNumber = vehicleNumber,
        shippedTo = shippedTo,
        items = items.map { it.toBackup() },
        totalBeforeTax = totalBeforeTax,
        gst = gst.toBackup(),
        shippingCharge = shippingCharge,
        totalAmount = totalAmount,
        amountInWords = amountInWords,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        cancelledAt = cancelledAt,
        cancelReason = cancelReason,
        deleted = deleted,
        deletedAt = deletedAt,
        version = version
    )

fun InvoiceBackup.toEntity(): InvoiceEntity =
    InvoiceEntity(
        id = id,
        invoiceNo = invoiceNo,
        date = date,
        sellerId = sellerId,
        customerDetails = customerDetails?.toModel(),
        supplyType = SupplyType.valueOf(supplyType),
        vehicleNumber = vehicleNumber,
        shippedTo = shippedTo,
        items = items.map { it.toEntity() },
        totalBeforeTax = totalBeforeTax,
        gst = gst.toEntity(),
        shippingCharge = shippingCharge,
        totalAmount = totalAmount,
        amountInWords = amountInWords,
        status = InvoiceStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        cancelledAt = cancelledAt,
        cancelReason = cancelReason,
        deleted = deleted,
        deletedAt = deletedAt,
        version = version
    )