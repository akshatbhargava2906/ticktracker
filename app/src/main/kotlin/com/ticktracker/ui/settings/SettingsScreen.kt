package com.ticktracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticktracker.BuildConfig
import com.ticktracker.ui.habits.ReminderHealthBanner
import com.ticktracker.ui.theme.CardOutline
import com.ticktracker.ui.theme.CardSurface
import com.ticktracker.ui.theme.TextMuted
import com.ticktracker.util.formatClock
import com.ticktracker.util.today

/** The Settings screen: the Plan day's start and wind-down times, and backup (D30, D33). */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var confirmImport by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::export) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::import) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        ReminderHealthBanner(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        Section("Plan") {
            SettingRow("Day starts at", formatClock(settings.dayStartMinute)) { editing = "start" }
            HorizontalDivider(color = CardOutline)
            SettingRow("Wind down at", formatClock(settings.windDownMinute)) { editing = "windDown" }
        }
        timeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp))
        }

        Section("Backup") {
            SettingRow("Export data", "Save everything to a file") {
                exportLauncher.launch("ticktracker-backup-${today()}.json")
            }
            HorizontalDivider(color = CardOutline)
            SettingRow("Import data", "Replace everything from a file") { confirmImport = true }
        }
        Text(
            text = "Your data is kept only on this phone. Uninstalling the app deletes it, so export " +
                "a backup now and then and keep the file somewhere safe, such as Google Drive.",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        if (viewModel.busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp))
        }
        viewModel.message?.let {
            Text(it, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        }

        Text(
            text = "TickTracker ${BuildConfig.VERSION_NAME}",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(20.dp),
        )
    }

    when (editing) {
        "start" -> TimeDialog("Day starts at", settings.dayStartMinute, onDismiss = { editing = null }) {
            timeError = if (viewModel.setDayStart(it)) null else "The day must start before wind down."
            editing = null
        }
        "windDown" -> TimeDialog("Wind down at", settings.windDownMinute, onDismiss = { editing = null }) {
            timeError = if (viewModel.setWindDown(it)) null else "Wind down must be after the day starts."
            editing = null
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text("Replace all data?") },
            text = {
                Text(
                    "Importing replaces every habit, task, plan and setting in the app with the " +
                        "contents of the file. What is in the app now will be lost. Export a backup first " +
                        "if you might want it back.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmImport = false
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                    },
                ) { Text("Choose file", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, color = TextMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(title: String, minute: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minute / 60, initialMinute = minute % 60, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
