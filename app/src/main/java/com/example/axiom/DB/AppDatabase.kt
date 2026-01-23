package com.example.axiom.DB


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.axiom.data.notes.NoteEntity
import com.example.axiom.data.notes.NotesDao
import com.example.axiom.data.temp.CalendarDao
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.vault.VaultDao
import com.example.axiom.data.vault.VaultEntryEntity


@Database(
    entities = [
        VaultEntryEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        EventEntity::class

        // add more entityies here
    ],
    version = AppDatabase.VERSION,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun vaultDao(): VaultDao
    abstract fun noteDao(): NotesDao
    abstract fun calendarDao(): CalendarDao

    // and daos here

    companion object {
        const val VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "axiom_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
