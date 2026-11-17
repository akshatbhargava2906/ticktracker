package com.ticktracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.PlanDao
import com.ticktracker.data.db.ReminderDao
import com.ticktracker.data.db.ReminderEntity
import com.ticktracker.data.db.TaskDao
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.domain.reminderAt
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Registers reminders with the Android alarm scheduler (D20).
 * Habit reminders repeat daily; task and Plan block reminders fire once (D29, D32).
 */
class ReminderScheduler(
    private val context: Context,
    private val reminderDao: ReminderDao,
    private val taskDao: TaskDao,
    private val planDao: PlanDao,
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
        setOneOff(if (task.completed) null else task.reminderAt(), taskIntent(task.id))
    }

    fun cancelTask(taskId: Long) {
        alarmManager.cancel(taskIntent(taskId))
    }

    /** The same as [scheduleTask], for a Plan block. Notes never have a reminder. */
    fun scheduleBlock(block: PlanBlockEntity) {
        setOneOff(if (block.completed) null else block.reminderAt(), blockIntent(block.id))
    }

    fun cancelBlock(blockId: Long) {
        alarmManager.cancel(blockIntent(blockId))
    }

    /** Android drops all alarms on restart, so every reminder is registered again. */
    suspend fun rescheduleAll() {
        reminderDao.getAll().forEach(::schedule)
        taskDao.getTasksWithReminders().forEach(::scheduleTask)
        planDao.getWithReminders().forEach(::scheduleBlock)
    }

    /** Cancels every alarm the app has set, as before restoring a backup. */
    suspend fun cancelAll() {
        reminderDao.getAll().forEach { cancel(it.id) }
        taskDao.getTasksWithReminders().forEach { cancelTask(it.id) }
        planDao.getWithReminders().forEach { cancelBlock(it.id) }
    }

    private fun setOneOff(at: LocalDateTime?, intent: PendingIntent) {
        if (at == null || !at.isAfter(LocalDateTime.now())) {
            alarmManager.cancel(intent)
        } else {
            setAlarm(at, intent)
        }
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

    // The actions keep task and block alarms apart from habit alarms that share the same number.
    private fun taskIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_TASK)
            .putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(context, taskId.toInt(), intent, FLAGS)
    }

    private fun blockIntent(blockId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_BLOCK)
            .putExtra(ReminderReceiver.EXTRA_BLOCK_ID, blockId)
        return PendingIntent.getBroadcast(context, blockId.toInt(), intent, FLAGS)
    }

    private companion object {
        const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
