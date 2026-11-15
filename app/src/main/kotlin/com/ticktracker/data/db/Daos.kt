package com.ticktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HabitEntryDao {
    /** Entries for every habit in a date range, for the history grids and the Weekly tab. */
    @Query("SELECT * FROM habit_entries WHERE date BETWEEN :from AND :to")
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<HabitEntryEntity>>

    /** Every entry of every habit. Streaks need the full history. */
    @Query("SELECT * FROM habit_entries")
    fun observeAll(): Flow<List<HabitEntryEntity>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun get(habitId: Long, date: LocalDate): HabitEntryEntity?

    @Upsert
    suspend fun upsert(entry: HabitEntryEntity)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: Long, date: LocalDate)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY builtIn DESC, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    /** Returns the new row id, or -1 if a category with that name already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE")
    suspend fun findByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun link(refs: List<HabitCategoryCrossRef>)

    @Query("SELECT * FROM habit_categories")
    fun observeLinks(): Flow<List<HabitCategoryCrossRef>>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE habitId = :habitId ORDER BY minuteOfDay")
    suspend fun getForHabit(habitId: Long): List<ReminderEntity>

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
