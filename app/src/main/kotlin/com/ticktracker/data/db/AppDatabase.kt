package com.ticktracker.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

/**
 * The single on-device database (D02, D23).
 *
 * Versions:
 * 1. Habits, entries, categories, reminders.
 * 2. Adds tasks, task lists and sub-tasks (D29).
 */
@Database(
    entities = [
        HabitEntity::class,
        HabitEntryEntity::class,
        CategoryEntity::class,
        HabitCategoryCrossRef::class,
        ReminderEntity::class,
        TaskListEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
    ],
    // Changing any entity means raising this number and adding a migration. See Migrations.kt.
    version = 2,
    // Writes the structure of each version to app/schemas/, which migrations are built from.
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun taskDao(): TaskDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "ticktracker.db")
                .addMigrations(*ALL_MIGRATIONS)
                .build()
    }
}

/** Dates are stored as a day number so they sort and compare correctly in SQL. */
class Converters {
    @TypeConverter
    fun dateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
