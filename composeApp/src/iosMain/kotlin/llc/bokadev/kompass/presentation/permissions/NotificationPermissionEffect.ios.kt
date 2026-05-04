package llc.bokadev.kompass.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    LaunchedEffect(enabled) {
        if (enabled) onPermissionResult(true)
    }
}

actual fun isNotificationPermissionPromptRelevant(): Boolean = false

@Composable
actual fun rememberShouldShowNotificationPrompt(): Boolean = false
