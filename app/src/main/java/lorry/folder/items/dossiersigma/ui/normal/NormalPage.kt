package lorry.folder.items.dossiersigma.ui.normal

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.unit.dp
import dev.materii.pullrefresh.PullRefreshLayout
import dev.materii.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.domain.ColoredTag
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.components.BrowserOverlay
import lorry.folder.items.dossiersigma.ui.components.ItemComponent
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.lazyGridItems

@Composable
context(SigmaActivity, ColumnScope)
fun NormalPage(
    onHoveredNotHovered: (Item?) -> Unit,
    onItemTapped: ((Item) -> Unit),
    onItemLongPressed: ((Item) -> Unit),
    onTopLeftPanelClick: (Item) -> Unit,
    getInfoSup: suspend (Item) -> String,
    getInfoInf: suspend (Item) -> String,
    onRefresh: () -> Unit,

) {
    val currentFolderFlow = mainViewModel.currentFolder
    val imageCache = mainViewModel.imageCache
    val flagCache = mainViewModel.flagCache
    val scaleCache = mainViewModel.scaleCache
    val memoCache = mainViewModel.memoCache

    val selectedItemFullPath = mainViewModel.selectedItemFullPath
    val draggableStartPosition = mainViewModel.draggableStartPosition

    val currentFolder by currentFolderFlow.collectAsState()

    val scrollStates =
        remember { mutableMapOf<String, LazyGridState>() }
    val currentScrollState =
        scrollStates.getOrPut(currentFolder.fullPath) {
            LazyGridState()
        }

    PullToRefreshContainer(
        onRefresh = onRefresh
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                state = currentScrollState
            ) {
                lazyGridItems(currentFolder.items, key = {
                    it.fullPath + "-" + it.id
                }) { item ->
                    ItemComponent(
                        item = item,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        onItemUpdated = { item ->
//                                                mainViewModel.updateItemInList(item)
                        },
//                                        onDrop = { tag: ColoredTag ->
//                                            mainViewModel.assignColoredTagToItem(item, tag)
//                                      }
                        imageCache = imageCache,
                        flagCache = flagCache,
                        scaleCache = scaleCache,
                        memoCache = memoCache,
                        onHoveredNotHovered = onHoveredNotHovered,
                        selectedItemFullPath = selectedItemFullPath,
                        draggableStartPosition = draggableStartPosition,
                        onItemTapped = onItemTapped,
                        onItemLongPressed = onItemLongPressed,
                        onTopLeftPanelClick = onTopLeftPanelClick,
                        getInfoSup = getInfoSup,
                        getInfoInf = getInfoInf
                    )
                }
            }

//            val url by mainViewModel.browserManager.currentPage.collectAsState()
//
//            if (url != null)
//                BrowserOverlay(
//                    currentPage = url,
//                    onClose = closeBrowser,
//                    onImageClicked = onGotBrowserImage,
//                    setCurrentPage = setCurrentBrowserPage,
//                    webView = webView,
//                    canGoBack = canGoBack,
//                    canGoForward = canGoForward,
//                    setCanGoBack = setCanGoBack,
//                    setCanGoForward = setCanGoForward,
//                    setWebView = setWebView
//                )
        }

    }
}

fun <T> LazyGridScope.lazyGridItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    itemsIndexed(items, key = { index, item -> key?.invoke(item) ?: index }) { _, item ->
        itemContent(item)
    }
}

@Composable
fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    content: @Composable () -> Unit,
) {
    var isRefreshing by remember {
        mutableStateOf(false)
    }
    var pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    PullRefreshLayout(
        modifier = Modifier,
        state = pullRefreshState
    ) {
        content()
    }
}

