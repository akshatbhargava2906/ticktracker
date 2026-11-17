package com.ticktracker.domain

import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.model.TaskRepeat
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRulesTest {
    private val today = LocalDate.of(2026, 11, 15)

    private fun task(
        date: LocalDate? = today,
        timeMinute: Int? = null,
        reminderOffsetMinutes: Int? = null,
    ) = TaskEntity(
        id = 1, listId = 1, title = "t", date = date, timeMinute = timeMinute,
        reminderOffsetMinutes = reminderOffsetMinutes, createdAt = 0,
    )

    @Test
    fun aTaskThatNeverRepeatsHasNoNextDate() {
        assertNull(nextRepeatDate(today, TaskRepeat.NEVER, today))
    }

    @Test
    fun dailyRepeatMovesToTomorrow() {
        assertEquals(today.plusDays(1), nextRepeatDate(today, TaskRepeat.DAILY, today))
    }

    @Test
    fun weeklyRepeatKeepsTheWeekday() {
        assertEquals(LocalDate.of(2026, 11, 22), nextRepeatDate(today, TaskRepeat.WEEKLY, today))
    }

    @Test
    fun monthlyRepeatKeepsTheDayOfMonthWithoutDrifting() {
        // From 31 January, three months on is 30 April, not the 28th left over from February.
        val start = LocalDate.of(2026, 1, 31)
        assertEquals(LocalDate.of(2026, 4, 30), nextRepeatDate(start, TaskRepeat.MONTHLY, LocalDate.of(2026, 4, 1)))
    }

    @Test
    fun anOverdueRepeatSkipsOccurrencesAlreadyInThePast() {
        val threeDaysAgo = today.minusDays(3)
        assertEquals(today, nextRepeatDate(threeDaysAgo, TaskRepeat.DAILY, today))
        // Weekly, 10 days overdue: 7 days on is still in the past, so it lands 14 days on.
        assertEquals(today.plusDays(4), nextRepeatDate(today.minusDays(10), TaskRepeat.WEEKLY, today))
    }

    @Test
    fun reminderIsCountedBackFromTheTaskTime() {
        val sixPm = 18 * 60
        assertEquals(
            LocalDateTime.of(2026, 11, 15, 17, 45),
            task(timeMinute = sixPm, reminderOffsetMinutes = 15).reminderAt(),
        )
        assertEquals(
            LocalDateTime.of(2026, 11, 15, 18, 0),
            task(timeMinute = sixPm, reminderOffsetMinutes = 0).reminderAt(),
        )
    }

    @Test
    fun reminderCanFallOnTheDayBefore() {
        // A task at 12:30 am with a one-hour reminder is reminded at 11:30 pm the night before.
        assertEquals(
            LocalDateTime.of(2026, 11, 14, 23, 30),
            task(timeMinute = 30, reminderOffsetMinutes = 60).reminderAt(),
        )
    }

    @Test
    fun noReminderWithoutADateATimeAndAnOffset() {
        assertNull(task(timeMinute = 600, reminderOffsetMinutes = null).reminderAt())
        assertNull(task(timeMinute = null, reminderOffsetMinutes = 10).reminderAt())
        assertNull(task(date = null, timeMinute = 600, reminderOffsetMinutes = 10).reminderAt())
    }

    @Test
    fun reminderLabels() {
        assertEquals("None", reminderOffsetLabel(null))
        assertEquals("At time of task", reminderOffsetLabel(0))
        assertEquals("15 min before", reminderOffsetLabel(15))
        assertEquals("1 hour before", reminderOffsetLabel(60))
    }
}
