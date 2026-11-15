package com.ticktracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticktracker.TickTrackerApp
import kotlinx.coroutines.launch

/**
 * Runs when the alarm for a reminder goes off, whether or not the app is open.
 * Shows the notification, then schedules the same reminder for tomorrow.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId < 0) return

        val container = (context.applicationContext as TickTrackerApp).container
        val pending = goAsync()
        container.appScope.launch {
            try {
                val reminder = container.database.reminderDao().getById(reminderId) ?: return@launch
                val habit = container.database.habitDao().getById(reminder.habitId) ?: return@launch
                ReminderNotifications.show(context, reminder, habit)
                container.reminderScheduler.schedule(reminder)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
