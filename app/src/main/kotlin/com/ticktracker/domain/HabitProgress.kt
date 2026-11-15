package com.ticktracker.domain

import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import com.ticktracker.util.formatAmount
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * The rules for whether a day or a week counts, and for streaks
 * (flow_state.md section 5, decisions D13 and D26). No Android code, so it is unit tested.
 */

/** One habit's entries, keyed by the day they belong to. */
typealias EntriesByDate = Map<LocalDate, HabitEntryEntity>

/** Weeks run Monday to Sunday everywhere in the app. */
fun weekStart(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** True for habits judged over a week: a weekly-goal Tick habit or a weekly Measurable one. */
val HabitEntity.countsByWeek: Boolean
    get() = repeatGoal == RepeatGoal.WEEKLY_GOAL || goalPeriod == GoalPeriod.WEEKLY

/**
 * Whether the day counts as a success for this habit.
 * For a weekly Measurable habit a day has no target of its own, so any amount counts.
 */
fun HabitEntity.isDayMet(entry: HabitEntryEntity?): Boolean {
    if (entry == null) return false
    return when {
        kind == HabitKind.MEASURABLE -> {
            val amount = entry.amount ?: return false
            if (goalPeriod == GoalPeriod.WEEKLY) amount > 0 else amount >= (targetAmount ?: 0.0)
        }
        direction == HabitDirection.QUIT -> entry.state == EntryState.RESISTED
        repeatGoal == RepeatGoal.MULTIPLE_PER_DAY -> entry.count >= (targetCount ?: 1)
        else -> entry.state == EntryState.DONE
    }
}

private fun weekDays(anyDayInWeek: LocalDate): List<LocalDate> {
    val monday = weekStart(anyDayInWeek)
    return (0L..6L).map(monday::plusDays)
}

/** How many days of the week were a success. */
fun HabitEntity.daysMetInWeek(entries: EntriesByDate, anyDayInWeek: LocalDate): Int =
    weekDays(anyDayInWeek).count { isDayMet(entries[it]) }

/** The total amount entered over the week, for Measurable habits. */
fun weekAmount(entries: EntriesByDate, anyDayInWeek: LocalDate): Double =
    weekDays(anyDayInWeek).sumOf { entries[it]?.amount ?: 0.0 }

/** Whether a week reached its goal. Only meaningful for habits that count by week. */
fun HabitEntity.isWeekMet(entries: EntriesByDate, anyDayInWeek: LocalDate): Boolean =
    if (kind == HabitKind.MEASURABLE) {
        weekAmount(entries, anyDayInWeek) >= (targetAmount ?: 0.0)
    } else {
        daysMetInWeek(entries, anyDayInWeek) >= (targetCount ?: 1)
    }

/**
 * The current streak, in days or in weeks depending on the habit.
 *
 * The streak includes today (or this week) once it is met. Until then it still shows
 * the run that ended yesterday (or last week): an unfinished day has not broken anything.
 */
fun HabitEntity.streak(entries: EntriesByDate, today: LocalDate): Int {
    val earliest = entries.keys.minOrNull() ?: return 0
    var count = 0
    if (countsByWeek) {
        var week = weekStart(today)
        if (!isWeekMet(entries, week)) week = week.minusWeeks(1)
        while (week >= weekStart(earliest) && isWeekMet(entries, week)) {
            count++
            week = week.minusWeeks(1)
        }
    } else {
        var day = if (isDayMet(entries[today])) today else today.minusDays(1)
        while (day >= earliest && isDayMet(entries[day])) {
            count++
            day = day.minusDays(1)
        }
    }
    return count
}

/** "Streak: 3" or "Streak: 3 weeks". */
fun HabitEntity.streakText(streak: Int): String =
    if (countsByWeek) "Streak: $streak weeks" else "Streak: $streak"

/** This week's progress, as shown on the Weekly tab: "2/7 days", "1/4 this week", and so on. */
fun HabitEntity.weekProgressText(entries: EntriesByDate, today: LocalDate): String = when {
    kind == HabitKind.MEASURABLE && goalPeriod == GoalPeriod.WEEKLY ->
        "${formatAmount(weekAmount(entries, today))}/${formatAmount(targetAmount ?: 0.0)} $unit this week"
    repeatGoal == RepeatGoal.WEEKLY_GOAL ->
        "${daysMetInWeek(entries, today)}/${targetCount ?: 1} this week"
    direction == HabitDirection.QUIT ->
        "${daysMetInWeek(entries, today)} resisted this week"
    else -> "${daysMetInWeek(entries, today)}/7 days"
}
