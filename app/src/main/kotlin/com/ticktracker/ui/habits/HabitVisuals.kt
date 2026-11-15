package com.ticktracker.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

/**
 * The icons a habit can use. An icon is stored as text: either "glyph:name"
 * for one of the drawn icons below, or the emoji itself.
 */
object HabitIcons {
    private const val GLYPH_PREFIX = "glyph:"

    private val glyphVectors: Map<String, ImageVector> = linkedMapOf(
        "favorite" to Icons.Filled.Favorite,
        "star" to Icons.Filled.Star,
        "thumb_up" to Icons.Filled.ThumbUp,
        "face" to Icons.Filled.Face,
        "home" to Icons.Filled.Home,
        "build" to Icons.Filled.Build,
        "create" to Icons.Filled.Create,
        "person" to Icons.Filled.Person,
        "place" to Icons.Filled.Place,
        "phone" to Icons.Filled.Phone,
        "email" to Icons.Filled.Email,
        "cart" to Icons.Filled.ShoppingCart,
        "notifications" to Icons.Filled.Notifications,
        "lock" to Icons.Filled.Lock,
    )

    val glyphs: List<String> = glyphVectors.keys.map { GLYPH_PREFIX + it }

    val emoji: List<String> = listOf(
        "💪", "🏃", "🥗", "💧", "📚", "💡", "✅", "🧘", "🚴", "🏋️",
        "🚶", "🏊", "😴", "🌅", "🍎", "☕", "💊", "🧠", "📝", "🎸",
        "🎨", "💰", "🧹", "🪥", "🙏", "📵", "🚭", "🍺", "🍬", "🎮",
    )

    val default: String = glyphs.first()

    fun vectorFor(icon: String): ImageVector? =
        if (icon.startsWith(GLYPH_PREFIX)) glyphVectors[icon.removePrefix(GLYPH_PREFIX)] else null
}

/** The colours a habit can use, as ARGB values. Every colour is available (D11). */
object HabitColors {
    val all: List<Long> = listOf(
        0xFFF47C7C, 0xFFFFA066, 0xFF6FB1FC, 0xFF9B8CFF, 0xFFD08BFF, 0xFF7ED9A0,
        0xFF67D6DE, 0xFFFF8E72, 0xFFFFD166, 0xFFF78FB3, 0xFF4DD0A1, 0xFF5C9DFF,
        0xFFBA68C8, 0xFF4FC3F7, 0xFFAED581, 0xFFFFB74D, 0xFFFF6F91, 0xFF00C2A8,
        0xFFC0CA33, 0xFF7986CB, 0xFFA1887F, 0xFFB0BEC5, 0xFFE57373, 0xFF90A4AE,
    )

    /** How many colours the picker shows before "See All". */
    const val PRESET_COUNT = 8

    val default: Long = all[6]
}

/** Draws a habit icon: a drawn glyph tinted with [tint], or an emoji. */
@Composable
fun HabitIconGlyph(icon: String, tint: Color, size: Dp, modifier: Modifier = Modifier) {
    val vector = HabitIcons.vectorFor(icon)
    if (vector != null) {
        Icon(vector, contentDescription = null, tint = tint, modifier = modifier.size(size))
    } else {
        Text(text = icon, fontSize = (size.value * 0.85f).sp, modifier = modifier)
    }
}

/** A habit icon on a soft circle of the habit colour, as shown on cards and in the wizard. */
@Composable
fun HabitBadge(icon: String, color: Long, size: Dp, modifier: Modifier = Modifier) {
    val tint = Color(color)
    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        HabitIconGlyph(icon = icon, tint = tint, size = size * 0.48f)
    }
}
