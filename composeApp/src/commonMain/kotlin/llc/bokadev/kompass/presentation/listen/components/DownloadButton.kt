package llc.bokadev.kompass.presentation.listen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.listen.DownloadState
import llc.bokadev.kompass.ui.theme.KompassTheme

/** 44dp: ↓ download · determinate ring while downloading (tap cancels) · ✓ `lamp` when downloaded. */
@Composable
fun DownloadButton(state: DownloadState, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KompassTheme.colors
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.slateDark)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            DownloadState.NotDownloaded -> Canvas(Modifier.size(18.dp)) {
                val w = size.width; val h = size.height; val s = 1.8.dp.toPx()
                drawLine(colors.boneWhite, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.66f), s, StrokeCap.Round)
                drawLine(colors.boneWhite, Offset(w * 0.28f, h * 0.45f), Offset(w * 0.5f, h * 0.67f), s, StrokeCap.Round)
                drawLine(colors.boneWhite, Offset(w * 0.72f, h * 0.45f), Offset(w * 0.5f, h * 0.67f), s, StrokeCap.Round)
                drawLine(colors.boneWhite, Offset(w * 0.18f, h * 0.88f), Offset(w * 0.82f, h * 0.88f), s, StrokeCap.Round)
            }
            is DownloadState.Downloading -> {
                val progress = state.progress
                if (progress == null) {
                    CircularProgressIndicator(color = colors.lampYellow, trackColor = colors.slateGray, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                } else {
                    CircularProgressIndicator(progress = { progress }, color = colors.lampYellow, trackColor = colors.slateGray, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                }
            }
            DownloadState.Downloaded -> Canvas(Modifier.size(18.dp)) {
                val w = size.width; val h = size.height; val s = 2.dp.toPx()
                drawLine(colors.lampYellow, Offset(w * 0.18f, h * 0.52f), Offset(w * 0.42f, h * 0.76f), s, StrokeCap.Round)
                drawLine(colors.lampYellow, Offset(w * 0.42f, h * 0.76f), Offset(w * 0.84f, h * 0.28f), s, StrokeCap.Round)
            }
        }
    }
}

@Preview
@Composable
private fun DownloadButtonPreview() {
    DownloadButton(DownloadState.Downloading(0.42f), "Downloading 42 percent, tap to cancel", {})
}
