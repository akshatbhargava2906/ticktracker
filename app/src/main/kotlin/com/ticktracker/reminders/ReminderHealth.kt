package com.ticktracker.reminders

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Whether the phone's settings let reminders arrive on time (D20).
 * Any of these three being off can make a reminder late or stop it altogether.
 */
data class ReminderHealth(
    /** Notifications are switched on for the app. Without this nothing is shown. */
    val notificationsAllowed: Boolean,
    /** The app may set alarms for an exact minute. Without this reminders can be minutes late. */
    val exactAlarmsAllowed: Boolean,
    /** The app is excluded from battery optimisation. Without this the phone may put it to sleep. */
    val batteryUnrestricted: Boolean,
) {
    val allGood: Boolean get() = notificationsAllowed && exactAlarmsAllowed && batteryUnrestricted

    companion object {
        fun check(context: Context): ReminderHealth {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val powerManager = context.getSystemService(PowerManager::class.java)
            return ReminderHealth(
                notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled(),
                exactAlarmsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms(),
                batteryUnrestricted = powerManager.isIgnoringBatteryOptimizations(context.packageName),
            )
        }

        /** Opens the app's notification settings. */
        fun notificationSettings(context: Context): Intent =
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

        /** Opens the "Alarms and reminders" permission screen for the app. */
        fun exactAlarmSettings(context: Context): Intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri(context))
            } else {
                appDetails(context)
            }

        /** Asks the system to exclude the app from battery optimisation. */
        @Suppress("BatteryLife") // A personal reminder app: staying awake for alarms is the point.
        fun batterySettings(context: Context): Intent =
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri(context))

        /** The app's page in system settings, used when a more specific screen is missing. */
        fun appDetails(context: Context): Intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(context))

        private fun packageUri(context: Context): Uri = Uri.parse("package:${context.packageName}")
    }
}
