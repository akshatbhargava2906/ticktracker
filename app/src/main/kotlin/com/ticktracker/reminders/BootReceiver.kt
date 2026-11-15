package com.ticktracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ticktracker.TickTrackerApp
import kotlinx.coroutines.launch

/** Re-registers every reminder after a restart or a clock / time zone change (D20). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val container = (context.applicationContext as TickTrackerApp).container
        val pending = goAsync()
        container.appScope.launch {
            try {
                container.reminderScheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
