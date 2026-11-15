package com.ticktracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import java.time.LocalDate

/** One habit, as created by the add habit wizard (D05 to D07). */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Glyph icon key or an emoji. */
    val icon: String,
    /** ARGB colour. */
    val color: Long,
    val kind: HabitKind,
    /** Tick habits only. */
    val direction: HabitDirection? = null,
    /** Tick habits only. */
    val repeatGoal: RepeatGoal? = null,
    /** Times per day or per week, for MULTIPLE_PER_DAY and WEEKLY_GOAL (D08). */
    val targetCount: Int? = null,
    /** Measurable habits only (D07). */
    val unit: String? = null,
    val targetAmount: Double? = null,
    val goalPeriod: GoalPeriod? = null,
    /** Epoch milliseconds. */
    val createdAt: Long,
)

/**
 * What was logged for one habit on one day. At most one row per habit per day (D17).
 * Rows are only ever written for today (D16); the repository enforces that.
 */
@Entity(
    tableName = "habit_entries",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitEntryEntity(
    val habitId: Long,
    val date: LocalDate,
    /** Tick habits: the result for the day. */
    val state: EntryState? = null,
    /** Tick habits with a multiple-per-day goal: how many times so far. */
    val count: Int = 0,
    /** Measurable habits: the amount entered, in the habit's unit. */
    val amount: Double? = null,
)

@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** False for categories added with "+ Custom". */
    val builtIn: Boolean,
)

/** Links habits to categories. A habit can have several (D09). */
@Entity(
    tableName = "habit_categories",
    primaryKeys = ["habitId", "categoryId"],
    indices = [Index("categoryId")],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitCategoryCrossRef(
    val habitId: Long,
    val categoryId: Long,
)

/**
 * One daily reminder for a habit (D10, D13, D14). Only a time of day is stored.
 * Reminders for Plan blocks and tasks will be added when those flows are designed.
 */
@Entity(
    tableName = "reminders",
    indices = [Index("habitId")],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    /** Minutes after midnight, 0 to 1439. */
    val minuteOfDay: Int,
    val enabled: Boolean = true,
)
