package lorry.folder.items.dossiersigma.ui.normal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.materii.pullrefresh.PullRefreshLayout
import dev.materii.pullrefresh.rememberPullRefreshState
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(SigmaActivity, ColumnScope)
fun NormalPage(
    onHoveredNotHovered: (Item?) -> Unit,
    onItemTapped: (Item) -> Unit,
    onItemLongPressed: (Item) -> Unit,
    onTopLeftPanelClick: (Item) -> Unit,
    getInfoSup: suspend (Item) -> String,
    getInfoInf: suspend (Item) -> String,
    onRefresh: () -> Unit,
    indexBar: IIndexBar,
    currentScrollState: LazyGridState,
    currentFolder: SigmaFolder,

    ) {
    val currentFolderFlow = mainViewModel.currentFolder
    val imageCache = mainViewModel.imageCache
    val flagCache = mainViewModel.flagCache
    val scaleCache = mainViewModel.scaleCache
    val memoCache = mainViewModel.memoCache

    val selectedItemFullPath = mainViewModel.selectedItemFullPath
    val draggableStartPosition = mainViewModel.draggableStartPosition

    PullToRefreshContainer(
        onRefresh = onRefresh
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = 0.dp,
                    bottom = 0.dp)
        ) {

            DisposableEffect(
                currentFolder, currentFolder.items,
                currentScrollState, indexBar,
                onItemTapped, onItemLongPressed, onTopLeftPanelClick,
                getInfoSup, getInfoInf
            ) {
                android.util.Log.d("Deps", buildString {
                    appendLine("folder#=${System.identityHashCode(currentFolder)} items#=${System.identityHashCode(currentFolder.items)} size=${currentFolder.items.size}")
                    appendLine("scroll#=${System.identityHashCode(currentScrollState)} indexBar#=${System.identityHashCode(indexBar)}")
                    appendLine("tap#=${System.identityHashCode(onItemTapped)} long#=${System.identityHashCode(onItemLongPressed)} topLeft#=${System.identityHashCode(onTopLeftPanelClick)}")
                    appendLine("infoSup#=${System.identityHashCode(getInfoSup)} infoInf#=${System.identityHashCode(getInfoInf)}")
                })
                onDispose { }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier
                    .padding(start = 25.dp, end = 0.dp),
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

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .fillMaxHeight().width(20.dp)
            ){

                indexBar.display(currentScrollState = currentScrollState)
            }
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

