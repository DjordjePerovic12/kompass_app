package llc.bokadev.kompass.presentation.shared

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun HostedPaymentWebView(
    url: String,
    modifier: Modifier,
    onUrlChange: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, loadedUrl: String?, favicon: Bitmap?) {
                        loadedUrl?.let(onUrlChange)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.toString()?.let(onUrlChange)
                        return false
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}
