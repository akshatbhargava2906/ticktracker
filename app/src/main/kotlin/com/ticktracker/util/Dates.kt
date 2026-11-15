package com.ticktracker.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/** The current day. Days run midnight to midnight in local time (D19). */
fun today(): LocalDate = LocalDate.now()

/**
 * The current day, re-emitted just after each midnight. Screens built on this
 * roll over to the new day by themselves if the app is open at 12:00 am.
 */
fun todayFlow(): Flow<LocalDate> = flow {
    while (true) {
        val now = LocalDateTime.now()
        emit(now.toLocalDate())
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        delay(Duration.between(now, nextMidnight).toMillis() + 500)
    }
}.distinctUntilChanged()

/** Header text such as "Today, 14th Nov". */
fun headerDate(date: LocalDate): String {
    val day = date.dayOfMonth
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    return "Today, $day$suffix ${shortMonth(date)}"
}

/** "Nov". */
fun shortMonth(date: LocalDate): String =
    date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
