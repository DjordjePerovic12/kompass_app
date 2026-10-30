package llc.bokadev.kompass.presentation.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.ui.theme.KompassTheme

private const val MAX_DOTS = 5

/**
 * The Lamplight page indicator shared by Home's Nearby carousel and the place detail photo
 * pager: the current page is a 28×8 `lamp` pill, the others 8dp dots at `textPrimary` 55%,
 * windowed to 5 for long lists. Purely visual: the pager itself announces "n of total".
 */
@Composable
fun PagerDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= 1) return
    val colors = KompassTheme.colors
    Row(
        modifier = modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dotWindow(current, total).forEach { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) 28.dp else 8.dp, tween(200), label = "dotWidth")
            Box(
                Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.lampYellow else colors.boneWhite.copy(alpha = 0.55f))
            )
        }
    }
}

/**
 * The page indices whose dots are shown: all of them up to [max], otherwise a [max]-wide window
 * that keeps [current] centred and slides only once it would run past either end.
 */
internal fun dotWindow(current: Int, total: Int, max: Int = MAX_DOTS): IntRange {
    if (total <= max) return 0 until total
    val start = (current - max / 2).coerceIn(0, total - max)
    return start until start + max
}

@Preview
@Composable
private fun PagerDotsPreview() {
    PagerDots(current = 2, total = 8)
}
