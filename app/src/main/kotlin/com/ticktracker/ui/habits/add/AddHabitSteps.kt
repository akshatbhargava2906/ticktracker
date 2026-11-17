package com.ticktracker.ui.habits.add

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import com.ticktracker.ui.habits.HabitBadge
import com.ticktracker.ui.habits.HabitColors
import com.ticktracker.ui.habits.HabitIconGlyph
import com.ticktracker.ui.habits.HabitIcons
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface
import com.ticktracker.util.cleanDecimalInput
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Step 1: What kind of habit?
// ---------------------------------------------------------------------------------------------

@Composable
fun KindStep(state: AddHabitState, viewModel: AddHabitViewModel) {
    StepTitle("What kind of habit?")

    val isTick = state.kind == HabitKind.TICK
    KindCard(
        title = "Tick",
        subtitle = "Track yes or no per day",
        selected = isTick,
        onClick = { viewModel.update { it.copy(kind = HabitKind.TICK) } },
    )

    if (isTick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DirectionButton(
                label = "Build a Habit",
                selected = state.direction == HabitDirection.BUILD,
                onClick = { viewModel.update { it.copy(direction = HabitDirection.BUILD) } },
                modifier = Modifier.weight(1f),
            )
            DirectionButton(
                label = "Quit a Habit",
                selected = state.direction == HabitDirection.QUIT,
                onClick = { viewModel.update { it.copy(direction = HabitDirection.QUIT) } },
                modifier = Modifier.weight(1f),
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        ) {
            Text(
                text = if (state.direction == HabitDirection.BUILD) {
                    "Add a new positive routine. Daily completions build your streak."
                } else {
                    "Break a routine you want to stop. Each day you resist builds your streak."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    KindCard(
        title = "Measurable",
        subtitle = "Track numbers, distance, or time",
        selected = !isTick,
        onClick = { viewModel.update { it.copy(kind = HabitKind.MEASURABLE) } },
    )
}

@Composable
private fun KindCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) colors.primary.copy(alpha = 0.12f) else CardSurface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else CardOutline),
        onClick = onClick,
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            SelectionDot(selected)
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(24.dp)
            .border(2.dp, if (selected) colors.primary else colors.onSurfaceVariant, CircleShape)
            .padding(5.dp)
            .background(if (selected) colors.primary else Color.Transparent, CircleShape),
    )
}

