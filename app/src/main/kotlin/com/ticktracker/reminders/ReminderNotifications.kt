package com.ticktracker.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ticktracker.MainActivity
import com.ticktracker.R
import com.ticktracker.data.db.HabitEntity
import com.ticktracker.data.db.PlanBlockEntity
import com.ticktracker.data.db.ReminderEntity
import com.ticktracker.data.db.TaskEntity
import com.ticktracker.util.formatClock

/** Builds and posts reminder notifications. */
object ReminderNotifications {
    private const val CHANNEL_ID = "reminders"
    private const val TAG_HABIT = "habit"
    private const val TAG_TASK = "task"
    private const val TAG_BLOCK = "block"

    /** Extra on the launch intent naming the habit the tapped reminder belongs to. */
    const val EXTRA_HABIT_ID = "habit_id"

    /** Extra on the launch intent naming the task the tapped reminder belongs to. */
    const val EXTRA_TASK_ID = "open_task_id"

    /** Extra on the launch intent naming the Plan block the tapped reminder belongs to. */
    const val EXTRA_BLOCK_ID = "open_block_id"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Posts a habit reminder. */
    fun show(context: Context, reminder: ReminderEntity, habit: HabitEntity) {
        post(
            context = context,
            tag = TAG_HABIT,
            id = reminder.id.toInt(),
            title = habit.name,
            text = "Time for ${habit.name}",
            extraName = EXTRA_HABIT_ID,
            extraValue = habit.id,
        )
    }

    /** Posts a task reminder. */
    fun showTask(context: Context, task: TaskEntity) {
        post(
            context = context,
            tag = TAG_TASK,
            id = task.id.toInt(),
            title = task.title,
            text = task.timeMinute?.let { "Due at ${formatClock(it)}" } ?: "Task reminder",
            extraName = EXTRA_TASK_ID,
            extraValue = task.id,
        )
    }

    /** Posts a Plan block reminder. */
    fun showBlock(context: Context, block: PlanBlockEntity) {
        post(
            context = context,
            tag = TAG_BLOCK,
            id = block.id.toInt(),
            title = block.title,
            text = "Starts at ${formatClock(block.startMinute)}",
            extraName = EXTRA_BLOCK_ID,
            extraValue = block.id,
        )
    }

    /**
     * Posts a reminder. It has no timeout and is never cleared by the app,
     * so it stays until the user dismisses or opens it (D22).
     */
    private fun post(
        context: Context,
        tag: String,
        id: Int,
        title: String,
        text: String,
        extraName: String,
        extraValue: Long,
    ) {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!allowed) return

        // The action makes habit and task intents distinct even when their ids match.
        val openApp = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java)
                .setAction("com.ticktracker.OPEN_$tag")
                .putExtra(extraName, extraValue)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(tag, id, notification)
    }
}
