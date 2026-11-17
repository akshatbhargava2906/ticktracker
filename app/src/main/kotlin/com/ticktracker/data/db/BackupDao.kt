package com.ticktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** Reads and replaces every table at once, for backup and restore (D33). */
@Dao
interface BackupDao {
    @Query("SELECT * FROM categories") suspend fun categories(): List<CategoryEntity>
    @Query("SELECT * FROM habits") suspend fun habits(): List<HabitEntity>
    @Query("SELECT * FROM habit_categories") suspend fun habitCategories(): List<HabitCategoryCrossRef>
    @Query("SELECT * FROM habit_entries") suspend fun habitEntries(): List<HabitEntryEntity>
    @Query("SELECT * FROM reminders") suspend fun reminders(): List<ReminderEntity>
    @Query("SELECT * FROM task_lists") suspend fun taskLists(): List<TaskListEntity>
    @Query("SELECT * FROM tasks") suspend fun tasks(): List<TaskEntity>
    @Query("SELECT * FROM subtasks") suspend fun subtasks(): List<SubtaskEntity>
    @Query("SELECT * FROM plan_blocks") suspend fun planBlocks(): List<PlanBlockEntity>

    // Inserts keep the ids from the file, so links between rows survive the restore.
    @Insert suspend fun insertCategories(rows: List<CategoryEntity>)
    @Insert suspend fun insertHabits(rows: List<HabitEntity>)
    @Insert suspend fun insertHabitCategories(rows: List<HabitCategoryCrossRef>)
    @Insert suspend fun insertHabitEntries(rows: List<HabitEntryEntity>)
    @Insert suspend fun insertReminders(rows: List<ReminderEntity>)
    @Insert suspend fun insertTaskLists(rows: List<TaskListEntity>)
    @Insert suspend fun insertTasks(rows: List<TaskEntity>)
    @Insert suspend fun insertSubtasks(rows: List<SubtaskEntity>)
    @Insert suspend fun insertPlanBlocks(rows: List<PlanBlockEntity>)

    // Children before parents, so no foreign key is ever left pointing at nothing.
    @Query("DELETE FROM plan_blocks") suspend fun clearPlanBlocks()
    @Query("DELETE FROM subtasks") suspend fun clearSubtasks()
    @Query("DELETE FROM tasks") suspend fun clearTasks()
    @Query("DELETE FROM task_lists") suspend fun clearTaskLists()
    @Query("DELETE FROM reminders") suspend fun clearReminders()
    @Query("DELETE FROM habit_entries") suspend fun clearHabitEntries()
    @Query("DELETE FROM habit_categories") suspend fun clearHabitCategories()
    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM categories") suspend fun clearCategories()
}
