package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable

/** True when the OS asks for reduced motion (Android: animator scale 0; iOS: Reduce Motion). */
@Composable
expect fun rememberReduceMotion(): Boolean

/**
 * Light (white) status bar icons while this is in composition, restoring the previous style on
 * dispose. iOS: no-op for now; the status bar style there is owned by the hosting view controller.
 */
@Composable
expect fun StatusBarIconsEffect(lightIcons: Boolean)

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
