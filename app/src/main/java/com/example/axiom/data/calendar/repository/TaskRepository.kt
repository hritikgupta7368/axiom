package com.example.axiom.data.calendar.repository

import com.example.axiom.data.calendar.domain.Task
import com.example.axiom.data.calendar.mapper.toDomain
import com.example.axiom.data.calendar.db.dao.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.axiom.data.calendar.mapper.toEntity

class TaskRepository(
    private val dao: TaskDao
) {

    val tasks: Flow<List<Task>> =
        dao.observeTasks().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun create(task: Task) {
        dao.insertTask(task.toEntity())
        task.recurrence?.let {
            dao.insertRecurrence(it.toEntity(task.id))
        }
    }
    suspend fun update(task: Task) {
        dao.updateTask(task.toEntity())
    }

    suspend fun delete(task: Task) {
        dao.deleteTask(task.toEntity())
    }
}
