package com.example.axiom.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axiom.data.calendar.domain.*
import com.example.axiom.data.calendar.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import java.time.Instant

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val tasksForSelectedDate: StateFlow<List<Task>> =
        repository.tasks
            .combine(selectedDate) { tasks, date ->
                tasks.filter { task ->
                    task.scheduledDate == date ||
                            task.recurrence?.let { r ->
                                !date.isBefore(r.startDate) &&
                                        (r.endDate == null || !date.isAfter(r.endDate))
                            } == true
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.create(task)
        }
    }
    fun toggleTask(task: Task) {
        viewModelScope.launch {
            // Determine new status
            val newStatus = if (task.status == TaskStatus.COMPLETED) {
                TaskStatus.PENDING
            } else {
                TaskStatus.COMPLETED
            }

            // Create a copy of the task with updated status and time
            val updatedTask = task.copy(
                status = newStatus,
                completedAt = if (newStatus == TaskStatus.COMPLETED) Instant.now() else null,
                updatedAt = Instant.now()
            )

            repository.update(updatedTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}
