package com.ticktracker.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ticktracker.TickTrackerApp
import com.ticktracker.data.db.BlockKind
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.repository.PlanRepository
import com.ticktracker.data.repository.TaskDraft
import com.ticktracker.data.repository.TaskRepository
import com.ticktracker.data.settings.AppSettings
import com.ticktracker.data.settings.SettingsStore
import com.ticktracker.domain.TimelineEntry
import com.ticktracker.domain.layoutDay
import com.ticktracker.util.nowMinute
import com.ticktracker.util.nowMinuteFlow
import com.ticktracker.util.today
import com.ticktracker.util.todayFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How long a timed task with no duration shows on the timeline. */
const val DEFAULT_TASK_MINUTES = 30

/** One thing on the Plan timeline: a planned block or a timed task (D30, D35). */
sealed interface PlanItem {
    val key: String
    val start: Int
    val duration: Int
    val title: String

    /** Made with "Plan this slot". */
    data class Block(val block: PlanBlockEntity) : PlanItem {
        override val key get() = "block-${block.id}"
        override val start get() = block.startMinute
        override val duration get() = block.durationMinutes
        override val title get() = block.title
    }

    /** A task with a time on this day. */
    data class Task(val task: TaskEntity) : PlanItem {
        override val key get() = "task-${task.id}"
        override val start get() = task.timeMinute ?: 0
        override val duration get() = task.durationMinutes ?: DEFAULT_TASK_MINUTES
        override val title get() = task.title
    }

}

data class PlanUiState(
    val date: LocalDate = today(),
    val today: LocalDate = today(),
    val nowMinute: Int = nowMinute(),
    val settings: AppSettings = AppSettings(),
    val entries: List<TimelineEntry<PlanItem>> = emptyList(),
    val lists: List<TaskListEntity> = emptyList(),
    /** False until the first read from the database has arrived. */
    val loaded: Boolean = false,
)

/** Supplies the Plan section and carries out its actions (D30, D32, D35). */
class PlanViewModel(
    private val plans: PlanRepository,
    private val tasks: TaskRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(today())

    private val clock = combine(selectedDate, todayFlow(), nowMinuteFlow()) { date, today, now ->
        Triple(date, today, now)
    }

    private val taskData = combine(tasks.observeTasks(), tasks.observeLists()) { taskList, lists -> taskList to lists }

    val uiState: StateFlow<PlanUiState> = combine(
        plans.observeBlocks(),
        taskData,
        settingsStore.settings,
        clock,
    ) { blocks, (taskList, lists), settings, (date, today, now) ->
        val items = buildList {
            blocks.filter { it.date == date }.forEach { add(PlanItem.Block(it)) }
            taskList.filter { it.date == date && it.timeMinute != null }.forEach { add(PlanItem.Task(it)) }
        }.sortedWith(compareBy({ it.start }, { it.title }))

        PlanUiState(
            date = date,
            today = today,
            nowMinute = now,
            settings = settings,
            entries = layoutDay(
                items = items,
                startOf = { it.start },
                endOf = { it.start + it.duration },
                dayStart = settings.dayStartMinute,
                windDown = settings.windDownMinute,
                now = if (date == today) now else null,
            ),
            lists = lists,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanUiState())

    /** The day a block is on, to show it when the app is opened from its reminder. */
    suspend fun blockDate(id: Long): LocalDate? = plans.getBlock(id)?.date

    /** A task in full, with its sub-tasks, for the task sheet. */
    suspend fun loadTask(id: Long): TaskDraft? = tasks.getDraft(id)

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    /** The circle on a block: ticks it off or back on. Notes have no circle. */
    fun toggleBlock(block: PlanBlockEntity) = viewModelScope.launch {
        plans.setCompleted(block.id, !block.completed)
    }

    /** The circle on a task: the same as ticking it in the Tasks section. */
    fun toggleTask(task: TaskEntity) = viewModelScope.launch {
        tasks.setCompleted(task.id, !task.completed)
    }

    /**
     * "Plan this slot" (D36). A Task is stored as a real task, so it shows in the Tasks section
     * as well as on the timeline. A Note is stored as a Plan block. A note switched to Task
     * becomes a task, and the note is removed.
     */
    fun save(draft: PlanDraft) {
        if (draft.title.isBlank()) return
        viewModelScope.launch {
            when (draft.kind) {
                PlanKind.TASK -> {
                    val listId = draft.listId ?: uiState.value.lists.firstOrNull()?.id ?: return@launch
                    tasks.saveTask(
                        TaskDraft(
                            listId = listId,
                            title = draft.title,
                            date = draft.date,
                            timeMinute = draft.startMinute,
                            durationMinutes = draft.durationMinutes,
                            reminderOffsetMinutes = draft.reminderOffsetMinutes,
                            completed = draft.completed,
                        ),
                    )
                    draft.id?.let { plans.delete(it) }
                }

                PlanKind.NOTE -> plans.saveBlock(
                    PlanBlockEntity(
                        id = draft.id ?: 0,
                        title = draft.title.trim(),
                        date = draft.date,
                        startMinute = draft.startMinute,
                        durationMinutes = draft.durationMinutes,
                        kind = BlockKind.NOTE,
                        reminderOffsetMinutes = null,
                        createdAt = draft.createdAt ?: System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    fun deleteBlock(id: Long) = viewModelScope.launch { plans.delete(id) }

    /** Saving and deleting tasks opened from the timeline, through the task sheet. */
    fun saveTask(draft: TaskDraft) {
        if (draft.title.isBlank()) return
        viewModelScope.launch { tasks.saveTask(draft) }
    }

    fun deleteTask(id: Long) = viewModelScope.launch { tasks.deleteTask(id) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as TickTrackerApp).container
                PlanViewModel(c.planRepository, c.taskRepository, c.settingsStore)
            }
        }
    }
}
