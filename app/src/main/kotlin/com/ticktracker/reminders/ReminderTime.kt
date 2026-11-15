package com.ticktracker.reminders

import java.time.LocalDateTime

/**
 * The next time a daily reminder should fire: today if its time is still ahead,
 * otherwise tomorrow (D13).
 */
fun nextOccurrence(minuteOfDay: Int, now: LocalDateTime): LocalDateTime {
    val todayAt = now.toLocalDate().atTime(minuteOfDay / 60, minuteOfDay % 60)
    return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
}
