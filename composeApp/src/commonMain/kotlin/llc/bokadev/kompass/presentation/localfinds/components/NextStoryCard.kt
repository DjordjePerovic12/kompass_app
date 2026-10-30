package llc.bokadev.kompass.presentation.localfinds.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.shared.icons.ArrowRightIcon
import llc.bokadev.kompass.ui.theme.KompassTheme

/** `limestone` card: NEXT STORY · title · a `night` circle with a `lamp` arrow. */
@Composable
fun NextStoryCard(label: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KompassTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.limestone)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { role = Role.Button; contentDescription = "$label: $title" }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = KompassTheme.typography.geologicaSemiBold11, color = colors.inkGray)
            Text(title, style = KompassTheme.typography.gloockRegular23.copy(fontSize = 22.sp, lineHeight = 25.sp), color = colors.nightBlack)
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(colors.nightBlack), contentAlignment = Alignment.Center) {
            ArrowRightIcon(colors.lampYellow, 16.dp)
        }
    }
}

@Preview
@Composable
private fun NextStoryCardPreview() {
    KompassTheme {
        NextStoryCard("NEXT STORY", "The stones of Our Lady of the Rocks", {})
    }
}
