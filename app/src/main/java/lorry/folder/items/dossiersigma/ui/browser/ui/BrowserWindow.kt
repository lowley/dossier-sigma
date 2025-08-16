package lorry.folder.items.dossiersigma.ui.browser.ui

import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserState

@Composable
fun BrowserWindow(
    modifier: Modifier,
    browserState: BrowserState,
    onImageClicked: (String) -> Unit,
) {
    val context = LocalContext.current
    val currentPage = browserState.url

    // 1) Une seule WebView, mémorisée
    val webView = remember {
        WebView(context).apply {
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    browserState.setCanGoBack(view.canGoBack())
                    browserState.setCanGoForward(view.canGoForward())
                    evaluateJavascript(
                        """
                        document.addEventListener('contextmenu', function(event) {
                            event.preventDefault();
                            var el = event.target;
                            if (el.tagName === 'IMG') {
                                window.android.onImageLongClick(el.src);
                            }
                        });
                        """.trimIndent(), null
                    )
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
                        browserState.closeBrowser()
                    }
                },
                "android"
            )
        }
    }

    // 2) Détruire proprement
    DisposableEffect(Unit) {
        onDispose { webView.destroy() }
    }

    // 3) AndroidView avec update: on réagit à l’état sans exposer la WebView
    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = {
            val url = currentPage.value
            if (url != null && it.url != url) {
                it.loadUrl(url)
            }

            // Consommer d’éventuelles "commandes" venant du BrowserState
            browserState.consumeCommand { cmd ->
                when (cmd) {
                    is BrowserCommand.GoBack     -> if (it.canGoBack()) it.goBack()
                    is BrowserCommand.GoForward  -> if (it.canGoForward()) it.goForward()
                    is BrowserCommand.Reload     -> it.reload()
                    is BrowserCommand.EvalJs     -> it.evaluateJavascript(cmd.script, null)
                    is BrowserCommand.LoadUrl    -> it.loadUrl(cmd.url)
                }
            }
        }
    )
}

//Toast.makeText(
//context,
//"Naviguez et appuyez longuement sur l'image choisie",
//Toast.LENGTH_LONG
//)
//.show()