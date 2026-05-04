package llc.bokadev.kompass.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun NotificationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember {
        mutableStateOf(hasNotificationPermission(context))
    }
    val hasRequested = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.value = granted
        onPermissionResult(granted)
    }

    LaunchedEffect(enabled, hasPermission.value) {
        if (!enabled) return@LaunchedEffect
        if (hasPermission.value) {
            onPermissionResult(true)
        } else if (!hasRequested.value) {
            hasRequested.value = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

actual fun isNotificationPermissionPromptRelevant(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@Composable
actual fun rememberShouldShowNotificationPrompt(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val context = LocalContext.current
    return remember { !hasNotificationPermission(context) }
}

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}
