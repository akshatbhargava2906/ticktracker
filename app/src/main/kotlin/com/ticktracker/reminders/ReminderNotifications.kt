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
import com.ticktracker.data.db.ReminderEntity

/** Builds and posts reminder notifications. */
object ReminderNotifications {
    private const val CHANNEL_ID = "reminders"

    /** Extra on the launch intent naming the habit the tapped reminder belongs to. */
    const val EXTRA_HABIT_ID = "habit_id"

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

    /**
     * Posts the reminder. It has no timeout and is never cleared by the app,
     * so it stays until the user dismisses or opens it (D22).
     */
    fun show(context: Context, reminder: ReminderEntity, habit: HabitEntity) {
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!allowed) return

        val openApp = PendingIntent.getActivity(
            context,
            habit.id.toInt(),
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_HABIT_ID, habit.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habit.name)
            .setContentText("Time for ${habit.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
    }
}
