package llc.bokadev.kompass.presentation.screens.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme

/**
 * Legends & Localisms (spec §2.5): a `lamp` card with an oversized decorative quote mark, so the
 * stories read as cultural interpretation rather than another venue category.
 */
@Composable
fun ExploreStoryCard(
    label: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.lampYellow)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
                onClick { onClick(); true }
            }
    ) {
        Text(
            text = "“",
            style = KompassTheme.typography.gloockRegular40.copy(fontSize = 140.sp, lineHeight = 140.sp),
            color = colors.nightBlack,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = (-18).dp).alpha(0.16f)
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = label.uppercase(), style = KompassTheme.typography.geologicaSemiBold11, color = colors.nightBlack)
            Text(text = title, style = KompassTheme.typography.gloockRegular32.copy(fontSize = 30.sp, lineHeight = 33.sp), color = colors.nightBlack)
            Text(
                text = subtitle,
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                color = colors.nightBlack,
                modifier = Modifier.widthIn(max = 260.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ExploreStoryCardPreview() {
    ExploreStoryCard("Read · Listen", "Legends & Localisms", "Local stories and traditions, told the way people here tell them.", {})
}
