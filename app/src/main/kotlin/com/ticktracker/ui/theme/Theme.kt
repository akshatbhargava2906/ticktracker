package com.ticktracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentGreen = Color(0xFF53C672)
val AppBackground = Color(0xFF0B0B0B)
val CardSurface = Color(0xFF171717)
val CardOutline = Color(0xFF2A2A2A)
val TextPrimary = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9A9A9A)

private val DarkColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color(0xFF07140B),
    background = AppBackground,
    onBackground = TextPrimary,
    surface = AppBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextMuted,
    surfaceContainer = CardSurface,
    outline = CardOutline,
)

/** Dark theme with a green accent (D01). There is no light theme yet. */
@Composable
fun TickTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
