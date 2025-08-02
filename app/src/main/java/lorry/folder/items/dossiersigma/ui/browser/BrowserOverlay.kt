package lorry.folder.items.dossiersigma.ui.browser

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.StateFlow
import kotlin.apply
import kotlin.text.trimIndent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserOverlay(
    currentPage: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onImageClicked: (String) -> Unit,
    setCurrentPage: (String?) -> Unit,
    webView: StateFlow<WebView?>,
    canGoBack: StateFlow<Boolean>,
    canGoForward: StateFlow<Boolean>,
    setCanGoBack: (Boolean) -> Unit,
    setCanGoForward: (Boolean) -> Unit,
    setWebView: (WebView) -> Unit
) {
    val context = LocalContext.current

    if (currentPage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                key(currentPage) {
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = {
                            WebView(it).apply {
                                webChromeClient = WebChromeClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String) {
                                        super.onPageFinished(view, url)
                                        setCanGoBack(view.canGoBack())
                                        setCanGoForward(view.canGoForward())
                                        val js = """
                            document.addEventListener('contextmenu', function(event) {
                                event.preventDefault();
                                var element = event.target;
                                if (element.tagName === 'IMG') {
                                    window.android.onImageLongClick(element.src);
                                }
                            });
                        """.trimIndent()
                                        evaluateJavascript(js, null)
                                    }

                                }
                                addJavascriptInterface(
                                    object {
                                        var hasClicked = false

                                        @JavascriptInterface
                                        fun onImageLongClick(imageUrl: String) {
                                            if (hasClicked) return
                                            hasClicked = true
                                            onImageClicked(imageUrl)
                                            onClose()
                                        }
                                    },
                                    "android"
                                )

                                loadUrl(currentPage)
                                setWebView(this)
                            }

                        }
                    )
                }


            }
        }

    }
}