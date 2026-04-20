package llc.bokadev.kompass.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val KompassShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // sm — buttons, tags, badges
    small = RoundedCornerShape(8.dp),         // sm
    medium = RoundedCornerShape(12.dp),       // md — cards, inputs, search bars
    large = RoundedCornerShape(16.dp),        // lg — bottom sheets, photo thumbnails
    extraLarge = RoundedCornerShape(24.dp)    // xl — feature cards, hero cards
)
