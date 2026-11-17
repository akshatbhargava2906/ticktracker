package com.ticktracker.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Light, dark, or whichever the phone is set to (D40). */
enum class ThemeMode(val label: String) {
    SYSTEM("Same as phone"),
    LIGHT("Light"),
    DARK("Dark"),
}

/** App settings (D30, D40). */
data class AppSettings(
    /** When the Plan timeline starts, in minutes after midnight. */
    val dayStartMinute: Int = DEFAULT_DAY_START,
    /** When the "Wind down" marker sits, in minutes after midnight. */
    val windDownMinute: Int = DEFAULT_WIND_DOWN,
    val themeMode: ThemeMode = ThemeMode.DARK,
) {
    companion object {
        const val DEFAULT_DAY_START = 7 * 60
        const val DEFAULT_WIND_DOWN = 22 * 60
    }
}

/**
 * Keeps the settings on the phone, outside the database, so they can be read
 * straight away without waiting on a query.
 */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val state = MutableStateFlow(
        AppSettings(
            dayStartMinute = prefs.getInt(KEY_DAY_START, AppSettings.DEFAULT_DAY_START),
            windDownMinute = prefs.getInt(KEY_WIND_DOWN, AppSettings.DEFAULT_WIND_DOWN),
            themeMode = prefs.getString(KEY_THEME, null)
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.DARK,
        ),
    )

    val settings: StateFlow<AppSettings> = state.asStateFlow()

    fun setDayStart(minute: Int) = replace(state.value.copy(dayStartMinute = minute))

    fun setWindDown(minute: Int) = replace(state.value.copy(windDownMinute = minute))

    fun setThemeMode(mode: ThemeMode) = replace(state.value.copy(themeMode = mode))

    /** Replaces every setting at once, as when restoring a backup. */
    fun replace(settings: AppSettings) {
        prefs.edit()
            .putInt(KEY_DAY_START, settings.dayStartMinute)
            .putInt(KEY_WIND_DOWN, settings.windDownMinute)
            .putString(KEY_THEME, settings.themeMode.name)
            .apply()
        state.value = settings
    }

    private companion object {
        const val KEY_DAY_START = "day_start_minute"
        const val KEY_WIND_DOWN = "wind_down_minute"
        const val KEY_THEME = "theme_mode"
    }
}
