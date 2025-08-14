package lorry.folder.items.dossiersigma.ui.browser

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.browser.ui.BrowserWindow
import lorry.folder.items.dossiersigma.ui.browser.utilities.BrowserTarget
import lorry.folder.items.dossiersigma.ui.browser.utilities.elaborateSearchString
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import javax.inject.Inject

class Browser @Inject constructor(
    val context: Context,
) : IBrowser {

    override var onGotBrowserImage: (String) -> Unit = {}

    private val _currentPage = MutableStateFlow<String?>(null)
    val currentPage: StateFlow<String?> = _currentPage

    fun setCurrentPage(page: String?) {
        _currentPage.value = page
    }

    fun openBrowserWith(html: String) {
        _currentPage.value = html
    }

    fun closeBrowser() {
        _currentPage.value = null
    }

    ////////////
    // zoneUI //
    ////////////
    @Composable
    override fun Render(
        item: Item,
        target: BrowserTarget,
        onClose: () -> Unit,
        onImageClicked: (String) -> Unit,
        setCurrentPage: (String?) -> Unit,
        webView: StateFlow<WebView?>,
        canGoBack: StateFlow<Boolean>,
        canGoForward: StateFlow<Boolean>,
        setCanGoBack: (Boolean) -> Unit,
        setCanGoForward: (Boolean) -> Unit,
        setWebView: (WebView) -> Unit){

        val final = elaborateSearchString(item, target)
        setCurrentPage(target.url + final)

        BrowserWindow(
            currentPageFlow = currentPage,
            onClose = onClose,
            onImageClicked = onGotBrowserImage,
            webView = webView,
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            setCanGoBack = setCanGoBack,
            setCanGoForward = setCanGoForward,
            setWebView = setWebView
        )

    }
}

fun manageImageClick(viewModel: SigmaViewModel, imageUrl: String) {
    if (viewModel.selectedItem.value != null)
        viewModel.viewModelScope.launch {
            viewModel.updatePicture(imageUrl)
        }
}