package llc.bokadev.kompass.presentation.screens.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.ui.theme.KompassTheme

/** A type filter: label + muted count. Selected = `chalk` fill, `night` 700 text; else outline. */
@Composable
fun SearchTypeChip(
    label: String,
    count: Int,
    selected: Boolean,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    val shape = RoundedCornerShape(20.dp)
    val text = if (selected) colors.nightBlack else colors.boneWhite
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .then(if (selected) Modifier.background(colors.chalkWhite) else Modifier.border(1.5.dp, colors.slateGray, shape))
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = accessibilityLabel
            }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = text
        )
        Text(text = count.toString(), style = KompassTheme.typography.geologicaRegular13, color = text.copy(alpha = 0.6f))
    }
}

@Preview
@Composable
private fun SearchTypeChipPreview() {
    SearchTypeChip("Places", 3, selected = true, accessibilityLabel = "Places, 3 results", onClick = {})
}
