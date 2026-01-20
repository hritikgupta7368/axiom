package com.example.axiom.data.notes

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


/* ---------- DATA CLASS (ENTITY) ---------- */

@Entity(
    tableName = "notes_entries",
    indices = [Index(value = ["title"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val content: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


/* ---------- DAO ---------- */

@Dao
interface NoteDao {

    // Get all notes, ordered by the most recently updated
    @Query("SELECT * FROM notes_entries ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<NoteEntity>>

    // Search for notes by title or content
    @Query("""
        SELECT * FROM notes_entries
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun search(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}



/* ---------- REPOSITORY ---------- */

class NotesRepository(private val dao: NoteDao) {

    val allNotes: Flow<List<NoteEntity>> = dao.getAll()

    fun search(query: String): Flow<List<NoteEntity>> =
        if (query.isBlank()) allNotes else dao.search(query)

    suspend fun add(title: String, content: String) {
        val note = NoteEntity(title = title, content = content)
        dao.insert(note)
    }

    suspend fun update(note: NoteEntity) {
        dao.update(note.copy(updatedAt = System.currentTimeMillis()))
    }
}


/* ---------- VIEWMODEL ---------- */

class NotesViewModel(private val repo: NotesRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val notes: StateFlow<List<NoteEntity>> =
        searchQuery
            .flatMapLatest { repo.search(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun onSearch(query: String) {
        searchQuery.value = query
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repo.add(title, content)
        }
    }
}
