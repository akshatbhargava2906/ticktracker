package com.ticktracker.data.backup

import androidx.room.withTransaction
import com.ticktracker.data.db.AppDatabase
import com.ticktracker.data.db.BlockKind
import com.ticktracker.data.db.CategoryEntity
import com.ticktracker.data.db.HabitCategoryCrossRef
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.HabitEntryEntity
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.ReminderEntity
import com.ticktracker.data.db.SubtaskEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.data.db.TaskListEntity
import com.ticktracker.data.model.EntryState
import com.ticktracker.data.model.GoalPeriod
import com.ticktracker.data.model.HabitDirection
import com.ticktracker.data.model.HabitKind
import com.ticktracker.data.model.RepeatGoal
import com.ticktracker.data.model.TaskPriority
import com.ticktracker.data.model.TaskRepeat
import com.ticktracker.data.repository.TaskRepository
import com.ticktracker.data.settings.AppSettings
import com.ticktracker.data.settings.SettingsStore
import com.ticktracker.data.settings.ThemeMode
import com.ticktracker.reminders.ReminderScheduler
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** A backup file could not be read. The message is shown to the user as it is. */
class BackupException(message: String) : Exception(message)

/** How much a backup holds, shown after exporting or importing. */
data class BackupSummary(val habits: Int, val tasks: Int, val planBlocks: Int)

/**
 * Exports everything to a single JSON file, and restores from one (D33).
 *
 * A restore replaces all the data in the app. The file is read in full and checked before
 * anything is changed, so a damaged or wrong file leaves the app's data untouched.
 */
