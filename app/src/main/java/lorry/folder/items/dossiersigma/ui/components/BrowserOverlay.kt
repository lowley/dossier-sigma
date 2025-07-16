package lorry.folder.items.dossiersigma.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
    viewmodel: SigmaViewModel
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    if (currentPage != null) {
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
                                    canGoBack = view.canGoBack()
                                    canGoForward = view.canGoForward()
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
                            webView = this
                        }

                    }
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(Color.Transparent)
                    .padding(8.dp)
                    .drawBehind {
                        // Dessine une ligne horizontale tout en haut
                        val strokeWidthPx = 2.dp.toPx()
                        drawLine(
                            color = Color.Blue,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidthPx
                        )
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { webView?.goBack() }, enabled = canGoBack,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.la_gauche),
                        contentDescription = "back",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                Button(
                    onClick = { viewmodel.browserManager.setCurrentPage("https://www.google.fr") },
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.maison),
                        contentDescription = "home",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier.padding(horizontal = 5.dp)
                ) {
                    Text("Retourner à l'application")
                }

                Button(
                    onClick = { webView?.goForward() }, enabled = canGoForward,
                    modifier = Modifier.padding(horizontal = 5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.la_droite),
                        contentDescription = "forward",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }
        }
    }
}