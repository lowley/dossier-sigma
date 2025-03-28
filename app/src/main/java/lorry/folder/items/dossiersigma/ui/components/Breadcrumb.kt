package lorry.folder.items.dossiersigma.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb(
    items: List<String>,
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    arrowColor: Color = Color.Gray,
    transitionDuration: Int = 600
) {
    val displayedItems = remember { mutableStateListOf<String>() }
    val previousItems = remember { mutableStateListOf<String>() }

    LaunchedEffect(items) {
        // Ajouter immédiatement les nouveaux items
        items.forEach { item ->
            if (item !in displayedItems) displayedItems.add(item)
        }

        // Identifier les items supprimés
        val removedItems = displayedItems.filter { it !in items }

        // Attendre la fin des animations avant de supprimer réellement
        delay(transitionDuration.toLong())
        displayedItems.removeAll(removedItems)

        // Après les animations, mettre à jour previousItems
        previousItems.clear()
        previousItems.addAll(items)
    }

    Row(modifier = modifier) {
        displayedItems.forEachIndexed { index, item ->
            key(item) {
                BreadcrumbItem(
                    text = item,
                    isActive = index == items.lastIndex,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    arrowColor = arrowColor,
                    onClick = {
                        val path = items.take(index + 1).joinToString("/")
                        onPathClick(path)
                    },
                    // Ici, la correction claire :
                    animationState = when {
                        item in items && item !in previousItems -> AnimationState.Appearing
                        item !in items && item in previousItems -> AnimationState.Disappearing
                        else -> AnimationState.Stable
                    },
                    duration = transitionDuration
                )
            }
        }
    }
}

enum class AnimationState { Appearing, Disappearing, Stable }

@Composable
fun BreadcrumbItem(
    text: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    arrowColor: Color,
    onClick: () -> Unit,
    animationState: AnimationState,
    duration: Int
) {
    val startAnimation = animationState != AnimationState.Disappearing

    var animationTriggered by remember { mutableStateOf(animationState == AnimationState.Stable) }

    LaunchedEffect(animationState) {
        if (animationState == AnimationState.Appearing) {
            animationTriggered = false
            delay(50)
            animationTriggered = true
        } else if (animationState == AnimationState.Disappearing) {
            animationTriggered = false
        } else {
            animationTriggered = true
        }
    }

    val transition = updateTransition(animationTriggered, label = "itemClipTransition")

    val clipFraction by transition.animateFloat(
        transitionSpec = { tween(durationMillis = duration, easing = FastOutSlowInEasing) }
    ) { visible ->
        if (visible) 1f else 0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clipToBounds()
            .drawWithContent {
                val width = size.width
                when (animationState) {
                    AnimationState.Appearing -> {
                        clipRect(right = width * clipFraction) {
                            this@drawWithContent.drawContent()
                        }
                    }

                    AnimationState.Disappearing -> {
                        clipRect(right = width * clipFraction) {
                            this@drawWithContent.drawContent()
                        }
                    }

                    AnimationState.Stable -> drawContent()
                }
            }
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = ParallelogramShape(),
            color = Color(0xFFF3F4E3),
            modifier = Modifier
                .border(2.dp, Color(0xFF8697CB), ParallelogramShape())
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 15.dp),
                fontSize = 16.sp,
                //fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) activeColor else inactiveColor,
                fontFamily = FontFamily.Monospace, // ou custom comme JetBrainsMono
                fontWeight = FontWeight.Medium,
                //fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

class ParallelogramShape(
    private val skew: Dp = 10.dp,
    private val cornerRadius: Dp = 4.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val skewPx = with(density) { skew.toPx() }
        val radiusPx = with(density) { cornerRadius.toPx().coerceAtMost(size.minDimension / 2) }

        val path = Path().apply {
            // Point 1 (haut gauche, après le biseau)
            moveTo(skewPx + radiusPx, 0f)

            // Ligne jusqu’au coin haut droit avec arrondi
            lineTo(size.width - radiusPx, 0f)
            arcTo(
                rect = Rect(
                    left = size.width - 2 * radiusPx,
                    top = 0f,
                    right = size.width,
                    bottom = 2 * radiusPx
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Ligne jusqu’au coin bas droit avec arrondi
            lineTo(size.width - skewPx, size.height - radiusPx)
            arcTo(
                rect = Rect(
                    left = size.width - skewPx - radiusPx * 2,
                    top = size.height - 2 * radiusPx,
                    right = size.width - skewPx,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Ligne jusqu’au coin bas gauche avec arrondi
            lineTo(radiusPx, size.height)
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = size.height - 2 * radiusPx,
                    right = 2 * radiusPx,
                    bottom = size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Ligne jusqu’au coin haut gauche (début du biseau)
            lineTo(skewPx + radiusPx, 0f)
            arcTo(
                rect = Rect(
                    left = skewPx,
                    top = 0f,
                    right = skewPx + 2 * radiusPx,
                    bottom = 2 * radiusPx
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }

        return Outline.Generic(path)
    }
}
