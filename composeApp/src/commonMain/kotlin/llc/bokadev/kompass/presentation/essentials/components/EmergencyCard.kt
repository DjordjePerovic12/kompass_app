package llc.bokadev.kompass.presentation.essentials.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import llc.bokadev.kompass.ui.theme.KompassTheme
import llc.bokadev.kompass.presentation.shared.icons.EssentialsGlyph
import llc.bokadev.kompass.presentation.shared.icons.EssentialsIcon

/** A dialable number with its (already localized) service label. */
data class DialEntry(val number: String, val label: String)

/**
 * Emergency numbers (Essentials spec §2.3): one large primary number and up to three tiles.
 * Every tap opens the dialer prefilled; nothing ever calls automatically. [callDescription] has
 * "{number}" and "{service}" placeholders.
 */
@Composable
fun EmergencyCard(
    title: String,
    primary: DialEntry,
    secondary: List<DialEntry>,
    callDescription: String,
    onDial: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = KompassTheme.colors
    fun describe(entry: DialEntry) = callDescription.replace("{number}", entry.number).replace("{service}", entry.label)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.charcoal)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EssentialsIcon(EssentialsGlyph.Warning, color = colors.coralRed, size = 14.dp)
            Text(text = title.uppercase(), style = KompassTheme.typography.geologicaSemiBold11, color = colors.coralRed)
        }
        // One row: 112 first (alert outline), then the CMS numbers. Every number stays one tap away.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf(primary) + secondary.take(3)).forEach { entry ->
                val isPrimary = entry == primary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.slateDark)
                        .then(if (isPrimary) Modifier.border(1.5.dp, colors.coralRed, RoundedCornerShape(14.dp)) else Modifier)
                        .clickable { onDial(entry.number) }
                        .clearAndSetSemantics {
                            role = Role.Button
                            contentDescription = describe(entry)
                            onClick { onDial(entry.number); true }
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = entry.number,
                        style = KompassTheme.typography.geologicaMedium16.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                        color = if (isPrimary) colors.coralRed else colors.boneWhite
                    )
                    Text(text = entry.label, style = KompassTheme.typography.geologicaRegular13.copy(fontSize = 12.sp), color = colors.stoneGray, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}

@Preview
@Composable
private fun EmergencyCardPreview() {
    KompassTheme {
        EmergencyCard(
            title = "Emergency",
            primary = DialEntry("112", "General emergency"),
            secondary = listOf(DialEntry("122", "Police"), DialEntry("123", "Fire deprtment"), DialEntry("124", "Ambulance")),
            callDescription = "Call {number}, {service}",
            onDial = {}
        )
    }
}
