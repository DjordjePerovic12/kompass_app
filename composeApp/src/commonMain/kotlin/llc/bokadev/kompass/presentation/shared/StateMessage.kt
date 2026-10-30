package llc.bokadev.kompass.presentation.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * A short, localized state line (empty / no filter match / couldn't load / couldn't refresh) with
 * an optional action such as Retry or Reset filters. Announced politely to screen readers.
 */
@Composable
fun StateMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val colors = LamplightTheme.colors
    Column(modifier = modifier.padding(horizontal = 2.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = text,
            style = LamplightType.body(),
            color = colors.textSecondary,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        actionLabel?.let {
            Text(
                text = it,
                style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
                color = colors.lamp,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Preview
@Composable
private fun StateMessagePreview() {
    StateMessage(text = "Couldn't load events.", actionLabel = "Retry")
}
