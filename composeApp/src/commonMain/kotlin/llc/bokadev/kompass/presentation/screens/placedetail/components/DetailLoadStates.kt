package llc.bokadev.kompass.presentation.screens.placedetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import llc.bokadev.kompass.presentation.theme.LamplightTheme
import llc.bokadev.kompass.presentation.theme.LamplightType

/**
 * A detail page that isn't loaded yet ([message] null: skeleton) or failed to load ([message]
 * + Retry). The compact bar above it keeps Back reachable.
 */
@Composable
fun DetailLoadState(
    message: String?,
    retryLabel: String,
    backLabel: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LamplightTheme.colors
    if (message == null) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(400.dp).background(colors.surfaceMuted))
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0.35f, 0.8f, 0.6f).forEach { fraction ->
                    Box(Modifier.fillMaxWidth(fraction).height(14.dp).clip(RoundedCornerShape(7.dp)).background(colors.surfaceMuted))
                }
                Spacer(Modifier.height(12.dp))
                repeat(2) { Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceRaised)) }
            }
        }
        return
    }
    Column(Modifier.fillMaxSize()) {
        DetailCompactBar(title = "", backLabel = backLabel, onBack = onBack)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(text = message, style = LamplightType.bodyL(), color = colors.textPrimary)
            DetailPillButton(label = retryLabel, glyph = null, filled = true, height = 48.dp, onClick = onRetry)
        }
    }
}

@Preview
@Composable
private fun DetailLoadStatePreview() {
    DetailLoadState("Couldn't load this activity.", "Retry", "Back", {}, {})
}
