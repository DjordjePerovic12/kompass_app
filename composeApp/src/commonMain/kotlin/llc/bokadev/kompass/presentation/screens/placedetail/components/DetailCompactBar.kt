package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.screens.home.components.BookmarkIcon
import llc.bokadev.kompass.presentation.screens.home.components.ChevronLeftIcon
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * The bar that replaces the hero's buttons once it scrolls away: Back, the title, Save. Opaque
 * rather than the spec's 96%, which let the large Gloock title ghost through underneath.
 * [onToggleSave] null hides the save button (e.g. the load-error state).
 */
@Composable
fun DetailCompactBar(
    title: String,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    saveLabel: String = "",
    savedStateLabel: String = "",
    onToggleSave: (() -> Unit)? = null
) {
    val colors = LamplightTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.night)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarButton(backLabel, null, onBack) { ChevronLeftIcon(color = colors.chalk, size = 18.dp) }
        Text(
            text = title,
            style = LamplightType.body().copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )
        onToggleSave?.let { BarButton(saveLabel, savedStateLabel, it) { BookmarkIcon(filled = isSaved, color = colors.chalk, size = 18.dp) } }
    }
}

@Composable
private fun BarButton(description: String, stateLabel: String?, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = description
                if (stateLabel != null) stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Preview
@Composable
private fun DetailCompactBarPreview() {
    DetailCompactBar("Kayak to Perast", "Back", {}, isSaved = true, saveLabel = "Remove from saved", savedStateLabel = "Saved", onToggleSave = {})
}
