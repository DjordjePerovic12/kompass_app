package llc.bokadev.kompass.presentation.screens.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.screens.home.components.ArrowRightIcon
import llc.bokadev.kompass.presentation.screens.home.components.BookmarkIcon
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.ui.theme.KompassShapes

private val Arch = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)

/** Nothing saved yet (Saved spec §4.3): a decorative lamp arch, a calm line, and one way out. */
@Composable
fun SavedEmptyState(
    title: String,
    body: String,
    actionLabel: String,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(width = 100.dp, height = 108.dp).clearAndSetSemantics { }) {
            Box(Modifier.size(width = 92.dp, height = 104.dp).offset((-0).dp, 4.dp).background(colors.nightBlack, Arch))
            Box(
                Modifier.size(width = 92.dp, height = 104.dp).offset(8.dp, 0.dp).clip(Arch).background(colors.lampYellow),
                contentAlignment = Alignment.Center
            ) {
                BookmarkIcon(filled = false, color = colors.nightBlack, size = 40.dp)
            }
        }
        Text(
            text = title,
            style = KompassTheme.typography.gloockRegular32.copy(fontSize = 30.sp, lineHeight = 33.sp),
            color = colors.boneWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = body,
            style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
            color = colors.stoneGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 290.dp)
        )
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .heightIn(min = 52.dp)
                .clip(KompassShapes.pill)
                .background(colors.lampYellow)
                .clickable(onClick = onExplore)
                .semantics(mergeDescendants = true) { role = Role.Button }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = actionLabel, style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.nightBlack)
            ArrowRightIcon(color = colors.nightBlack, size = 16.dp)
        }
    }
}

@Preview
@Composable
private fun SavedEmptyStatePreview() {
    SavedEmptyState("Nothing saved yet", "Tap the bookmark on any place or activity and it will wait here, even offline.", "Explore Kotor", {})
}
