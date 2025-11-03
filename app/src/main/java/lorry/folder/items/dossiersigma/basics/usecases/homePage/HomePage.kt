package lorry.folder.items.dossiersigma.headless.usecases.homePage

import android.graphics.Rect
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import kotlin.collections.indexOf
import kotlin.math.roundToInt

@Composable
context(ColumnScope)
fun HomePage(
    homeItems: List<HomeItem>,
    onItemClicked: (HomeItem) -> Unit,
    onEditTapped: (HomeItem) -> Unit,
    onDeleteTapped: (HomeItem) -> Unit,
    onItemsReordered: (List<HomeItem>) -> Unit,
    modifier: Modifier
) {
    // --- NOUVEAUX ÉTATS POUR LE SCROLL ---
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }
    var gridBounds by remember { mutableStateOf<Rect?>(null) }
    // --- FIN DES NOUVEAUX ÉTATS ---

    var draggedItem by remember { mutableStateOf<HomeItem?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Companion.Zero) }
    var dropTarget by remember { mutableStateOf<HomeItem?>(null) }
    val itemPositions = remember { mutableMapOf<HomeItem, Offset>() }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            modifier = Modifier.Companion
                .fillMaxSize()
                .align(Alignment.Companion.Center)
                .alpha(0.2f),
            painter = painterResource(R.drawable.mesh_homepage),
            alignment = Alignment.Companion.Center,
            contentScale = ContentScale.Companion.Crop,
            contentDescription = "",
            colorFilter = ColorFilter.Companion.tint(SigmaColors.current.tertiary)
        )

        LazyVerticalGrid(
            state = gridState, // On lie l'état à la grille
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .background(Color.Companion.Transparent)
                .onGloballyPositioned { layoutCoordinates ->
                    // On récupère les dimensions et la position de la grille à l'écran
                    val rect = layoutCoordinates.localToRoot(Offset.Companion.Zero).let {
                        Rect(
                            it.x.toInt(),
                            it.y.toInt(),
                            (it.x + layoutCoordinates.size.width).toInt(),
                            (it.y + layoutCoordinates.size.height).toInt()
                        )
                    }
                    gridBounds = rect
                }
        ) {
            items(homeItems.size, key = { homeItems[it].id }) { index ->
                val item = homeItems[index]
                val px150 = 150.dp.convertToPx()

                DraggableItem(
                    item = item,
                    isDragging = item == draggedItem,
                    isDropTarget = item == dropTarget && item != draggedItem,
                    dragOffset = dragOffset,
                    on1DragStart = {
                        draggedItem = item
                        dragOffset = Offset.Companion.Zero
                    },
                    on1Drag = { currentDragOffset ->
                        dragOffset += currentDragOffset

                        // --- LOGIQUE D'AUTO-SCROLL ---
                        gridBounds?.let { bounds ->
                            val itemCenterY = itemPositions[item]!!.y + dragOffset.y
                            val scrollThreshold =
                                bounds.height() * 0.1f // Zone de 10% en haut et en bas

                            // Si on est près du bord inférieur
                            if (itemCenterY > bounds.bottom - scrollThreshold) {
                                if (autoScrollJob?.isActive != true) {
                                    autoScrollJob = coroutineScope.launch {
                                        while (true) {
                                            gridState.scrollBy(15f)
                                            delay(16) // ~60fps
                                        }
                                    }
                                }
                            }
                            // Si on est près du bord supérieur
                            else if (itemCenterY < bounds.top + scrollThreshold) {
                                if (autoScrollJob?.isActive != true) {
                                    autoScrollJob = coroutineScope.launch {
                                        while (true) {
                                            gridState.scrollBy(-15f)
                                            delay(16) // ~60fps
                                        }
                                    }
                                }
                            }
                            // Sinon, on arrête le scroll
                            else {
                                autoScrollJob?.cancel()
                            }
                        }
                        // --- FIN DE LA LOGIQUE D'AUTO-SCROLL ---

                        dropTarget = itemPositions.entries
                            .firstOrNull { (_, position) ->
                                val dragPosition = itemPositions[item]!! + dragOffset
                                (dragPosition - position).getDistanceSquared() < (px150 * px150)
                            }?.key
                    },
                    on1DragEnd = {
                        autoScrollJob?.cancel() // On arrête le scroll à la fin du drag
                        if (draggedItem != null && dropTarget != null) {
                            val fromIndex = homeItems.indexOf(draggedItem)
                            val toIndex = homeItems.indexOf(dropTarget)
                            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                                val newList = homeItems.toMutableList().apply {
                                    this.add(toIndex, this.removeAt(fromIndex))
                                }
                                    .mapIndexed { index, homeItem ->
                                        homeItem.copy(index = index)
                                    }
                                onItemsReordered(newList)
                            }
                        }
                        draggedItem = null
                        dragOffset = Offset.Companion.Zero
                        dropTarget = null
                    },
                    onPositioned = { position ->
                        itemPositions[item] = position
                    }
                ) {
                    HomeItemContent(
                        item = item,
                        onItemClicked = onItemClicked,
                        onEditTapped = onEditTapped,
                        onDeleteTapped = onDeleteTapped
                    )
                }
            }
        }
    }

}

