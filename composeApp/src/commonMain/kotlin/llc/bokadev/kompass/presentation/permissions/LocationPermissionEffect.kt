package llc.bokadev.kompass.presentation.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun LocationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
)
