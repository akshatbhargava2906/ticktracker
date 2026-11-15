package com.ticktracker.data.repository

import androidx.room.withTransaction
import com.ticktracker.data.db.AppDatabase
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.db.HabitCategoryCrossRef
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.db.ReminderEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.reminders.ReminderScheduler
import com.ticktracker.util.today
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * The only way screens read or change habit data.
 *
 * Entry writes take no date: they always act on today. That is how the
 * "editable on their own day only" rule (D16) is enforced.
 */
class HabitRepository(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler,
) {
    private val habits = db.habitDao()
    private val entries = db.habitEntryDao()
    private val categories = db.categoryDao()
    private val reminders = db.reminderDao()

    fun observeHabits(): Flow<List<HabitEntity>> = habits.observeAll()

    fun observeCategories(): Flow<List<CategoryEntity>> = categories.observeAll()

    fun observeEntries(from: LocalDate, to: LocalDate): Flow<List<HabitEntryEntity>> =
        entries.observeBetween(from, to)

    fun observeAllEntries(): Flow<List<HabitEntryEntity>> = entries.observeAll()

    /** Which habit belongs to which categories. */
    fun observeCategoryLinks(): Flow<List<HabitCategoryCrossRef>> = categories.observeLinks()

    /** Adds the built-in categories (D06). Safe to call on every launch. */
    suspend fun ensureBuiltInCategories() {
        categories.insertAll(BUILT_IN_CATEGORIES.map { CategoryEntity(name = it, builtIn = true) })
    }

    /**
     * Adds a user-defined category ("+ Custom") and returns its id.
     * If a category with that name already exists, returns the existing one.
     */
    suspend fun addCategory(name: String): Long {
        categories.findByName(name)?.let { return it.id }
        return categories.insert(CategoryEntity(name = name, builtIn = false))
    }

    /**
     * Saves a habit from the wizard (flow_state.md section 2): the habit itself,
     * its categories and its reminders, then registers the reminders with Android.
     */
    suspend fun createHabit(
        habit: HabitEntity,
        categoryIds: List<Long>,
        reminderMinutes: List<Int>,
    ): Long {
        val (habitId, savedReminders) = db.withTransaction {
            val id = habits.insert(habit)
            categories.link(categoryIds.map { HabitCategoryCrossRef(id, it) })
            val saved = reminderMinutes.map { minute ->
                val reminder = ReminderEntity(habitId = id, minuteOfDay = minute)
                reminder.copy(id = reminders.insert(reminder))
            }
            id to saved
        }
        savedReminders.forEach(scheduler::schedule)
        return habitId
    }

    suspend fun deleteHabit(habitId: Long) {
        reminders.getForHabit(habitId).forEach { scheduler.cancel(it.id) }
        habits.deleteById(habitId)
    }

    /** Tick habits: record today's result (D12). */
    suspend fun setTodayState(habitId: Long, state: EntryState) {
        val existing = entries.get(habitId, today())
        entries.upsert(
            existing?.copy(state = state) ?: HabitEntryEntity(habitId, today(), state = state),
        )
    }

    /** Tick habits with a multiple-per-day goal: set how many times it was done today. */
    suspend fun setTodayCount(habitId: Long, count: Int) {
        entries.upsert(HabitEntryEntity(habitId, today(), count = count))
    }

    /** Measurable habits: set today's amount. Replaces any earlier amount (D17). */
    suspend fun setTodayAmount(habitId: Long, amount: Double) {
        entries.upsert(HabitEntryEntity(habitId, today(), amount = amount))
    }

    /** Removes today's entry: the reset button (D15), or undoing a tick. */
    suspend fun clearToday(habitId: Long) {
        entries.delete(habitId, today())
    }

    private companion object {
        val BUILT_IN_CATEGORIES = listOf(
            "Art", "Finances", "Fitness", "Health", "Nutrition", "Social",
            "Study", "Work", "Morning", "Day", "Evening", "Other",
        )
    }
}
