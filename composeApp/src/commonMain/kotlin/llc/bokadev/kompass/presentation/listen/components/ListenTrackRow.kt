package llc.bokadev.kompass.presentation.listen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import llc.bokadev.kompass.presentation.listen.DownloadState
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * One library row (Listen spec §2.3): the photo *is* the play button, the text opens the place,
 * the right-hand button downloads. Three separate targets, in that focus order.
 */
@Composable
fun ListenTrackRow(
    label: String,
    title: String,
    meta: String,
    photoUrl: String?,
    disc: DiscState,
    active: Boolean,
    unavailable: Boolean,
    playDescription: String,
    download: DownloadState,
    downloadDescription: String,
    reduceMotion: Boolean,
    onPlay: () -> Unit,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val container by animateColorAsState(if (active) colors.lampShadow else colors.charcoal, label = "listenRow")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.slateDark)
                .clickable(onClick = onPlay)
                .clearAndSetSemantics { role = Role.Button; contentDescription = playDescription },
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp))
            }
            PlayDisc(disc, 40.dp, reduceMotion, Modifier.alpha(if (unavailable) 0.4f else 1f))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpen)
                .semantics(mergeDescendants = true) { role = Role.Button },
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = KompassTheme.typography.geologicaSemiBold11.copy(fontSize = 10.sp), color = colors.lampYellow, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                title,
                style = KompassTheme.typography.gloockRegular23.copy(fontSize = 19.sp, lineHeight = 21.sp),
                color = if (unavailable) colors.pebbleGray else colors.boneWhite,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(meta, style = KompassTheme.typography.geologicaRegular13, color = colors.pebbleGray)
        }
        DownloadButton(download, downloadDescription, onDownload)
    }
}

@Preview
@Composable
private fun ListenTrackRowPreview() {
    ListenTrackRow(
        label = "CATHEDRAL · OLD TOWN", title = "Cathedral of Saint Tryphon", meta = "4:12 · Downloaded", photoUrl = null,
        disc = DiscState.Idle, active = false, unavailable = false, playDescription = "Play Cathedral of Saint Tryphon",
        download = DownloadState.Downloaded, downloadDescription = "Downloaded, tap to remove", reduceMotion = true,
        onPlay = {}, onOpen = {}, onDownload = {}
    )
}
