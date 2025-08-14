package lorry.folder.items.dossiersigma.ui.browser.ui

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWindow(
    currentPageFlow: StateFlow<String?>,
    onClose: () -> Unit,
    modifier: Modifier,
    onImageClicked: (String) -> Unit,
    setCanGoBack: (Boolean) -> Unit,
    setCanGoForward: (Boolean) -> Unit,
    setWebView: (WebView) -> Unit,
) {
    val context = LocalContext.current
    val currentPage = currentPageFlow.collectAsState()

    if (currentPage != null) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(Color.Companion.Black.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Companion.White)
            ) {
                key(currentPage) {
                    AndroidView(
                        modifier = Modifier.Companion.weight(1f),
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

                                loadUrl(currentPage.value ?: "")
                                setWebView(this)
                            }
                        }
                    )
                }
            }

            Toast.makeText(
                context,
                "Naviguez et appuyez longuement sur l'image choisie",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }
}