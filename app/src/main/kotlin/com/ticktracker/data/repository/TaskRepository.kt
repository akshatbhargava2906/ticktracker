package com.ticktracker.data.repository

import androidx.room.withTransaction
import com.ticktracker.data.db.AppDatabase
import com.ticktracker.data.db.SubtaskEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.data.model.TaskRepeat
import com.ticktracker.domain.nextRepeatDate
import com.ticktracker.reminders.ReminderScheduler
import com.ticktracker.util.today
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** A task as it is being created or edited in the task sheet. [id] is null for a new task. */
data class TaskDraft(
    val id: Long? = null,
    val listId: Long,
    val title: String = "",
    val date: LocalDate? = null,
    val timeMinute: Int? = null,
    val durationMinutes: Int? = null,
    val reminderOffsetMinutes: Int? = null,
    val repeat: TaskRepeat = TaskRepeat.NEVER,
    val priority: TaskPriority = TaskPriority.NONE,
    val completed: Boolean = false,
    val subtasks: List<SubtaskDraft> = emptyList(),
)

data class SubtaskDraft(val title: String, val completed: Boolean = false)

/** The only way screens read or change tasks, lists and sub-tasks (D29). */
class TaskRepository(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler,
) {
    private val dao = db.taskDao()

    fun observeTasks(): Flow<List<TaskEntity>> = dao.observeTasks()

    fun observeSubtasks(): Flow<List<SubtaskEntity>> = dao.observeSubtasks()

    fun observeLists(): Flow<List<TaskListEntity>> = dao.observeLists()

    /** Makes sure there is always at least one list. Safe to call on every launch. */
    suspend fun ensureDefaultList() {
        db.withTransaction {
            if (dao.listCount() == 0) addList(DEFAULT_LIST_NAME)
        }
    }

    suspend fun addList(name: String): Long =
        dao.insertList(TaskListEntity(name = name.trim(), createdAt = System.currentTimeMillis()))

    suspend fun renameList(id: Long, name: String) = dao.renameList(id, name.trim())

    /** Deletes a list and every task in it. The last remaining list cannot be deleted. */
    suspend fun deleteList(id: Long) {
        if (dao.listCount() <= 1) return
        dao.getTasksInList(id).forEach { scheduler.cancelTask(it.id) }
        dao.deleteList(id)
    }

    /**
     * Creates or updates a task from the task sheet, replaces its sub-tasks,
     * and sets or cancels its reminder. Returns the task's id.
     */
    suspend fun saveTask(draft: TaskDraft): Long {
        // A time needs a date; a reminder needs a time; a repeat needs a date.
        val date = draft.date
        val time = draft.timeMinute.takeIf { date != null }
        val reminder = draft.reminderOffsetMinutes.takeIf { time != null }
        val repeat = if (date == null) TaskRepeat.NEVER else draft.repeat

        val taskId = db.withTransaction {
            val existing = draft.id?.let { dao.getTask(it) }
            val base = existing ?: TaskEntity(
                listId = draft.listId,
                title = "",
                createdAt = System.currentTimeMillis(),
            )
            val task = base.copy(
                listId = draft.listId,
                title = draft.title.trim(),
                date = date,
                timeMinute = time,
                durationMinutes = draft.durationMinutes.takeIf { time != null },
                reminderOffsetMinutes = reminder,
                repeat = repeat,
                priority = draft.priority,
            )
            val id = if (existing == null) {
                dao.insertTask(task)
            } else {
                dao.updateTask(task)
                task.id
            }
            dao.deleteSubtasks(id)
            dao.insertSubtasks(
                draft.subtasks
                    .filter { it.title.isNotBlank() }
                    .mapIndexed { index, sub ->
                        SubtaskEntity(taskId = id, title = sub.title.trim(), completed = sub.completed, position = index)
                    },
            )
            id
        }

        val saved = dao.getTask(taskId) ?: return taskId
        if (saved.completed != draft.completed) {
            setCompleted(taskId, draft.completed)
        } else {
            scheduler.scheduleTask(saved)
        }
        return taskId
    }

    /**
     * Ticks a task off or back on. Ticking off a repeating task creates its next occurrence,
     * once: unticking and ticking again does not create another.
     */
    suspend fun setCompleted(id: Long, completed: Boolean) {
        val changed = db.withTransaction {
            val task = dao.getTask(id) ?: return@withTransaction emptyList()
            var updated = task.copy(
                completed = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
            )
            var next: TaskEntity? = null
            val nextDate = task.date?.let { nextRepeatDate(it, task.repeat, today()) }
            if (completed && nextDate != null && !task.repeatSpawned) {
                val copy = task.copy(
                    id = 0,
                    date = nextDate,
                    completed = false,
                    completedAt = null,
                    repeatSpawned = false,
                    createdAt = System.currentTimeMillis(),
                )
                val nextId = dao.insertTask(copy)
                dao.insertSubtasks(
                    dao.getSubtasks(id).map { it.copy(id = 0, taskId = nextId, completed = false) },
                )
                next = copy.copy(id = nextId)
                updated = updated.copy(repeatSpawned = true)
            }
            dao.updateTask(updated)
            listOfNotNull(updated, next)
        }
        // A completed task loses its alarm; an open one (or the new occurrence) gets one.
        changed.forEach(scheduler::scheduleTask)
    }

    suspend fun deleteTask(id: Long) {
        scheduler.cancelTask(id)
        dao.deleteTask(id)
    }

    private companion object {
        const val DEFAULT_LIST_NAME = "My Tasks"
    }
}
