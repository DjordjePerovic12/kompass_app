package llc.bokadev.kompass.presentation.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun LocationPermissionEffect(
    enabled: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember {
        mutableStateOf(
            hasLocationPermission(context)
        )
    }
    val hasRequested = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        hasPermission.value = granted
        onPermissionResult(granted)
    }

    LaunchedEffect(enabled, hasPermission.value) {
        if (!enabled) return@LaunchedEffect
        if (hasPermission.value) {
            onPermissionResult(true)
        } else if (!hasRequested.value) {
            hasRequested.value = true
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val hasFine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return hasFine || hasCoarse
}
