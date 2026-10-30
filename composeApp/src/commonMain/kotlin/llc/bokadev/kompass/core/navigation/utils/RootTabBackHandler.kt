package llc.bokadev.kompass.core.navigation.utils

import androidx.compose.runtime.Composable

@Composable
expect fun RootTabBackHandler(enabled: Boolean, onBack: () -> Unit)
