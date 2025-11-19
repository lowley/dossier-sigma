package lorry.folder.items.dossiersigma.ui.folderContent.items.overlays

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

enum class Layer { TOP, EQUATOR, BOTTOM }
typealias OverlayContent = @Composable BoxScope.() -> Unit

@Composable
fun SphericOverlayedBox(
    modifier: Modifier = Modifier,
    backgroundContent: @Composable BoxScope.() -> Unit,
    topOverlay: OverlayContent,
    equatorOverlays: List<OverlayContent>,
    bottomOverlay: OverlayContent,
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
            .onSizeChanged {
                widthPx = it.width.toFloat()
                heightPx = it.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val (dx, dy) = dragAmount
                        dragX += dx
                        dragY += dy
                    },
                    onDragEnd = {
                        val absX = kotlin.math.abs(dragX)
                        val absY = kotlin.math.abs(dragY)

                        val horizontalThreshold = widthPx / 3f
                        val verticalThreshold = heightPx / 3f

                        when {
                            // GESTE HORIZONTAL : seulement sur l'équateur
                            layer == Layer.EQUATOR &&
                                    absX > absY &&
                                    absX > horizontalThreshold -> {

                                val direction = if (dragX < 0f) +1 else -1
                                equatorIndex = (equatorIndex + direction)
                                    .wrap(equatorOverlays.size)
                            }

                            // GESTE VERTICAL : changement de calotte / anneau
                            absY > absX && absY > verticalThreshold -> {
                                val goingDown = dragY > 0f
                                when (layer) {
                                    Layer.EQUATOR ->
                                        layer = if (goingDown) Layer.BOTTOM else Layer.TOP

                                    Layer.TOP ->
                                        if (goingDown) layer = Layer.EQUATOR

                                    Layer.BOTTOM ->
                                        if (!goingDown) layer = Layer.EQUATOR
                                }
                            }
                        }

                        dragX = 0f
                        dragY = 0f
                    }
                )
            }
    ) {
        // 1) Fond (ce qu'il y avait avant, image, etc.)
        backgroundContent()

        // 2) Overlay selon le "pôle"
        val overlay: OverlayContent = when (layer) {
            Layer.TOP -> topOverlay
            Layer.EQUATOR -> equatorOverlays[equatorIndex]
            Layer.BOTTOM -> bottomOverlay
        }

        // Petit effet de suivi du doigt (optionnel)
        val translationX = if (layer == Layer.EQUATOR) dragX else 0f
        val translationY = dragY

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.translationX = translationX
                    this.translationY = translationY
                }
        ) {
            overlay()
        }
    }
}