package com.example.axiom.data.calendar.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.axiom.data.calendar.db.entity.TaskEntity
import com.example.axiom.data.calendar.db.entity.RecurrenceEntity

data class TaskWithRecurrence(

    @Embedded
    val task: TaskEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val recurrence: RecurrenceEntity?
)
