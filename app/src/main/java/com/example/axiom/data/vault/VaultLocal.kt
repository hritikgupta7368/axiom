package com.example.axiom.data.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.example.axiom.DB.AppDatabase

/* ---------- DATA CLASS (ENTITY) ---------- */

@Entity(
    tableName = "vault_entries",
    indices = [
        Index(value = ["serviceName"]),
        Index(value = ["username"]),
        Index(value = ["serviceName", "username"], unique = false)
    ]
)
data class VaultEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val serviceIcon: String? = null, // nullable, url
    val serviceName: String,     // Google, Netflix
    val username: String,        // email / user id
    val password: String,        // encrypted later
    val note: String? = null,

    val expiryDate: Long? = null, // nullable, epoch millis

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


/* ---------- DAO ---------- */

@Dao
interface VaultDao {

    // all entries
    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<VaultEntryEntity>>

    // filter by service
    @Query("""
        SELECT * FROM vault_entries
        WHERE serviceName LIKE '%' || :service || '%'
        ORDER BY updatedAt DESC
    """)
    fun getByService(service: String): Flow<List<VaultEntryEntity>>

    // filter by username
    @Query("""
        SELECT * FROM vault_entries
        WHERE username LIKE '%' || :user || '%'
        ORDER BY updatedAt DESC
    """)
    fun getByUser(user: String): Flow<List<VaultEntryEntity>>

    // search (service OR username)
    @Query("""
        SELECT * FROM vault_entries
        WHERE serviceName LIKE '%' || :query || '%'
           OR username LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun search(query: String): Flow<List<VaultEntryEntity>>

    // expired entries
    @Query("""
        SELECT * FROM vault_entries
        WHERE expiryDate IS NOT NULL
          AND expiryDate < :now
    """)
    fun getExpired(now: Long): Flow<List<VaultEntryEntity>>

    @Insert
    suspend fun insert(entry: VaultEntryEntity)

    @Update
    suspend fun update(entry: VaultEntryEntity)

    @Delete
    suspend fun delete(entry: VaultEntryEntity)

    // backup and restore methods
    @Query("SELECT * FROM vault_entries")
    suspend fun exportAll(): List<VaultEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entries: List<VaultEntryEntity>)

}



/* ---------- REPOSITORY ---------- */

class VaultRepository(
    private val dao: VaultDao,
    private val prefs: VaultPreferences
) {

    val allEntries = dao.getAll()
    val isLocked = prefs.isLocked

    fun search(query: String): Flow<List<VaultEntryEntity>> =
        if (query.isBlank()) allEntries else dao.search(query)

    fun byUser(user: String): Flow<List<VaultEntryEntity>> =
        dao.getByUser(user)

    fun byService(service: String): Flow<List<VaultEntryEntity>> =
        dao.getByService(service)

    fun expired(): Flow<List<VaultEntryEntity>> =
        dao.getExpired(System.currentTimeMillis())

    suspend fun add(
        service: String,
        user: String,
        password: String,
        expiry: Long?,
        note: String?
    ) {
        dao.insert(
            VaultEntryEntity(
                serviceName = service,
                username = user,
                password = password,
                expiryDate = expiry,
                note = note,
            )
        )
    }
    suspend fun delete(entry: VaultEntryEntity) {
        dao.delete(entry)
    }
}


/* ---------- VIEWMODEL ---------- */

class VaultViewModel(
    private val repo: VaultRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val entries: StateFlow<List<VaultEntryEntity>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    val isLocked = repo.isLocked

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun addEntry(
        service: String,
        user: String,
        password: String,
        expiry: Long?,
        note: String?
    ) {
        viewModelScope.launch {
            repo.add(service, user, password, expiry , note)
        }
    }
    fun deleteEntry(entry: VaultEntryEntity) {
        viewModelScope.launch {
            repo.delete(entry)
        }
    }
}

/* ---------- VIEWMODEL REPOSITORY   ---------- */

class VaultViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {

            val db = AppDatabase.get(context)
            val dao = db.vaultDao()
            val prefs = VaultPreferences(context.applicationContext)

            val repo = VaultRepository(dao, prefs)
            return VaultViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}