@Composable
private fun DirectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) colors.primary else CardOutline),
        onClick = onClick,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Step 2: Name, icon, colour
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NameStep(state: AddHabitState, viewModel: AddHabitViewModel) {
    val habitColor = Color(state.color)
    StepTitle("Name Your Habit")

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        HabitBadge(icon = state.icon, color = state.color, size = 96.dp)
    }

    TextField(
        value = state.name,
        onValueChange = { text -> viewModel.update { it.copy(name = text.take(40)) } },
        placeholder = {
            Text(
                "Habit name",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        ),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = habitColor,
            unfocusedIndicatorColor = habitColor.copy(alpha = 0.5f),
            cursorColor = habitColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    )

    var allIcons by rememberSaveable { mutableStateOf(false) }
    FieldLabel("Select Icon")
    PickerCard(expanded = allIcons, onToggle = { allIcons = !allIcons }) {
        val glyphs = if (allIcons) HabitIcons.glyphs else HabitIcons.glyphs.take(ICONS_PER_ROW)
        val emoji = if (allIcons) HabitIcons.emoji else HabitIcons.emoji.take(ICONS_PER_ROW)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (glyphs + emoji).forEach { icon ->
                IconChoice(
                    icon = icon,
                    tint = habitColor,
                    selected = icon == state.icon,
                    onClick = { viewModel.update { it.copy(icon = icon) } },
                )
            }
        }
    }

    var allColors by rememberSaveable { mutableStateOf(false) }
    FieldLabel("Select Color")
    PickerCard(expanded = allColors, onToggle = { allColors = !allColors }) {
        val shown = if (allColors) HabitColors.all else HabitColors.all.take(HabitColors.PRESET_COUNT)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shown.forEach { color ->
                val selected = color == state.color
                Box(
                    Modifier
                        .size(34.dp)
                        .border(2.dp, if (selected) Color.White else Color.Transparent, CircleShape)
                        .padding(if (selected) 4.dp else 0.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .clickable { viewModel.update { it.copy(color = color) } },
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

private const val ICONS_PER_ROW = 6

/** The card around the icon and colour pickers, with its "See All" / "Show Less" toggle. */
@Composable
private fun PickerCard(expanded: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardOutline),
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
            content()
            TextButton(onClick = onToggle, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(if (expanded) "Show Less" else "See All")
            }
        }
    }
}

@Composable
private fun IconChoice(icon: String, tint: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) tint.copy(alpha = 0.18f) else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (selected) tint else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        HabitIconGlyph(
            icon = icon,
            tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
            size = 26.dp,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Step 3: How do you track it?
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackingStep(
    state: AddHabitState,
    categories: List<CategoryEntity>,
    viewModel: AddHabitViewModel,
) {
    StepTitle("How do you track it?")

    if (state.isEditing) {
        Text(
            text = "The kind of tracking, the repeat goal, the unit and the goal period are fixed " +
                "once a habit is created. Counts, the target and categories can be changed.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }

    when {
        state.kind == HabitKind.MEASURABLE -> MeasurableFields(state, viewModel)
        // Assumed: a Quit habit is judged once a day, so it has no repeat goal to choose.
        state.direction == HabitDirection.BUILD -> RepeatGoalFields(state, viewModel)
    }

    var showCustomDialog by remember { mutableStateOf(false) }
    FieldLabel("Categories")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            ChoiceChip(
                label = category.name,
                selected = category.id in state.categoryIds,
                onClick = { viewModel.toggleCategory(category.id) },
            )
        }
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            onClick = { showCustomDialog = true },
        ) {
            Text(
                text = "+ Custom",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    if (showCustomDialog) {
        CustomCategoryDialog(
            onDismiss = { showCustomDialog = false },
            onAdd = { name ->
                viewModel.addCustomCategory(name)
                showCustomDialog = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepeatGoalFields(state: AddHabitState, viewModel: AddHabitViewModel) {
    FieldLabel("Repeat Goal")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChoiceChip(
            label = "Once a Day",
            selected = state.repeatGoal == RepeatGoal.ONCE_A_DAY,
            onClick = { viewModel.update { it.copy(repeatGoal = RepeatGoal.ONCE_A_DAY) } },
        )
        ChoiceChip(
            label = "Multiple per Day",
            selected = state.repeatGoal == RepeatGoal.MULTIPLE_PER_DAY,
            onClick = { viewModel.update { it.copy(repeatGoal = RepeatGoal.MULTIPLE_PER_DAY) } },
        )
        ChoiceChip(
            label = "Weekly Goal",
            selected = state.repeatGoal == RepeatGoal.WEEKLY_GOAL,
            onClick = { viewModel.update { it.copy(repeatGoal = RepeatGoal.WEEKLY_GOAL) } },
        )
    }

    when (state.repeatGoal) {
        RepeatGoal.MULTIPLE_PER_DAY -> CountStepper(
            count = state.perDayCount,
            suffix = "day",
            range = AddHabitState.PER_DAY_RANGE,
            onChange = { count -> viewModel.update { it.copy(perDayCount = count) } },
            modifier = Modifier.padding(top = 20.dp),
        )
        RepeatGoal.WEEKLY_GOAL -> CountStepper(
            count = state.perWeekCount,
            suffix = "week",
            range = AddHabitState.PER_WEEK_RANGE,
            onChange = { count -> viewModel.update { it.copy(perWeekCount = count) } },
            modifier = Modifier.padding(top = 20.dp),
        )
        RepeatGoal.ONCE_A_DAY -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeasurableFields(state: AddHabitState, viewModel: AddHabitViewModel) {
    FieldLabel("Unit")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AddHabitState.UNITS.forEach { unit ->
            ChoiceChip(
                label = unit,
                selected = state.unit == unit,
                onClick = { viewModel.update { it.copy(unit = unit) } },
            )
        }
        ChoiceChip(
            label = "Custom",
            selected = state.unit == AddHabitState.CUSTOM_UNIT,
            onClick = { viewModel.update { it.copy(unit = AddHabitState.CUSTOM_UNIT) } },
        )
    }
    if (state.unit == AddHabitState.CUSTOM_UNIT) {
        OutlinedTextField(
            value = state.customUnit,
            onValueChange = { text -> viewModel.update { it.copy(customUnit = text.take(12)) } },
            label = { Text("Custom unit") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
    }

    FieldLabel("Goal")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChip(
            label = "Everyday",
            selected = state.goalPeriod == GoalPeriod.EVERYDAY,
            onClick = { viewModel.update { it.copy(goalPeriod = GoalPeriod.EVERYDAY) } },
        )
        ChoiceChip(
            label = "Weekly",
            selected = state.goalPeriod == GoalPeriod.WEEKLY,
            onClick = { viewModel.update { it.copy(goalPeriod = GoalPeriod.WEEKLY) } },
        )
    }

    // Assumed: with a Weekly goal the typed target is for the whole week (flow_state.md, question 4).
    val weekly = state.goalPeriod == GoalPeriod.WEEKLY
    FieldLabel(if (weekly) "Weekly Target" else "Daily Target")
    OutlinedTextField(
        value = state.targetText,
        onValueChange = { text -> viewModel.update { it.copy(targetText = cleanDecimalInput(text)) } },
        placeholder = { Text("0") },
        suffix = { Text(state.effectiveUnit) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CustomCategoryDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(20) },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ---------------------------------------------------------------------------------------------
// Step 4: Reminder
// ---------------------------------------------------------------------------------------------

@Composable
fun ReminderStep(state: AddHabitState, viewModel: AddHabitViewModel) {
    // Android 13 and later only show notifications once the user has allowed them.
    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* The reminder is saved either way; it shows once notifications are allowed. */ }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set a Daily Reminder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = state.remindersOn,
            onCheckedChange = { on ->
                viewModel.update { it.copy(remindersOn = on) }
                if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
    }
    Text(
        text = "A reminder stays on your phone until you dismiss it.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
    )

    if (!state.remindersOn) return

    // The wheels start at 9:00 AM.
    var hourIndex by rememberSaveable { mutableIntStateOf(8) }
    var minuteIndex by rememberSaveable { mutableIntStateOf(0) }
    var isPm by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardOutline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            WheelPicker(
                items = HOURS,
                initialIndex = hourIndex,
                onSelected = { hourIndex = it },
                modifier = Modifier.width(88.dp),
            )
            Text(":", fontSize = 28.sp, modifier = Modifier.padding(horizontal = 8.dp))
            WheelPicker(
                items = MINUTES,
                initialIndex = minuteIndex,
                onSelected = { minuteIndex = it },
                modifier = Modifier.width(88.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AmPmButton("AM", selected = !isPm) { isPm = false }
                AmPmButton("PM", selected = isPm) { isPm = true }
            }
        }
    }

    OutlinedButton(
        onClick = {
            val hour12 = hourIndex + 1
            val hour24 = hour12 % 12 + if (isPm) 12 else 0
            viewModel.addReminder(hour24 * 60 + minuteIndex * MINUTE_STEP)
        },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(54.dp),
    ) {
        Text("+  Set This Time")
    }

    state.reminderMinutes.forEach { minute ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
                ) {
                    Text(
                        text = formatTime(minute),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Daily", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.removeReminder(minute) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove reminder")
                }
            }
        }
    }

    Text(
        text = "More reminders can be added later from the habit settings.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    )
}

@Composable
private fun AmPmButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primary else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) colors.primary else CardOutline),
        onClick = onClick,
    ) {
        Text(
            text = label,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

private const val MINUTE_STEP = 5
private val HOURS = (1..12).map { String.format(Locale.ROOT, "%02d", it) }
private val MINUTES = (0 until 60 step MINUTE_STEP).map { String.format(Locale.ROOT, "%02d", it) }

/** A time of day as 24-hour text, such as "09:00" or "18:00". */
fun formatTime(minuteOfDay: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60)
