package lorry.folder.items.dossiersigma.ui.browser

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget

interface IBrowser {

    var onGotBrowserImage: (String) -> Unit

    @Composable
    fun Render(item: Item, target: BrowserTarget)

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
    )




}
