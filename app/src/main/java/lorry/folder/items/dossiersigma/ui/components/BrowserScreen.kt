package lorry.folder.items.dossiersigma.ui.components

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import lorry.folder.items.dossiersigma.GlobalStateManager
import lorry.folder.items.dossiersigma.SigmaApplication

@Composable
fun BrowserScreen(
    globalStateManager: GlobalStateManager,
    person: String
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
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
                        @JavascriptInterface
                        fun onImageLongClick(imageUrl: String) {
                            manageImageClick(globalStateManager, imageUrl)
                        }
                    },
                    "android"
                )
                loadUrl("${SigmaApplication.INTERNET_SITE_SEARCH}${person}")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun manageImageClick(globalStateManager: GlobalStateManager, imageUrl: String) {
    if (globalStateManager.selectedItem.value != null)
        globalStateManager.setSelectedItem(
            globalStateManager.selectedItem.value!!.copy(picture = imageUrl)
        )
}