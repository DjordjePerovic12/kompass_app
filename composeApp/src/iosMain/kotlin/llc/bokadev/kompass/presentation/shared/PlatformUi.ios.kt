package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberReduceMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }

@Composable
actual fun StatusBarIconsEffect(lightIcons: Boolean) = Unit

@Composable
actual fun rememberExternalLauncher(): ExternalLauncher = remember { IosExternalLauncher() }

private class IosExternalLauncher : ExternalLauncher {
    override fun openMaps(latitude: Double, longitude: Double, label: String): Boolean {
        // Apple Maps' https link opens the Maps app at this coordinate, labelled with the name;
        // unlike `maps://` it needs no LSApplicationQueriesSchemes entry.
        return open("https://maps.apple.com/?ll=$latitude,$longitude&q=${label.encoded()}")
    }

    override fun dial(phone: String): Boolean = open("tel:${phone.toDialString()}")

    override fun openUrl(url: String): Boolean = open(url)

    override fun openAppSettings(): Boolean = open(UIApplicationOpenSettingsURLString)

    private fun open(raw: String): Boolean {
        val url = NSURL.URLWithString(raw) ?: return false
        // canOpenURL would need every scheme declared in Info.plist; just hand off.
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        return true
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun String.encoded(): String =
        (this as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: ""
}
