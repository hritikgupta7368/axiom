package com.example.axiom.ui.screens.finances.quotation.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.axiom.DB.AppDatabase
import com.example.axiom.data.finances.CustomerFirmEntity
import com.example.axiom.data.finances.SellerFirmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Entity(
    tableName = "quotation_items",
    foreignKeys = [
        ForeignKey(
            entity = QuotationEntity::class,
            parentColumns = ["id"],
            childColumns = ["quotationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quotationId")]
)
data class QuotationItemEntity(
    @PrimaryKey val id: String,
    val quotationId: String,

    val productId: String?,
    val name: String,
    val hsn: String,
    val unit: String,

    val rate: Double,
    val quantity: Double,
    val discountPercent: Double,

    val taxableAmount: Double   // (qty * rate - discount)
)

@Entity(
    tableName = "quotations",
    foreignKeys = [
        ForeignKey(
            entity = SellerFirmEntity::class,
            parentColumns = ["id"],
            childColumns = ["sellerId"]
        ),
        ForeignKey(
            entity = CustomerFirmEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"]
        )
    ],
    indices = [Index("sellerId"), Index("customerId")]
)
data class QuotationEntity(
    @PrimaryKey val id: String,
    val quotationNo: String,
    val sellerId: String,
    val customerId: String,
    val issueDate: Long,
    val placeOfSupply: String,

    val totalTaxableAmount: Double,

    // GST applied on total taxable
    val taxRate: Double = 18.00,

    val totalTax: Double,
    val totalAmount: Double,

    val totalDiscountAmount: Double,

    val amountInWords: String
)

// Model
data class QuotationFull(
    @Embedded val quotation: QuotationEntity,

    @Relation(
        parentColumn = "sellerId",
        entityColumn = "id"
    )
    val seller: SellerFirmEntity,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerFirmEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "quotationId"
    )
    val items: List<QuotationItemEntity>
)

//DAo
@Dao
interface QuotationDao {

    // Get all quotations (for list screen)
    @Query("SELECT * FROM quotations ORDER BY issueDate DESC")
    fun getAll(): Flow<List<QuotationEntity>>

    // Search
    @Query(
        """
        SELECT * FROM quotations 
        WHERE quotationNo LIKE '%' || :query || '%' 
        ORDER BY issueDate DESC
    """
    )
    fun search(query: String): Flow<List<QuotationEntity>>

    // Get single quotation with items
    @Transaction
    @Query("SELECT * FROM quotations WHERE id = :id")
    suspend fun getFullQuotation(id: String): QuotationFull?

    // Insert header
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity)

    // Insert items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<QuotationItemEntity>)

    // Update header
    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    // Delete one quotation (items auto deleted due to CASCADE)
    @Query("DELETE FROM quotations WHERE id = :id")
    suspend fun deleteById(id: String)

    // Delete many
    @Query("DELETE FROM quotations WHERE id IN (:ids)")
    suspend fun deleteMany(ids: List<String>)
}

class QuotationViewModel(
    private val dao: QuotationDao
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _selectedQuotation = MutableStateFlow<QuotationFull?>(null)
    val selectedQuotation: StateFlow<QuotationFull?> = _selectedQuotation

    // List screen
    val quotations: StateFlow<List<QuotationEntity>> =
        searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) dao.getAll()
                else dao.search(query)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // BottomSheet data
    fun loadQuotation(id: String) {
        viewModelScope.launch {
            _selectedQuotation.value = dao.getFullQuotation(id)
        }
    }

    // Create
    fun createQuotation(
        quotation: QuotationEntity,
        items: List<QuotationItemEntity>
    ) {
        viewModelScope.launch {
            dao.insertQuotation(quotation)
            dao.insertItems(items)
        }
    }

    // Update (replace items fully)
    fun updateQuotation(
        quotation: QuotationEntity,
        items: List<QuotationItemEntity>
    ) {
        viewModelScope.launch {
            dao.updateQuotation(quotation)
            dao.insertItems(items) // replace strategy
        }
    }

    // Delete one
    fun deleteQuotation(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    // Delete many
    fun deleteMany(ids: List<String>) {
        viewModelScope.launch {
            dao.deleteMany(ids)
        }
    }
}

// factory

class QuotationViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotationViewModel::class.java)) {

            val db = AppDatabase.get(context.applicationContext)
            val dao = db.quotationDao()

            return QuotationViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}