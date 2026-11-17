package com.ticktracker.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ticktracker.TickTrackerApp
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.repository.HabitRepository
import com.ticktracker.domain.EntriesByDate
import com.ticktracker.domain.streak
import com.ticktracker.util.todayFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything one habit card needs to draw itself. */
data class HabitCardState(
    val habit: HabitEntity,
    val entries: EntriesByDate,
    val today: LocalDate,
    val streak: Int,
    val categoryIds: Set<Long>,
) {
    val todayEntry: HabitEntryEntity? get() = entries[today]
}

data class HabitsUiState(
    val cards: List<HabitCardState> = emptyList(),
    /** Categories that at least one habit uses, for the filter chips. */
    val categories: List<CategoryEntity> = emptyList(),
)

/**
 * Supplies the Habits section with its cards and carries out its logging actions.
 * Every action writes to today only (D16); the repository has no way to write another day.
 */
class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = combine(
        repository.observeHabits(),
        repository.observeAllEntries(),
        repository.observeCategoryLinks(),
        repository.observeCategories(),
        todayFlow(),
    ) { habits, entries, links, categories, today ->
        val entriesByHabit = entries.groupBy { it.habitId }
        val categoriesByHabit = links.groupBy({ it.habitId }, { it.categoryId })
        val cards = habits.map { habit ->
            val byDate = entriesByHabit[habit.id].orEmpty().associateBy { it.date }
            HabitCardState(
                habit = habit,
                entries = byDate,
                today = today,
                streak = habit.streak(byDate, today),
                categoryIds = categoriesByHabit[habit.id].orEmpty().toSet(),
            )
        }
        val usedCategoryIds = links.map { it.categoryId }.toSet()
        HabitsUiState(cards, categories.filter { it.id in usedCategoryIds })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

    /** True once any habit has a reminder. Decides whether reminder warnings are worth showing. */
    val hasReminders: StateFlow<Boolean> = repository.observeReminderCount()
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** The tick button. Tapping a completed tick undoes it; multiple-per-day adds one (D26). */
    fun onTick(card: HabitCardState) = viewModelScope.launch { repository.tapTick(card.habit.id) }

    /** The skip (−) button on weekly-goal habits. */
    fun onSkip(card: HabitCardState) = viewModelScope.launch {
        repository.toggleTodayState(card.habit.id, EntryState.SKIPPED)
    }

    /** The shield button on Quit habits. */
    fun onResist(card: HabitCardState) = viewModelScope.launch {
        repository.toggleTodayState(card.habit.id, EntryState.RESISTED)
    }

    /** The × button on Quit habits. */
    fun onSlip(card: HabitCardState) = viewModelScope.launch {
        repository.toggleTodayState(card.habit.id, EntryState.SLIPPED)
    }

    /** Sets today's amount on a Measurable habit, replacing any earlier one (D17). */
    fun onSetAmount(habitId: Long, amount: Double) = viewModelScope.launch {
        repository.setTodayAmount(habitId, amount)
    }

    /** The reset button: removes today's entry (D15). */
    fun onReset(habitId: Long) = viewModelScope.launch { repository.clearToday(habitId) }

    fun onDelete(habitId: Long) = viewModelScope.launch { repository.deleteHabit(habitId) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TickTrackerApp
                HabitsViewModel(app.container.habitRepository)
            }
        }
    }
}
