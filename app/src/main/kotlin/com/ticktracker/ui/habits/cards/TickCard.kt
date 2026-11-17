package com.ticktracker.ui.habits.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.RepeatGoal
import com.ticktracker.domain.isDayMet
import com.ticktracker.domain.weekStart
import com.ticktracker.ui.habits.HabitCardState
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.shortMonth

/**
 * A Tick habit on the Tick tab (D12): tap to complete, with a heatmap of recent months.
 * The buttons depend on the habit: tick, tick with a count, skip and tick, or slipped and resisted.
 */
@Composable
fun TickCard(
    card: HabitCardState,
    onTick: () -> Unit,
    onSkip: () -> Unit,
    onResist: () -> Unit,
    onSlip: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val habit = card.habit
    val entry = card.todayEntry
    val color = Color(habit.color)

    HabitCardFrame(habit.color) {
        HabitCardHeader(card, onDelete, onEdit = onEdit) {
            when {
                habit.direction == HabitDirection.QUIT -> {
                    ActionButton(
                        active = entry?.state == EntryState.SLIPPED,
                        activeColor = SlippedRed,
                        onClick = onSlip,
                    ) { tint -> Icon(Icons.Filled.Close, contentDescription = "Slipped", tint = tint) }
                    ActionButton(
                        active = entry?.state == EntryState.RESISTED,
                        activeColor = color,
                        onClick = onResist,
                    ) { Text("🛡️", fontSize = 20.sp) }
                }

                habit.repeatGoal == RepeatGoal.MULTIPLE_PER_DAY -> {
                    val target = habit.targetCount ?: 1
                    val count = entry?.count ?: 0
                    ActionButton(active = count >= target, activeColor = color, onClick = onTick) { tint ->
                        Text("$count/$target", color = tint, fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    if (habit.repeatGoal == RepeatGoal.WEEKLY_GOAL) {
                        ActionButton(
                            active = entry?.state == EntryState.SKIPPED,
                            activeColor = color.copy(alpha = 0.5f),
                            onClick = onSkip,
                        ) { tint -> Text("−", color = tint, fontSize = 24.sp) }
                    }
                    ActionButton(active = habit.isDayMet(entry), activeColor = color, onClick = onTick) { tint ->
                        Icon(Icons.Filled.Check, contentDescription = "Done", tint = tint)
                    }
                }
            }
        }
        Heatmap(card, Modifier.padding(top = 14.dp))
    }
}

private const val HEATMAP_WEEKS = 20

/**
 * The history grid: one column per week, rows Monday to Sunday, month names above,
 * today outlined. Read-only: past days cannot be changed (D16).
 */
@Composable
private fun Heatmap(card: HabitCardState, modifier: Modifier = Modifier) {
    val habit = card.habit
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = TextMuted)

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val labelWidth = 16.dp
        val gap = 3.dp
        val headerHeight = 16.dp
        val cell = (maxWidth - labelWidth - gap * (HEATMAP_WEEKS - 1)) / HEATMAP_WEEKS

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(headerHeight + cell * 7 + gap * 6),
        ) {
            val cellPx = cell.toPx()
            val stepPx = cellPx + gap.toPx()
            val leftPx = labelWidth.toPx()
            val topPx = headerHeight.toPx()
            val corner = CornerRadius(cellPx * 0.25f)

            listOf(0 to "M", 2 to "W", 4 to "F", 6 to "S").forEach { (row, label) ->
                drawText(textMeasurer, label, Offset(0f, topPx + row * stepPx), labelStyle)
            }

            val firstMonday = weekStart(card.today).minusWeeks(HEATMAP_WEEKS - 1L)
            for (week in 0 until HEATMAP_WEEKS) {
                val monday = firstMonday.plusWeeks(week.toLong())
                val x = leftPx + week * stepPx

                // Name the month above the first column that falls in it.
                val startsMonth = week > 0 && monday.month != monday.minusWeeks(1).month
                if (startsMonth && week <= HEATMAP_WEEKS - 3) {
                    drawText(textMeasurer, shortMonth(monday), Offset(x, 0f), labelStyle)
                }

                for (dayIndex in 0..6) {
                    val date = monday.plusDays(dayIndex.toLong())
                    if (date > card.today) continue
                    val topLeft = Offset(x, topPx + dayIndex * stepPx)
                    val size = Size(cellPx, cellPx)
                    drawRoundRect(dayCellColor(habit, card.entries[date]), topLeft, size, corner)
                    if (date == card.today) {
                        drawRoundRect(Color.White, topLeft, size, corner, style = Stroke(1.5.dp.toPx()))
                    }
                }
            }
        }
    }
}
