package com.example.axiom.data.calendar.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.axiom.data.calendar.domain.TaskPriority
import com.example.axiom.data.calendar.domain.TaskStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "tasks",
    indices = [Index("scheduledDate")]
)
data class TaskEntity(

    @PrimaryKey
    val id: String,

    val title: String,
    val notes: String?,

    val createdAt: Instant,
    val updatedAt: Instant,

    val scheduledDate: LocalDate,

    val status: TaskStatus,
    val priority: TaskPriority,

    val colorHex: String?,
    val completedAt: Instant?
)
