package com.example.axiom.data.finances

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.axiom.DB.AppDatabase
import com.example.axiom.ui.utils.IdGenerator
import com.example.axiom.ui.utils.InvoicePdfRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


enum class SupplyType { INTRA_STATE, INTER_STATE }
enum class InvoiceStatus { DRAFT, FINAL, CANCELLED }

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
    val vehicleNumber: String? = "",
    val shippedTo: String? = "",
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
    val cancelReason: String? = "",
    val deleted: Boolean = false,
    val deletedAt: Long? = null,
    val version: Int = 1
)

/* ---------- MAPPERS ---------- */
fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    hsn = hsn,
    sellingPrice = sellingPrice,
    unit = unit,
    category = category,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    hsn = hsn,
    sellingPrice = sellingPrice,
    unit = unit,
    category = category,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SupplierFirmEntity.toDomain() = SupplierFirm(
    id = id,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    stateCode = stateCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)

fun SupplierFirm.toEntity() = SupplierFirmEntity(
    id = id,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    stateCode = stateCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)

fun PurchaseRecordEntity.toDomain() = PurchaseRecord(
    id = id,
    supplierId = supplierId,
    purchaseDate = purchaseDate,
    items = items,
    remarks = remarks,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PurchaseRecord.toEntity() = PurchaseRecordEntity(
    id = id,
    supplierId = supplierId,
    purchaseDate = purchaseDate,
    items = items,
    remarks = remarks,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CustomerFirmEntity.toDomain() = CustomerFirm(
    id = id,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    stateCode = stateCode,
    image = image,
    createdAt = createdAt,
    updatedAt = updatedAt,
    active = active
)

fun CustomerFirm.toEntity() = CustomerFirmEntity(
    id = id,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    stateCode = stateCode,
    image = image,
    createdAt = createdAt,
    updatedAt = updatedAt,
    active = active
)

fun SellerFirmEntity.toDomain() = SellerFirm(
    id = id,
    stateCode = stateCode,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive

)

fun SellerFirm.toEntity() = SellerFirmEntity(
    id = id,
    stateCode = stateCode,
    name = name,
    gstin = gstin,
    address = address,
    contactNumber = contactNumber,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive

)

fun InvoiceEntity.toDomain() = Invoice(
    id = id,
    invoiceNo = invoiceNo,
    date = date,
    sellerId = sellerId,
    customerDetails = customerDetails,
    supplyType = supplyType,
    vehicleNumber = vehicleNumber,
    shippedTo = shippedTo,
    items = items,
    totalBeforeTax = totalBeforeTax,
    gst = gst,
    shippingCharge = shippingCharge,
    totalAmount = totalAmount,
    amountInWords = amountInWords,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cancelledAt = cancelledAt,
    cancelReason = cancelReason,
    deleted = deleted,
    deletedAt = deletedAt,
    version = version
)

fun Invoice.toEntity() = InvoiceEntity(
    id = id,
    invoiceNo = invoiceNo,
    date = date,
    sellerId = sellerId,
    customerDetails = customerDetails,
    supplyType = supplyType,
    vehicleNumber = vehicleNumber,
    shippedTo = shippedTo,
    items = items,
    totalBeforeTax = totalBeforeTax,
    gst = gst,
    shippingCharge = shippingCharge,
    totalAmount = totalAmount,
    amountInWords = amountInWords,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cancelledAt = cancelledAt,
    cancelReason = cancelReason,
    deleted = deleted,
    deletedAt = deletedAt,
    version = version
)

/* ---------- TYPE CONVERTERS ---------- */
class FinanceTypeConverters {

    @TypeConverter
    fun fromPurchasedItemList(items: List<PurchasedItem>?): String {
        if (items == null) return ""
        return items.joinToString(";;;") { item ->
            "${item.id}|||${item.productId ?: ""}|||${item.name}|||${item.hsn ?: ""}|||${item.unit}|||${item.quantity}|||${item.costPrice}|||${item.total}"
        }
    }

    @TypeConverter
    fun toPurchasedItemList(value: String): List<PurchasedItem> {
        if (value.isBlank()) return emptyList()
        return value.split(";;;").mapNotNull { itemStr ->
            val parts = itemStr.split("|||")
            if (parts.size == 8) {
                PurchasedItem(
                    id = parts[0],
                    productId = parts[1].ifBlank { null },
                    name = parts[2],
                    hsn = parts[3].ifBlank { null },
                    unit = parts[4],
                    quantity = parts[5].toDoubleOrNull() ?: 0.0,
                    costPrice = parts[6].toDoubleOrNull() ?: 0.0,
                    total = parts[7].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    @TypeConverter
    fun fromInvoiceItemList(items: List<InvoiceItem>?): String {
        if (items == null) return ""
        return items.joinToString(";;;") { item ->
            "${item.id}|||${item.productId}|||${item.name}|||${item.unit}|||${item.price}|||${item.quantity}|||${item.hsn}|||${item.total}"
        }
    }

    @TypeConverter
    fun toInvoiceItemList(value: String): List<InvoiceItem> {
        if (value.isBlank()) return emptyList()
        return value.split(";;;").mapNotNull { itemStr ->
            val parts = itemStr.split("|||")
            if (parts.size == 8) {
                InvoiceItem(
                    id = parts[0],
                    productId = parts[1],
                    name = parts[2],
                    unit = parts[3],
                    price = parts[4].toDoubleOrNull() ?: 0.0,
                    quantity = parts[5].toDoubleOrNull() ?: 0.0,
                    hsn = parts[6],
                    total = parts[7].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    @TypeConverter
    fun fromGstBreakdown(gst: GstBreakdown?): String {
        if (gst == null) return ""
        return "${gst.cgstRate}|||${gst.sgstRate}|||${gst.igstRate}|||${gst.cgstAmount}|||${gst.sgstAmount}|||${gst.igstAmount}|||${gst.totalTax}"
    }

    @TypeConverter
    fun toGstBreakdown(value: String): GstBreakdown {
        if (value.isBlank()) return GstBreakdown()
        val parts = value.split("|||")
        return if (parts.size == 7) {
            GstBreakdown(
                cgstRate = parts[0].toDoubleOrNull() ?: 0.0,
                sgstRate = parts[1].toDoubleOrNull() ?: 0.0,
                igstRate = parts[2].toDoubleOrNull() ?: 0.0,
                cgstAmount = parts[3].toDoubleOrNull() ?: 0.0,
                sgstAmount = parts[4].toDoubleOrNull() ?: 0.0,
                igstAmount = parts[5].toDoubleOrNull() ?: 0.0,
                totalTax = parts[6].toDoubleOrNull() ?: 0.0
            )
        } else GstBreakdown()
    }

    @TypeConverter
    fun fromCustomerFirm(customer: CustomerFirm?): String {
        if (customer == null) return ""
        return "${customer.id}|||${customer.name}|||${customer.gstin ?: ""}|||${customer.address}|||${customer.contactNumber ?: ""}|||${customer.email ?: ""}|||${customer.stateCode ?: ""}|||${customer.image ?: ""}|||${customer.createdAt}|||${customer.updatedAt ?: ""}|||${customer.active}"
    }

    @TypeConverter
    fun toCustomerFirm(value: String): CustomerFirm? {
        if (value.isBlank()) return null
        val parts = value.split("|||")
        return if (parts.size == 11) {
            CustomerFirm(
                id = parts[0],
                name = parts[1],
                gstin = parts[2].ifBlank { null },
                address = parts[3],
                contactNumber = parts[4].ifBlank { null },
                email = parts[5].ifBlank { null },
                stateCode = parts[6].ifBlank { null },
                image = parts[7].ifBlank { null },
                createdAt = parts[8].toLongOrNull() ?: 0L,
                updatedAt = parts[9].toLongOrNull(),
                active = parts[10].toBoolean()
            )
        } else null
    }

    @TypeConverter
    fun fromSupplyType(type: SupplyType): String = type.name

    @TypeConverter
    fun toSupplyType(value: String): SupplyType = SupplyType.valueOf(value)

    @TypeConverter
    fun fromInvoiceStatus(status: InvoiceStatus): String = status.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus = InvoiceStatus.valueOf(value)
}

/* ---------- ENTITIES ---------- */

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["active"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val hsn: String,
    val sellingPrice: Double,
    val unit: String,
    val category: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long?
)

@Entity(
    tableName = "supplier_firms",
    indices = [
        Index(value = ["name"]),
        Index(value = ["isActive"])
    ]
)
data class SupplierFirmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gstin: String?,
    val address: String,
    val contactNumber: String?,
    val email: String?,
    val stateCode: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val isActive: Boolean
)

@Entity(
    tableName = "purchase_records",
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["purchaseDate"])
    ]
)
@TypeConverters(FinanceTypeConverters::class)
data class PurchaseRecordEntity(
    @PrimaryKey val id: String,
    val supplierId: String,
    val purchaseDate: Long,
    val items: List<PurchasedItem>,
    val remarks: String?,
    val createdAt: Long,
    val updatedAt: Long?
)

@Entity(
    tableName = "seller_firms",
    indices = [
        Index(value = ["name"]),
        Index(value = ["isActive"])
    ]
)
data class SellerFirmEntity(
    @PrimaryKey val id: String,
    val stateCode: String,
    val name: String,
    val gstin: String,
    val address: String,
    val contactNumber: String,
    val email: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val isActive: Boolean
)

@Entity(
    tableName = "customer_firms",
    indices = [
        Index(value = ["name"]),
        Index(value = ["active"])
    ]
)
data class CustomerFirmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gstin: String?,
    val address: String,
    val contactNumber: String?,
    val email: String?,
    val stateCode: String?,
    val image: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val active: Boolean
)

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNo"], unique = true),
        Index(value = ["sellerId"]),
        Index(value = ["status"]),
        Index(value = ["deleted"]),
        Index(value = ["date"])
    ]
)
@TypeConverters(FinanceTypeConverters::class)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNo: String,
    val date: String,
    val sellerId: String,
    val customerDetails: CustomerFirm?,
    val supplyType: SupplyType,
    val vehicleNumber: String?,
    val shippedTo: String?,
    val items: List<InvoiceItem>,
    val totalBeforeTax: Double,
    val gst: GstBreakdown,
    val shippingCharge: Double?,
    val totalAmount: Double,
    val amountInWords: String,
    val status: InvoiceStatus,
    val createdAt: Long,
    val updatedAt: Long?,
    val cancelledAt: Long?,
    val cancelReason: String?,
    val deleted: Boolean,
    val deletedAt: Long?,
    val version: Int
)

/* ---------- DAOs ---------- */

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category AND active = 1 ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM products")
    suspend fun exportAll(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<ProductEntity>)
}

@Dao
interface SupplierFirmDao {
    @Query("SELECT * FROM supplier_firms WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<SupplierFirmEntity>>

    @Query("SELECT * FROM supplier_firms ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<SupplierFirmEntity>>

    @Query("SELECT * FROM supplier_firms WHERE id = :id")
    suspend fun getById(id: String): SupplierFirmEntity?

    @Query("SELECT * FROM supplier_firms WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<SupplierFirmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierFirmEntity)

    @Update
    suspend fun update(supplier: SupplierFirmEntity)

    @Query("DELETE FROM supplier_firms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM supplier_firms")
    suspend fun exportAll(): List<SupplierFirmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<SupplierFirmEntity>)
}

@Dao
interface PurchaseRecordDao {
    @Query("SELECT * FROM purchase_records ORDER BY purchaseDate DESC")
    fun getAll(): Flow<List<PurchaseRecordEntity>>

    @Query("SELECT * FROM purchase_records WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getBySupplier(supplierId: String): Flow<List<PurchaseRecordEntity>>

    @Query("SELECT * FROM purchase_records WHERE id = :id")
    suspend fun getById(id: String): PurchaseRecordEntity?

    @Query("SELECT * FROM purchase_records WHERE purchaseDate BETWEEN :startDate AND :endDate ORDER BY purchaseDate DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<PurchaseRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseRecordEntity)

    @Update
    suspend fun update(purchase: PurchaseRecordEntity)

    @Query("DELETE FROM purchase_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM purchase_records")
    suspend fun exportAll(): List<PurchaseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<PurchaseRecordEntity>)
}

@Dao
interface SellerFirmDao {
    @Query("SELECT * FROM seller_firms WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<SellerFirmEntity>>

    @Query("SELECT * FROM seller_firms ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<SellerFirmEntity>>

    @Query("SELECT * FROM seller_firms WHERE id = :id")
    suspend fun getById(id: String): SellerFirmEntity?

    @Query("SELECT * FROM seller_firms WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<SellerFirmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seller: SellerFirmEntity)

    @Update
    suspend fun update(seller: SellerFirmEntity)


    @Query("DELETE FROM seller_firms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM seller_firms")
    suspend fun exportAll(): List<SellerFirmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<SellerFirmEntity>)
}

@Dao
interface CustomerFirmDao {
    @Query("SELECT * FROM customer_firms WHERE active = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<CustomerFirmEntity>>

    @Query("SELECT * FROM customer_firms ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<CustomerFirmEntity>>

    @Query("SELECT * FROM customer_firms WHERE id = :id")
    suspend fun getById(id: String): CustomerFirmEntity?

    @Query("SELECT * FROM customer_firms WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<CustomerFirmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerFirmEntity)

    @Update
    suspend fun update(customer: CustomerFirmEntity)

    @Query("DELETE FROM customer_firms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM customer_firms")
    suspend fun exportAll(): List<CustomerFirmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<CustomerFirmEntity>)

}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE deleted = 0 ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE deleted = 0 AND status = :status ORDER BY date DESC")
    fun getByStatus(status: InvoiceStatus): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE invoiceNo = :invoiceNo")
    suspend fun getByInvoiceNo(invoiceNo: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE sellerId = :sellerId AND deleted = 0 ORDER BY date DESC")
    fun getBySeller(sellerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE deleted = 0 AND (invoiceNo LIKE '%' || :query || '%') ORDER BY date DESC")
    fun search(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE date BETWEEN :startDate AND :endDate AND deleted = 0 ORDER BY date DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)


    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM invoices")
    suspend fun exportAll(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<InvoiceEntity>)
}

/* ---------- REPOSITORIES ---------- */

class ProductRepository(
    private val dao: ProductDao
) {
    val allProducts: Flow<List<Product>> =
        dao.getAll().map { it.map(ProductEntity::toDomain) }

    val activeProducts: Flow<List<Product>> =
        dao.getAllActive().map { it.map(ProductEntity::toDomain) }

    fun search(query: String): Flow<List<Product>> =
        if (query.isBlank()) activeProducts
        else dao.search(query).map { it.map(ProductEntity::toDomain) }

    fun getByCategory(category: String): Flow<List<Product>> =
        dao.getByCategory(category).map { it.map(ProductEntity::toDomain) }

    suspend fun getById(id: String): Product? =
        dao.getById(id)?.toDomain()


    suspend fun insert(product: Product) {
        val entity = product
            .ensureId()
            .toEntity()

        dao.insert(entity)
    }

    suspend fun update(product: Product) =
        dao.update(product.toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)
}


class SupplierFirmRepository(
    private val dao: SupplierFirmDao
) {
    val suppliers: Flow<List<SupplierFirm>> =
        dao.getAll().map { it.map(SupplierFirmEntity::toDomain) }

    val activeSuppliers: Flow<List<SupplierFirm>> =
        dao.getAllActive().map { it.map(SupplierFirmEntity::toDomain) }

    fun search(query: String): Flow<List<SupplierFirm>> =
        if (query.isBlank()) suppliers
        else dao.search(query).map { it.map(SupplierFirmEntity::toDomain) }

    suspend fun getById(id: String): SupplierFirm? =
        dao.getById(id)?.toDomain()


    suspend fun insert(supplier: SupplierFirm) {
        val entity = supplier
            .ensureId()
            .toEntity()

        dao.insert(entity)
    }


    suspend fun update(supplier: SupplierFirm) =
        dao.update(supplier.toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)
}


class PurchaseRecordRepository(
    private val dao: PurchaseRecordDao
) {
    val purchases: Flow<List<PurchaseRecord>> =
        dao.getAll().map { it.map(PurchaseRecordEntity::toDomain) }

    fun getBySupplier(supplierId: String): Flow<List<PurchaseRecord>> =
        dao.getBySupplier(supplierId)
            .map { it.map(PurchaseRecordEntity::toDomain) }

    fun getByDateRange(start: Long, end: Long): Flow<List<PurchaseRecord>> =
        dao.getByDateRange(start, end)
            .map { it.map(PurchaseRecordEntity::toDomain) }

    suspend fun getById(id: String): PurchaseRecord? =
        dao.getById(id)?.toDomain()


    suspend fun insert(record: PurchaseRecord) {
        val entity = record
            .ensureId()
            .toEntity()

        dao.insert(entity)
    }

    suspend fun update(record: PurchaseRecord) =
        dao.update(record.toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)
}

class SellerFirmRepository(
    private val dao: SellerFirmDao,
) {
    val sellers: Flow<List<SellerFirm>> =
        dao.getAll().map { it.map(SellerFirmEntity::toDomain) }

//    val selectedSeller = prefs.selectedSeller

    fun search(query: String): Flow<List<SellerFirm>> =
        if (query.isBlank()) sellers
        else dao.search(query).map { it.map(SellerFirmEntity::toDomain) }

    suspend fun getById(id: String): SellerFirm? = dao.getById(id)?.toDomain()

    suspend fun insert(seller: SellerFirm) {
        val entity = seller
            .ensureId()
            .toEntity()
        dao.insert(entity)
    }

    suspend fun update(seller: SellerFirm) = dao.update(seller.toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)

//    suspend fun selectSeller(id: String, name: String, stateCode: String) =
//        prefs.saveSelectedSellerFirm(id, name, stateCode)

//    suspend fun clearSelection() = prefs.clearSelectedSellerFirm()
}

class CustomerFirmRepository(
    private val dao: CustomerFirmDao
) {
    val customers: Flow<List<CustomerFirm>> =
        dao.getAll().map { it.map(CustomerFirmEntity::toDomain) }

    val activeCustomers: Flow<List<CustomerFirm>> =
        dao.getAllActive().map { it.map(CustomerFirmEntity::toDomain) }

    fun search(query: String): Flow<List<CustomerFirm>> =
        if (query.isBlank()) customers
        else dao.search(query).map { it.map(CustomerFirmEntity::toDomain) }

    suspend fun getById(id: String): CustomerFirm? =
        dao.getById(id)?.toDomain()

    suspend fun insert(customer: CustomerFirm) {
        val entity = customer
            .ensureId()
            .toEntity()

        dao.insert(entity)
    }

    suspend fun update(customer: CustomerFirm) =
        dao.update(customer.toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)

}

class InvoiceRepository(
    private val dao: InvoiceDao,
//    private val prefs: FinancePreferences
) {
    val invoices: Flow<List<Invoice>> =
        dao.getAll().map { it.map(InvoiceEntity::toDomain) }

//    val lastInvoiceNumber: StateFlow<Long> =
//        prefs.lastInvoiceNumber

    fun search(query: String): Flow<List<Invoice>> =
        if (query.isBlank()) invoices
        else dao.search(query).map { it.map(InvoiceEntity::toDomain) }

    fun getByStatus(status: InvoiceStatus): Flow<List<Invoice>> =
        dao.getByStatus(status).map { it.map(InvoiceEntity::toDomain) }

    fun getBySeller(sellerId: String): Flow<List<Invoice>> =
        dao.getBySeller(sellerId).map { it.map(InvoiceEntity::toDomain) }

    fun getByDateRange(start: String, end: String): Flow<List<Invoice>> =
        dao.getByDateRange(start, end).map { it.map(InvoiceEntity::toDomain) }

    suspend fun getById(id: String): Invoice? =
        dao.getById(id)?.toDomain()

    suspend fun getByInvoiceNo(no: String): Invoice? =
        dao.getByInvoiceNo(no)?.toDomain()


    suspend fun insert(invoice: Invoice) {
        val entity = invoice
            .ensureId()
            .toEntity()

        dao.insert(entity)
    }


    // In CustomerFirmRepository
    suspend fun update(invoice: Invoice) {
        val updatedInvoice = invoice.copy(
            updatedAt = System.currentTimeMillis()
        )
        dao.update(updatedInvoice.toEntity())
    }


    suspend fun softDelete(invoice: Invoice) =
        dao.update(invoice.copy(deleted = true).toEntity())

    suspend fun deleteById(id: String) =
        dao.deleteById(id)

//    suspend fun getAndIncrementInvoiceNumber(): Long =
//        prefs.getAndIncrementInvoiceNumber()
//
//    suspend fun saveLastInvoiceNumber(value: Long) =
//        prefs.saveLastInvoiceNumber(value)
//
//    suspend fun nextInvoiceNumber(): Long =
//        prefs.getAndIncrementInvoiceNumber()
}

/* ---------- VIEWMODELS ---------- */

class ProductViewModel(
    private val repo: ProductRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeProducts: StateFlow<List<Product>> =
        repo.activeProducts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun getByCategory(category: String): Flow<List<Product>> =
        repo.getByCategory(category)


    fun insert(product: Product) =
        viewModelScope.launch { repo.insert(product) }

    fun update(product: Product) =
        viewModelScope.launch { repo.update(product) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }
}

class SupplierFirmViewModel(
    private val repo: SupplierFirmRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val suppliers: StateFlow<List<SupplierFirm>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    val activeSuppliers: StateFlow<List<SupplierFirm>> =
        repo.activeSuppliers
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )


    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun insert(supplier: SupplierFirm) =
        viewModelScope.launch { repo.insert(supplier) }

    fun update(supplier: SupplierFirm) =
        viewModelScope.launch { repo.update(supplier) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }
}

class PurchaseRecordViewModel(
    private val productRepo: ProductRepository,
    private val purchaseRepo: PurchaseRecordRepository,
    private val supplierRepo: SupplierFirmRepository
) : ViewModel() {


    private val productSearchQuery = MutableStateFlow("")
    private val supplierSearchQuery = MutableStateFlow("")
    private val purhcaseSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> =
        productSearchQuery
            .flatMapLatest { productRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val suppliers: StateFlow<List<SupplierFirm>> =
        supplierSearchQuery
            .flatMapLatest { supplierRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val purchases: StateFlow<List<PurchaseRecord>> =
        purchaseRepo.purchases
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getBySupplier(supplierId: String): Flow<List<PurchaseRecord>> =
        purchaseRepo.getBySupplier(supplierId)

    fun getByDateRange(start: Long, end: Long): Flow<List<PurchaseRecord>> =
        purchaseRepo.getByDateRange(start, end)

    fun insert(record: PurchaseRecord) =
        viewModelScope.launch { purchaseRepo.insert(record) }

    fun update(record: PurchaseRecord) =
        viewModelScope.launch { purchaseRepo.update(record) }

    fun insertProduct(product: Product) =
        viewModelScope.launch { productRepo.insert(product) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            purchaseRepo.deleteById(id)
        }
}

class SellerFirmViewModel(
    private val repo: SellerFirmRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val sellers: StateFlow<List<SellerFirm>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

//    val activeSellers = repo.activeSellers
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

//    val selectedSeller = repo.selectedSeller
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5_000),
//            SelectedSellerPref(null, null, null)
//        )

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun insert(seller: SellerFirm) {
        viewModelScope.launch { repo.insert(seller) }
    }

    fun update(seller: SellerFirm) {
        viewModelScope.launch { repo.update(seller) }
    }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }

//    fun selectSeller(id: String, name: String, stateCode: String) {
//        viewModelScope.launch { repo.selectSeller(id, name, stateCode) }
//    }
//
//    fun clearSelection() {
//        viewModelScope.launch { repo.clearSelection() }
//    }
}

class CustomerFirmViewModel(
    private val repo: CustomerFirmRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val customers: StateFlow<List<CustomerFirm>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeCustomers: StateFlow<List<CustomerFirm>> =
        repo.activeCustomers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun insert(customer: CustomerFirm) =
        viewModelScope.launch { repo.insert(customer) }

    fun update(customer: CustomerFirm) =
        viewModelScope.launch { repo.update(customer) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }

}


class InvoiceViewModel(
    application: Application,
    private val repo: InvoiceRepository
) : AndroidViewModel(application) {

    private val searchQuery = MutableStateFlow("")
    private val _invoiceById = MutableStateFlow<Invoice?>(null)
    val invoiceById: StateFlow<Invoice?> = _invoiceById


    //pdf methods
    private val pdfRepo = InvoicePdfRepository(getApplication())

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    val invoices: StateFlow<List<Invoice>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearch(query: String) {
        searchQuery.value = query
    }


//    val lastInvoiceNumber: StateFlow<Long> =
//        repo.lastInvoiceNumber
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)


    fun getInvoiceById(id: String) {
        viewModelScope.launch {
            _invoiceById.value = repo.getById(id)
        }
    }


    fun getByStatus(status: InvoiceStatus): Flow<List<Invoice>> =
        repo.getByStatus(status)

    fun getBySeller(sellerId: String): Flow<List<Invoice>> =
        repo.getBySeller(sellerId)

    fun getByDateRange(start: String, end: String): Flow<List<Invoice>> =
        repo.getByDateRange(start, end)

    fun insert(invoice: Invoice) =
        viewModelScope.launch { repo.insert(invoice) }

    fun update(invoice: Invoice) =
        viewModelScope.launch { repo.update(invoice) }


    fun deleteById(id: String) =
        viewModelScope.launch {
            repo.deleteById(id)
        }

//    suspend fun nextInvoiceNumber(): Long =
//        repo.nextInvoiceNumber()
//
//    suspend fun getAndIncrementInvoiceNumber(): Long =
//        repo.getAndIncrementInvoiceNumber()
//
//    fun saveLastInvoiceNumber(value: Long) {
//        viewModelScope.launch { repo.saveLastInvoiceNumber(value) }
//    }


    fun generatePdf(invoice: Invoice, logoUri: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _pdfUri.value = pdfRepo.generate(invoice, logoUri)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearPdf() {
        _pdfUri.value = null
    }

}

class CreateInvoiceViewModel(
    private val productRepo: ProductRepository,
    private val invoiceRepo: InvoiceRepository,
    private val customerRepo: CustomerFirmRepository
) : ViewModel() {

    private val productSearchQuery = MutableStateFlow("")
    private val customerSearchQuery = MutableStateFlow("")
    private val invoiceSearchQuery = MutableStateFlow("")

    private val _invoiceById = MutableStateFlow<Invoice?>(null)

    val invoiceById: StateFlow<Invoice?> = _invoiceById


    @OptIn(ExperimentalCoroutinesApi::class)
    val invoices: StateFlow<List<Invoice>> =
        invoiceSearchQuery
            .flatMapLatest { invoiceRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> =
        productSearchQuery
            .flatMapLatest { productRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<CustomerFirm>> =
        customerSearchQuery
            .flatMapLatest { customerRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    fun insertProduct(product: Product) =
        viewModelScope.launch { productRepo.insert(product) }

    fun deleteById(id: String) =
        viewModelScope.launch {
            productRepo.deleteById(id)
        }

    fun insertInvoice(invoice: Invoice) =
        viewModelScope.launch { invoiceRepo.insert(invoice) }

    fun updateInvoice(invoice: Invoice) =
        viewModelScope.launch { invoiceRepo.update(invoice) }

    fun getInvoiceById(id: String) {
        viewModelScope.launch {
            _invoiceById.value = invoiceRepo.getById(id)
        }
    }

    fun onProductSearch(query: String) {
        productSearchQuery.value = query
    }

    fun onCustomerSearch(query: String) {
        customerSearchQuery.value = query
    }

}

class BusinessAnalyticsViewModel(
    private val invoiceRepo: InvoiceRepository,
    private val purchaseRepo: PurchaseRecordRepository
) : ViewModel() {

    // ---------- RAW STREAMS ----------

    private val invoiceSearchQuery = MutableStateFlow("")


    @OptIn(ExperimentalCoroutinesApi::class)
    val invoices: StateFlow<List<Invoice>> =
        invoiceSearchQuery
            .flatMapLatest { invoiceRepo.search(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    val purchases: StateFlow<List<PurchaseRecord>> =
        purchaseRepo.purchases
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    // ---------- DERIVED METRICS ----------

    val totalSales: StateFlow<Double> =
        invoices
            .map { list ->
                list
                    .filter { it.status != InvoiceStatus.CANCELLED && !it.deleted }
                    .sumOf { it.totalAmount }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalPurchases: StateFlow<Double> =
        purchases
            .map { list ->
                list.sumOf { record ->
                    record.items.sumOf { it.total }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val invoiceCount: StateFlow<Int> =
        invoices
            .map { it.count { inv -> inv.status != InvoiceStatus.CANCELLED && !inv.deleted } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val purchaseCount: StateFlow<Int> =
        purchases
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val averageInvoiceValue: StateFlow<Double> =
        combine(totalSales, invoiceCount) { sales, count ->
            if (count == 0) 0.0 else sales / count
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val netCashFlow: StateFlow<Double> =
        combine(totalSales, totalPurchases) { sales, purchases ->
            sales - purchases
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ---------- MONTHLY (FOR DASHBOARD / GST SCREEN) ----------

    fun monthlySales(monthStart: Long, monthEnd: Long): Flow<Double> =
        invoices.map { list ->
            list
                .filter {
                    it.createdAt in monthStart..monthEnd &&
                            it.status != InvoiceStatus.CANCELLED &&
                            !it.deleted
                }
                .sumOf { it.totalAmount }
        }

    fun monthlyPurchases(monthStart: Long, monthEnd: Long): Flow<Double> =
        purchases.map { list ->
            list
                .filter { it.purchaseDate in monthStart..monthEnd }
                .sumOf { record -> record.items.sumOf { it.total } }
        }
}


/* ---------- VIEWMODEL FACTORIES ---------- */


class BusinessAnalyticsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessAnalyticsViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val purchaseRepo = PurchaseRecordRepository(db.purchaseRecordDao())
            val invoiceRepo = InvoiceRepository(db.invoiceDao())
            return BusinessAnalyticsViewModel(invoiceRepo, purchaseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}


class CreateInvoiceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateInvoiceViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val productRepo = ProductRepository(db.productDao())
            val customerRepo = CustomerFirmRepository(db.customerFirmDao())
            val invoiceRepo = InvoiceRepository(db.invoiceDao())
            return CreateInvoiceViewModel(productRepo, invoiceRepo, customerRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class ProductViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.productDao()
            val repo = ProductRepository(dao)
            return ProductViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class SupplierFirmViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupplierFirmViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.supplierFirmDao()
            val repo = SupplierFirmRepository(dao)
            return SupplierFirmViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class PurchaseRecordViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseRecordViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val productRepo = ProductRepository(db.productDao())
            val supplierRepo = SupplierFirmRepository(db.supplierFirmDao())
            val purchaseRepo = PurchaseRecordRepository(db.purchaseRecordDao())


            return PurchaseRecordViewModel(productRepo, purchaseRepo, supplierRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class SellerFirmViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerFirmViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.sellerFirmDao()
            val prefs = FinancePreferences(context.applicationContext)
            val repo = SellerFirmRepository(dao)
            return SellerFirmViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class CustomerFirmViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerFirmViewModel::class.java)) {
            val db = AppDatabase.get(context)
            val dao = db.customerFirmDao()
            val repo = CustomerFirmRepository(dao)
            return CustomerFirmViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class InvoiceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {

            val app = context.applicationContext as Application
            val db = AppDatabase.get(context)
            val dao = db.invoiceDao()
//            val prefs = FinancePreferences(context.applicationContext)
            val repo = InvoiceRepository(dao)

            return InvoiceViewModel(application = app, repo = repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

// util fxns
fun CustomerFirm.ensureId(): CustomerFirm {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

fun Product.ensureId(): Product {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

fun SellerFirm.ensureId(): SellerFirm {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

fun SupplierFirm.ensureId(): SupplierFirm {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

fun PurchaseRecord.ensureId(): PurchaseRecord {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

fun Invoice.ensureId(): Invoice {
    return if (id.isBlank()) {
        copy(id = IdGenerator.newId())
    } else {
        this
    }
}