@Composable
fun Dp.convertToPx() =
    with(LocalDensity.current) { this@convertToPx.toPx() }

@Composable
fun DraggableItem(
    item: HomeItem,
    isDragging: Boolean,
    isDropTarget: Boolean,
    dragOffset: Offset,
    on1DragStart: () -> Unit,
    on1Drag: (Offset) -> Unit,
    on1DragEnd: () -> Unit,
    onPositioned: (Offset) -> Unit,
    content: @Composable () -> Unit
) {
    val animatedOffset by animateOffsetAsState(targetValue = if (isDragging) dragOffset else Offset.Companion.Zero)

    Box(
        modifier = Modifier.Companion
            .onGloballyPositioned { layoutCoordinates ->
                onPositioned(layoutCoordinates.localToRoot(Offset.Companion.Zero))
            }
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
            .graphicsLayer {
                scaleX = if (isDropTarget) 1.1f else 1f
                scaleY = if (isDropTarget) 1.1f else 1f
                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
            }
            .pointerInput(Unit) { // Un seul pointerInput suffit !
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        // On appelle on1DragStart ici, au vrai début du glissement.
                        on1DragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        on1Drag(dragAmount) // On passe directement le déplacement.
                    },
                    onDragEnd = { on1DragEnd() },
                    onDragCancel = { on1DragEnd() }
                )
            }
    ) {
        content()
    }
}

@Composable
fun HomeItemContent(
    item: HomeItem,
    onItemClicked: (HomeItem) -> Unit,
    onEditTapped: (HomeItem) -> Unit,
    onDeleteTapped: (HomeItem) -> Unit
) {
    val _60Color = SigmaColors.current.secondary
    val _30Color = SigmaColors.current.onSecondary
    val _10Color = SigmaColors.current.tertiary

    Card(
        modifier = Modifier.Companion
            .padding(
                start = 10.dp,
                end = 10.dp,
                bottom = 20.dp
            )
            .size(150.dp)
            .clip(RoundedCornerShape(13.dp)),
        colors = CardDefaults.cardColors(
            containerColor = lerp(_60Color, SigmaColors.current.primary, 0.6f),
            contentColor = _30Color,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        border = BorderStroke(2.dp, lerp(_60Color, SigmaColors.current.primary, 0.4f)),
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
        ) {
            AsyncImage(
                modifier = Modifier.Companion
                    .size(120.dp)
                    .align(Alignment.Companion.TopCenter)
                    .padding(top = 27.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onItemClicked(item) })
                    },
                model = item.picture ?: if (item.icon != 0) item.icon else R.drawable.dossier,
                contentDescription = "Miniature",
                contentScale = ContentScale.Companion.Fit,
            )

            Text(
                text = item.title,
                color = _30Color,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.BottomCenter)
                    .padding(bottom = 5.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onItemClicked(item) })
                    }
            )

            Icon(
                modifier = Modifier.Companion
                    .size(25.dp)
                    .padding(start = 10.dp, top = 10.dp)
                    .align(Alignment.Companion.TopStart)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onEditTapped(item) })
                    },
                painter = painterResource(R.drawable.stylo),
                tint = Color.Companion.Gray,
                contentDescription = null
            )

            Icon(
                modifier = Modifier.Companion
                    .size(25.dp)
                    .padding(end = 10.dp, top = 10.dp)
                    .align(Alignment.Companion.TopEnd)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDeleteTapped(item) })
                    },
                painter = painterResource(R.drawable.corbeille),
                tint = Color.Companion.Gray,
                contentDescription = null
            )
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