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
import lorry.folder.items.dossiersigma.basics.domain.lastSegment
import lorry.folder.items.dossiersigma.basics.domain.str
import lorry.folder.items.dossiersigma.ui.folderContent.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContent.items.ItemsComponent
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
    val currentFolder by mainViewModel.folderContentComponent.currentFolderFlow.collectAsStateWithLifecycle()
    val scrollStates =
        remember { mutableMapOf<SigmaPath, LazyGridState>() }
    val currentScrollState =
        scrollStates.getOrPut(SigmaPath(currentFolder?.fullPath?.str ?: "")) {
            LazyGridState()
        }

    val currentPath =
        mainViewModel.folderContentComponent.currentPath.collectAsStateWithLifecycle(
            initialValue = null
        )

    val fastPathFlow = mainViewModel.folderContentComponent.fastPath
    val fastPath = fastPathFlow.collectAsStateWithLifecycle()

    val selectedItemFullPath = mainViewModel.selectedItemFullPath
    val TAG = "dsplitms"

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
            val items = currentFolder?.items.orEmpty()
            val ready = currentFolder?.fullPath == fastPath.value && items.isNotEmpty()
            val pathMatches = samePath(currentFolder?.fullPath, fastPath.value)

            Log.d(TAG, "################")
            Log.d(TAG, "## ITEMS PAGE ##")
            Log.d(TAG, "################")
            Log.d(TAG, "Bonjour le \u200Bmonde\u200B")
            Log.d(TAG, "Bonjour \u001B[31mmonde\u001B[0m en couleur !");
            Log.d("SigmaTest", "Bonjour avec un tag \u200Binvisible\u200B")
            Log.d(TAG, "éléments de décision: ① FASTPATH: ${fastPath.value?.lastSegment}, ② CURRENTFOLDER: ${currentFolder?.fullPath?.lastSegment}")
            Log.d(TAG, "d'où: ② pathMatches=$pathMatches, ② items (dans currentFolder)=${items.size}")

            when {
                pathMatches && items.isNotEmpty() -> {
                    Log.d(TAG, "      -> pathMatches && items.isNotEmpty() => AFFICHAGE DES ITEMS")

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.Companion
                            .padding(start = 25.dp, end = 0.dp),
                        state = currentScrollState
                    ) {
                        lazyGridItems(items, key = {
                            it.fullPath.str + "-" + it.id
                        }) { item ->
                            ItemComponent(
                                item = item,
                                modifier = Modifier.Companion
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                onHoveredNotHovered = onHoveredNotHovered,
                                selectedItemFullPath = selectedItemFullPath,
                                onItemTapped = onItemTapped,
                                onItemLongPressed = onItemLongPressed,
                                onTopLeftPanelClick = onTopLeftPanelClick,
                                getInfoSup = getInfoSup,
                                getInfoInf = getInfoInf,
                                dragState = dragState,
                            )
                        }
                    }
                }

                !pathMatches -> {
                    Log.d(TAG, "      -> !pathMatches => 'CHARGEMENT...'")

                    Text(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.Center),
                        text = "Chargement...",
                    )
                }

                items.isEmpty() -> {
                    Log.d(TAG, "      -> items.isEmpty() => 'DOSSIER VIDE'")

                    Text(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.Center),
                        text = "Dossier vide",
                    )
                }

                else -> {
                    Log.d(TAG, "      -> (pathMatches + items ∅)/(!pathMatches)/(items ≠ ∅) => 'ETAT INDETERMINE'")

                    Text(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.Center),
                        text = "Etat indéterminé",
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