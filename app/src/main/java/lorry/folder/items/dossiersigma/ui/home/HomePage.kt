package lorry.folder.items.dossiersigma.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.domain.usecases.homePage.HomeItem
import lorry.folder.items.dossiersigma.ui.components.HomeItemInfos


@Composable
context(ColumnScope)
fun homePage(
    homeItemsInVM: StateFlow<List<HomeItem>>,
    onItemClicked: (HomeItem) -> Unit,
    onEditTapped: (HomeItem) -> Unit,
    onDeleteTapped: (HomeItem) -> Unit

    ) {
    val homeItems by homeItemsInVM.collectAsState(
        emptyList()
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .weight(1f)
    ) {
        val _60Color = Color(0xFF243e36)
        val _30Color = Color(0xFF7ca982)
        val _10Color = Color(0xFF8fc0a9)

        lazyGridItems<HomeItem>(
            homeItems,
            key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 20.dp
                    )
                    .size(150.dp)
                    .clip(RoundedCornerShape(13.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = _60Color,
                    contentColor = _30Color,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                ),
                border = BorderStroke(2.dp, _10Color),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 27.dp)
                            .clickable {
                                onItemClicked(item)
                            },
                        model = item.picture
                            ?: if (item.icon != 0) item.icon else R.drawable.dossier,
                        contentDescription = "Miniature",
                        contentScale = ContentScale.Fit,
                    )

                    Text(
                        text = item.title,
                        color = _30Color,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 5.dp)
                            .clickable {
                                onItemClicked(item)
                            }
                    )

                    //icône de modification
                    Icon(
                        modifier = Modifier
                            .size(25.dp)
                            .padding(
                                start = 10.dp,
                                top = 10.dp
                            )
                            .align(Alignment.TopStart)
                            .pointerInput(true) {
                                detectTapGestures(
                                    onTap = {
                                        onEditTapped(item)
                                    }
                                )
                            },
                        painter = painterResource(R.drawable.stylo),
                        tint = Color.Gray,
                        contentDescription = null
                    )

                    //icône de suppression
                    Icon(
                        modifier = Modifier
                            .size(25.dp)
                            .padding(
                                end = 10.dp,
                                top = 10.dp
                            )
                            .align(Alignment.TopEnd)
                            .pointerInput(true) {
                                detectTapGestures(
                                    onTap = {
                                        onDeleteTapped(item)
                                    }
                                )
                            },
                        painter = painterResource(R.drawable.corbeille),
                        tint = Color.Gray,
                        contentDescription = null
                    )
                }
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
