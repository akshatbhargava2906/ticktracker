package com.ticktracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ticktracker.R
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.times

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

/** The app's typeface (D39), bundled with the app so it works offline. */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

/**
 * Every text style is this much smaller than Material's defaults (D38). The phone's own font
 * size setting still applies on top, so text grows for anyone who has made it larger.
 */
private const val TEXT_SCALE = 0.86f

private fun TextStyle.scaled(): TextStyle = copy(
    fontFamily = PlusJakartaSans,
    fontSize = if (fontSize.isSpecified) TEXT_SCALE * fontSize else fontSize,
    lineHeight = if (lineHeight.isSpecified) TEXT_SCALE * lineHeight else lineHeight,
)

private val CompactTypography: Typography = Typography().let { t ->
    Typography(
        displayLarge = t.displayLarge.scaled(),
        displayMedium = t.displayMedium.scaled(),
        displaySmall = t.displaySmall.scaled(),
        headlineLarge = t.headlineLarge.scaled(),
        headlineMedium = t.headlineMedium.scaled(),
        headlineSmall = t.headlineSmall.scaled(),
        titleLarge = t.titleLarge.scaled(),
        titleMedium = t.titleMedium.scaled(),
        titleSmall = t.titleSmall.scaled(),
        bodyLarge = t.bodyLarge.scaled(),
        bodyMedium = t.bodyMedium.scaled(),
        bodySmall = t.bodySmall.scaled(),
        labelLarge = t.labelLarge.scaled(),
        labelMedium = t.labelMedium.scaled(),
        labelSmall = t.labelSmall.scaled(),
    )
}

/** Dark theme with a green accent (D01, D34), compact text (D38), in Plus Jakarta Sans (D39). */
@Composable
fun TickTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = CompactTypography, content = content)
}
