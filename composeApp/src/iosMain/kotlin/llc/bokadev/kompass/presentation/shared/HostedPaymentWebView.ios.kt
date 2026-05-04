package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private class PaymentNavigationDelegate(
    private val onUrlChange: (String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        decidePolicyForNavigationAction.request.URL?.absoluteString?.let(onUrlChange)
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun HostedPaymentWebView(
    url: String,
    modifier: Modifier,
    onUrlChange: (String) -> Unit
) {
    val delegate = remember(onUrlChange) { PaymentNavigationDelegate(onUrlChange) }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration()
            ).apply {
                navigationDelegate = delegate
                loadRequest(NSURLRequest.requestWithURL(NSURL.URLWithString(url)!!))
            }
        },
        update = { webView ->
            val currentUrl = webView.URL?.absoluteString
            if (currentUrl != url) {
                webView.loadRequest(NSURLRequest.requestWithURL(NSURL.URLWithString(url)!!))
            }
        }
    )
}
