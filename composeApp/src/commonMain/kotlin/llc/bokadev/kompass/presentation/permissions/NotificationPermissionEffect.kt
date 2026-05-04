package llc.bokadev.kompass.presentation.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun NotificationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
)

expect fun isNotificationPermissionPromptRelevant(): Boolean

/** Returns true only when the notification permission is needed but not yet granted. */
@Composable
expect fun rememberShouldShowNotificationPrompt(): Boolean
