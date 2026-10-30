package llc.bokadev.kompass.presentation.screens.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** One row of "What's on & updates". [extraDescription] is appended for screen readers. */
data class ExploreListRow(
    val title: String,
    val subtitle: String,
    val leading: ExploreRowLeading,
    val extraDescription: String? = null,
    val onClick: () -> Unit
)

sealed interface ExploreRowLeading {
    /** Lamp mini-calendar with the real next-event date. */
    data class DateTile(val month: String, val day: String) : ExploreRowLeading
    /** Icon on `surfaceMuted`; [alertDot] marks an active important notice. */
    data class Icon(val glyph: ExploreGlyph, val alertDot: Boolean = false) : ExploreRowLeading
}

/** Grouped rows for Events / Info Center / Services (spec §2.6): one raised container, inset dividers. */
@Composable
fun ExploreGroupedList(rows: List<ExploreListRow>, modifier: Modifier = Modifier) {
    val colors = LamplightTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceRaised)
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(colors.divider))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .clickable(onClick = row.onClick)
                    .clearAndSetSemantics {
                        role = Role.Button
                        contentDescription = listOfNotNull(row.title, row.subtitle, row.extraDescription).joinToString(", ")
                        onClick { row.onClick(); true }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Leading(row.leading)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = row.title, style = LamplightType.bodyL(), color = colors.textPrimary)
                    Text(
                        text = row.subtitle,
                        style = LamplightType.meta(),
                        color = colors.textTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ExploreIcon(ExploreGlyph.ChevronRight, color = colors.textTertiary, size = 18.dp)
            }
        }
    }
}

@Composable
private fun Leading(leading: ExploreRowLeading) {
    val colors = LamplightTheme.colors
    val tileShape = RoundedCornerShape(12.dp)
    when (leading) {
        is ExploreRowLeading.DateTile -> Column(
            modifier = Modifier.size(44.dp).clip(tileShape).background(colors.lamp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = leading.month.uppercase(), style = LamplightType.label().copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp), color = colors.night)
            Text(text = leading.day, style = LamplightType.body().copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp), color = colors.night)
        }
        is ExploreRowLeading.Icon -> Box(Modifier.size(44.dp)) {
            Box(
                Modifier.size(44.dp).clip(tileShape).background(colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                ExploreIcon(leading.glyph, color = colors.lamp, size = 22.dp)
            }
            if (leading.alertDot) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.alert)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExploreGroupedListPreview() {
    ExploreGroupedList(
        rows = listOf(
            ExploreListRow("Events", "What's happening today, this week and this month", ExploreRowLeading.DateTile("Oct", "12")) {},
            ExploreListRow("Info Center", "Town notices, disruptions and practical updates", ExploreRowLeading.Icon(ExploreGlyph.Info, alertDot = true)) {},
            ExploreListRow("Services", "Local providers for practical needs", ExploreRowLeading.Icon(ExploreGlyph.Wrench)) {}
        )
    )
}
