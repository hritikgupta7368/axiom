package com.example.axiom.data.calendar.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.axiom.data.calendar.domain.RecurrenceType
import java.time.LocalDate

@Entity(
    tableName = "task_recurrence",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecurrenceEntity(

    @PrimaryKey
    val taskId: String,

    val type: RecurrenceType,
    val interval: Int,

    val startDate: LocalDate,
    val endDate: LocalDate?
)
