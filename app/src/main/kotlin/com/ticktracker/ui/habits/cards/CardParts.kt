package com.ticktracker.ui.habits.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.domain.isDayMet
import com.ticktracker.domain.streakText
import com.ticktracker.ui.habits.HabitBadge
import com.ticktracker.ui.habits.HabitCardState
import com.ticktracker.ui.theme.CardSurface

/** Colour for a slipped day on a Quit habit. */
val SlippedRed = Color(0xFFE5484D)

private val IdleButton = Color(0xFF2A2A2A)

/** The rounded card every habit sits in, outlined faintly in the habit colour. */
@Composable
fun HabitCardFrame(color: Long, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, Color(color).copy(alpha = 0.35f)),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/**
 * The top of a card. First row: icon, name, the action buttons and the overflow menu.
 * Second row: the streak, plus optional progress text.
 * Must be called inside a column, such as [HabitCardFrame].
 */
@Composable
fun HabitCardHeader(
    card: HabitCardState,
    onDelete: () -> Unit,
    progress: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val habit = card.habit
    Row(verticalAlignment = Alignment.CenterVertically) {
        HabitBadge(icon = habit.icon, color = habit.color, size = 52.dp)
        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        )
        actions()
        OverflowMenu(onDelete)
    }

    // The streak has a line of its own, across the full width of the card. Beside the
    // buttons it was squeezed out of view on cards with two of them (Quit, weekly goal, Measure).
    Row(
        modifier = Modifier.padding(top = 10.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🔥 " + habit.streakText(card.streak),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        if (progress != null) {
            Text(
                text = "  •  ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = progress,
                color = Color(habit.color),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun OverflowMenu(onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Delete habit") },
                onClick = {
                    open = false
                    onDelete()
                },
            )
        }
    }
}

/** A square button on a card. Filled with [activeColor] when [active], dark grey otherwise. */
@Composable
fun ActionButton(
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (tint: Color) -> Unit,
) {
    Surface(
        modifier = modifier
            .padding(start = 8.dp)
            .size(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) activeColor else IdleButton,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        enabled = enabled,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content(
                when {
                    active -> Color.White
                    enabled -> Color(0xFFBDBDBD)
                    else -> Color(0xFF5A5A5A)
                },
            )
        }
    }
}

/**
 * The fill colour of one day in a history grid:
 * full colour when the day was a success, red for a slip, half strength for a skip
 * or a partly done day, and faint when nothing counted.
 */
fun dayCellColor(habit: HabitEntity, entry: HabitEntryEntity?): Color {
    val color = Color(habit.color)
    return when {
        habit.isDayMet(entry) -> color
        entry?.state == EntryState.SLIPPED -> SlippedRed.copy(alpha = 0.75f)
        entry?.state == EntryState.SKIPPED -> color.copy(alpha = 0.4f)
        entry != null && (entry.count > 0 || (entry.amount ?: 0.0) > 0) -> color.copy(alpha = 0.5f)
        else -> color.copy(alpha = 0.13f)
    }
}
