package llc.bokadev.kompass.presentation.shared

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
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.icons.InfoCircleIcon
import llc.bokadev.kompass.presentation.shared.icons.WarningTriangleIcon

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
    val colors = KompassTheme.colors
    val shape = KompassShapes.notice
    val isAlert = level != NoticeLevel.GENERAL

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isAlert) Color.Transparent else colors.charcoal)
            .let {
                if (isAlert) it.border(1.5.dp, colors.coralRed, shape) else it
            }
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        if (level == NoticeLevel.URGENT) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(colors.coralRed)
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isAlert) {
                    WarningTriangleIcon(color = colors.coralRed, size = 14.dp)
                } else {
                    InfoCircleIcon(color = colors.stoneGray, size = 14.dp)
                }
                Text(
                    text = label,
                    style = KompassTheme.typography.geologicaSemiBold11,
                    color = if (isAlert) colors.coralRed else colors.stoneGray
                )
            }
            Text(
                text = title,
                style = KompassTheme.typography.geologicaSemiBold17,
                color = colors.boneWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                style = KompassTheme.typography.geologicaRegular14,
                color = colors.stoneGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
