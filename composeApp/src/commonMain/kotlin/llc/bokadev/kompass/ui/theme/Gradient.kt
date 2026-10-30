package llc.bokadev.kompass.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/**
 * The whole page sits on one continuous [mutedViolet] → [duskViolet] → [inkBlack] gradient. Lettering
 * directly on it is white ([white85] for secondary text keeps WCAG AA at every point); dark text
 * belongs on its own surface (cards, sheets, the yellow header).
 */
val pageGradientStops = listOf(mutedViolet, duskViolet, inkBlack)

/**
 * The app-wide page background, pinned to the screen: apply it *before* `verticalScroll`, so content
 * scrolls over a fixed backdrop, exactly like Home.
 */
fun Modifier.pageGradientBackground(): Modifier =
    background(Brush.verticalGradient(pageGradientStops))
