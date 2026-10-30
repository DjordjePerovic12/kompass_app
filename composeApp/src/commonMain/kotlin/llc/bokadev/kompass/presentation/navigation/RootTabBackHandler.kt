package llc.bokadev.kompass.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun RootTabBackHandler(enabled: Boolean, onBack: () -> Unit)
