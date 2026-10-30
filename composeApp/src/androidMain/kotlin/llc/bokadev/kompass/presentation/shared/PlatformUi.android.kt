package llc.bokadev.kompass.presentation.shared

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

@Composable
actual fun StatusBarIconsEffect(lightIcons: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, lightIcons) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = !lightIcons
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
}

@Composable
actual fun rememberExternalLauncher(): ExternalLauncher {
    val context = LocalContext.current
    return remember(context) { AndroidExternalLauncher(context) }
}

private class AndroidExternalLauncher(private val context: Context) : ExternalLauncher {
    override fun openMaps(latitude: Double, longitude: Double, label: String): Boolean {
        val geo = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        return start(Intent(Intent.ACTION_VIEW, geo)) ||
            start(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")))
    }

    override fun dial(phone: String): Boolean =
        start(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.toDialString()}")))

    override fun openUrl(url: String): Boolean =
        start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    override fun openAppSettings(): Boolean =
        start(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))

    private fun start(intent: Intent): Boolean = try {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
