package com.ticktracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.times
import com.ticktracker.R
import com.ticktracker.data.settings.ThemeMode

/**
 * The colours of one theme, following the 60-30-10 rule (D40):
 *
 * - 60% [background]: the screen behind everything. A soft off-white or a deep charcoal,
 *   never pure white or pure black.
 * - 30% [card], [raised] and [outline]: cards, sheets, navigation and controls at rest,
 *   a step away from the background so sections separate.
 * - 10% [accent]: the one vibrant colour, kept for buttons, selection and key highlights.
 *
 * Text is [text] or the quieter [muted], again never pure white or black.
 */
data class AppPalette(
    val background: Color,
    val card: Color,
    /** Filled controls at rest: idle buttons, fields, pills. A step beyond [card]. */
    val raised: Color,
    val outline: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    /** Warnings, such as the reminder settings banner. */
    val warning: Color,
)

/** The blue of the app icon. */
private val LogoBlue = Color(0xFF3B82F6)

val DarkPalette = AppPalette(
    background = Color(0xFF14161A),
    card = Color(0xFF1D2026),
    raised = Color(0xFF282C34),
    outline = Color(0xFF2F343D),
    text = Color(0xFFE6E8EB),
    muted = Color(0xFF9198A1),
    accent = Color(0xFF4C8DF6),
    onAccent = Color(0xFFF7F9FC),
    warning = Color(0xFFF5B54A),
)

val LightPalette = AppPalette(
    background = Color(0xFFF2F3F5),
    card = Color(0xFFFAFAFB),
    raised = Color(0xFFE7E9EC),
    outline = Color(0xFFDADDE2),
    text = Color(0xFF1C1F24),
    muted = Color(0xFF68707B),
    accent = Color(0xFF2B6FE0),
    onAccent = Color(0xFFF7F9FC),
    warning = Color(0xFFB7791F),
)

private val LocalPalette = staticCompositionLocalOf { DarkPalette }

/** The current theme's colours, for anything the Material colour scheme has no name for. */
val Palette: AppPalette
    @Composable @ReadOnlyComposable get() = LocalPalette.current

// Short names kept from the first theme, now following the chosen theme.
val AppBackground: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.background
val CardSurface: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card
val RaisedSurface: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.raised
val CardOutline: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.outline
val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.text
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.muted

private fun AppPalette.toDarkScheme() = darkColorScheme(
    primary = accent,
    onPrimary = onAccent,
    secondary = accent,
    onSecondary = onAccent,
    background = background,
    onBackground = text,
    surface = background,
    onSurface = text,
    surfaceVariant = card,
    onSurfaceVariant = muted,
    surfaceContainer = card,
    surfaceContainerHigh = card,
    surfaceContainerHighest = raised,
    surfaceContainerLow = card,
    outline = outline,
    outlineVariant = outline,
)

private fun AppPalette.toLightScheme() = lightColorScheme(
    primary = accent,
    onPrimary = onAccent,
    secondary = accent,
    onSecondary = onAccent,
    background = background,
    onBackground = text,
    surface = background,
    onSurface = text,
    surfaceVariant = card,
    onSurfaceVariant = muted,
    surfaceContainer = card,
    surfaceContainerHigh = card,
    surfaceContainerHighest = raised,
    surfaceContainerLow = card,
    outline = outline,
    outlineVariant = outline,
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

/** Whether [mode] means dark right now. System follows the phone's own setting. */
@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** The app theme: light or dark (D40), compact text (D38), in Plus Jakarta Sans (D39). */
@Composable
fun TickTrackerTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val palette = if (dark) DarkPalette else LightPalette
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) palette.toDarkScheme() else palette.toLightScheme(),
            typography = CompactTypography,
            content = content,
        )
    }
}
