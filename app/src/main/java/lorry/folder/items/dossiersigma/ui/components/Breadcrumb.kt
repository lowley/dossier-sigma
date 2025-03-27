package lorry.folder.items.dossiersigma.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
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
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
            fontSize = 16.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else inactiveColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(">", color = arrowColor, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
    }
}