class BackupManager(
    private val db: AppDatabase,
    private val settings: SettingsStore,
    private val scheduler: ReminderScheduler,
    private val tasks: TaskRepository,
) {
    private val dao = db.backupDao()

    /** The parsed contents of a backup file. */
    private data class Snapshot(
        val categories: List<CategoryEntity>,
        val habits: List<HabitEntity>,
        val habitCategories: List<HabitCategoryCrossRef>,
        val habitEntries: List<HabitEntryEntity>,
        val reminders: List<ReminderEntity>,
        val taskLists: List<TaskListEntity>,
        val tasks: List<TaskEntity>,
        val subtasks: List<SubtaskEntity>,
        val planBlocks: List<PlanBlockEntity>,
        val settings: AppSettings,
    ) {
        val summary get() = BackupSummary(habits.size, tasks.size, planBlocks.size)
    }

    suspend fun export(out: OutputStream): BackupSummary {
        val snapshot = db.withTransaction {
            Snapshot(
                categories = dao.categories(),
                habits = dao.habits(),
                habitCategories = dao.habitCategories(),
                habitEntries = dao.habitEntries(),
                reminders = dao.reminders(),
                taskLists = dao.taskLists(),
                tasks = dao.tasks(),
                subtasks = dao.subtasks(),
                planBlocks = dao.planBlocks(),
                settings = settings.settings.value,
            )
        }
        val json = JSONObject()
            .put("app", APP_NAME)
            .put("format", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("settings", JSONObject()
                .put("dayStartMinute", snapshot.settings.dayStartMinute)
                .put("windDownMinute", snapshot.settings.windDownMinute)
                .put("themeMode", snapshot.settings.themeMode.name))
            .put("categories", snapshot.categories.toJsonArray {
                JSONObject().put("id", it.id).put("name", it.name).put("builtIn", it.builtIn)
            })
            .put("habits", snapshot.habits.toJsonArray {
                JSONObject()
                    .put("id", it.id).put("name", it.name).put("icon", it.icon).put("color", it.color)
                    .put("kind", it.kind.name).putOpt("direction", it.direction?.name)
                    .putOpt("repeatGoal", it.repeatGoal?.name).putOpt("targetCount", it.targetCount)
                    .putOpt("unit", it.unit).putOpt("targetAmount", it.targetAmount)
                    .putOpt("goalPeriod", it.goalPeriod?.name).put("createdAt", it.createdAt)
            })
            .put("habitCategories", snapshot.habitCategories.toJsonArray {
                JSONObject().put("habitId", it.habitId).put("categoryId", it.categoryId)
            })
            .put("habitEntries", snapshot.habitEntries.toJsonArray {
                JSONObject()
                    .put("habitId", it.habitId).put("date", it.date.toString())
                    .putOpt("state", it.state?.name).put("count", it.count).putOpt("amount", it.amount)
            })
            .put("reminders", snapshot.reminders.toJsonArray {
                JSONObject()
                    .put("id", it.id).put("habitId", it.habitId)
                    .put("minuteOfDay", it.minuteOfDay).put("enabled", it.enabled)
            })
            .put("taskLists", snapshot.taskLists.toJsonArray {
                JSONObject().put("id", it.id).put("name", it.name).put("createdAt", it.createdAt)
            })
            .put("tasks", snapshot.tasks.toJsonArray {
                JSONObject()
                    .put("id", it.id).put("listId", it.listId).put("title", it.title)
                    .putOpt("date", it.date?.toString()).putOpt("timeMinute", it.timeMinute)
                    .putOpt("durationMinutes", it.durationMinutes)
                    .putOpt("reminderOffsetMinutes", it.reminderOffsetMinutes)
                    .put("repeat", it.repeat.name).put("repeatSpawned", it.repeatSpawned)
                    .put("priority", it.priority.name).put("completed", it.completed)
                    .putOpt("completedAt", it.completedAt).put("createdAt", it.createdAt)
            })
            .put("subtasks", snapshot.subtasks.toJsonArray {
                JSONObject()
                    .put("id", it.id).put("taskId", it.taskId).put("title", it.title)
                    .put("completed", it.completed).put("position", it.position)
            })
            .put("planBlocks", snapshot.planBlocks.toJsonArray {
                JSONObject()
                    .put("id", it.id).put("title", it.title).put("date", it.date.toString())
                    .put("startMinute", it.startMinute).put("durationMinutes", it.durationMinutes)
                    .put("kind", it.kind.name).put("completed", it.completed)
                    .putOpt("reminderOffsetMinutes", it.reminderOffsetMinutes)
                    .put("createdAt", it.createdAt)
            })
        out.write(json.toString(2).toByteArray(Charsets.UTF_8))
        out.flush()
        return snapshot.summary
    }

    /** Replaces all the app's data with the contents of a backup file. */
    suspend fun import(input: InputStream): BackupSummary {
        val snapshot = parse(input.readBytes().toString(Charsets.UTF_8))

        scheduler.cancelAll()
        db.withTransaction {
            dao.clearPlanBlocks()
            dao.clearSubtasks()
            dao.clearTasks()
            dao.clearTaskLists()
            dao.clearReminders()
            dao.clearHabitEntries()
            dao.clearHabitCategories()
            dao.clearHabits()
            dao.clearCategories()

            dao.insertCategories(snapshot.categories)
            dao.insertHabits(snapshot.habits)
            dao.insertHabitCategories(snapshot.habitCategories)
            dao.insertHabitEntries(snapshot.habitEntries)
            dao.insertReminders(snapshot.reminders)
            dao.insertTaskLists(snapshot.taskLists)
            dao.insertTasks(snapshot.tasks)
            dao.insertSubtasks(snapshot.subtasks)
            dao.insertPlanBlocks(snapshot.planBlocks)
        }
        settings.replace(snapshot.settings)
        // A backup made before D36 can hold task blocks; they become real tasks here too.
        tasks.convertTaskBlocks()
        scheduler.rescheduleAll()
        return snapshot.summary
    }

    private fun parse(text: String): Snapshot {
        val root = try {
            JSONObject(text)
        } catch (_: JSONException) {
            throw BackupException("This file is not a TickTracker backup.")
        }
        if (root.optString("app") != APP_NAME) throw BackupException("This file is not a TickTracker backup.")
        val format = root.optInt("format", -1)
        if (format > FORMAT_VERSION) {
            throw BackupException("This backup was made by a newer version of TickTracker. Update the app first.")
        }
        if (format < 1) throw BackupException("This backup file is damaged.")

        try {
            val settingsJson = root.optJSONObject("settings")
            val snapshot = Snapshot(
                settings = AppSettings(
                    dayStartMinute = settingsJson?.optInt("dayStartMinute", AppSettings.DEFAULT_DAY_START)
                        ?: AppSettings.DEFAULT_DAY_START,
                    windDownMinute = settingsJson?.optInt("windDownMinute", AppSettings.DEFAULT_WIND_DOWN)
                        ?: AppSettings.DEFAULT_WIND_DOWN,
                    // Backups made before the theme setting existed keep the current theme.
                    themeMode = settingsJson?.optString("themeMode")
                        ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                        ?: settings.settings.value.themeMode,
                ),
                categories = root.array("categories").map {
                    CategoryEntity(id = it.getLong("id"), name = it.getString("name"), builtIn = it.getBoolean("builtIn"))
                },
                habits = root.array("habits").map {
                    HabitEntity(
                        id = it.getLong("id"),
                        name = it.getString("name"),
                        icon = it.getString("icon"),
                        color = it.getLong("color"),
                        kind = HabitKind.valueOf(it.getString("kind")),
                        direction = it.stringOrNull("direction")?.let(HabitDirection::valueOf),
                        repeatGoal = it.stringOrNull("repeatGoal")?.let(RepeatGoal::valueOf),
                        targetCount = it.intOrNull("targetCount"),
                        unit = it.stringOrNull("unit"),
                        targetAmount = it.doubleOrNull("targetAmount"),
                        goalPeriod = it.stringOrNull("goalPeriod")?.let(GoalPeriod::valueOf),
                        createdAt = it.getLong("createdAt"),
                    )
                },
                habitCategories = root.array("habitCategories").map {
                    HabitCategoryCrossRef(habitId = it.getLong("habitId"), categoryId = it.getLong("categoryId"))
                },
                habitEntries = root.array("habitEntries").map {
                    HabitEntryEntity(
                        habitId = it.getLong("habitId"),
                        date = LocalDate.parse(it.getString("date")),
                        state = it.stringOrNull("state")?.let(EntryState::valueOf),
                        count = it.optInt("count", 0),
                        amount = it.doubleOrNull("amount"),
                    )
                },
                reminders = root.array("reminders").map {
                    ReminderEntity(
                        id = it.getLong("id"),
                        habitId = it.getLong("habitId"),
                        minuteOfDay = it.getInt("minuteOfDay"),
                        enabled = it.optBoolean("enabled", true),
                    )
                },
                taskLists = root.array("taskLists").map {
                    TaskListEntity(id = it.getLong("id"), name = it.getString("name"), createdAt = it.getLong("createdAt"))
                },
                tasks = root.array("tasks").map {
                    TaskEntity(
                        id = it.getLong("id"),
                        listId = it.getLong("listId"),
                        title = it.getString("title"),
                        date = it.stringOrNull("date")?.let(LocalDate::parse),
                        timeMinute = it.intOrNull("timeMinute"),
                        durationMinutes = it.intOrNull("durationMinutes"),
                        reminderOffsetMinutes = it.intOrNull("reminderOffsetMinutes"),
                        repeat = TaskRepeat.valueOf(it.getString("repeat")),
                        repeatSpawned = it.optBoolean("repeatSpawned", false),
                        priority = TaskPriority.valueOf(it.getString("priority")),
                        completed = it.getBoolean("completed"),
                        completedAt = it.longOrNull("completedAt"),
                        createdAt = it.getLong("createdAt"),
                    )
                },
                subtasks = root.array("subtasks").map {
                    SubtaskEntity(
                        id = it.getLong("id"),
                        taskId = it.getLong("taskId"),
                        title = it.getString("title"),
                        completed = it.getBoolean("completed"),
                        position = it.getInt("position"),
                    )
                },
                planBlocks = root.array("planBlocks").map {
                    PlanBlockEntity(
                        id = it.getLong("id"),
                        title = it.getString("title"),
                        date = LocalDate.parse(it.getString("date")),
                        startMinute = it.getInt("startMinute"),
                        durationMinutes = it.getInt("durationMinutes"),
                        kind = BlockKind.valueOf(it.getString("kind")),
                        completed = it.getBoolean("completed"),
                        reminderOffsetMinutes = it.intOrNull("reminderOffsetMinutes"),
                        createdAt = it.getLong("createdAt"),
                    )
                },
            )
            // Tasks need a list to live in; a backup with none is not usable.
            if (snapshot.taskLists.isEmpty()) throw BackupException("This backup file is damaged.")
            return snapshot
        } catch (e: BackupException) {
            throw e
        } catch (_: Exception) {
            // A missing field, a wrong type, an unknown value or a bad date.
            throw BackupException("This backup file is damaged.")
        }
    }

    private companion object {
        const val APP_NAME = "TickTracker"

        /** Raise this when the file layout changes, and keep reading older layouts. */
        const val FORMAT_VERSION = 1
    }
}

private fun <T> List<T>.toJsonArray(toJson: (T) -> JSONObject): JSONArray =
    JSONArray().also { array -> forEach { array.put(toJson(it)) } }

/** A missing array is read as empty, so a file from before a section existed still loads. */
private fun JSONObject.array(name: String): List<JSONObject> {
    val array = optJSONArray(name) ?: return emptyList()
    return (0 until array.length()).map { array.getJSONObject(it) }
}

private fun JSONObject.stringOrNull(name: String): String? = if (isNull(name)) null else getString(name)
private fun JSONObject.intOrNull(name: String): Int? = if (isNull(name)) null else getInt(name)
private fun JSONObject.longOrNull(name: String): Long? = if (isNull(name)) null else getLong(name)
private fun JSONObject.doubleOrNull(name: String): Double? = if (isNull(name)) null else getDouble(name)
