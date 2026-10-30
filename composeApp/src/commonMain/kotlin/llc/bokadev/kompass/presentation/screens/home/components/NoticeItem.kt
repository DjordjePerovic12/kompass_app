package llc.bokadev.kompass.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

enum class NoticeLevel { URGENT, IMPORTANT, GENERAL }

/**
 * "Lamplight" notice (spec §3.4). Square corners on purpose — notices read as operational, not
 * discovery. [label] is the caller's already-built "IMPORTANT · UNTIL {date}" (or just
 * "IMPORTANT" when there's no end date — the "until" segment is the caller's to omit, this
 * component doesn't parse dates) so this stays presentation-only.
 */
@Composable
fun NoticeItem(
    level: NoticeLevel,
    label: String,
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    val shape = LamplightShapes.notice
    val isAlert = level != NoticeLevel.GENERAL

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isAlert) Color.Transparent else colors.surfaceRaised)
            .let {
                if (isAlert) it.border(1.5.dp, colors.alert, shape) else it
            }
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        if (level == NoticeLevel.URGENT) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(colors.alert)
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isAlert) {
                    WarningTriangleIcon(color = colors.alert, size = 14.dp)
                } else {
                    InfoCircleIcon(color = colors.textSecondary, size = 14.dp)
                }
                Text(
                    text = label,
                    style = LamplightType.label(),
                    color = if (isAlert) colors.alert else colors.textSecondary
                )
            }
            Text(
                text = title,
                style = LamplightType.bodyL(),
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                style = LamplightType.bodyS(),
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
