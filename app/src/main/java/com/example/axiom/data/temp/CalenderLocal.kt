package com.example.axiom.data.temp

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.example.axiom.DB.AppDatabase
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

enum class TaskStatus {
    PENDING,
    COMPLETED,
    MISSED,
    CANCELLED
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// data class
@Entity(
    tableName = "calendar_tasks",
    indices = [
        Index(value = ["date", "startTime"]),
        Index(value = ["status"]),
        Index(value = ["priority"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val note: String? = null,

    // Start of day millis
    val date: Long,

    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean = false,

    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Priority,

    val color: Int,

    // RFC 5545 RRULE string (TASKS ONLY)
    val recurrenceRule: String? = null,

    val sortIndex: Int = 0,
    val timeZone: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["date", "startTime"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String? = null,

    val date: Long,

    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean = false,

    val importance: Int, // 1–5
    val pinned: Boolean = false,

    val color: Int,
    val timeZone: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// DAO
@Dao
interface CalendarDao {

    /* ---------- TASKS ---------- */

    @Query("""
        SELECT * FROM calendar_tasks
        WHERE date = :day
        ORDER BY startTime ASC, sortIndex ASC
    """)
    fun tasksForDay(day: Long): Flow<List<TaskEntity>>

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("""
        UPDATE calendar_tasks
        SET status = :status,
            updatedAt = :now
        WHERE id = :taskId
    """)
    suspend fun updateStatus(
        taskId: Long,
        status: TaskStatus,
        now: Long
    )

    @Query("""
        UPDATE calendar_tasks
        SET date = :newDate,
            startTime = :newStart,
            endTime = :newEnd,
            updatedAt = :now
        WHERE id = :taskId
    """)
    suspend fun rescheduleTask(
        taskId: Long,
        newDate: Long,
        newStart: Long,
        newEnd: Long,
        now: Long
    )

    /* ---------- CONFLICT CHECK ---------- */

    @Query("""
        SELECT * FROM calendar_tasks
        WHERE date = :day
          AND startTime < :end
          AND endTime > :start
    """)
    suspend fun findConflicts(
        day: Long,
        start: Long,
        end: Long
    ): List<TaskEntity>

    /* ---------- EVENTS ---------- */

    @Query("""
        SELECT * FROM calendar_events
        WHERE date = :day
        ORDER BY startTime ASC
    """)
    fun eventsForDay(day: Long): Flow<List<EventEntity>>

    @Insert
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    /* ---------- BACKUP ---------- */

    @Query("SELECT * FROM calendar_tasks")
    suspend fun exportTasks(): List<TaskEntity>

    @Query("SELECT * FROM calendar_events")
    suspend fun exportEvents(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreEvents(events: List<EventEntity>)
}

//Repsoitory

class CalendarRepository(
    private val dao: CalendarDao
) {

    fun tasksForDay(day: Long): Flow<List<TaskEntity>> =
        dao.tasksForDay(day)

    fun eventsForDay(day: Long): Flow<List<EventEntity>> =
        dao.eventsForDay(day)

    suspend fun addTask(
        title: String,
        note: String?,
        date: Long,
        start: Long,
        end: Long,
        priority: Priority,
        color: Int,
        recurrenceRule: String?,
        sortIndex: Int,
        timeZone: String
    ) {
        dao.insertTask(
            TaskEntity(
                title = title,
                note = note,
                date = date,
                startTime = start,
                endTime = end,
                priority = priority,
                color = color,
                recurrenceRule = recurrenceRule,
                sortIndex = sortIndex,
                timeZone = timeZone
            )
        )
    }

    suspend fun addEvent(
        title: String,
        description: String?,
        date: Long,
        start: Long,
        end: Long,
        importance: Int,
        pinned: Boolean,
        color: Int,
        timeZone: String
    ) {
        dao.insertEvent(
            EventEntity(
                title = title,
                description = description,
                date = date,
                startTime = start,
                endTime = end,
                importance = importance,
                pinned = pinned,
                color = color,
                timeZone = timeZone
            )
        )
    }

    suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        dao.updateStatus(id, status, System.currentTimeMillis())
    }

    suspend fun rescheduleTask(
        id: Long,
        newDate: Long,
        newStart: Long,
        newEnd: Long
    ) {
        dao.rescheduleTask(
            id,
            newDate,
            newStart,
            newEnd,
            System.currentTimeMillis()
        )
    }

    suspend fun deleteTask(task: TaskEntity) =
        dao.deleteTask(task)

    suspend fun deleteEvent(event: EventEntity) =
        dao.deleteEvent(event)
}

// ViewModel

class CalendarViewModel(
    private val repo: CalendarRepository
) : ViewModel() {

    private val selectedDay = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskEntity>> =
        selectedDay
            .flatMapLatest { day ->
                day?.let { repo.tasksForDay(it) } ?: flowOf(emptyList())
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<EventEntity>> =
        selectedDay
            .flatMapLatest { day ->
                day?.let { repo.eventsForDay(it) } ?: flowOf(emptyList())
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun selectDay(day: Long) {
        selectedDay.value = day
    }

    fun addTask(
        title: String,
        note: String?,
        date: Long,
        start: Long,
        end: Long,
        priority: Priority,
        color: Int,
        recurrenceRule: String?,
        sortIndex: Int,
        timeZone: String
    ) {
        viewModelScope.launch {
            repo.addTask(
                title, note, date, start, end,
                priority, color, recurrenceRule,
                sortIndex, timeZone
            )
        }
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            repo.updateTaskStatus(taskId, TaskStatus.COMPLETED)
        }
    }

    fun rescheduleTask(
        taskId: Long,
        newDate: Long,
        newStart: Long,
        newEnd: Long
    ) {
        viewModelScope.launch {
            repo.rescheduleTask(taskId, newDate, newStart, newEnd)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repo.deleteTask(task)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repo.deleteEvent(event)
        }
    }
}

// Factory code

class CalendarViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {

            val db = AppDatabase.get(context)
            val dao = db.calendarDao()
            val repo = CalendarRepository(dao)

            return CalendarViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}


