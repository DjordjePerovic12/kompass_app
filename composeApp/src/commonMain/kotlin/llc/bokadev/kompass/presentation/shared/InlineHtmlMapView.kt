package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun InlineHtmlMapView(
    html: String,
    modifier: Modifier = Modifier
)
