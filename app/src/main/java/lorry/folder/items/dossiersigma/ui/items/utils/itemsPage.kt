package lorry.folder.items.dossiersigma.ui.items.utils

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.items.ItemsComponent
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(SigmaActivity, ColumnScope)
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

    val currentFolder by mainViewModel.folderContentComponent.currentFolderFlow.collectAsState(
        null
    )
    val scrollStates =
        remember { mutableMapOf<String, LazyGridState>() }
    val currentScrollState =
        scrollStates.getOrPut(currentFolder?.fullPath ?: "") {
            LazyGridState()
        }

    val selectedItemFullPath = mainViewModel.selectedItemFullPath
    val waitingForItems =
        mainViewModel.folderContentComponent.waitingForItems.collectAsStateWithLifecycle(
            initialValue = false
        )

    PullToRefreshContainer(
        onRefresh = onRefresh
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = 10.dp,
                    bottom = 0.dp
                )
        ) {
            val currentPath =
                mainViewModel.folderContentComponent.currentPath.collectAsStateWithLifecycle(
                    initialValue = null
                )

            val fastPath =
                mainViewModel.folderContentComponent.fastPath.collectAsStateWithLifecycle(
                    initialValue = null
                )

            val folder = mainViewModel.folderContentComponent.currentFolderFlow
                .collectAsStateWithLifecycle(initialValue = null)

            val items = folder.value?.items.orEmpty()
            val ready = folder.value?.fullPath == fastPath.value && items.isNotEmpty()
            val pathMatches = samePath(folder.value?.fullPath, fastPath.value)

            Log.d(
                "sgmact",
                "NormalPage: path actuel(${currentPath.value}), currentFolderFlow.Path(${folder.value?.fullPath}, fastPath(${fastPath.value}), currentFolderFlow.items(${items.size}))"
            )

            LaunchedEffect(pathMatches, items.size) {
                Log.d(
                    "sgmact",
                    "    -> LaunchedEffect: pathMatches($pathMatches), items.size(${items.size})"
                )
                if (pathMatches && waitingForItems.value) {
                    mainViewModel.folderContentComponent.setWaitingForItems(false)
                }
                Log.d("sgmact", "    -> LaunchedEffect: waitingForItems mis à false")

            }

            Log.d(
                "sgmact",
                "    -> et avant le choix: waitingForItems(${waitingForItems.value}), pathMatches($pathMatches)"
            )

            when {
                // 1) Bon path reçu ET items présents -> affiche la grille
                pathMatches && !waitingForItems.value && items.isNotEmpty() -> {
                    Log.d("sgmact", "    -> affichage des items")

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.Companion
                            .padding(start = 25.dp, end = 0.dp),
                        state = currentScrollState
                    ) {
                        lazyGridItems(items, key = {
                            it.fullPath + "-" + it.id
                        }) { item ->
                            ItemComponent(
                                item = item,
                                modifier = Modifier.Companion
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                onItemUpdated = { item ->
//                                                mainViewModel.updateItemInList(item)
                                },
//                                        onDrop = { tag: ColoredTag ->
//                                            mainViewModel.assignColoredTagToItem(item, tag)
//                                      }
                                onHoveredNotHovered = onHoveredNotHovered,
                                selectedItemFullPath = selectedItemFullPath,
                                draggableStartPosition = draggableStartPosition,
                                onItemTapped = onItemTapped,
                                onItemLongPressed = onItemLongPressed,
                                onTopLeftPanelClick = onTopLeftPanelClick,
                                getInfoSup = getInfoSup,
                                getInfoInf = getInfoInf,
                                dragState = dragState
                            )
                        }
                    }
                }

                // 2) Nouveau path ou attente explicite -> placeholder
                waitingForItems.value || !pathMatches -> {
                    Log.d("sgmact", "    -> affichage 'Chargement...'")

                    Text(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.Center),
                        text = "Chargement...",
                    )
                }

                // 3) Bon path reçu ET liste vide -> état "dossier vide"
                else -> {
                    Log.d("sgmact", "    -> affichage 'Dossier vide'")

                    Text(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.Center),
                        text = "Dossier vide",
                    )
                }


            }

            Box(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.CenterStart)
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

fun samePath(a: String?, b: String?): Boolean {
    if (a == null || b == null) return false
    fun norm(s: String) = s.trimEnd('/')
    return norm(a) == norm(b)
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
        modifier = Modifier.Companion,
        state = pullRefreshState
    ) {
        content()
    }
}