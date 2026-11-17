package com.ticktracker.ui.tasks

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.monthAndDay
import com.ticktracker.util.shortWeekday
import java.time.LocalDate

/** How many days before today the timeline shows (D37). */
private const val PAST_DAYS_SHOWN = 7L

/** How many days after today the timeline shows (D37). */
private const val FUTURE_DAYS_SHOWN = 365L

/**
 * The Timeline tab (D01, D29): tasks grouped by day, newest day at the top.
 * Shows the last week, today and the year ahead, every day in that range whether or not it has
 * a task (D37). Tasks outside the range are still listed under All Tasks.
 * Tasks with no date are not here; they are under All Tasks.
 */
@Composable
fun TimelineTab(
    items: List<TaskItem>,
    today: LocalDate,
    onToggle: (TaskItem) -> Unit,
    onOpen: (TaskItem) -> Unit,
    onAdd: (LocalDate) -> Unit,
) {
    val byDate = items.filter { it.task.date != null }.groupBy { it.task.date!! }
    val days = (-PAST_DAYS_SHOWN..FUTURE_DAYS_SHOWN).map(today::plusDays).sortedDescending()

    // Which days have their "Completed" list open. Closed by default.
    val completedOpen = remember { mutableStateMapOf<LocalDate, Boolean>() }
    // Start with today near the top instead of the furthest future day.
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (days.indexOf(today) - 1).coerceAtLeast(0),
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
    ) {
        items(days, key = { it.toEpochDay() }) { date ->
            DaySection(
                date = date,
                isToday = date == today,
                today = today,
                tasks = byDate[date].orEmpty(),
                completedOpen = completedOpen[date] == true,
                onToggleCompletedOpen = { completedOpen[date] = completedOpen[date] != true },
                onToggle = onToggle,
                onOpen = onOpen,
                onAdd = { onAdd(date) },
            )
        }
    }
}

/** Height of a day's heading row. The dot on the line is centred on it. */
private val HEADING_HEIGHT = 34.dp

@Composable
private fun DaySection(
    date: LocalDate,
    isToday: Boolean,
    today: LocalDate,
    tasks: List<TaskItem>,
    completedOpen: Boolean,
    onToggleCompletedOpen: () -> Unit,
    onToggle: (TaskItem) -> Unit,
    onOpen: (TaskItem) -> Unit,
    onAdd: () -> Unit,
) {
    val green = MaterialTheme.colorScheme.primary
    val open = tasks.filter { !it.task.completed }
    val done = tasks.filter { it.task.completed }
    val hasTasks = tasks.isNotEmpty()
    // Empty days are a single quiet line; days with tasks, and today, get the full section.
    val expanded = hasTasks || isToday

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The vertical line down the left, with a dot beside each date.
            .drawBehind {
                val x = 6.dp.toPx()
                val dotY = (HEADING_HEIGHT / 2).toPx()
                drawLine(CardOutline, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5.dp.toPx())
                when {
                    isToday -> drawCircle(green, radius = 6.dp.toPx(), center = Offset(x, dotY))
                    hasTasks -> {
                        drawCircle(Color(0xFF0B0B0B), radius = 5.dp.toPx(), center = Offset(x, dotY))
                        drawCircle(
                            green.copy(alpha = 0.7f),
                            radius = 5.dp.toPx(),
                            center = Offset(x, dotY),
                            style = Stroke(1.5.dp.toPx()),
                        )
                    }
                    else -> drawCircle(CardOutline, radius = 3.dp.toPx(), center = Offset(x, dotY))
                }
            }
            .padding(start = 22.dp, bottom = if (expanded) 10.dp else 0.dp),
    ) {
        // Date heading, with progress on the right, or a small + when the day is empty.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HEADING_HEIGHT)
                .then(if (expanded) Modifier else Modifier.clickable(onClick = onAdd)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = monthAndDay(date),
                style = if (expanded) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (expanded) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isToday -> green
                    hasTasks -> MaterialTheme.colorScheme.onSurface
                    else -> TextMuted.copy(alpha = 0.7f)
                },
            )
            Text(
                text = shortWeekday(date),
                color = TextMuted.copy(alpha = if (expanded) 1f else 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 6.dp),
            )
            if (isToday) {
                Surface(
                    color = green,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text(
                        text = "TODAY",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                if (hasTasks) {
                    Text(
                        text = "${done.size}/${tasks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (open.isEmpty()) green else TextMuted,
                    )
                } else if (!isToday) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add task",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        if (!expanded) return@Column

        open.forEach { item ->
            TaskRow(item, today, showDate = false, onToggle = { onToggle(item) }, onOpen = { onOpen(item) })
        }

        if (hasTasks && open.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(green.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = green, modifier = Modifier.size(16.dp))
                Text(
                    text = "All done",
                    color = green,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Text(
                    text = if (done.size == 1) "1 task completed" else "${done.size} tasks completed",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }

        if (done.isNotEmpty()) {
            CollapsibleHeader("Completed", done.size, completedOpen, onToggleCompletedOpen)
            if (completedOpen) {
                done.forEach { item ->
                    TaskRow(item, today, showDate = false, onToggle = { onToggle(item) }, onOpen = { onOpen(item) })
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAdd)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(16.dp),
            )
            Text(
                "Add task",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
