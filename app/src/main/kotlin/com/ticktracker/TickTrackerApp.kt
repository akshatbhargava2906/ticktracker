package com.ticktracker

import android.app.Application
import android.content.Context
import com.ticktracker.data.db.AppDatabase
import com.ticktracker.data.repository.HabitRepository
import com.ticktracker.data.repository.TaskRepository
import com.ticktracker.reminders.ReminderNotifications
import com.ticktracker.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Process entry point. Builds the objects the rest of the app shares. */
class TickTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderNotifications.createChannel(this)
        container.appScope.launch {
            container.habitRepository.ensureBuiltInCategories()
            container.taskRepository.ensureDefaultList()
        }
    }
}

/**
 * Hand-written dependency container: one database, one scheduler and the repositories,
 * shared by the screens and the reminder receivers.
 */
class AppContainer(context: Context) {
    /** For work that must outlive a screen, such as handling a reminder. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase = AppDatabase.create(context)
    val reminderScheduler = ReminderScheduler(context, database.reminderDao(), database.taskDao())
    val habitRepository = HabitRepository(database, reminderScheduler)
    val taskRepository = TaskRepository(database, reminderScheduler)
}
