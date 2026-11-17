package com.ticktracker.ui.tasks

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

/** How many days before today the timeline always shows, even when they have no tasks. */
private const val PAST_DAYS_SHOWN = 14L

/**
 * The Timeline tab (D01, D29): tasks grouped by day, newest day at the top.
 * Shows tomorrow, today and the last two weeks, plus any other day that has a task.
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
    val window = (-PAST_DAYS_SHOWN..1L).map(today::plusDays)
    val days = (byDate.keys + window).distinct().sortedDescending()

    // Which days have their "Completed" list open. Closed by default.
    val completedOpen = remember { mutableStateMapOf<LocalDate, Boolean>() }
    // Start with today near the top instead of the furthest future day.
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (days.indexOf(today) - 1).coerceAtLeast(0),
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The vertical line down the left, with a dot beside each date.
            .drawBehind {
                val x = 10.dp.toPx()
                val dotY = 22.dp.toPx()
                drawLine(CardOutline, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
                when {
                    isToday -> drawCircle(green, radius = 9.dp.toPx(), center = Offset(x, dotY))
                    hasTasks -> {
                        drawCircle(Color(0xFF0B0B0B), radius = 7.dp.toPx(), center = Offset(x, dotY))
                        drawCircle(green.copy(alpha = 0.7f), radius = 7.dp.toPx(), center = Offset(x, dotY), style = Stroke(2.dp.toPx()))
                    }
                    else -> drawCircle(CardOutline, radius = 6.dp.toPx(), center = Offset(x, dotY))
                }
            }
            .padding(start = 36.dp, bottom = 12.dp),
    ) {
        // Date heading, with progress on the right, or a + when the day is empty.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = monthAndDay(date),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (hasTasks || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isToday -> green
                    hasTasks -> MaterialTheme.colorScheme.onSurface
                    else -> TextMuted
                },
            )
            Text(
                text = shortWeekday(date),
                color = TextMuted,
                modifier = Modifier.padding(start = 8.dp),
            )
            if (isToday) {
                Surface(
                    color = green,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(start = 10.dp),
                ) {
                    Text(
                        text = "TODAY",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Row(Modifier.weight(1f), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                if (hasTasks) {
                    Text(
                        text = "${done.size}/${tasks.size}",
                        color = if (open.isEmpty()) green else TextMuted,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                } else {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Filled.Add, contentDescription = "Add task", tint = TextMuted)
                    }
                }
            }
        }

        if (!hasTasks) return@Column

        open.forEach { item ->
            TaskRow(item, today, showDate = false, onToggle = { onToggle(item) }, onOpen = { onOpen(item) })
        }

        if (open.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = green.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, green.copy(alpha = 0.3f)),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = green)
                    Text(
                        text = "All done!",
                        color = green,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                    Text(
                        text = if (done.size == 1) "1 task completed" else "${done.size} tasks completed",
                        color = TextMuted,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
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
                .clickable(onClick = onAdd)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, CardOutline),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                )
            }
            Text("Add task", color = TextMuted, modifier = Modifier.padding(start = 12.dp))
        }
    }
}
