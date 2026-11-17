package com.ticktracker.domain

import com.ticktracker.data.db.BlockKind
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.model.TaskRepeat
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The rules for task repeats, and for one-off reminders on tasks and Plan blocks (D29, D30).
 * No Android code, so it is unit tested.
 */

/** The choices for how early a one-off reminder fires, in minutes before the start. */
val REMINDER_OFFSETS: List<Int> = listOf(0, 5, 10, 15, 30, 60)

fun reminderOffsetLabel(offsetMinutes: Int?): String = when (offsetMinutes) {
    null -> "None"
    0 -> "At time of task"
    60 -> "1 hour before"
    else -> "$offsetMinutes min before"
}

/**
 * The date of the next occurrence of a repeating task, or null if it does not repeat.
 *
 * Counted from the task's own date, not from the day it was ticked off, so a weekly task
 * stays on its weekday. If the task was overdue, occurrences already in the past are skipped:
 * the next one is never before [today].
 */
fun nextRepeatDate(date: LocalDate, repeat: TaskRepeat, today: LocalDate): LocalDate? {
    if (repeat == TaskRepeat.NEVER) return null
    var step = 1L
    while (true) {
        val next = when (repeat) {
            TaskRepeat.DAILY -> date.plusDays(step)
            TaskRepeat.WEEKLY -> date.plusWeeks(step)
            TaskRepeat.MONTHLY -> date.plusMonths(step)
            TaskRepeat.NEVER -> return null
        }
        if (next >= today) return next
        step++
    }
}

/** When a reminder [offsetMinutes] before [minuteOfDay] on [date] fires, or null if any part is missing. */
fun reminderTime(date: LocalDate?, minuteOfDay: Int?, offsetMinutes: Int?): LocalDateTime? {
    val day = date ?: return null
    val minute = minuteOfDay ?: return null
    val offset = offsetMinutes ?: return null
    return day.atTime(minute / 60, minute % 60).minusMinutes(offset.toLong())
}

/** When the task's reminder should fire, or null if it has none. Needs a date, a time and an offset. */
fun TaskEntity.reminderAt(): LocalDateTime? = reminderTime(date, timeMinute, reminderOffsetMinutes)

/** When the block's reminder should fire, or null if it has none. Notes never have one. */
fun PlanBlockEntity.reminderAt(): LocalDateTime? =
    if (kind == BlockKind.NOTE) null else reminderTime(date, startMinute, reminderOffsetMinutes)
