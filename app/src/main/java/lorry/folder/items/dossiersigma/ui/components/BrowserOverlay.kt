package lorry.folder.items.dossiersigma.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
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