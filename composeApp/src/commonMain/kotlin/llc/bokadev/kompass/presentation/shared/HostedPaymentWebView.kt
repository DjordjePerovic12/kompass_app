package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun HostedPaymentWebView(
    url: String,
    modifier: Modifier = Modifier,
    onUrlChange: (String) -> Unit
)
