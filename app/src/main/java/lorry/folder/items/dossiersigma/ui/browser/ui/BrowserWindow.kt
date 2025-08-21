package lorry.folder.items.dossiersigma.ui.browser.ui

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserCommand
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserState

@Composable
fun BrowserWindow(
    modifier: Modifier = Modifier,
    browserState: BrowserState,
    onImageClicked: (String) -> Unit,
    setCanGoBack: (Boolean) -> Unit,
    setCanGoForward: (Boolean) -> Unit,
    closeBrowser: () -> Unit,

    // 🔸 Nouveaux callbacks : fournis par l'hôte (Service overlay / Activity)
    // En Activity "classique", tu peux laisser ceux par défaut (vides)
    enterOverlayEditMode: () -> Unit = {},
    leaveOverlayEditMode: () -> Unit = {},
) {
    val context = LocalContext.current
    val currentPage = browserState.url

    // 1) Une seule WebView, mémorisée
    val webView = remember {
        WebView(context).apply {
            // Focus clavier
            isFocusable = true
            isFocusableInTouchMode = true

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    setCanGoBack(view.canGoBack())
                    setCanGoForward(view.canGoForward())

                    // JS pour détecter long-press image (inchangé)
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

            // Touch = prendre le focus + basculer overlay focalisable + ouvrir l'IME
            setOnTouchListener { v, ev ->
                if (ev.action == android.view.MotionEvent.ACTION_DOWN) {
                    // 1) rendre la fenêtre focalisable (overlay)
                    enterOverlayEditMode()
                    // 2) focus vue
                    if (!v.hasFocus()) {
                        v.requestFocus()
                        requestFocusFromTouch()
                    }
                    // 3) ouvrir IME
                    val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                            as android.view.inputmethod.InputMethodManager
                    post { imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT) }
                }
                false // ne pas consommer -> le WebView gère normalement le reste
            }

            addJavascriptInterface(
                object {
                    @android.webkit.JavascriptInterface
                    fun onImageLongClick(imageUrl: String) {
                        // NB : on laisse le WebView focalisable uniquement quand il est affiché.
                        onImageClicked(imageUrl)
                        closeBrowser()
                    }
                },
                "android"
            )
        }
    }

    // 2) Détruire proprement + repasser overlay en non-focalisable
    DisposableEffect(Unit) {
        onDispose {
            // fermer le clavier si encore ouvert
            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(webView.windowToken, 0)
            // overlay hors mode édition
            leaveOverlayEditMode()
            webView.destroy()
        }
    }

    // 3) AndroidView + maj d’URL
    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = {
            val url = currentPage
            if (url != null && it.url != url) {
                it.loadUrl(url)
            }
        }
    )

    // 4) Consommer les commandes (inchangé, robustifié)
    LaunchedEffect(webView) {
        browserState.commands.collect { cmd ->
            when (cmd) {
                is BrowserCommand.goBack -> if (webView.canGoBack()) webView.goBack()
                is BrowserCommand.goForward -> if (webView.canGoForward()) webView.goForward()
                // is BrowserCommand.Reload -> webView.reload()
                // is BrowserCommand.EvalJs -> webView.evaluateJavascript(cmd.script, null)
                // is BrowserCommand.LoadUrl -> webView.loadUrl(cmd.url)
            }
        }
    }
}

//Toast.makeText(
//context,
//"Naviguez et appuyez longuement sur l'image choisie",
//Toast.LENGTH_LONG
//)
//.show()