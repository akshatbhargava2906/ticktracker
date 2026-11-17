package com.ticktracker.ui.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticktracker.data.db.BlockKind
import com.ticktracker.data.repository.TaskDraft
import com.ticktracker.domain.TimelineEntry
import com.ticktracker.domain.weekStart
import com.ticktracker.ui.tasks.CheckCircle
import com.ticktracker.ui.tasks.TaskSheet
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatClock
import com.ticktracker.util.formatDuration
import com.ticktracker.util.shortMonth
import com.ticktracker.util.shortWeekday
import java.time.LocalDate

private val TIME_COLUMN = 76.dp
private val LINE_COLUMN = 64.dp

/**
 * The Plan section (D30, D32, D35): a week strip, then the chosen day as a timeline of
 * planned blocks and timed tasks, with the free gaps between them. Habits are not shown here.
 *
 * [addRequests] goes up by one each time the + button in the bottom bar is tapped.
 * [openBlockId] is a block to show, set when the app was opened from its reminder.
 */
@Composable
fun PlanScreen(
    addRequests: Int = 0,
    openBlockId: Long? = null,
    onBlockOpened: () -> Unit = {},
    viewModel: PlanViewModel = viewModel(factory = PlanViewModel.Factory),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    var planning by remember { mutableStateOf<PlanDraft?>(null) }
    var editingTask by remember { mutableStateOf<TaskDraft?>(null) }
    // A task tapped on the timeline. It is read in full, with its sub-tasks, before the sheet opens,
    // because saving the sheet replaces the task's sub-tasks with what the sheet holds.
    var openTaskId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(openTaskId) {
        val id = openTaskId ?: return@LaunchedEffect
        editingTask = viewModel.loadTask(id)
        openTaskId = null
    }

    /** A new block on the shown day: from the next five minutes on today, or the day's start. */
    fun newBlock(start: Int? = null, length: Int = 60) {
        val begin = start ?: if (ui.date == ui.today) {
            ((ui.nowMinute + 4) / 5 * 5).coerceAtMost(23 * 60 + 55)
        } else {
            ui.settings.dayStartMinute
        }
        planning = PlanDraft(date = ui.date, startMinute = begin, durationMinutes = length)
    }

    var handledAdds by rememberSaveable { mutableStateOf(addRequests) }
    LaunchedEffect(addRequests, ui.loaded) {
        if (ui.loaded && addRequests > handledAdds) {
            handledAdds = addRequests
            newBlock()
        }
    }

    // Opened from a reminder: show the block's day.
    LaunchedEffect(openBlockId, ui.loaded) {
        val id = openBlockId ?: return@LaunchedEffect
        if (!ui.loaded) return@LaunchedEffect
        viewModel.blockDate(id)?.let(viewModel::selectDate)
        onBlockOpened()
    }

    Column(Modifier.fillMaxSize()) {
        WeekStrip(selected = ui.date, today = ui.today, onSelect = viewModel::selectDate)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.selectDate(ui.date.minusDays(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
            }
            Text(
                text = "${shortWeekday(ui.date)}, ${shortMonth(ui.date)} ${ui.date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { viewModel.selectDate(ui.date.plusDays(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
            }
            Box(Modifier.weight(1f))
            if (ui.date != ui.today) {
                TextButton(onClick = { viewModel.selectDate(ui.today) }) { Text("Today") }
            }
        }
        HorizontalDivider(color = CardOutline)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp, end = 12.dp),
        ) {
            items(ui.entries, key = ::entryKey) { entry ->
                when (entry) {
                    is TimelineEntry.Gap -> {
                        // The gap after the now marker starts at the current minute, which the marker
                        // already shows; planning in it starts on the next five minutes.
                        val startsNow = ui.date == ui.today && entry.start == ui.nowMinute
                        GapRow(entry, showTime = !startsNow) {
                            val begin = if (startsNow) (entry.start + 4) / 5 * 5 else entry.start
                            newBlock(start = begin, length = minOf(60, entry.end - begin).coerceAtLeast(5))
                        }
                    }
                    is TimelineEntry.Now -> NowRow(entry.minute)
                    is TimelineEntry.WindDown -> WindDownRow(entry.minute)
                    is TimelineEntry.Item -> ItemRow(
                        item = entry.value,
                        start = entry.start,
                        end = entry.end,
                        onToggle = {
                            when (val item = entry.value) {
                                is PlanItem.Block -> viewModel.toggleBlock(item.block)
                                is PlanItem.Task -> viewModel.toggleTask(item.task)
                            }
                        },
                        onOpen = {
                            when (val item = entry.value) {
                                is PlanItem.Block -> planning = PlanDraft.from(item.block)
                                is PlanItem.Task -> openTaskId = item.task.id
                            }
                        },
                    )
                }
            }
        }
    }

    planning?.let { draft ->
        PlanSheet(
            initial = draft,
            today = ui.today,
            lists = ui.lists,
            onSave = {
                viewModel.save(it)
                planning = null
            },
            onDelete = draft.id?.let { id ->
                {
                    viewModel.deleteBlock(id)
                    planning = null
                }
            },
            onDismiss = { planning = null },
        )
    }

    editingTask?.let { draft ->
        TaskSheet(
            initial = draft,
            lists = ui.lists,
            today = ui.today,
            onClose = {
                viewModel.saveTask(it)
                editingTask = null
            },
            onDelete = {
                draft.id?.let(viewModel::deleteTask)
                editingTask = null
            },
        )
    }

}

