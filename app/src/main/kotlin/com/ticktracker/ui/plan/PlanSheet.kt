package com.ticktracker.ui.plan

import com.ticktracker.ui.theme.RaisedSurface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ticktracker.data.db.BlockKind
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.domain.REMINDER_OFFSETS
import com.ticktracker.domain.reminderOffsetLabel
import com.ticktracker.ui.habits.add.ChoiceChip
import com.ticktracker.ui.tasks.DateDialog
import com.ticktracker.ui.tasks.OptionsDialog
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatClock
import com.ticktracker.util.formatDuration
import com.ticktracker.util.relativeDate
import java.time.LocalDate

/** "What is this?" in the Plan this slot sheet (D30, D36). */
enum class PlanKind(val label: String, val description: String) {
    TASK("Task", "Tick it off here and in Tasks"),
    NOTE("Note", "Just logging, e.g. \"Scrolled Insta\". Stays on the Plan only"),
}

/**
 * What is being planned in the sheet. [id] is a note being edited, or null for something new.
 * A Task is saved as a real task in [listId] (D36); a Note is saved as a Plan block.
 */
data class PlanDraft(
    val id: Long? = null,
    val listId: Long? = null,
    val title: String = "",
    val date: LocalDate,
    val startMinute: Int,
    val durationMinutes: Int = 60,
    val kind: PlanKind = PlanKind.TASK,
    val reminderOffsetMinutes: Int? = null,
    val completed: Boolean = false,
    val createdAt: Long? = null,
) {
    companion object {
        fun from(block: PlanBlockEntity) = PlanDraft(
            id = block.id,
            title = block.title,
            date = block.date,
            startMinute = block.startMinute,
            durationMinutes = block.durationMinutes,
            kind = if (block.kind == BlockKind.NOTE) PlanKind.NOTE else PlanKind.TASK,
            reminderOffsetMinutes = block.reminderOffsetMinutes,
            completed = block.completed,
            createdAt = block.createdAt,
        )
    }
}

/** Lengths offered for a block. */
private val DURATIONS = listOf(15, 30, 45, 60, 90, 120, 180, 240)

/**
 * The "Plan this slot" sheet (D30). Saved with the tick in the corner; closing the sheet
 * any other way discards the changes, as in the reference app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanSheet(
    initial: PlanDraft,
    today: LocalDate,
    lists: List<TaskListEntity>,
    onSave: (PlanDraft) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial.copy(listId = initial.listId ?: lists.firstOrNull()?.id)) }
    var picking by remember { mutableStateOf<String?>(null) }
    val isNew = initial.id == null
    val green = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardSurface,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isNew) "Plan this slot" else "Edit note",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                val canSave = draft.title.isNotBlank()
                Surface(
                    shape = CircleShape,
                    color = if (canSave) green else green.copy(alpha = 0.3f),
                    enabled = canSave,
                    onClick = { onSave(draft) },
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(26.dp),
                    )
                }
            }

            val focus = remember { FocusRequester() }
            LaunchedEffect(Unit) { if (isNew) runCatching { focus.requestFocus() } }
            TextField(
                value = draft.title,
                onValueChange = { draft = draft.copy(title = it.take(120)) },
                placeholder = { Text("What are you working on?", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = green,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .focusRequester(focus),
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FieldButton(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null, tint = green) },
                    text = relativeDate(draft.date, today),
                    onClick = { picking = "date" },
                    modifier = Modifier.weight(1f),
                )
                FieldButton(
                    icon = { Text("🕒") },
                    text = "${formatClock(draft.startMinute)} · ${formatDuration(draft.durationMinutes)}",
                    onClick = { picking = "time" },
                    modifier = Modifier.weight(1.2f),
                )
            }

            Text(
                text = "What is this?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            PlanKind.entries.forEach { kind ->
                val selected = draft.kind == kind
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { draft = draft.copy(kind = kind) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RadioButton(selected = selected, onClick = { draft = draft.copy(kind = kind) })
                    Column(Modifier.padding(start = 8.dp, top = 10.dp)) {
                        Text(
                            text = kind.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) green else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(kind.description, color = TextMuted)
                    }
                }
            }

            // A task goes into one of the task lists. Only worth asking when there is a choice.
            if (draft.kind == PlanKind.TASK && lists.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { picking = "list" }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = TextMuted)
                    Text(
                        "List",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                    )
                    Text(lists.firstOrNull { it.id == draft.listId }?.name.orEmpty(), color = green)
                }
            }

            // Notes are only a record, so they have nothing to be reminded of.
            if (draft.kind != PlanKind.NOTE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { picking = "reminder" }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = TextMuted)
                    Text(
                        "Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                    )
                    Text(
                        text = reminderOffsetLabel(draft.reminderOffsetMinutes),
                        color = if (draft.reminderOffsetMinutes != null) green else TextMuted,
                    )
                }
            }

            if (onDelete != null) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    when (picking) {
        "date" -> DateDialog(
            initial = draft.date,
            onDismiss = { picking = null },
            onPick = {
                draft = draft.copy(date = it)
                picking = null
            },
        )

        "time" -> StartAndLengthDialog(
            startMinute = draft.startMinute,
            durationMinutes = draft.durationMinutes,
            onDismiss = { picking = null },
            onPick = { start, length ->
                draft = draft.copy(startMinute = start, durationMinutes = length)
                picking = null
            },
        )

        "list" -> OptionsDialog(
            title = "List",
            options = lists.map { it.id },
            selected = draft.listId,
            label = { id -> lists.firstOrNull { it.id == id }?.name.orEmpty() },
            onDismiss = { picking = null },
            onSelect = {
                draft = draft.copy(listId = it)
                picking = null
            },
        )

        "reminder" -> OptionsDialog(
            title = "Reminder",
            options = listOf<Int?>(null) + REMINDER_OFFSETS,
            selected = draft.reminderOffsetMinutes,
            label = ::reminderOffsetLabel,
            onDismiss = { picking = null },
            onSelect = {
                draft = draft.copy(reminderOffsetMinutes = it)
                picking = null
            },
        )
    }
}

@Composable
private fun FieldButton(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = RaisedSurface,
        border = BorderStroke(1.dp, CardOutline),
        onClick = onClick,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(text, modifier = Modifier.padding(start = 10.dp), maxLines = 1)
        }
    }
}

/** Picks a start time and a length together. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun StartAndLengthDialog(
    startMinute: Int,
    durationMinutes: Int,
    onDismiss: () -> Unit,
    onPick: (start: Int, length: Int) -> Unit,
) {
    val time = rememberTimePickerState(
        initialHour = startMinute / 60,
        initialMinute = startMinute % 60,
        is24Hour = false,
    )
    var length by remember { mutableIntStateOf(durationMinutes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TimePicker(state = time)
                Text("Length", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (DURATIONS + durationMinutes).distinct().sorted().forEach { minutes ->
                        ChoiceChip(
                            label = formatDuration(minutes),
                            selected = minutes == length,
                            onClick = { length = minutes },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(time.hour * 60 + time.minute, length) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
