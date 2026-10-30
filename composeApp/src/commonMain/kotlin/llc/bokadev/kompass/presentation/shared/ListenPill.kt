package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.core.audio.NavAudioMode
import llc.bokadev.kompass.presentation.listen.components.DiscState
import llc.bokadev.kompass.presentation.listen.components.PlayDisc
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/**
 * The explicit play control on a detail screen ("Listen to this story", "Listen to the audio guide").
 * The center button only opens the library, so this is how content audio is played and paused.
 */
@Composable
fun ListenPill(
    label: String,
    disc: DiscState,
    unavailable: Boolean,
    description: String,
    reduceMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(KompassShapes.pill)
            .background(colors.charcoal)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description }
            .alpha(if (unavailable) 0.45f else 1f)
            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayDisc(disc, 36.dp, reduceMotion, showPause = true)
        Text(label, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold), color = colors.boneWhite)
    }
}

@Preview
@Composable
private fun ListenPillPreview() {
    ListenPill("Listen to this story", DiscState.Idle, false, "Listen to this story", true, {})
}

/** The disc shown for a content track's state. */
fun NavAudioMode.toDisc(): DiscState = when (this) {
    is NavAudioMode.Loading -> DiscState.Loading
    is NavAudioMode.Playing -> DiscState.Playing
    is NavAudioMode.Paused -> DiscState.Paused
    is NavAudioMode.Failed -> DiscState.Failed
    else -> DiscState.Idle
}
