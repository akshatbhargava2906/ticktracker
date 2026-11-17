package com.ticktracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ticktracker.data.db.ReminderDao
import com.ticktracker.data.db.ReminderEntity
import com.ticktracker.data.db.TaskDao
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.domain.reminderAt
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Registers reminders with the Android alarm scheduler (D20).
 * Habit reminders repeat daily; task reminders fire once (D29).
 */
class ReminderScheduler(
    private val context: Context,
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Sets the alarm for the next occurrence of a habit reminder, replacing any existing one. */
    fun schedule(reminder: ReminderEntity) {
        if (!reminder.enabled) {
            cancel(reminder.id)
            return
        }
        setAlarm(nextOccurrence(reminder.minuteOfDay, LocalDateTime.now()), habitIntent(reminder.id))
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(habitIntent(reminderId))
    }

    /**
     * Sets the one-off alarm for a task, or cancels it if the task is done,
     * has no reminder, or its reminder time has already passed.
     */
    fun scheduleTask(task: TaskEntity) {
        val at = task.reminderAt()
        if (task.completed || at == null || !at.isAfter(LocalDateTime.now())) {
            cancelTask(task.id)
            return
        }
        setAlarm(at, taskIntent(task.id))
    }

    fun cancelTask(taskId: Long) {
        alarmManager.cancel(taskIntent(taskId))
    }

    /** Android drops all alarms on restart, so every reminder is registered again. */
    suspend fun rescheduleAll() {
        reminderDao.getAll().forEach(::schedule)
        taskDao.getTasksWithReminders().forEach(::scheduleTask)
    }

    private fun setAlarm(at: LocalDateTime, intent: PendingIntent) {
        val triggerAt = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val exactAllowed =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (exactAllowed) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            // Without the exact alarm permission Android may deliver this a few minutes late.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    private fun habitIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        return PendingIntent.getBroadcast(context, reminderId.toInt(), intent, FLAGS)
    }

    private fun taskIntent(taskId: Long): PendingIntent {
        // The action keeps task alarms separate from habit alarms that share the same number.
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_TASK)
            .putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(context, taskId.toInt(), intent, FLAGS)
    }

    private companion object {
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
