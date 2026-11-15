package com.ticktracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ticktracker.data.db.ReminderDao
import com.ticktracker.data.db.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId

/** Registers reminders with the Android alarm scheduler (D20). */
class ReminderScheduler(
    private val context: Context,
    private val reminderDao: ReminderDao,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Sets the alarm for the next occurrence of the reminder, replacing any existing one. */
    fun schedule(reminder: ReminderEntity) {
        if (!reminder.enabled) {
            cancel(reminder.id)
            return
        }
        val triggerAt = nextOccurrence(reminder.minuteOfDay, LocalDateTime.now())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val intent = pendingIntent(reminder.id)

        val exactAllowed =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (exactAllowed) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            // Without the exact alarm permission Android may deliver this a few minutes late.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntent(reminderId))
    }

    /** Android drops all alarms on restart, so every reminder is registered again. */
    suspend fun rescheduleAll() {
        reminderDao.getAll().forEach(::schedule)
    }

    private fun pendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
