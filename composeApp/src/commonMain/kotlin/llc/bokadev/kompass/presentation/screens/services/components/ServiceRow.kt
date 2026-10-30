package llc.bokadev.kompass.presentation.screens.services.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.screens.home.components.ArrowRightIcon
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

/** A compact, functional service row: name, the area it serves, and its contact (website host) when known. */
@Composable
fun ServiceRow(
    name: String,
    area: String?,
    contact: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(KompassShapes.cardEvent)
            .background(colors.charcoal)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = listOfNotNull(name, area, contact).joinToString(", ")
                onClick { onClick(); true }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = name,
                style = KompassTheme.typography.gloockRegular23.copy(fontSize = 20.sp, lineHeight = 23.sp),
                color = colors.boneWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(area, contact).joinToString(" · ")
            if (meta.isNotBlank()) Text(text = meta, style = KompassTheme.typography.geologicaRegular14, color = colors.stoneGray, maxLines = 2)
        }
        ArrowRightIcon(color = colors.pebbleGray, size = 14.dp)
    }
}

@Preview
@Composable
private fun ServiceRowPreview() {
    ServiceRow(name = "Bay Laundry", area = "Dobrota", contact = "baylaundry.me", onClick = {})
}
