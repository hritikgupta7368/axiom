package com.example.axiom.data.calendar.db.dao

import androidx.room.*
import com.example.axiom.data.calendar.db.entity.TaskEntity
import com.example.axiom.data.calendar.db.entity.RecurrenceEntity
import com.example.axiom.data.calendar.db.relation.TaskWithRecurrence
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks")
    fun observeTasks(): Flow<List<TaskWithRecurrence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurrence(recurrence: RecurrenceEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
