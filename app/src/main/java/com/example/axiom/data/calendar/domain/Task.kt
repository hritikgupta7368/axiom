package com.example.axiom.data.calendar.domain

import java.time.LocalDate
import java.time.Instant

enum class TaskStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM_RANGE // start → end/current
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@JvmInline
value class TaskColor(val hex: String)

data class TaskNotes(
    val raw: String
) {
    val lines: List<String>
        get() = raw.split("\n")
}

data class TaskRecurrence(
    val type: RecurrenceType,
    val interval: Int = 1,               // every X days/weeks/months
    val startDate: LocalDate,
    val endDate: LocalDate? = null        // null = infinite
)


data class Task(
    val id: String,
    val title: String,
    val notes: TaskNotes? = null,

    val createdAt: Instant,
    val updatedAt: Instant,

    val scheduledDate: LocalDate, // base date
    val recurrence: TaskRecurrence? = null,

    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,

    val color: TaskColor? = null,

    val completedAt: Instant? = null
)
