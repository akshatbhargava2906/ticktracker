package com.ticktracker.ui.habits.cards

import com.ticktracker.ui.theme.TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.domain.weekAmount
import com.ticktracker.domain.weekStart
import com.ticktracker.ui.habits.HabitCardState
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatAmount
import com.ticktracker.util.shortMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * A Measurable habit on the Measure tab (D12): + enters or edits today's amount,
 * reset undoes it (D15), and the grid shows recent weeks with a Total row.
 */
@Composable
fun MeasureCard(
    card: HabitCardState,
    onEnterAmount: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val habit = card.habit
    val color = Color(habit.color)
    val unit = habit.unit.orEmpty()
    val target = formatAmount(habit.targetAmount ?: 0.0)

    // Daily habits show today against the target; weekly ones show the week so far.
    val progress = if (habit.goalPeriod == GoalPeriod.WEEKLY) {
        "${formatAmount(weekAmount(card.entries, card.today))}/$target $unit this week"
    } else {
        "${formatAmount(card.todayEntry?.amount ?: 0.0)}/$target $unit"
    }

    HabitCardFrame(habit.color) {
        HabitCardHeader(card, onDelete, progress = progress, onEdit = onEdit) {
            ActionButton(
                active = false,
                activeColor = color,
                onClick = onReset,
                enabled = card.todayEntry != null,
            ) { tint -> Icon(Icons.Filled.Refresh, contentDescription = "Undo today's entry", tint = tint) }
            ActionButton(active = true, activeColor = color, onClick = onEnterAmount) { tint ->
                Icon(Icons.Filled.Add, contentDescription = "Enter today's amount", tint = tint)
            }
        }
        MeasureGrid(card, Modifier.padding(top = 10.dp))
    }
}

private const val GRID_WEEKS = 7
private val DAY_LABEL_WIDTH = 30.dp
private val CELL_GAP = 3.dp

/** Recent weeks as columns, Monday to Sunday as rows, and a Total row. Read-only (D16). */
@Composable
private fun MeasureGrid(card: HabitCardState, modifier: Modifier = Modifier) {
    val habit = card.habit
    val color = Color(habit.color)
    val unit = habit.unit.orEmpty()
    val mondays = (0 until GRID_WEEKS).map {
        weekStart(card.today).minusWeeks((GRID_WEEKS - 1 - it).toLong())
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
        // Month names above the first column of each month.
        Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            Spacer(Modifier.width(DAY_LABEL_WIDTH))
            mondays.forEachIndexed { index, monday ->
                val startsMonth = index == 0 || monday.month != mondays[index - 1].month
                Text(
                    text = if (startsMonth) shortMonth(monday) else "",
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        for (dayIndex in 0..6) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mondays[0].plusDays(dayIndex.toLong()).dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 10.sp,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.width(DAY_LABEL_WIDTH),
                )
                mondays.forEach { monday ->
                    val date = monday.plusDays(dayIndex.toLong())
                    val isToday = date == card.today
                    GridCell(
                        // Days that have not happened yet are left blank.
                        value = if (date > card.today) null else card.entries[date]?.amount ?: 0.0,
                        unit = unit,
                        color = color,
                        filled = isToday && card.entries[date]?.amount != null,
                        outlined = isToday,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total",
                fontSize = 10.sp,
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.width(DAY_LABEL_WIDTH),
            )
            mondays.forEach { monday ->
                val total = weekAmount(card.entries, monday)
                GridCell(
                    value = total,
                    unit = unit,
                    color = color,
                    filled = monday == weekStart(card.today) && total > 0,
                    outlined = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** One value in the grid. [filled] marks today or the current week's total; null leaves it blank. */
@Composable
private fun GridCell(
    value: Double?,
    unit: String,
    color: Color,
    filled: Boolean,
    outlined: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val background = when {
        value == null -> Color.Transparent
        filled -> color.copy(alpha = 0.6f)
        else -> color.copy(alpha = 0.14f)
    }
    Box(
        modifier = modifier
            // A minimum rather than a fixed height: with a large font setting the cell grows
            // to fit its number and unit instead of cutting them off.
            .heightIn(min = 30.dp)
            .background(background, shape)
            .then(if (outlined) Modifier.border(1.5.dp, TextPrimary.copy(alpha = 0.8f), shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 3.dp),
            ) {
                Text(
                    text = formatAmount(value),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (filled) Color.White else color,
                    maxLines = 1,
                )
                Text(
                    text = unit,
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    color = if (filled) Color.White else color.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}
