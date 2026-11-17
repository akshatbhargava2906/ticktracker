package com.ticktracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticktracker.TickTrackerApp
import kotlinx.coroutines.launch

/**
 * Runs when the alarm for a reminder goes off, whether or not the app is open.
 *
 * Habit reminder: shows the notification, then schedules the same reminder for tomorrow.
 * Task or Plan block reminder: shows the notification once, unless it has been ticked off.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val blockId = intent.getLongExtra(EXTRA_BLOCK_ID, -1)
        if (reminderId < 0 && taskId < 0 && blockId < 0) return

        val container = (context.applicationContext as TickTrackerApp).container
        val database = container.database
        val pending = goAsync()
        container.appScope.launch {
            try {
                when {
                    taskId >= 0 -> {
                        val task = database.taskDao().getTask(taskId) ?: return@launch
                        if (!task.completed) ReminderNotifications.showTask(context, task)
                    }
                    blockId >= 0 -> {
                        val block = database.planDao().getById(blockId) ?: return@launch
                        if (!block.completed) ReminderNotifications.showBlock(context, block)
                    }
                    else -> {
                        val reminder = database.reminderDao().getById(reminderId) ?: return@launch
                        val habit = database.habitDao().getById(reminder.habitId) ?: return@launch
                        ReminderNotifications.show(context, reminder, habit)
                        container.reminderScheduler.schedule(reminder)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_BLOCK_ID = "block_id"
        const val ACTION_TASK = "com.ticktracker.TASK_REMINDER"
        const val ACTION_BLOCK = "com.ticktracker.BLOCK_REMINDER"
    }
}
