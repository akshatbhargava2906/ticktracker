package com.ticktracker.ui.tasks

import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.data.model.TaskRepeat
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatClock
import com.ticktracker.util.relativeDate
import java.time.LocalDate

/** The colour that marks a priority. */
@Composable
@ReadOnlyComposable
fun priorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> Color(0xFFE5484D)
    TaskPriority.MEDIUM -> Color(0xFFFFB74D)
    TaskPriority.LOW -> Color(0xFF6FB1FC)
    TaskPriority.NONE -> TextMuted
}

/** The round tick box on tasks and sub-tasks: an empty ring, or a filled green tick. */
@Composable
fun CheckCircle(checked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (checked) colors.primary else Color.Transparent)
            .border(1.5.dp, if (checked) colors.primary else TextMuted, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Done",
                tint = colors.onPrimary,
                modifier = Modifier.size(size * 0.65f),
            )
        }
    }
}

/**
 * One task in a list. Tapping the circle ticks it off; tapping anywhere else opens the task sheet.
 * Under the title it shows the task's time, repeat and sub-task progress, when it has them.
 * [showDate] adds the task's date on the right, for lists that are not grouped by day.
 */
@Composable
fun TaskRow(
    item: TaskItem,
    today: LocalDate,
    showDate: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val task = item.task
    val details = listOfNotNull(
        task.timeMinute?.let(::formatClock),
        task.repeat.takeIf { it != TaskRepeat.NEVER }?.let { "↻ ${it.label}" },
        item.subtasks.takeIf { it.isNotEmpty() }?.let { "☑ ${item.subtasksDone}/${it.size}" },
    ).joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpen)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(checked = task.completed, onClick = onToggle)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (task.completed) TextMuted else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (details.isNotEmpty()) {
                Text(text = details, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
        if (task.priority != TaskPriority.NONE) {
            Text(
                "⚑",
                color = priorityColor(task.priority),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        val date = task.date
        if (showDate && date != null) {
            val overdue = !task.completed && date < today
            Text(
                text = relativeDate(date, today),
                style = MaterialTheme.typography.bodySmall,
                color = if (overdue) priorityColor(TaskPriority.HIGH) else TextMuted,
            )
        }
    }
}

/** A tappable heading that opens and closes the rows under it, such as "Completed 3". */
@Composable
fun CollapsibleHeader(
    label: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = TextMuted,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (expanded) {
                Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = TextMuted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 4.dp),
        )
        Text(
            text = count.toString(),
            color = TextMuted,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

/** A small dialog asking for one line of text, used to name a list. */
@Composable
fun TextInputDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(24) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
