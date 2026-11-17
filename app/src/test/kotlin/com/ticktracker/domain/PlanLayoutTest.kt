package com.ticktracker.domain

import com.ticktracker.domain.TimelineEntry.Gap
import com.ticktracker.domain.TimelineEntry.Item
import com.ticktracker.domain.TimelineEntry.Now
import com.ticktracker.domain.TimelineEntry.WindDown
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanLayoutTest {
    /** A test item: a name with a start and end in minutes. */
    private data class Slot(val name: String, val start: Int, val end: Int)

    private val dayStart = 7 * 60
    private val windDown = 22 * 60

    private fun layout(vararg slots: Slot, now: Int? = null) =
        layoutDay(slots.toList(), { it.start }, { it.end }, dayStart, windDown, now)

    private fun item(slot: Slot) = Item(slot, slot.start, slot.end)

    @Test
    fun emptyDayIsOneGapThenWindDown() {
        assertEquals(listOf(Gap(dayStart, windDown), WindDown(windDown)), layout())
    }

    @Test
    fun aGapAfterNowCountsFromNow() {
        // A 6:00 to 6:30 PM block, viewed at 6:38 PM. The 8 minutes already gone are too short
        // to show, so the marker is followed by the 3h 22m actually left before wind down.
        val walk = Slot("10k", 18 * 60, 18 * 60 + 30)
        val now = 18 * 60 + 38
        assertEquals(
            listOf(
                Gap(dayStart, 18 * 60),
                item(walk),
                Now(now),
                Gap(now, windDown),
                WindDown(windDown),
            ),
            layout(walk, now = now),
        )
        assertEquals(11 * 60, (layout(walk)[0] as Gap).minutes)
    }

    @Test
    fun aGapThatContainsNowIsSplitAtNow() {
        // The bug from the phone: day starts 6:00 AM, first task at 2:00 PM, viewed at 1:36 PM.
        // It used to show one 8h gap after the marker; the time left is 24 minutes.
        val start = 6 * 60
        val task = Slot("Build an app", 14 * 60, 15 * 60)
        val now = 13 * 60 + 36
        val result = layoutDay(listOf(task), { it.start }, { it.end }, start, windDown, now)
        assertEquals(
            listOf(Gap(start, now), Now(now), Gap(now, 14 * 60), item(task)),
            result.take(4),
        )
        assertEquals(24, (result[2] as Gap).minutes)
    }

    @Test
    fun aShortGapLeftBeforeTheNextItemIsNotShown() {
        val task = Slot("t", 14 * 60, 15 * 60)
        val now = 13 * 60 + 50 // 10 minutes before the task
        assertEquals(
            listOf(Gap(dayStart, now), Now(now), item(task)),
            layout(task, now = now).take(3),
        )
    }

    @Test
    fun overlappingItemsAreListedInOrderWithoutAGap() {
        val a = Slot("a", 600, 700)
        val b = Slot("b", 630, 660)
        val c = Slot("c", 690, 720)
        assertEquals(
            listOf(Gap(dayStart, 600), item(a), item(b), item(c), Gap(720, windDown), WindDown(windDown)),
            layout(c, a, b),
        )
    }

    @Test
    fun shortGapsAreLeftOut() {
        val a = Slot("a", 600, 660)
        val b = Slot("b", 670, 700) // 10 minutes after a ends
        assertEquals(
            listOf(Gap(dayStart, 600), item(a), item(b), Gap(700, windDown), WindDown(windDown)),
            layout(a, b),
        )
    }

    @Test
    fun itemsOutsideTheDayStillAppear() {
        val early = Slot("early", 6 * 60, 6 * 60 + 30)
        val late = Slot("late", 23 * 60, 23 * 60 + 30)
        assertEquals(
            listOf(item(early), Gap(dayStart, windDown), WindDown(windDown), item(late)),
            layout(early, late),
        )
    }

    @Test
    fun nowMarkerFollowsAnItemInProgress() {
        val a = Slot("a", 600, 720)
        assertEquals(
            listOf(Gap(dayStart, 600), item(a), Now(650), Gap(720, windDown), WindDown(windDown)),
            layout(a, now = 650),
        )
    }

    @Test
    fun nowMarkerAfterWindDownGoesLast() {
        val result = layout(now = 23 * 60)
        assertEquals(Now(23 * 60), result.last())
    }
}
