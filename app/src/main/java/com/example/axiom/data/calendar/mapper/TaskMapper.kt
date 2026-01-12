package com.example.axiom.data.calendar.mapper

import com.example.axiom.data.calendar.domain.*
import com.example.axiom.data.calendar.db.entity.*
import com.example.axiom.data.calendar.db.relation.TaskWithRecurrence

fun TaskWithRecurrence.toDomain(): Task =
    Task(
        id = task.id,
        title = task.title,
        notes = task.notes?.let { TaskNotes(it) },
        createdAt = task.createdAt,
        updatedAt = task.updatedAt,
        scheduledDate = task.scheduledDate,
        recurrence = recurrence?.let {
            TaskRecurrence(
                type = it.type,
                interval = it.interval,
                startDate = it.startDate,
                endDate = it.endDate
            )
        },
        status = task.status,
        priority = task.priority,
        color = task.colorHex?.let { TaskColor(it) },
        completedAt = task.completedAt
    )
fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        notes = notes?.raw,
        createdAt = createdAt,
        updatedAt = updatedAt,
        scheduledDate = scheduledDate,
        status = status,
        priority = priority,
        colorHex = color?.hex,
        completedAt = completedAt
    )

fun TaskRecurrence.toEntity(taskId: String): RecurrenceEntity =
    RecurrenceEntity(
        taskId = taskId,
        type = type,
        interval = interval,
        startDate = startDate,
        endDate = endDate
    )
