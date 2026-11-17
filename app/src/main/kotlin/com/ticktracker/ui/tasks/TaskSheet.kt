package com.ticktracker.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.data.model.TaskRepeat
import com.ticktracker.data.repository.SubtaskDraft
import com.ticktracker.data.repository.TaskDraft
import com.ticktracker.domain.REMINDER_OFFSETS
import com.ticktracker.domain.reminderOffsetLabel
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatClock
import com.ticktracker.util.relativeDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val SheetCard = Color(0xFF0E0E0E)

/** Which of the sheet's pickers is open. */
private enum class Picker { DATE, TIME, REMINDER, REPEAT, LIST, PRIORITY }

/**
 * The sheet for creating or editing a task (D29): title, date, time, reminder, repeat,
 * list, priority and sub-tasks. There is no save button: closing the sheet keeps the changes,
 * and [onClose] receives the edited task. Delete removes it instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSheet(
    initial: TaskDraft,
    lists: List<TaskListEntity>,
    today: LocalDate,
    onClose: (TaskDraft) -> Unit,
    onDelete: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    var picker by remember { mutableStateOf<Picker?>(null) }
    val isNew = initial.id == null
    val green = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = { onClose(draft) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardSurface,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            // Title, with the circle that ticks the task off.
            val titleFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                if (isNew) runCatching { titleFocus.requestFocus() }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckCircle(
                    checked = draft.completed,
                    onClick = { draft = draft.copy(completed = !draft.completed) },
                    size = 34.dp,
                )
                TextField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it.take(120)) },
                    placeholder = { Text("Task title", style = MaterialTheme.typography.headlineSmall) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(titleFocus),
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SheetCard,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    SettingRow(
                        icon = Icons.Filled.DateRange,
                        label = "Date",
                        value = draft.date?.let { relativeDate(it, today) } ?: "None",
                        highlighted = draft.date != null,
                        onClick = { picker = Picker.DATE },
                        // Clearing the date also clears what depends on it.
                        onClear = if (draft.date == null) null else {
                            {
                                draft = draft.copy(
                                    date = null,
                                    timeMinute = null,
                                    reminderOffsetMinutes = null,
                                    repeat = TaskRepeat.NEVER,
                                )
                            }
                        },
                    )
                    SettingRow(
                        emoji = "🕒",
                        label = "Time",
                        value = draft.timeMinute?.let(::formatClock) ?: "None",
                        highlighted = draft.timeMinute != null,
                        onClick = { picker = Picker.TIME },
                    )
                    SettingRow(
                        icon = Icons.Filled.Notifications,
                        label = "Reminder",
                        // A reminder is counted back from the task's time, so it needs one first.
                        value = if (draft.timeMinute == null) {
                            "Set a time first"
                        } else {
                            reminderOffsetLabel(draft.reminderOffsetMinutes)
                        },
                        highlighted = draft.reminderOffsetMinutes != null,
                        enabled = draft.timeMinute != null,
                        onClick = { picker = Picker.REMINDER },
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.Filled.Refresh,
                        label = "Repeat",
                        value = draft.repeat.label,
                        highlighted = draft.repeat != TaskRepeat.NEVER,
                        onClick = { picker = Picker.REPEAT },
                    )
                    RowDivider()
                    SettingRow(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "List",
                        value = lists.firstOrNull { it.id == draft.listId }?.name.orEmpty(),
                        highlighted = true,
                        onClick = { picker = Picker.LIST },
                    )
                    RowDivider()
                    SettingRow(
                        emoji = "⚑",
                        emojiColor = priorityColor(draft.priority),
                        label = draft.priority.label,
                        value = "",
                        onClick = { picker = Picker.PRIORITY },
                    )
                }
            }

            SubtasksCard(
                subtasks = draft.subtasks,
                onChange = { draft = draft.copy(subtasks = it) },
            )

            HorizontalDivider(color = CardOutline, modifier = Modifier.padding(top = 16.dp))
            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(
                    text = if (isNew) "Discard" else "Delete",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    when (picker) {
        Picker.DATE -> DateDialog(
            initial = draft.date ?: today,
            onDismiss = { picker = null },
            onPick = {
                draft = draft.copy(date = it)
                picker = null
            },
        )

        Picker.TIME -> TimeDialog(
            initialMinute = draft.timeMinute,
            onDismiss = { picker = null },
            onPick = { minute ->
                draft = draft.copy(
                    timeMinute = minute,
                    // A time needs a day to be on.
                    date = if (minute != null) draft.date ?: today else draft.date,
                    reminderOffsetMinutes = if (minute == null) null else draft.reminderOffsetMinutes,
                )
                picker = null
            },
        )

        Picker.REMINDER -> OptionsDialog(
            title = "Reminder",
            options = listOf<Int?>(null) + REMINDER_OFFSETS,
            selected = draft.reminderOffsetMinutes,
            label = ::reminderOffsetLabel,
            onDismiss = { picker = null },
            onSelect = {
                draft = draft.copy(reminderOffsetMinutes = it)
                picker = null
            },
        )

        Picker.REPEAT -> OptionsDialog(
            title = "Repeat",
            options = TaskRepeat.entries,
            selected = draft.repeat,
            label = { it.label },
            onDismiss = { picker = null },
            onSelect = {
                // A repeating task needs a date to repeat from.
                draft = draft.copy(repeat = it, date = if (it != TaskRepeat.NEVER) draft.date ?: today else draft.date)
                picker = null
            },
        )

        Picker.LIST -> OptionsDialog(
            title = "List",
            options = lists.map { it.id },
            selected = draft.listId,
            label = { id -> lists.firstOrNull { it.id == id }?.name.orEmpty() },
            onDismiss = { picker = null },
            onSelect = {
                draft = draft.copy(listId = it)
                picker = null
            },
        )

        Picker.PRIORITY -> OptionsDialog(
            title = "Priority",
            options = TaskPriority.entries.reversed(),
            selected = draft.priority,
            label = { it.label },
            onDismiss = { picker = null },
            onSelect = {
                draft = draft.copy(priority = it)
                picker = null
            },
        )

        null -> Unit
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = CardOutline, modifier = Modifier.padding(start = 68.dp))
}

/** One line of the sheet: an icon, a label, the current value, and a chevron. */
@Composable
private fun SettingRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    emoji: String? = null,
    emojiColor: Color = TextMuted,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClear: (() -> Unit)? = null,
) {
    val green = MaterialTheme.colorScheme.primary
    val contentAlpha = if (enabled) 1f else 0.45f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = if (highlighted) green.copy(alpha = 0.16f) else Color(0xFF242424),
                    shape = RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = (if (highlighted) green else TextMuted).copy(alpha = contentAlpha),
                    modifier = Modifier.size(20.dp),
                )
            } else if (emoji != null) {
                Text(emoji, color = emojiColor.copy(alpha = contentAlpha))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        )
        Text(
            text = value,
            color = (if (highlighted) green else TextMuted).copy(alpha = contentAlpha),
        )
        if (onClear != null) {
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Clear $label",
                    tint = TextMuted,
                    modifier = Modifier
                        .background(Color(0xFF242424), CircleShape)
                        .padding(4.dp)
                        .size(16.dp),
                )
            }
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = contentAlpha),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

