package com.ticktracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

/** The single on-device database (D02, D23). */
@Database(
    entities = [
        HabitEntity::class,
        HabitEntryEntity::class,
        CategoryEntity::class,
        HabitCategoryCrossRef::class,
        ReminderEntity::class,
    ],
    version = 1,
    // TODO: export the schema and write migrations before the first real data is stored.
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "ticktracker.db").build()
    }
}

/** Dates are stored as a day number so they sort and compare correctly in SQL. */
class Converters {
    @TypeConverter
    fun dateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
