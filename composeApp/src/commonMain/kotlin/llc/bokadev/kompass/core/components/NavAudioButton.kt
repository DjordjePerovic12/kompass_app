package llc.bokadev.kompass.core.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.audio.AudioState
import llc.bokadev.kompass.presentation.shared.rememberReduceMotion
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

// Step 7.9: 50/64 → 46/58, so the Listen button no longer sets a much taller bar; still ≥ 48dp to touch.
private val ButtonSize = 46.dp
private val RingSize = 58.dp // button + room for the 3dp progress ring outside it
private val RingRadius = ButtonSize / 2 + 4.dp + 1.5.dp

/** What the center button shows: app-wide playback, whichever screen started it. */
sealed interface ListenButtonState {
    data object Idle : ListenButtonState
    data object Loading : ListenButtonState
    data class Playing(val progress: Float?) : ListenButtonState
    data class Paused(val progress: Float?) : ListenButtonState
}

fun AudioState.toListenButtonState(): ListenButtonState = when (this) {
    is AudioState.Loading -> ListenButtonState.Loading
    is AudioState.Playing -> ListenButtonState.Playing(fraction(positionMs, durationMs))
    is AudioState.Paused -> ListenButtonState.Paused(fraction(positionMs, durationMs))
    AudioState.Idle, is AudioState.Failed -> ListenButtonState.Idle
}

private fun fraction(positionMs: Long, durationMs: Long?): Float? =
    durationMs?.takeIf { it > 0 }?.let { (positionMs.toFloat() / it).coerceIn(0f, 1f) }

/**
 * The floating bar's center button: a stable, labeled entry to the Listen library (owner decision
 * 2026-10-07). It never plays or pauses on its own — content screens have explicit play controls.
 * While something plays it shows equalizer bars and the progress ring, so the library (with its
 * Now playing card) is where to go to control it. [current]: the library is on top ("you're here").
 */
@Composable
fun NavAudioButton(
    state: ListenButtonState,
    label: String,
    description: String,
    current: Boolean,
    onClick: () -> Unit
) {
    val colors = LamplightTheme.colors
    val reduceMotion = rememberReduceMotion()
    Column(
        modifier = Modifier
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                if (current) selected = true
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Everything stays inside the bar (the shell clips above it); the bar bottom-aligns its
        // items, so this label shares the tab labels' baseline.
        Box(Modifier.size(RingSize), contentAlignment = Alignment.Center) {
            if (current) Box(Modifier.size(ButtonSize + 10.dp).border(2.dp, colors.lamp, CircleShape))
            when (state) {
                is ListenButtonState.Playing -> state.progress?.let { ProgressRing(it) }
                is ListenButtonState.Paused -> state.progress?.let { ProgressRing(it) }
                ListenButtonState.Loading -> LoadingRing(reduceMotion)
                ListenButtonState.Idle -> Unit
            }
            Box(
                modifier = Modifier
                    .size(ButtonSize)
                    .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color.Black.copy(alpha = 0.2f), spotColor = Color.Black.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(colors.lamp),
                contentAlignment = Alignment.Center
            ) {
                if (state is ListenButtonState.Playing) EqualizerBars(color = colors.night, reduceMotion = reduceMotion)
                else Canvas(Modifier.size(24.dp)) { drawHeadphones(colors.night) }
            }
        }
        Text(
            text = label,
            style = LamplightType.navLabel(selected = current),
            color = if (current) colors.chalk else colors.textTertiary,
            maxLines = 1
        )
    }
}

/** Determinate 3dp ring 4dp outside the button, from 12 o'clock, clockwise (spec §2.4 C). */
@Composable
private fun ProgressRing(progress: Float) {
    val lamp = LamplightTheme.colors.lamp
    val sweep by animateFloatAsState(progress * 360f, tween(200, easing = LinearEasing), label = "audioProgress")
    Canvas(Modifier.size(RingSize)) {
        val stroke = 3.dp.toPx()
        val radius = RingRadius.toPx()
        drawArc(
            color = lamp,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/** Indeterminate ring for Loading. Static quarter arc with reduced motion. */
@Composable
private fun LoadingRing(reduceMotion: Boolean) {
    val lamp = LamplightTheme.colors.lamp
    val rotation = if (reduceMotion) 0f else {
        val transition = rememberInfiniteTransition(label = "audioLoading")
        val value by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1_000, easing = LinearEasing)), label = "audioLoadingRotation")
        value
    }
    Canvas(Modifier.size(RingSize).graphicsLayer { rotationZ = rotation }) {
        val radius = RingRadius.toPx()
        drawArc(
            color = lamp,
            startAngle = -90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/** Three bars bouncing while audio actually plays (spec §2.4 B). */
@Composable
internal fun EqualizerBars(color: Color, reduceMotion: Boolean) {
    val staticHeights = listOf(0.6f, 1f, 0.75f)
    val transition = rememberInfiniteTransition(label = "equalizer")
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        repeat(3) { index ->
            val scale = if (reduceMotion) staticHeights[index] else {
                val value by transition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(550, delayMillis = 0, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(200 * index)
                    ),
                    label = "bar$index"
                )
                value
            }
            Box(
                Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .graphicsLayer {
                        scaleY = scale
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

internal fun DrawScope.drawPlay(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.22f, h * 0.12f)
        lineTo(w * 0.86f, h * 0.5f)
        lineTo(w * 0.22f, h * 0.88f)
        close()
    }
    drawPath(path, color)
}

internal fun DrawScope.drawPause(color: Color) {
    val w = size.width
    val h = size.height
    val barWidth = w * 0.26f
    drawRoundRect(color, topLeft = Offset(w * 0.16f, h * 0.12f), size = Size(barWidth, h * 0.76f), cornerRadius = CornerRadius(1.5.dp.toPx()))
    drawRoundRect(color, topLeft = Offset(w * 0.58f, h * 0.12f), size = Size(barWidth, h * 0.76f), cornerRadius = CornerRadius(1.5.dp.toPx()))
}

/** A circular "retry" arrow. */
internal fun DrawScope.drawRetry(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = 2.dp.toPx()
    drawArc(
        color = color,
        startAngle = -60f,
        sweepAngle = 300f,
        useCenter = false,
        topLeft = Offset(w * 0.16f, h * 0.16f),
        size = Size(w * 0.68f, h * 0.68f),
        style = Stroke(width = stroke, cap = StrokeCap.Round)
    )
    val head = Path().apply {
        moveTo(w * 0.62f, h * 0.1f)
        lineTo(w * 0.8f, h * 0.22f)
        lineTo(w * 0.6f, h * 0.34f)
    }
    drawPath(head, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
