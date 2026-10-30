package llc.bokadev.kompass.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/**
 * The app-wide page background: Home's light-purple → violet → ink gradient, pinned to the screen
 * (apply it *before* `verticalScroll`, so content scrolls over a fixed backdrop, exactly like Home).
 */
fun Modifier.pageGradientBackground(): Modifier =
    background(Brush.verticalGradient(HomePalette.pageGradientStops))
