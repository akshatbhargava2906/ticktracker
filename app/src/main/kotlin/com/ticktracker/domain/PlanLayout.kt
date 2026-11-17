package com.ticktracker.domain

/**
 * Turns a day's timed items into the rows of the Plan timeline (D30, D32): the items in time
 * order, the free gaps between them, the "Wind down" marker, and the "now" marker on today.
 * No Android code, so it is unit tested.
 */
sealed interface TimelineEntry<out T> {
    /** One item, from [start] to [end], in minutes after midnight. */
    data class Item<T>(val value: T, val start: Int, val end: Int) : TimelineEntry<T>

    /** Free time between items, long enough to plan something in. */
    data class Gap(val start: Int, val end: Int) : TimelineEntry<Nothing> {
        val minutes: Int get() = end - start
    }

    data class WindDown(val minute: Int) : TimelineEntry<Nothing>

    data class Now(val minute: Int) : TimelineEntry<Nothing>
}

/** Gaps shorter than this are not shown. */
const val MIN_GAP_MINUTES = 15

/**
 * Lays out a day. Free time is counted from [dayStart] up to [windDown]; items outside that
 * range still appear, in time order. Items that overlap are simply listed one after the other.
 * [now] is the current minute on today, or null on other days.
 */
fun <T> layoutDay(
    items: List<T>,
    startOf: (T) -> Int,
    endOf: (T) -> Int,
    dayStart: Int,
    windDown: Int,
    now: Int?,
): List<TimelineEntry<T>> {
    val result = mutableListOf<TimelineEntry<T>>()
    var cursor = dayStart
    var windDownShown = false

    fun addGap(from: Int, to: Int) {
        if (to - from >= MIN_GAP_MINUTES) result += TimelineEntry.Gap(from, to)
    }

    fun showWindDown() {
        addGap(cursor, windDown)
        result += TimelineEntry.WindDown(windDown)
        cursor = maxOf(cursor, windDown)
        windDownShown = true
    }

    for (item in items.sortedBy(startOf)) {
        val start = startOf(item)
        val end = maxOf(endOf(item), start)
        if (!windDownShown && start >= windDown) showWindDown()
        if (!windDownShown) addGap(cursor, start)
        result += TimelineEntry.Item(item, start, end)
        cursor = maxOf(cursor, end)
    }
    if (!windDownShown) showWindDown()

    if (now != null) {
        // The marker goes after anything already started, and before the first thing still ahead.
        val index = result.indexOfFirst { entry ->
            when (entry) {
                is TimelineEntry.Gap -> entry.end > now
                is TimelineEntry.Item -> entry.start > now
                is TimelineEntry.WindDown -> entry.minute > now
                is TimelineEntry.Now -> false
            }
        }
        val gap = result.getOrNull(index) as? TimelineEntry.Gap
        if (gap != null && gap.start < now) {
            // A gap that has already begun is split at now: the part that has passed sits above
            // the marker, and the time actually left before the next item sits below it.
            val split = buildList<TimelineEntry<T>> {
                if (now - gap.start >= MIN_GAP_MINUTES) add(TimelineEntry.Gap(gap.start, now))
                add(TimelineEntry.Now(now))
                if (gap.end - now >= MIN_GAP_MINUTES) add(TimelineEntry.Gap(now, gap.end))
            }
            result.removeAt(index)
            result.addAll(index, split)
        } else {
            result.add(if (index < 0) result.size else index, TimelineEntry.Now(now))
        }
    }
    return result
}
