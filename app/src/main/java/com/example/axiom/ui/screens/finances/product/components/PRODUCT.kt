package com.example.axiom.ui.screens.finances.product.components

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
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
    val costPrice: Double = 0.0,
    val lastSellingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,


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
    val lastSellingPrice: Double,
    val imageUrl: String? = null,
    val hsn: String,
    val unit: String,
    val category: String
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
        SELECT * FROM products 
        WHERE isDeleted = 0 AND 
        (name LIKE '%' || :query || '%' OR hsn LIKE '%' || :query || '%')
        ORDER BY name ASC
    """
    )
    fun search(query: String): Flow<List<ProductEntity>>

    @Query(
        """
    SELECT id, name, hsn, lastSellingPrice, sellingPrice, imageUrl,  unit , category FROM products
    WHERE isDeleted = 0
    ORDER BY name
"""
    )
    fun getAllBasic(): Flow<List<ProductBasic>>


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
}