package llc.bokadev.kompass.core.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun String.toComposeColor(): Color {
    val hex = removePrefix("#").uppercase()
    return when (hex.length) {
        6 -> Color(
            red   = hex.substring(0, 2).toInt(16) / 255f,
            green = hex.substring(2, 4).toInt(16) / 255f,
            blue  = hex.substring(4, 6).toInt(16) / 255f
        )
        else -> Color.Gray
    }
}
