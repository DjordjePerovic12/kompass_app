package llc.bokadev.kompass.presentation.shared

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
actual fun InlineHtmlMapView(
    html: String,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // Required for the viewport meta tag to be respected so that
                // window.innerWidth/Height return real CSS pixel dimensions.
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                // file:///android_asset/ origin lets the WebView load external
                // HTTPS images (OSM tiles) without triggering mixed-content or
                // cross-origin restrictions that affect data: and about:blank origins.
                tag = html
                loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)

                // Prevent the parent Compose verticalScroll from stealing touch
                // events so the map can handle pan and pinch-to-zoom itself.
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE,
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false // let the WebView process the event normally
                }
            }
        },
        update = { webView ->
            // Only reload if HTML actually changed — prevents resetting the map's
            // zoom/pan state on every audio-progress recomposition.
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
            }
        }
    )
}
