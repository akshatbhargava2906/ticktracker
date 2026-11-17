package com.ticktracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ticktracker.reminders.ReminderNotifications
import com.ticktracker.ui.AppRoot
import com.ticktracker.ui.OpenTarget
import com.ticktracker.ui.theme.TickTrackerTheme
import kotlinx.coroutines.launch

/** The only activity. Every screen is a Compose destination inside [AppRoot]. */
class MainActivity : ComponentActivity() {
    /** The habit, task or Plan block to show, set when the app is opened by tapping a reminder. */
    private var openTarget by mutableStateOf<OpenTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only on a fresh start: after a rotation the same intent must not reopen the item.
        if (savedInstanceState == null) openTarget = targetFrom(intent)
        setContent {
            TickTrackerTheme {
                AppRoot(
                    openTarget = openTarget,
                    onTargetOpened = { openTarget = null },
                )
            }
        }
    }

    /** A reminder was tapped while the app was already running. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        targetFrom(intent)?.let { openTarget = it }
    }

    override fun onResume() {
        super.onResume()
        // Re-register every reminder whenever the app is opened. This picks up a newly
        // granted alarm permission and restores alarms the system may have dropped.
        val container = (application as TickTrackerApp).container
        container.appScope.launch { container.reminderScheduler.rescheduleAll() }
    }

    private fun targetFrom(intent: Intent?): OpenTarget? {
        intent ?: return null
        val habitId = intent.getLongExtra(ReminderNotifications.EXTRA_HABIT_ID, -1)
        val taskId = intent.getLongExtra(ReminderNotifications.EXTRA_TASK_ID, -1)
        val blockId = intent.getLongExtra(ReminderNotifications.EXTRA_BLOCK_ID, -1)
        return when {
            habitId >= 0 -> OpenTarget.Habit(habitId)
            taskId >= 0 -> OpenTarget.Task(taskId)
            blockId >= 0 -> OpenTarget.Block(blockId)
            else -> null
        }
    }
}
