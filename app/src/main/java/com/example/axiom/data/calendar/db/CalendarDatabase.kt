package com.example.axiom.data.calendar.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.axiom.data.calendar.db.dao.TaskDao
import com.example.axiom.data.calendar.db.entity.TaskEntity
import com.example.axiom.data.calendar.db.entity.RecurrenceEntity

@Database(
    entities = [TaskEntity::class, RecurrenceEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
