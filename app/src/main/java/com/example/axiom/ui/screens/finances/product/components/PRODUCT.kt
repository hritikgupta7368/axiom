package com.example.axiom.ui.screens.finances.product.components

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val description: String? = null,
    val hsn: String = "",
    val category: String = "",
    val brand: String? = null,

    //pricing
    val costPrice: Double = 0.0,            // Your purchase price
    val sellingPrice: Double = 0.0,         // Default/Current price
    val lastSellingPrice: Double = 0.0,     // Price from the very last invoice


    val peakPrice: Double = 0.0,        // Highest price ever sold
    val floorPrice: Double = 0.0,       // Lowest price ever sold


    val unit: String = "",
    val imageUrl: String? = null,
    val productLink: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long? = null,
    val isDeleted: Boolean = false
)

// for card list for basic info
data class ProductBasic(
    val id: String,
    val name: String,
    val sellingPrice: Double,
    val imageUrl: String? = null,
    val costPrice: Double,
    val hsn: String,
    val unit: String,
    val category: String
)

// for bottomsheet to see invoices by provinding product id
data class ProductInvoiceUsage(
    val invoiceId: String,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val customerName: String?,
    val quantity: Double,
    val sellingPrice: Double,
    val taxableAmount: Double,
    val invoiceStatus: InvoiceStatus
)

@Dao
interface ProductDao {


    // --- CREATE & UPDATE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    // --- READ ---
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT id, name, hsn, sellingPrice, imageUrl, unit, category , costPrice
        FROM products
        WHERE isDeleted = 0
        ORDER BY name
    """
    )
    fun getAllBasic(): Flow<List<ProductBasic>>

    @Query(
        """
        SELECT id, name, hsn, sellingPrice, imageUrl, unit, category , costPrice
        FROM products
        WHERE isDeleted = 0 AND 
        (name LIKE '%' || :query || '%' OR hsn LIKE '%' || :query || '%')
        ORDER BY name
    """
    )
    fun searchBasic(query: String): Flow<List<ProductBasic>>

    // searching
    @Query(
        """
    SELECT * FROM products
    WHERE isDeleted = 0 AND category = :category
    ORDER BY name
"""
    )
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE name = :name AND isDeleted = 0)")
    suspend fun existsByName(name: String): Boolean

    @Query(
        """
    SELECT DISTINCT category 
    FROM products
    WHERE isDeleted = 0 AND category != ''
    ORDER BY category ASC
"""
    )
    fun getAllCategories(): Flow<List<String>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    // --- DELETE ---
    // Soft Delete: Keeps history intact for old invoices
    @Query("UPDATE products SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE products SET isDeleted = 1 WHERE id IN (:ids)")
    suspend fun softDeleteAll(ids: List<String>)

    // Hard Delete: Use ONLY if the product was never used in any invoice
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun hardDelete(id: String)

    data class ProductProfitStats(
        val productId: String,
        val totalUnitsSold: Double,
        val totalRevenue: Double,
        val totalProfit: Double,
        val avgMarginPercent: Double
    )

    @Query(
        """
    SELECT 
        :productId AS productId,
        COALESCE(SUM(ii.quantity), 0.0) AS totalUnitsSold,
        COALESCE(SUM(ii.taxableAmount), 0.0) AS totalRevenue,
        COALESCE(SUM(ii.taxableAmount) - SUM(ii.quantity * ii.costPriceAtTime), 0.0) AS totalProfit,
        COALESCE(((SUM(ii.taxableAmount) - SUM(ii.quantity * ii.costPriceAtTime)) / NULLIF(SUM(ii.taxableAmount), 0)) * 100, 0.0) AS avgMarginPercent
    FROM invoice_items ii
    INNER JOIN invoices i ON ii.invoiceId = i.id
    WHERE ii.productId = :productId AND i.status = 'ACTIVE'
    """
    )
    fun getProductProfitStats(productId: String): Flow<ProductProfitStats?>


    // --- EXPORT ---
    @Query("SELECT * FROM products")
    suspend fun exportAll(): List<ProductEntity>

    // --- RESTORE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(products: List<ProductEntity>)
}