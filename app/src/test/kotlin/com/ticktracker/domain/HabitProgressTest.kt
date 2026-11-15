package com.ticktracker.domain

import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitProgressTest {
    // Saturday. Its week runs Monday 9 Nov to Sunday 15 Nov.
    private val today = LocalDate.of(2026, 11, 14)

    private fun tick(
        direction: HabitDirection = HabitDirection.BUILD,
        repeatGoal: RepeatGoal = RepeatGoal.ONCE_A_DAY,
        targetCount: Int? = null,
    ) = HabitEntity(
        id = 1, name = "h", icon = "", color = 0, kind = HabitKind.TICK,
        direction = direction, repeatGoal = repeatGoal, targetCount = targetCount, createdAt = 0,
    )

    private fun measurable(target: Double, period: GoalPeriod) = HabitEntity(
        id = 1, name = "h", icon = "", color = 0, kind = HabitKind.MEASURABLE,
        unit = "km", targetAmount = target, goalPeriod = period, createdAt = 0,
    )

    private fun entries(vararg items: HabitEntryEntity): EntriesByDate = items.associateBy { it.date }

    private fun done(date: LocalDate) = HabitEntryEntity(1, date, state = EntryState.DONE)

    @Test
    fun weekStartsOnMonday() {
        assertEquals(LocalDate.of(2026, 11, 9), weekStart(today))
        assertEquals(LocalDate.of(2026, 11, 9), weekStart(LocalDate.of(2026, 11, 9)))
        assertEquals(LocalDate.of(2026, 11, 9), weekStart(LocalDate.of(2026, 11, 15)))
    }

    @Test
    fun dailyStreakCountsConsecutiveDaysIncludingToday() {
        val e = entries(done(today), done(today.minusDays(1)), done(today.minusDays(2)))
        assertEquals(3, tick().streak(e, today))
    }

    @Test
    fun dailyStreakSurvivesAnUnfinishedToday() {
        val e = entries(done(today.minusDays(1)), done(today.minusDays(2)))
        assertEquals(2, tick().streak(e, today))
    }

    @Test
    fun dailyStreakBreaksOnAMissedDay() {
        val e = entries(done(today), done(today.minusDays(2)), done(today.minusDays(3)))
        assertEquals(1, tick().streak(e, today))
    }

    @Test
    fun streakIsZeroWithNoEntries() {
        assertEquals(0, tick().streak(emptyMap(), today))
    }

    @Test
    fun quitHabitCountsResistedDaysOnly() {
        val habit = tick(direction = HabitDirection.QUIT)
        assertTrue(habit.isDayMet(HabitEntryEntity(1, today, state = EntryState.RESISTED)))
        assertFalse(habit.isDayMet(HabitEntryEntity(1, today, state = EntryState.SLIPPED)))
    }

    @Test
    fun multiplePerDayNeedsTheFullCount() {
        val habit = tick(repeatGoal = RepeatGoal.MULTIPLE_PER_DAY, targetCount = 2)
        assertFalse(habit.isDayMet(HabitEntryEntity(1, today, count = 1)))
        assertTrue(habit.isDayMet(HabitEntryEntity(1, today, count = 2)))
    }

    @Test
    fun measurableDayNeedsTheTarget() {
        val habit = measurable(7.0, GoalPeriod.EVERYDAY)
        assertFalse(habit.isDayMet(HabitEntryEntity(1, today, amount = 5.0)))
        assertTrue(habit.isDayMet(HabitEntryEntity(1, today, amount = 7.0)))
    }

    @Test
    fun weeklyGoalStreakCountsWeeksAndIgnoresSkips() {
        val habit = tick(repeatGoal = RepeatGoal.WEEKLY_GOAL, targetCount = 2)
        val lastMonday = LocalDate.of(2026, 11, 2)
        val e = entries(
            // Last week: two done days and a skip. Goal met.
            done(lastMonday),
            done(lastMonday.plusDays(2)),
            HabitEntryEntity(1, lastMonday.plusDays(3), state = EntryState.SKIPPED),
            // This week: one done day so far. Not met yet, but not broken either.
            done(today),
        )
        assertEquals(1, habit.streak(e, today))
        assertEquals("1/2 this week", habit.weekProgressText(e, today))
    }

    @Test
    fun weeklyGoalStreakIncludesThisWeekOnceMet() {
        val habit = tick(repeatGoal = RepeatGoal.WEEKLY_GOAL, targetCount = 2)
        val lastMonday = LocalDate.of(2026, 11, 2)
        val e = entries(done(lastMonday), done(lastMonday.plusDays(1)), done(today), done(today.minusDays(1)))
        assertEquals(2, habit.streak(e, today))
    }

    @Test
    fun weeklyMeasurableSumsTheWeek() {
        val habit = measurable(20.0, GoalPeriod.WEEKLY)
        val e = entries(
            HabitEntryEntity(1, today, amount = 12.0),
            HabitEntryEntity(1, today.minusDays(2), amount = 8.5),
        )
        assertTrue(habit.isWeekMet(e, today))
        assertEquals(1, habit.streak(e, today))
        assertEquals("20.5/20 km this week", habit.weekProgressText(e, today))
    }

    @Test
    fun weekProgressTextForDailyHabits() {
        val e = entries(done(today), done(today.minusDays(1)))
        assertEquals("2/7 days", tick().weekProgressText(e, today))
        assertEquals("0 resisted this week", tick(direction = HabitDirection.QUIT).weekProgressText(e, today))
    }
}
