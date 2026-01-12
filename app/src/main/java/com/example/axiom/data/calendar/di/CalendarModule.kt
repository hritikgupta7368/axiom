package com.example.axiom.data.calendar.di

import android.content.Context
import androidx.room.Room
import com.example.axiom.data.calendar.db.CalendarDatabase
import com.example.axiom.data.calendar.db.dao.TaskDao
import com.example.axiom.data.calendar.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CalendarDatabase =
        Room.databaseBuilder(
            context,
            CalendarDatabase::class.java,
            "calendar_db"
        ).build()

    @Provides
    fun provideTaskDao(
        database: CalendarDatabase
    ): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideTaskRepository(
        dao: TaskDao
    ): TaskRepository = TaskRepository(dao)
}
