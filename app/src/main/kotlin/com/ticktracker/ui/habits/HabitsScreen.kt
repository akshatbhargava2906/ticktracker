package com.ticktracker.ui.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.model.HabitKind
import com.ticktracker.ui.PlaceholderMessage
import com.ticktracker.ui.habits.cards.MeasureCard
import com.ticktracker.ui.habits.cards.TickCard
import com.ticktracker.ui.habits.cards.WeeklyCard
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.util.cleanDecimalInput
import com.ticktracker.util.formatAmount

/** The three tabs of the Habits section (D01). Tick is the default, as in the reference app. */
enum class HabitsTab(val label: String) {
    MEASURE("Measure"),
    TICK("Tick"),
    WEEKLY("Weekly"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    /** A habit to bring into view, set when the app was opened from its reminder. */
    focusHabitId: Long? = null,
    onFocusHandled: () -> Unit = {},
    /** Opens the habit in the editor (D31). */
    onEditHabit: (Long) -> Unit = {},
    viewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasReminders by viewModel.hasReminders.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var tab by rememberSaveable { mutableStateOf(HabitsTab.TICK) }
    var categoryFilter by rememberSaveable { mutableStateOf<Long?>(null) }

    // The habit whose amount is being entered, and the habit waiting for a delete confirmation.
    var enteringAmountFor by remember { mutableStateOf<HabitCardState?>(null) }
    var deleting by remember { mutableStateOf<HabitEntity?>(null) }

    // Opened from a reminder: switch to the tab the habit lives on, drop any filter that
    // would hide it, and scroll to its card. Waits until the habits have loaded.
    LaunchedEffect(focusHabitId, uiState.cards) {
        val id = focusHabitId ?: return@LaunchedEffect
        if (uiState.cards.isEmpty()) return@LaunchedEffect
        val target = uiState.cards.firstOrNull { it.habit.id == id }
        if (target != null) {
            val kind = target.habit.kind
            tab = if (kind == HabitKind.MEASURABLE) HabitsTab.MEASURE else HabitsTab.TICK
            categoryFilter = null
            val index = uiState.cards.filter { it.habit.kind == kind }.indexOf(target)
            withFrameNanos { } // let the list switch to the new tab before scrolling
            listState.scrollToItem(index.coerceAtLeast(0))
        }
        onFocusHandled()
    }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = tab.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            HabitsTab.entries.forEach { item ->
                Tab(
                    selected = item == tab,
                    onClick = { tab = item },
                    text = { Text(item.label) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Each tab shows the habits of its kind; Weekly shows all of them (flow_state.md section 3).
        val onTab = when (tab) {
            HabitsTab.MEASURE -> uiState.cards.filter { it.habit.kind == HabitKind.MEASURABLE }
            HabitsTab.TICK -> uiState.cards.filter { it.habit.kind == HabitKind.TICK }
            HabitsTab.WEEKLY -> uiState.cards
        }

        // Only categories used by a habit on this tab are offered as filters.
        val chips = uiState.categories.filter { category -> onTab.any { category.id in it.categoryIds } }
        val activeFilter = categoryFilter?.takeIf { id -> chips.any { it.id == id } }
        val visible = if (activeFilter == null) onTab else onTab.filter { activeFilter in it.categoryIds }

        if (chips.isNotEmpty()) {
            CategoryChips(
                categories = chips,
                selectedId = activeFilter,
                onSelect = { id -> categoryFilter = if (id == activeFilter) null else id },
            )
        }

        if (hasReminders) {
            ReminderHealthBanner(Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }

        if (visible.isEmpty()) {
            EmptyHabits(tab)
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(visible, key = { it.habit.id }) { card ->
                    val onDelete = { deleting = card.habit }
                    val onEdit = { onEditHabit(card.habit.id) }
                    when {
                        tab == HabitsTab.WEEKLY -> WeeklyCard(card, onDelete, onEdit)
                        card.habit.kind == HabitKind.MEASURABLE -> MeasureCard(
                            card = card,
                            onEnterAmount = { enteringAmountFor = card },
                            onReset = { viewModel.onReset(card.habit.id) },
                            onDelete = onDelete,
                            onEdit = onEdit,
                        )
                        else -> TickCard(
                            card = card,
                            onTick = { viewModel.onTick(card) },
                            onSkip = { viewModel.onSkip(card) },
                            onResist = { viewModel.onResist(card) },
                            onSlip = { viewModel.onSlip(card) },
                            onDelete = onDelete,
                            onEdit = onEdit,
                        )
                    }
                }
            }
        }
    }

    enteringAmountFor?.let { card ->
        AmountDialog(
            card = card,
            onDismiss = { enteringAmountFor = null },
            onSave = { amount ->
                viewModel.onSetAmount(card.habit.id, amount)
                enteringAmountFor = null
            },
        )
    }

    deleting?.let { habit ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete ${habit.name}?") },
            text = { Text("The habit, its history and its reminders will be removed. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDelete(habit.id)
                        deleting = null
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

/** The filter chips under the tabs. Tapping the selected chip clears the filter. */
@Composable
private fun CategoryChips(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            val selected = category.id == selectedId
            val colors = MaterialTheme.colorScheme
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent,
                border = BorderStroke(1.dp, if (selected) colors.primary else CardOutline),
                onClick = { onSelect(category.id) },
            ) {
                Text(
                    text = category.name,
                    color = if (selected) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/**
 * Enter or edit today's amount on a Measurable habit. Opens with the current amount,
 * and the number typed replaces it (D17).
 */
@Composable
private fun AmountDialog(card: HabitCardState, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    val habit = card.habit
    var text by remember { mutableStateOf(card.todayEntry?.amount?.let(::formatAmount).orEmpty()) }
    val amount = text.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(habit.name) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = cleanDecimalInput(it) },
                label = { Text("Today") },
                suffix = { Text(habit.unit.orEmpty()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        },
        confirmButton = {
            TextButton(onClick = { amount?.let(onSave) }, enabled = amount != null) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EmptyHabits(tab: HabitsTab) {
    when (tab) {
        HabitsTab.MEASURE -> PlaceholderMessage(
            title = "Log Your Hours",
            message = "Reading, exercise, meditation. Track time spent on what matters.",
        )
        HabitsTab.TICK -> PlaceholderMessage(
            title = "Build Your Streak",
            message = "Add a habit and tick it off each day.",
        )
        HabitsTab.WEEKLY -> PlaceholderMessage(
            title = "Your Week",
            message = "Habits you add will show their week here.",
        )
    }
}
