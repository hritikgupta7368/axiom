package com.example.axiom.data.notes

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.axiom.DB.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/* ---------- DATA CLASS (ENTITY) ---------- */

enum class SaveState {
    LOADING,
    READY,
    SAVING,
    SAVED
}


@Entity(
    tableName = "notes",
    indices = [
        Index("updatedAt")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,

    val content: String, // multiline plain text
    val color: Int,

    val createdAt: Long,

    val updatedAt: Long
)


/* ---------- DAO ---------- */

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<NoteEntity?>

    @Insert
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}


/* ---------- REPOSITORY ---------- */

class NotesRepository(
    private val dao: NotesDao
) {

    fun observeNotes(): Flow<List<NoteEntity>> =
        dao.getAllNotes()

    fun observeNote(noteId: Long): Flow<NoteEntity?> =
        dao.getNoteById(noteId)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        dao.searchNotes(query)

    suspend fun createNote(title: String, content: String, color: Int): Long {
        return dao.insertNote(
            NoteEntity(
                title = title,
                content = content,
                color = color,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),

                )
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        dao.updateNote(
            note.copy(updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteNote(note: NoteEntity) {
        dao.deleteNote(note)
    }
}


/* ---------- VIEWMODEL ---------- */

class NotesViewModel(
    private val repo: NotesRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val notes: StateFlow<List<NoteEntity>> =
        searchQuery
            .flatMapLatest { q ->
                if (q.isBlank()) repo.observeNotes()
                else repo.searchNotes(q)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repo.deleteNote(note)
        }
    }

    suspend fun createNewNote(): Long {
        return repo.createNote("", "", Color.Transparent.toArgb())
    }
}


class NoteEditorViewModel(
    private val noteId: Long,
    private val repo: NotesRepository
) : ViewModel() {

    private val titleFlow = MutableStateFlow("")
    private val contentFlow = MutableStateFlow("")
    private val colorFlow = MutableStateFlow<Int?>(null)

    val title: StateFlow<String> = titleFlow
    val content: StateFlow<String> = contentFlow
    val color: StateFlow<Int?> = colorFlow

    private val _saveState = MutableStateFlow(SaveState.LOADING)
    val saveState: StateFlow<SaveState> = _saveState

    private val noteFlow = repo.observeNote(noteId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    init {
        // 1️⃣ INITIAL LOAD (ONE TIME)
        viewModelScope.launch {
            val note = noteFlow.first { it != null }!!

            titleFlow.value = note.title
            contentFlow.value = note.content
            colorFlow.value = note.color

            _saveState.value = SaveState.READY
        }

        // 2️⃣ AUTOSAVE PIPELINE (ONLY AFTER READY)
        combine(titleFlow, contentFlow, colorFlow) { title, content, color ->
//            title to content
            Triple(title, content, color)
        }
            .debounce(1200)
            .distinctUntilChanged()
            .onEach { (title, content, color) ->
                if (_saveState.value != SaveState.READY) return@onEach

                val current = noteFlow.value ?: return@onEach
                if (title == current.title && content == current.content && color == current.color) return@onEach

                _saveState.value = SaveState.SAVING

                repo.updateNote(
                    current.copy(
                        title = title,
                        content = content,
                        color = color ?: current.color
                    )
                )

                _saveState.value = SaveState.SAVED
            }
            .launchIn(viewModelScope)
    }

    fun onTitleChange(value: String) {
        titleFlow.value = value
        if (_saveState.value == SaveState.SAVED) {
            _saveState.value = SaveState.READY
        }
    }

    fun onContentChange(value: String) {
        contentFlow.value = value
        if (_saveState.value == SaveState.SAVED) {
            _saveState.value = SaveState.READY
        }
    }

    fun onColorChange(value: Int) {
        colorFlow.value = value
        resetSavedState()
    }

    private fun resetSavedState() {
        if (_saveState.value == SaveState.SAVED) {
            _saveState.value = SaveState.READY
        }
    }
}


// Factory code

class NotesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            var db = AppDatabase.get(context)
            val dao = db.noteDao()
            val repo = NotesRepository(dao)
            return NotesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

class NoteEditorViewModelFactory(
    private val context: Context,
    private val noteId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteEditorViewModel::class.java)) {

            val db = AppDatabase.get(context)
            val dao = db.noteDao()
            val repo = NotesRepository(dao)

            @Suppress("UNCHECKED_CAST")
            return NoteEditorViewModel(noteId, repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}



