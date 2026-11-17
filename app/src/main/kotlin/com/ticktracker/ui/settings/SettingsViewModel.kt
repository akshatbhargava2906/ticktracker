package com.ticktracker.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ticktracker.TickTrackerApp
import com.ticktracker.data.backup.BackupException
import com.ticktracker.data.backup.BackupManager
import com.ticktracker.data.backup.BackupSummary
import com.ticktracker.data.settings.AppSettings
import com.ticktracker.data.settings.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Supplies the Settings screen and runs backup and restore (D30, D33). */
class SettingsViewModel(
    private val store: SettingsStore,
    private val backup: BackupManager,
    private val resolver: ContentResolver,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = store.settings

    /** The result of the last export or import, shown under the Backup section. */
    var message by mutableStateOf<String?>(null)
        private set

    var busy by mutableStateOf(false)
        private set

    /** Returns false, and changes nothing, if the time is not before wind down. */
    fun setDayStart(minute: Int): Boolean {
        if (minute >= settings.value.windDownMinute) return false
        store.setDayStart(minute)
        return true
    }

    /** Returns false, and changes nothing, if the time is not after the day's start. */
    fun setWindDown(minute: Int): Boolean {
        if (minute <= settings.value.dayStartMinute) return false
        store.setWindDown(minute)
        return true
    }

    fun export(uri: Uri) = run("Export failed") {
        val summary = resolver.openOutputStream(uri)?.use { backup.export(it) }
            ?: throw BackupException("The file could not be opened.")
        "Exported ${describe(summary)}."
    }

    fun import(uri: Uri) = run("Import failed") {
        val summary = resolver.openInputStream(uri)?.use { backup.import(it) }
            ?: throw BackupException("The file could not be opened.")
        "Imported ${describe(summary)}."
    }

    private fun run(failure: String, work: suspend () -> String) {
        if (busy) return
        busy = true
        message = null
        viewModelScope.launch {
            message = try {
                withContext(Dispatchers.IO) { work() }
            } catch (e: BackupException) {
                "$failure: ${e.message}"
            } catch (e: Exception) {
                "$failure: ${e.message ?: "unknown error"}"
            }
            busy = false
        }
    }

    private fun describe(s: BackupSummary): String =
        "${s.habits} ${plural(s.habits, "habit")}, ${s.tasks} ${plural(s.tasks, "task")} " +
            "and ${s.planBlocks} planned ${plural(s.planBlocks, "block")}"

    private fun plural(n: Int, word: String) = if (n == 1) word else "${word}s"

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TickTrackerApp
                SettingsViewModel(app.container.settingsStore, app.container.backupManager, app.contentResolver)
            }
        }
    }
}
