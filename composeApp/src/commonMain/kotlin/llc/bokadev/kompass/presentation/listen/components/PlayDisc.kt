package llc.bokadev.kompass.presentation.listen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.components.EqualizerBars
import llc.bokadev.kompass.core.components.drawPause
import llc.bokadev.kompass.core.components.drawPlay
import llc.bokadev.kompass.core.components.drawRetry
import llc.bokadev.kompass.ui.theme.KompassTheme

/** What a play control shows (Listen spec §3). */
enum class DiscState { Idle, Loading, Playing, Paused, Failed }

/**
 * The `lamp` play disc: ▶ idle/paused, spinner loading, equalizer playing, ↻ failed. With
 * [showPause] (the Now playing card's main button) Playing shows a pause icon instead.
 * Decorative: the surrounding button carries the label.
 */
@Composable
fun PlayDisc(
    state: DiscState,
    size: Dp,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    background: Color = KompassTheme.colors.lampYellow,
    ink: Color = KompassTheme.colors.nightBlack,
    showPause: Boolean = false
) {
    Box(modifier.size(size).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        val glyph = size * 0.42f
        when (state) {
            DiscState.Loading -> CircularProgressIndicator(color = ink, strokeWidth = 2.dp, modifier = Modifier.size(size * 0.5f))
            DiscState.Playing -> if (showPause) Canvas(Modifier.size(glyph)) { drawPause(ink) } else EqualizerBars(ink, reduceMotion)
            DiscState.Failed -> Canvas(Modifier.size(glyph)) { drawRetry(ink) }
            DiscState.Idle, DiscState.Paused -> Canvas(Modifier.size(glyph).offset(x = 1.dp)) { drawPlay(ink) }
        }
    }
}

@Preview
@Composable
private fun PlayDiscPreview() {
    PlayDisc(DiscState.Idle, 40.dp, reduceMotion = true)
}
