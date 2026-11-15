package com.ticktracker.ui.habits.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ticktracker.TickTrackerApp
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import com.ticktracker.data.repository.HabitRepository
import com.ticktracker.ui.habits.HabitColors
import com.ticktracker.ui.habits.HabitIcons
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the wizard has collected so far (D06). */
data class AddHabitState(
    /** 0 kind, 1 name, 2 tracking, 3 reminder. */
    val step: Int = 0,
    val kind: HabitKind = HabitKind.TICK,
    val direction: HabitDirection = HabitDirection.BUILD,
    val name: String = "",
    val icon: String = HabitIcons.default,
    val color: Long = HabitColors.default,
    val repeatGoal: RepeatGoal = RepeatGoal.ONCE_A_DAY,
    val perDayCount: Int = 2,
    val perWeekCount: Int = 3,
    val unit: String = "min",
    val customUnit: String = "",
    val targetText: String = "",
    val goalPeriod: GoalPeriod = GoalPeriod.EVERYDAY,
    val categoryIds: Set<Long> = emptySet(),
    val remindersOn: Boolean = false,
    /** Minutes after midnight, sorted, no duplicates. */
    val reminderMinutes: List<Int> = emptyList(),
    val saving: Boolean = false,
) {
    val isLastStep: Boolean get() = step == LAST_STEP

    /** The typed target as a number, or null if it is empty or not above zero. */
    val target: Double? get() = targetText.toDoubleOrNull()?.takeIf { it > 0 }

    val effectiveUnit: String get() = if (unit == CUSTOM_UNIT) customUnit.trim() else unit

    /** Whether the current step is complete enough to move on. */
    val canContinue: Boolean
        get() = when (step) {
            1 -> name.isNotBlank()
            2 -> kind == HabitKind.TICK || (target != null && effectiveUnit.isNotEmpty())
            else -> true
        }

    companion object {
        const val STEP_COUNT = 4
        const val LAST_STEP = STEP_COUNT - 1

        /** Marker for the "custom" choice in the unit list. */
        const val CUSTOM_UNIT = "custom"

        /** Units offered for measurable habits (D07). */
        val UNITS = listOf(
            "hr", "min", "sec", "km", "mi", "pages", "glasses", "reps", "kcal", "words", "$",
        )

        val PER_DAY_RANGE = 2..20
        val PER_WEEK_RANGE = 1..7
    }
}

class AddHabitViewModel(private val repository: HabitRepository) : ViewModel() {
    var state by mutableStateOf(AddHabitState())
        private set

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun update(change: (AddHabitState) -> AddHabitState) {
        state = change(state)
    }

    fun next() {
        if (state.canContinue && !state.isLastStep) state = state.copy(step = state.step + 1)
    }

    fun back() {
        if (state.step > 0) state = state.copy(step = state.step - 1)
    }

    fun toggleCategory(id: Long) {
        val ids = state.categoryIds
        state = state.copy(categoryIds = if (id in ids) ids - id else ids + id)
    }

    /** "+ Custom": creates the category if needed and selects it. */
    fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = repository.addCategory(trimmed)
            state = state.copy(categoryIds = state.categoryIds + id)
        }
    }

    fun addReminder(minuteOfDay: Int) {
        state = state.copy(reminderMinutes = (state.reminderMinutes + minuteOfDay).distinct().sorted())
    }

    fun removeReminder(minuteOfDay: Int) {
        state = state.copy(reminderMinutes = state.reminderMinutes - minuteOfDay)
    }

    /** "Save Habit": stores the habit, its categories and reminders, then calls [onSaved]. */
    fun save(onSaved: () -> Unit) {
        val s = state
        if (s.saving) return
        state = s.copy(saving = true)

        val isTick = s.kind == HabitKind.TICK
        // Assumed: a Quit habit is always judged once a day, so it has no repeat goal choice.
        val repeatGoal = when {
            !isTick -> null
            s.direction == HabitDirection.QUIT -> RepeatGoal.ONCE_A_DAY
            else -> s.repeatGoal
        }
        val habit = HabitEntity(
            name = s.name.trim(),
            icon = s.icon,
            color = s.color,
            kind = s.kind,
            direction = if (isTick) s.direction else null,
            repeatGoal = repeatGoal,
            targetCount = when (repeatGoal) {
                RepeatGoal.MULTIPLE_PER_DAY -> s.perDayCount
                RepeatGoal.WEEKLY_GOAL -> s.perWeekCount
                else -> null
            },
            unit = if (isTick) null else s.effectiveUnit,
            targetAmount = if (isTick) null else s.target,
            goalPeriod = if (isTick) null else s.goalPeriod,
            createdAt = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            repository.createHabit(
                habit = habit,
                categoryIds = s.categoryIds.toList(),
                reminderMinutes = if (s.remindersOn) s.reminderMinutes else emptyList(),
            )
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TickTrackerApp
                AddHabitViewModel(app.container.habitRepository)
            }
        }
    }
}
