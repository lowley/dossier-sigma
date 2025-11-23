package lorry.folder.items.dossiersigma.ui.folderContent.items.sphere

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.IOverlayContent
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.Layer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.external.capsule.CapsuleComponent
import lorry.folder.items.dossiersigma.external.capsule.utilities.CountryClass
import lorry.folder.items.dossiersigma.external.capsule.utilities.Scale
import lorry.folder.items.dossiersigma.ui.folderContent.items.sphere.overlays.Equators

@Composable
fun SphericOverlayedBox(
    modifier: Modifier = Modifier,
    backgroundContent: IOverlayContent,
    topOverlay: IOverlayContent,
    equatorOverlays: List<IOverlayContent>,
    bottomOverlay: IOverlayContent,
    isHovered: Boolean = false,
    length: Dp,
    bounds: MutableState<Rect?>,
    item: Item
) {
    require(equatorOverlays.isNotEmpty()) { "equatorOverlays ne doit pas être vide" }

    var layer by remember { mutableStateOf(Layer.EQUATOR) }
    var equatorIndex by remember { mutableIntStateOf(0) }

    var widthPx by remember { mutableStateOf(0f) }
    var heightPx by remember { mutableStateOf(0f) }

    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }

    fun Int.wrap(size: Int): Int =
        ((this % size) + size) % size

    Box(
        modifier = modifier
            .size(length)
            .onSizeChanged {
                widthPx = it.width.toFloat()
                heightPx = it.height.toFloat()
            }
            .onGloballyPositioned {
                val pos = it.positionInRoot()
                bounds.value = Rect(
                    offset = pos,
                    size = Size(
                        it.size.width.toFloat(),
                        it.size.height.toFloat()
                    )
                )
            }
            .then(
                if (isHovered) Modifier.Companion.border(2.dp, Color.Companion.Black)
                else Modifier.Companion
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        //pas pris en compte par au dessus
                        val dx = dragAmount
                        change.consume()
                        dragX += dx
                    },
                    onDragEnd = {
                        val absX = kotlin.math.abs(dragX)

                        val horizontalThreshold = widthPx / 3f

                        when {
                            layer == Layer.EQUATOR &&
                                    absX > horizontalThreshold -> {

                                val direction = if (dragX < 0f) +1 else -1
                                equatorIndex = (equatorIndex + direction)
                                    .wrap(equatorOverlays.size)
                            }
                        }

                        dragX = 0f
                        dragY = 0f
                    }
                )
            }
    ) {
        // 1) Fond (ce qu'il y avait avant, image, etc.)
        backgroundContent.display(Modifier, item.name, item.country, item.fullPath)

        // 2) Animation de fade entre les overlays
        AnimatedContent(
            modifier = Modifier
                .matchParentSize(),
            targetState = layer to equatorIndex,
            transitionSpec = {
                // L'ancien disparaît sur 150 ms
                // Le nouveau commence à apparaître APRÈS ces 150 ms
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 150,
                        delayMillis = 150      // commence après le fadeOut
                    )
                ) togetherWith
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 150
                            )
                        )
            },
            label = "SphereOverlayFade"
        ) { (animatedLayer, animatedIndex) ->

            val animatedOverlay: IOverlayContent = when (animatedLayer) {
                Layer.TOP -> topOverlay
                Layer.BOTTOM -> bottomOverlay
                Layer.EQUATOR -> equatorOverlays[animatedIndex]
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
            ) {
                animatedOverlay.display(
                    Modifier
                        .align(Alignment.Center),
                    item.name,
                    animatedOverlay.country,
                    item.fullPath
                )
            }
        }
    }
}