private fun entryKey(entry: TimelineEntry<PlanItem>): String = when (entry) {
    is TimelineEntry.Item -> entry.value.key
    is TimelineEntry.Gap -> "gap-${entry.start}"
    is TimelineEntry.Now -> "now"
    is TimelineEntry.WindDown -> "wind-down"
}

/** Monday to Sunday of the shown week; the chosen day is green, today is outlined. */
@Composable
private fun WeekStrip(selected: LocalDate, today: LocalDate, onSelect: (LocalDate) -> Unit) {
    val monday = weekStart(selected)
    val green = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (0L..6L).map(monday::plusDays).forEach { day ->
            val isSelected = day == selected
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) green else Color(0xFF181818),
                border = if (day == today && !isSelected) BorderStroke(1.dp, green) else null,
                onClick = { onSelect(day) },
            ) {
                Column(
                    Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextMuted
                    Text(shortWeekday(day), color = color, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

/** The three columns every timeline row shares: time label, the line with its marker, and content. */
@Composable
private fun TimelineRow(
    time: String?,
    timeColor: Color = TextMuted,
    lineDashed: Boolean = true,
    minHeight: Dp = 0.dp,
    marker: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .drawBehind {
                // The vertical line down the middle column.
                val x = (TIME_COLUMN + LINE_COLUMN / 2).toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = if (lineDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) else null,
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(TIME_COLUMN).padding(start = 12.dp)) {
            if (time != null) Text(time, color = timeColor, style = MaterialTheme.typography.bodyMedium)
        }
        Box(Modifier.width(LINE_COLUMN), contentAlignment = Alignment.Center) { marker() }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun GapRow(gap: TimelineEntry.Gap, showTime: Boolean, onPlan: () -> Unit) {
    val green = MaterialTheme.colorScheme.primary
    TimelineRow(time = if (showTime) formatClock(gap.start) else null, minHeight = 120.dp) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Text("🕒 ${formatDuration(gap.minutes)} gap", color = TextMuted)
            Surface(
                modifier = Modifier.padding(top = 10.dp),
                shape = CircleShape,
                color = green.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, green.copy(alpha = 0.5f)),
                onClick = onPlan,
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = green, modifier = Modifier.size(18.dp))
                    Text("Plan this slot", color = green, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun NowRow(minute: Int) {
    val green = MaterialTheme.colorScheme.primary
    TimelineRow(
        time = formatClock(minute),
        timeColor = green,
        minHeight = 36.dp,
        marker = {
            Box(
                Modifier
                    .size(22.dp)
                    .background(green.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(12.dp).background(green, CircleShape))
            }
        },
    )
}

@Composable
private fun WindDownRow(minute: Int) {
    TimelineRow(
        time = formatClock(minute),
        minHeight = 72.dp,
        marker = { Pill(color = Color(0xFF2A2A2A), height = 52.dp) { Text("🌙") } },
    ) {
        Text(
            "Wind down",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/** The rounded marker on the line. Taller for longer items. */
@Composable
private fun Pill(color: Color, height: Dp, content: @Composable () -> Unit) {
    Box(
        Modifier
            .width(52.dp)
            .height(height)
            .background(color, RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ItemRow(item: PlanItem, start: Int, end: Int, onToggle: () -> Unit, onOpen: () -> Unit) {
    val green = MaterialTheme.colorScheme.primary
    // Half a dp per minute, so a 30-minute item is short and a 3-hour one is tall.
    val pillHeight = (52 + (end - start) / 2).coerceIn(52, 150).dp

    val (pillColor, iconTint) = when (item) {
        is PlanItem.Block ->
            if (item.block.kind == BlockKind.NOTE) Color(0xFF2A2A2A) to TextMuted else green to Color.White
        is PlanItem.Task -> Color(0xFF2E5B8A) to Color.White
    }
    val done = when (item) {
        is PlanItem.Block -> item.block.completed
        is PlanItem.Task -> item.task.completed
    }
    // Notes have nothing to tick.
    val showCircle = !(item is PlanItem.Block && item.block.kind == BlockKind.NOTE)

    TimelineRow(
        time = formatClock(start),
        minHeight = pillHeight + 20.dp,
        marker = {
            Pill(color = pillColor, height = pillHeight) {
                when (item) {
                    is PlanItem.Task -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = iconTint)
                    is PlanItem.Block -> Icon(
                        if (item.block.kind == BlockKind.NOTE) Icons.Filled.Create else Icons.Filled.Check,
                        contentDescription = null,
                        tint = iconTint,
                    )
                }
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${formatClock(start)} – ${formatClock(end % (24 * 60))} · ${formatDuration(end - start)}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) TextMuted else MaterialTheme.colorScheme.onSurface,
                )
                val detail = when (item) {
                    is PlanItem.Task -> "Task"
                    is PlanItem.Block -> if (item.block.kind == BlockKind.NOTE) "Note" else null
                }
                if (detail != null) Text(detail, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (showCircle) {
                CheckCircle(
                    checked = done,
                    onClick = onToggle,
                    size = 32.dp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
