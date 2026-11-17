package com.ticktracker.ui.habits

import com.ticktracker.ui.theme.Palette
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ticktracker.reminders.ReminderHealth


/**
 * Warns when a phone setting could delay or block reminders, with a button to fix each one (D20).
 * Shows nothing when everything is in order. Re-checks each time the app comes back to the
 * front, so it clears itself after the user returns from system settings.
 */
@Composable
fun ReminderHealthBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var health by remember { mutableStateOf(ReminderHealth.check(context)) }
    LifecycleResumeEffect(Unit) {
        health = ReminderHealth.check(context)
        onPauseOrDispose { }
    }
    if (health.allGood) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Palette.warning.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Palette.warning.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)) {
            Text(
                text = "Reminders may not arrive",
                color = Palette.warning,
                fontWeight = FontWeight.SemiBold,
            )
            if (!health.notificationsAllowed) {
                Issue("Notifications are switched off for TickTracker.") {
                    context.open(ReminderHealth.notificationSettings(context))
                }
            }
            if (!health.exactAlarmsAllowed) {
                Issue("Alarms are not allowed, so reminders can be late.") {
                    context.open(ReminderHealth.exactAlarmSettings(context))
                }
            }
            if (!health.batteryUnrestricted) {
                Issue("Battery saving can put the app to sleep and drop reminders.") {
                    context.open(ReminderHealth.batterySettings(context))
                }
            }
        }
    }
}

@Composable
private fun Issue(text: String, onFix: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onFix) { Text("Fix") }
    }
}

/** Opens a settings screen, falling back to the app's general settings page if it is missing. */
private fun Context.open(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        try {
            startActivity(ReminderHealth.appDetails(this))
        } catch (_: ActivityNotFoundException) {
            // No settings screen available on this device; nothing more can be done from here.
        }
    }
}
