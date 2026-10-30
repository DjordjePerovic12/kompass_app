package llc.bokadev.kompass.presentation.localfinds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsGlyph
import llc.bokadev.kompass.presentation.screens.essentials.components.EssentialsIcon
import llc.bokadev.kompass.ui.theme.KompassTheme

/** A location with coordinates: pin · name · Map (opens external maps at that point, like Essentials). */
@Composable
fun StoryLocationRow(title: String, mapLabel: String, onMap: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KompassTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.charcoal)
            .clickable(onClick = onMap)
            .clearAndSetSemantics { role = Role.Button; contentDescription = "$title, $mapLabel" }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EssentialsIcon(EssentialsGlyph.Pin, colors.lampYellow, 18.dp)
        Text(title, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.SemiBold), color = colors.boneWhite, modifier = Modifier.weight(1f))
        Text(mapLabel, style = KompassTheme.typography.geologicaRegular14.copy(fontWeight = FontWeight.Bold), color = colors.lampYellow)
    }
}

@Preview
@Composable
private fun StoryLocationRowPreview() {
    KompassTheme {
        StoryLocationRow("Karampana Wall", "Map", {})
    }
}
