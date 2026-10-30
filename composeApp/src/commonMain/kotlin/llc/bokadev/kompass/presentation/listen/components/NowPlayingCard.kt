package llc.bokadev.kompass.presentation.listen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme

data class NowPlayingLabels(
    val status: String,
    val stop: String,
    val back15: String,
    val forward15: String,
    val mainButton: String,
    val region: String,
    /** "{elapsed} of {duration}" for the slider. */
    val progressValue: String,
    val retry: String
)

/**
 * The library's Now playing card (Listen spec §2.2): status + Stop, the title (opens the place),
 * a seekable 6dp progress bar (a slider with ±15s actions for accessibility), and −15 / play-pause / +15.
 */
@Composable
fun NowPlayingCard(
    title: String,
    disc: DiscState,
    positionMs: Long,
    durationMs: Long?,
    labels: NowPlayingLabels,
    reduceMotion: Boolean,
    formatTime: (Long) -> String,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val ink = colors.nightBlack
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.lampYellow)
            .semantics { paneTitle = labels.region }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(labels.status, style = KompassTheme.typography.geologicaSemiBold11, color = ink, modifier = Modifier.weight(1f))
            if (disc == DiscState.Failed) {
                Text(
                    labels.retry,
                    style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.Bold),
                    color = ink,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onToggle).padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ink.copy(alpha = 0.1f))
                    .clickable(onClick = onStop)
                    .clearAndSetSemantics { role = Role.Button; contentDescription = labels.stop },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(ink))
            }
        }
        Text(
            title,
            style = KompassTheme.typography.gloockRegular23.copy(fontSize = 24.sp, lineHeight = 27.sp),
            color = ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(onClick = onOpen).semantics { role = Role.Button }
        )
        ProgressSlider(positionMs, durationMs, ink, labels, formatTime, onSeek, onSkip)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkipButton("−15", labels.back15, ink) { onSkip(-15_000) }
            Box(
                Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onToggle)
                    .clearAndSetSemantics { role = Role.Button; contentDescription = labels.mainButton }
            ) {
                PlayDisc(disc, 64.dp, reduceMotion, background = ink, ink = colors.lampYellow, showPause = true)
            }
            SkipButton("+15", labels.forward15, ink) { onSkip(15_000) }
        }
    }
}

@Composable
private fun SkipButton(text: String, description: String, ink: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description },
        contentAlignment = Alignment.Center
    ) { Text(text, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.Bold), color = ink) }
}

@Composable
private fun ProgressSlider(
    positionMs: Long,
    durationMs: Long?,
    ink: Color,
    labels: NowPlayingLabels,
    formatTime: (Long) -> String,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit
) {
    val duration = durationMs?.takeIf { it > 0 }
    // While dragging, the bar follows the finger; the seek is sent on release.
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val fraction = dragFraction ?: duration?.let { (positionMs.toFloat() / it).coerceIn(0f, 1f) } ?: 0f
    val shownPosition = dragFraction?.let { f -> duration?.let { (it * f).toLong() } } ?: positionMs
    val value = labels.progressValue
        .replace("{elapsed}", formatTime(shownPosition))
        .replace("{duration}", duration?.let(formatTime) ?: "")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 24.dp)
                .then(
                    if (duration == null) Modifier else Modifier
                        .pointerInput(duration) {
                            detectTapGestures { offset -> onSeek((duration * (offset.x / size.width).coerceIn(0f, 1f)).toLong()) }
                        }
                        .pointerInput(duration) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragFraction = (it.x / size.width).coerceIn(0f, 1f) },
                                onDragEnd = {
                                    dragFraction?.let { onSeek((duration * it).toLong()) }
                                    dragFraction = null
                                },
                                onDragCancel = { dragFraction = null },
                                onHorizontalDrag = { change, _ -> dragFraction = (change.position.x / size.width).coerceIn(0f, 1f) }
                            )
                        }
                )
                .semantics {
                    stateDescription = value
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    if (duration != null) {
                        setProgress { target -> onSeek((duration * target.coerceIn(0f, 1f)).toLong()); true }
                        customActions = listOf(
                            CustomAccessibilityAction(labels.back15) { onSkip(-15_000); true },
                            CustomAccessibilityAction(labels.forward15) { onSkip(15_000); true }
                        )
                    }
                }
        ) {
            val h = 6.dp.toPx()
            val top = (size.height - h) / 2
            val radius = CornerRadius(h / 2, h / 2)
            drawRoundRect(ink.copy(alpha = 0.18f), Offset(0f, top), Size(size.width, h), radius)
            drawRoundRect(ink, Offset(0f, top), Size(size.width * fraction, h), radius)
            if (duration != null) drawCircle(ink, radius = 7.dp.toPx(), center = Offset(size.width * fraction, size.height / 2))
        }
        Row(Modifier.fillMaxWidth()) {
            Text(formatTime(shownPosition), style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = ink, modifier = Modifier.weight(1f))
            Text(duration?.let(formatTime) ?: "", style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = ink)
        }
    }
}

@Preview
@Composable
private fun NowPlayingCardPreview() {
    NowPlayingCard(
        title = "Cathedral of Saint Tryphon", disc = DiscState.Playing, positionMs = 65_000, durationMs = 252_000,
        labels = NowPlayingLabels("NOW PLAYING", "Stop", "Back 15 seconds", "Forward 15 seconds", "Pause", "Now playing", "{elapsed} of {duration}", "Retry"),
        reduceMotion = true, formatTime = { "${it / 60_000}:${((it / 1000) % 60).toString().padStart(2, '0')}" },
        onToggle = {}, onStop = {}, onSeek = {}, onSkip = {}, onOpen = {}
    )
}
