package com.ticktracker.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.data.model.TaskRepeat
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

// The Tasks tables (D29). Added to the database in version 2.

/** A named list of tasks, such as "My Tasks". */
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Epoch milliseconds. */
    val createdAt: Long,
)

/** One task. */
@Entity(
    tableName = "tasks",
    indices = [Index("date"), Index("listId")],
    foreignKeys = [
        ForeignKey(
            entity = TaskListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val title: String,
    /** The day the task is for. Null means no date: it shows under All Tasks only. */
    val date: LocalDate? = null,
    /** Time of day as minutes after midnight. Needs a date. */
    val timeMinute: Int? = null,
    /** How long the task takes, for showing it on the Plan timeline. */
    val durationMinutes: Int? = null,
    /** Minutes before the task's time that its reminder fires; 0 is at the time. Null is no reminder. */
    val reminderOffsetMinutes: Int? = null,
    val repeat: TaskRepeat = TaskRepeat.NEVER,
    /** True once ticking this task off has created its next occurrence, so it is only created once. */
    val repeatSpawned: Boolean = false,
    val priority: TaskPriority = TaskPriority.NONE,
    val completed: Boolean = false,
    /** Epoch milliseconds, set when the task is ticked off. */
    val completedAt: Long? = null,
    /** Epoch milliseconds. */
    val createdAt: Long,
)

/** One item in a task's checklist. */
@Entity(
    tableName = "subtasks",
    indices = [Index("taskId")],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val completed: Boolean = false,
    /** Order within the task, starting at 0. */
    val position: Int,
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM subtasks ORDER BY position")
    fun observeSubtasks(): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM task_lists ORDER BY id")
    fun observeLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE listId = :listId")
    suspend fun getTasksInList(listId: Long): List<TaskEntity>

    /** Open tasks that have a reminder, for re-registering alarms. */
    @Query("SELECT * FROM tasks WHERE completed = 0 AND reminderOffsetMinutes IS NOT NULL")
    suspend fun getTasksWithReminders(): List<TaskEntity>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position")
    suspend fun getSubtasks(taskId: Long): List<SubtaskEntity>

    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Insert
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasks(taskId: Long)

    @Query("SELECT COUNT(*) FROM task_lists")
    suspend fun listCount(): Int

    @Insert
    suspend fun insertList(list: TaskListEntity): Long

    @Query("UPDATE task_lists SET name = :name WHERE id = :id")
    suspend fun renameList(id: Long, name: String)

    @Query("DELETE FROM task_lists WHERE id = :id")
    suspend fun deleteList(id: Long)
}
