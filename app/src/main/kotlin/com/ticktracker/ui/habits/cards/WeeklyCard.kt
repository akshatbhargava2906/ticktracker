package com.ticktracker.ui.habits.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticktracker.domain.weekProgressText
import com.ticktracker.domain.weekStart
import com.ticktracker.ui.habits.HabitCardState
import com.ticktracker.ui.theme.TextMuted

private val DAY_LABELS = listOf("M", "Tu", "W", "Th", "F", "Sa", "Su")

/**
 * Any habit on the Weekly tab (D12): the current week as seven cells, Monday to Sunday,
 * with the week's progress beside the streak. It only shows the week; logging is done
 * on the Tick and Measure tabs.
 */
@Composable
fun WeeklyCard(card: HabitCardState, onDelete: () -> Unit, onEdit: () -> Unit) {
    val habit = card.habit
    val monday = weekStart(card.today)

    HabitCardFrame(habit.color) {
        HabitCardHeader(
            card = card,
            onDelete = onDelete,
            onEdit = onEdit,
            progress = habit.weekProgressText(card.entries, card.today),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DAY_LABELS.forEachIndexed { index, label ->
                val date = monday.plusDays(index.toLong())
                val isToday = date == card.today
                val shape = RoundedCornerShape(12.dp)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (isToday) Color.White else TextMuted,
                        fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    Box(
                        Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                // Days still to come are drawn fainter than empty past days.
                                color = if (date > card.today) {
                                    Color(habit.color).copy(alpha = 0.06f)
                                } else {
                                    dayCellColor(habit, card.entries[date])
                                },
                                shape = shape,
                            )
                            .then(if (isToday) Modifier.border(2.dp, Color.White, shape) else Modifier),
                    )
                }
            }
        }
    }
}
