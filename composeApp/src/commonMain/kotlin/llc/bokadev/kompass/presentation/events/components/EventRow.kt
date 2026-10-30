package llc.bokadev.kompass.presentation.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/**
 * A scannable event row: time (or "Ongoing") | title, venue, price. Only data the CMS has is
 * shown; a missing venue or price simply drops out.
 */
@Composable
fun EventRow(
    time: String,
    title: String,
    venue: String?,
    price: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(KompassShapes.cardEvent)
            .background(colors.charcoal)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = listOfNotNull(time, title, venue, price).joinToString(", ")
                onClick { onClick(); true }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time,
            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold),
            color = colors.lampYellow,
            modifier = Modifier.width(64.dp)
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = KompassTheme.typography.gloockRegular23.copy(fontSize = 20.sp, lineHeight = 23.sp),
                color = colors.boneWhite,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(venue, price).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(text = meta, style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray)
            }
        }
    }
}

@Preview
@Composable
private fun EventRowPreview() {
    KompassTheme {
        EventRow(time = "20:00", title = "Kotor Summer Fest: Klapa evening", venue = "Square of Arms", price = "Free", onClick = {})
    }
}
