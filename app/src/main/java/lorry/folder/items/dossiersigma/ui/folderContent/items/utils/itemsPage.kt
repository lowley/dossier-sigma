package lorry.folder.items.dossiersigma.ui.folderContent.items.utils

import android.util.Log
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.materii.pullrefresh.PullRefreshLayout
import dev.materii.pullrefresh.rememberPullRefreshState
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContent.items.ItemsComponent
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(activity: SigmaActivity, column: ColumnScope)
fun ItemsComponent.ItemsPage(
    onHoveredNotHovered: (Item?) -> Unit,
    onItemTapped: (Item) -> Unit,
    onItemLongPressed: (Item) -> Unit,
    onTopLeftPanelClick: (Item) -> Unit,
    getInfoSup: suspend (Item) -> String,
    getInfoInf: suspend (Item) -> String,
    onRefresh: () -> Unit,
    indexBar: IIndexBar,
) {
    val currentFolder by activity.mainViewModel.folderContentComponent.currentFolderFlow.collectAsStateWithLifecycle()
    val scrollStates = remember { mutableMapOf<SigmaPath, LazyGridState>() }
    val currentScrollState = scrollStates.getOrPut(SigmaPath(currentFolder?.fullPath?.str ?: "")) {
        LazyGridState()
    }

    val fastPath by activity.mainViewModel.folderContentComponent.fastPath.collectAsStateWithLifecycle()
    val selectedItemFullPath = activity.mainViewModel.selectedItemFullPath
    val TAG = "dsplitms"

    PullToRefreshContainer(
        onRefresh = onRefresh
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            val items = currentFolder?.items.orEmpty()
            val pathMatches = samePath(currentFolder?.fullPath, fastPath)

            Log.d(TAG, "ItemsPage: pathMatches=$pathMatches, items=${items.size}")

            when {
                pathMatches && items.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.padding(start = 25.dp),
                        state = currentScrollState
                    ) {
                        lazyGridItems(items, key = { it.fullPath.str + "-" + it.id }) { item ->
                            ItemComponent(
                                item = item,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                onHoveredNotHovered = onHoveredNotHovered,
                                selectedItemFullPath = selectedItemFullPath,
                                draggableStartPosition = draggableStartPosition,
                                onItemTapped = onItemTapped,
                                onItemLongPressed = onItemLongPressed,
                                onTopLeftPanelClick = onTopLeftPanelClick,
                                getInfoSup = getInfoSup,
                                getInfoInf = getInfoInf,
                                dragState = dragState,
                                onItemUpdated = { }
                            )
                        }
                    }
                }

                !pathMatches -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Chargement...",
                    )
                }

                items.isEmpty() -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Dossier vide",
                    )
                }

                else -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Etat indéterminé",
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .fillMaxHeight()
                    .width(20.dp)
            ) {
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

fun samePath(a: SigmaPath?, b: SigmaPath?): Boolean {
    if (a == null || b == null) return false
    fun norm(s: String) = s.trimEnd('/')
    return norm(a.str) == norm(b.str)
}

@Composable
fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    content: @Composable () -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            // In a real app, onRefresh would be an async operation.
            // Since onRefresh is a Unit function, we'll reset isRefreshing immediately
            // or after a small delay to show the indicator.
            isRefreshing = false 
        }
    )

    PullRefreshLayout(
        state = pullRefreshState
    ) {
        content()
    }
}
