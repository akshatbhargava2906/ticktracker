package com.ticktracker.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ticktracker.TickTrackerApp
import com.ticktracker.data.db.SubtaskEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.repository.SubtaskDraft
import com.ticktracker.data.repository.TaskDraft
import com.ticktracker.data.repository.TaskRepository
import com.ticktracker.util.today
import com.ticktracker.util.todayFlow
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A task together with its sub-tasks, as the lists show it. */
data class TaskItem(val task: TaskEntity, val subtasks: List<SubtaskEntity>) {
    val subtasksDone: Int get() = subtasks.count { it.completed }

    /** The same task in the form the task sheet edits. */
    fun toDraft() = TaskDraft(
        id = task.id,
        listId = task.listId,
        title = task.title,
        date = task.date,
        timeMinute = task.timeMinute,
        durationMinutes = task.durationMinutes,
        reminderOffsetMinutes = task.reminderOffsetMinutes,
        repeat = task.repeat,
        priority = task.priority,
        completed = task.completed,
        subtasks = subtasks.map { SubtaskDraft(it.title, it.completed) },
    )
}

data class TasksUiState(
    val items: List<TaskItem> = emptyList(),
    val lists: List<TaskListEntity> = emptyList(),
    val today: LocalDate = today(),
    /** False until the first read from the database has arrived. */
    val loaded: Boolean = false,
)

/** Supplies the Tasks section and carries out its actions (D29). */
class TasksViewModel(private val repository: TaskRepository) : ViewModel() {

    val uiState: StateFlow<TasksUiState> = combine(
        repository.observeTasks(),
        repository.observeSubtasks(),
        repository.observeLists(),
        todayFlow(),
    ) { tasks, subtasks, lists, today ->
        val subtasksByTask = subtasks.groupBy { it.taskId }
        TasksUiState(
            items = tasks.map { TaskItem(it, subtasksByTask[it.id].orEmpty()) },
            lists = lists,
            today = today,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    /** The circle on a task row: ticks the task off, or back on. */
    fun toggle(task: TaskEntity) = viewModelScope.launch {
        repository.setCompleted(task.id, !task.completed)
    }

    /** Closing the task sheet: stores the task. A task with no title is not stored. */
    fun save(draft: TaskDraft) {
        if (draft.title.isBlank()) return
        viewModelScope.launch { repository.saveTask(draft) }
    }

    fun delete(taskId: Long) = viewModelScope.launch { repository.deleteTask(taskId) }

    fun addList(name: String, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch { onCreated(repository.addList(name)) }
    }

    fun renameList(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.renameList(id, name) }
    }

    fun deleteList(id: Long) = viewModelScope.launch { repository.deleteList(id) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TickTrackerApp
                TasksViewModel(app.container.taskRepository)
            }
        }
    }
}
