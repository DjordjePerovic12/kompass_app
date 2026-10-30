package llc.bokadev.kompass.presentation.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import llc.bokadev.kompass.ui.theme.KompassTheme

/** True when the OS asks for reduced motion (Android: animator scale 0; iOS: Reduce Motion). */
@Composable
expect fun rememberReduceMotion(): Boolean

/**
 * Light (white) status bar icons while this is in composition, restoring the previous style on
 * dispose. iOS sets the app-wide style (Info.plist turns off view-controller-based appearance).
 */
@Composable
expect fun StatusBarIconsEffect(lightIcons: Boolean)

/**
 * A night-black bar behind the status bar that fades in while [visible] (content has scrolled
 * under it), with light icons on top. At rest it is invisible, so headers look as designed.
 */
@Composable
fun BoxScope.StatusBarScrim(visible: Boolean) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(180))
    if (visible) StatusBarIconsEffect(lightIcons = true)
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .graphicsLayer { this.alpha = alpha }
            .background(KompassTheme.colors.nightBlack)
    )
}

/** Hands off to other apps. Every call returns false when nothing on the device could handle it. */
interface ExternalLauncher {
    /** Opens the system maps app at exactly this coordinate (never a name search), labelled [label]. */
    fun openMaps(latitude: Double, longitude: Double, label: String): Boolean

    /** Opens the dialer with [phone] prefilled. Never places the call. */
    fun dial(phone: String): Boolean

    /** Opens [url] in the external browser. */
    fun openUrl(url: String): Boolean

    /** Opens this app's page in the system settings (e.g. to turn location on); never re-prompts. */
    fun openAppSettings(): Boolean
}

@Composable
expect fun rememberExternalLauncher(): ExternalLauncher

/** Dial string: digits plus a leading `+` only (spec §1.5). */
fun String.toDialString(): String {
    val trimmed = trim()
    val digits = trimmed.filter { it.isDigit() }
    return if (trimmed.startsWith("+")) "+$digits" else digits
}
