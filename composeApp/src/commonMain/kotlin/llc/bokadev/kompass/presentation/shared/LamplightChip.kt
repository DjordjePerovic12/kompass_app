package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.LamplightShapes
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/** A single-choice filter pill on Lamplight surfaces (48dp touch height, selected = lamp fill). */
@Composable
fun LamplightChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LamplightTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LamplightType.bodyS().copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) colors.night else colors.textPrimary,
            modifier = Modifier
                .clip(LamplightShapes.pill)
                .background(if (selected) colors.lamp else Color.Transparent)
                .border(1.5.dp, if (selected) colors.lamp else colors.borderStrong, LamplightShapes.pill)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Preview
@Composable
private fun LamplightChipPreview() {
    LamplightChip(label = "Today", selected = true, onClick = {})
}
