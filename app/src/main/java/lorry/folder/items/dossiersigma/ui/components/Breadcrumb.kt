package lorry.folder.items.dossiersigma.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
    val previousItems = remember { mutableStateListOf<String>() }

    // Détecter les ajouts/suppressions
    val transition = updateTransition(targetState = items, label = "breadcrumbTransition")

    LaunchedEffect(items) {
        previousItems.clear()
        previousItems.addAll(items)
    }

    Row(modifier = modifier) {
        transition.AnimatedContent(
            transitionSpec = {
                fadeIn(animationSpec = tween(transitionDuration)) with
                        fadeOut(animationSpec = tween(transitionDuration))
            },
            contentKey = { it.joinToString("/") } // Clé stable selon le chemin complet
        ) { currentItems ->
            Row {
                currentItems.forEachIndexed { index, item ->
                    BreadcrumbItem(
                        text = item,
                        isActive = index == currentItems.lastIndex,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        arrowColor = arrowColor,
                        onClick = {
                            val path = currentItems.take(index + 1).joinToString("/")
                            onPathClick(path)
                        },
                        animateAppearance = item in currentItems,
                        duration = transitionDuration
                    )
                }
            }
        }
    }
}

@Composable
fun BreadcrumbItem(
    text: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    arrowColor: Color,
    onClick: () -> Unit,
    animateAppearance: Boolean, // true: apparition, false: disparition
    duration: Int
) {
    var startAnimation by remember { mutableStateOf(!animateAppearance) }

    LaunchedEffect(animateAppearance) {
        if (animateAppearance) {
            startAnimation = false
            delay(50) // Petite attente (~50 ms) pour garantir le rendu du texte
            startAnimation = true
        } else {
            startAnimation = false
        }
    }

    val transition = updateTransition(targetState = startAnimation, label = "itemClipTransition")

    val clipFraction by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = duration, easing = FastOutSlowInEasing)
        }, label = "clipFraction"
    ) { visible ->
        if (visible) 1f else 0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clipToBounds()
            .drawWithContent {
                val width = size.width

                if (animateAppearance) {
                    // Apparition progressive gauche vers droite
                    clipRect(right = width * clipFraction) {
                        this@drawWithContent.drawContent()
                    }
                } else {
                    // Disparition progressive droite vers gauche
                    clipRect(left = width * (1 - clipFraction)) {
                        this@drawWithContent.drawContent()
                    }
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
