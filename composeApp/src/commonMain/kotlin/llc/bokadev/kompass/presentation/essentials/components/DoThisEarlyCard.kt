package llc.bokadev.kompass.presentation.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon

/**
 * "Do this early" (Essentials spec §2.2): a calm limestone card for the one formality visitors
 * shouldn't leave late. [summary] is verified CMS copy only (no invented deadlines or amounts).
 */
@Composable
fun DoThisEarlyCard(
    label: String,
    title: String,
    summary: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    /** Hides only this reminder on this device; never implies the task is done. */
    dismissLabel: String? = null,
    onDismiss: () -> Unit = {}
) {
    val colors = KompassTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.limestone)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EssentialsIcon(EssentialsGlyph.Clock, color = colors.inkGray, size = 14.dp)
            Text(text = label.uppercase(), style = KompassTheme.typography.geologicaSemiBold11, color = colors.inkGray)
        }
        Text(
            text = title,
            style = KompassTheme.typography.gloockRegular23.copy(fontSize = 25.sp, lineHeight = 27.sp),
            color = colors.nightBlack,
            modifier = Modifier.semantics { heading() }
        )
        if (summary.isNotBlank()) {
            Text(
                text = summary,
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
                color = colors.inkGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .heightIn(min = 48.dp)
                .clip(KompassShapes.pill)
                .background(colors.nightBlack)
                .clickable(onClick = onAction)
                .semantics(mergeDescendants = true) { role = Role.Button }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = actionLabel, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.lampYellow)
            EssentialsIcon(EssentialsGlyph.ArrowUpRight, color = colors.lampYellow, size = 14.dp)
        }
        dismissLabel?.let {
            Text(
                text = it,
                style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = colors.inkGray,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .heightIn(min = 48.dp)
                    .clip(KompassShapes.pill)
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(horizontal = 8.dp, vertical = 14.dp)
            )
        }
        }
    }
}

@Preview
@Composable
private fun DoThisEarlyCardPreview() {
    KompassTheme {
        DoThisEarlyCard("Do this early", "Tourist Registration & Tax", "If your stay requires registration, complete it within the first 24 hours.", "How it works", {})
    }
}
