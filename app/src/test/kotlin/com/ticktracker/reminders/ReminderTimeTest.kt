package com.ticktracker.reminders

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTimeTest {
    private val nineAm = 9 * 60

    @Test
    fun firesTodayWhenTheTimeIsStillAhead() {
        val now = LocalDateTime.of(2026, 11, 14, 8, 30)
        assertEquals(LocalDateTime.of(2026, 11, 14, 9, 0), nextOccurrence(nineAm, now))
    }

    @Test
    fun firesTomorrowWhenTheTimeHasPassed() {
        val now = LocalDateTime.of(2026, 11, 14, 18, 0)
        assertEquals(LocalDateTime.of(2026, 11, 15, 9, 0), nextOccurrence(nineAm, now))
    }

    @Test
    fun firesTomorrowWhenItIsExactlyTheReminderTime() {
        val now = LocalDateTime.of(2026, 11, 14, 9, 0)
        assertEquals(LocalDateTime.of(2026, 11, 15, 9, 0), nextOccurrence(nineAm, now))
    }
}
