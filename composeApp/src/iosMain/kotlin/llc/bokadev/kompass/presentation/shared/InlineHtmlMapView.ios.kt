package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun InlineHtmlMapView(
    html: String,
    modifier: Modifier
) {
    // Track the last-loaded HTML to avoid resetting Leaflet zoom/pan on every
    // audio-progress recomposition (array used as a mutable ref, not Compose state)
    val loadedHtml = remember { arrayOf("") }

    UIKitView(
        modifier = modifier,
        factory = {
            loadedHtml[0] = html
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration()
            ).apply {
                loadHTMLString(html, baseURL = null)
            }
        },
        update = { webView ->
            if (html != loadedHtml[0]) {
                loadedHtml[0] = html
                webView.loadHTMLString(html, baseURL = null)
            }
        }
    )
}