/** The checklist of smaller items inside a task. */
@Composable
private fun SubtasksCard(subtasks: List<SubtaskDraft>, onChange: (List<SubtaskDraft>) -> Unit) {
    var newTitle by remember { mutableStateOf("") }
    val add = {
        if (newTitle.isNotBlank()) {
            onChange(subtasks + SubtaskDraft(newTitle.trim()))
            newTitle = ""
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SheetCard,
        modifier = Modifier.padding(top = 12.dp),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sub-tasks", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (subtasks.isNotEmpty()) {
                    Text(
                        text = "${subtasks.count { it.completed }}/${subtasks.size}",
                        color = TextMuted,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            }

            subtasks.forEachIndexed { index, sub ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckCircle(
                        checked = sub.completed,
                        onClick = {
                            onChange(subtasks.toMutableList().also { it[index] = sub.copy(completed = !sub.completed) })
                        },
                        size = 24.dp,
                    )
                    Text(
                        text = sub.title,
                        color = if (sub.completed) TextMuted else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (sub.completed) TextDecoration.LineThrough else null,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    )
                    IconButton(onClick = { onChange(subtasks.toMutableList().also { it.removeAt(index) }) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove sub-task", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it.take(80) },
                    placeholder = { Text("Add sub-task") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = add, enabled = newTitle.isNotBlank()) {
                    Icon(Icons.Filled.Add, contentDescription = "Add sub-task")
                }
            }
        }
    }
}

/** A dialog listing choices, with the current one marked. Picking one closes it. */
@Composable
internal fun <T> OptionsDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = { onSelect(option) })
                        Text(label(option), modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateDialog(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    // The date picker works in UTC milliseconds; a calendar day converts to and from UTC midnight.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onPick(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = state.selectedDateMillis != null,
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

/** Picks a time of day. [onPick] receives minutes after midnight, or null when the time is cleared. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initialMinute: Int?, onDismiss: () -> Unit, onPick: (Int?) -> Unit) {
    val start = initialMinute ?: (9 * 60)
    val state = rememberTimePickerState(
        initialHour = start / 60,
        initialMinute = start % 60,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) { Text("OK") }
        },
        dismissButton = {
            Row {
                if (initialMinute != null) {
                    TextButton(onClick = { onPick(null) }) { Text("Clear") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
