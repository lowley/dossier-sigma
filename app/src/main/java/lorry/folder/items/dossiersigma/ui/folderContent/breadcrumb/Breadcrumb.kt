package lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun BreadcrumbItems(
    path: List<String>,
    onClick: (index: Int) -> Unit
) {
    var prev by remember { mutableStateOf(path) }
    val commonPart = computeCommonPart(prev, path)

    // Suffixe visible (partie animée)
    val suffix = remember { mutableStateListOf<String>() }
    // Visibilité par item (clé = segment)
    val vis = remember { mutableStateMapOf<String, MutableTransitionState<Boolean>>() }

    LaunchedEffect(path) {
        prev = manageUI(suffix, prev, commonPart, vis, path)
    }

    UI(commonPart, prev, onClick, suffix, vis, path)
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private suspend fun manageUI(
    suffix: SnapshotStateList<String>,
    prev: List<String>,
    commonPart: Int,
    vis: SnapshotStateMap<String, MutableTransitionState<Boolean>>,
    path: List<String>
): List<String> {
    // initialise le suffixe courant à l'ancien (au-delà du LCP)
    var prev1 = prev
    suffix.clear()
    suffix.addAll(prev1.drop(commonPart))

    // 1) Disparitions (de droite vers gauche)
    for (i in prev1.size - 1 downTo commonPart) {
        val id = prev1[i]
        val st = vis.getOrPut(id) { MutableTransitionState(true) }
        st.targetState = false                  // déclenche l'animation de sortie
        delay(90)                               // rythme (ajuste à ton goût)
        suffix.removeLast()                     // retire visuellement après l'anim
        vis.remove(id)
    }

    // 2) Apparitions (de gauche vers droite après le LCP)
    for (i in commonPart until path.size) {
        val id = path[i]
        suffix.add(id)                          // ajoute l’item (invisible au début)
        vis[id] = MutableTransitionState(false).also { it.targetState = true }
        delay(90)
    }

    prev1 = path
    return prev1
}

@Composable
private fun UI(
    commonPart: Int,
    prev: List<String>,
    onClick: (Int) -> Unit,
    suffix: SnapshotStateList<String>,
    vis: SnapshotStateMap<String, MutableTransitionState<Boolean>>,
    path: List<String>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // --- Préfixe stable, jamais masqué ---
        for (i in 0 until commonPart) {
            BreadcrumbChip(
                text = prev[i],
                path = prev.take(i + 1).joinToString("/"),
            ) { onClick(i) }
            if (i < commonPart - 1) Separator()
        }
        if (commonPart > 0 && suffix.isNotEmpty()) Separator()

        // --- Suffixe animé ---
        suffix.forEachIndexed { idx, seg ->
            key(seg) {
                AnimatedVisibility(
                    visibleState = vis.getValue(seg),
                    enter = expandHorizontally(expandFrom = Alignment.Start),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.End)
                ) {
                    BreadcrumbChip(
                        text = seg,
                        path = path.take(commonPart + idx + 1).joinToString("/"),
                    ) { onClick(commonPart + idx) }
//                }
                    if (idx < suffix.lastIndex) Separator()
                }
            }
        }
    }
}

@Composable
private fun computeCommonPart(
    prev: List<String>,
    path: List<String>
): Int = remember(prev, path) {
    val n = minOf(prev.size, path.size)
    var i = 0
    while (i < n && prev[i] == path[i]) i++
    i
}

@Composable
fun BreadcrumbChip(
    modifier: Modifier = Modifier,
    text: String,
    path: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
fun Separator() {
    Text(text = "/")
}


@Composable
fun AnimatedBreadcrumbItems(
    modifier: Modifier,
    newItemTexts: List<String>,
    duration: Int,
    stateOfLastElement: AnimationState,
    onPathClick: (String) -> Unit
) {
    var acc = ""
    newItemTexts.forEachIndexed { index, newItemText ->
        acc = if (acc.isEmpty()) newItemText else "$acc/$newItemText"

        key(acc) {
            val isLast = index == newItemTexts.lastIndex
            BreadcrumbItem(
                content = { Text(text = newItemText) },
                animationState = if (isLast) stateOfLastElement else AnimationState.Stable,
                duration = duration,
                onPathClick = onPathClick,
                pathItems = newItemTexts.take(index + 1),
                text = newItemText
            )
        }
    }
}

@Composable
fun BreadcrumbItem(
    content: @Composable () -> Unit,
    animationState: AnimationState,
    duration: Int,
    onPathClick: (String) -> Unit,
    pathItems: List<String>,
    text: String,
) {
    // Déterminer si l'item est "nouveau" (doit apparaître) ou existant
    val shouldStartVisible = animationState != AnimationState.Appearing

    // IMPORTANT : initial = visible? 1 sinon 0
    val st = remember(text) { MutableTransitionState(shouldStartVisible) }

    // On ne touche qu'à targetState (public)
    LaunchedEffect(animationState, text) {
        st.targetState = when (animationState) {
            AnimationState.Appearing -> true   // 0 -> 1
            AnimationState.Disappearing -> false  // 1 -> 0
            AnimationState.Stable -> true
        }
    }

    val transition = updateTransition(st, label = "itemClipTransition")
    val clipFraction by transition.animateFloat(
        transitionSpec = { tween(durationMillis = duration, easing = FastOutSlowInEasing) }
    ) { visible -> if (visible) 1f else 0f }

    BreadcrumbItemFraction(
        content = content,
        clipFraction = clipFraction,
        modifier = Modifier,
        onPathClick = onPathClick,
        text = text,
        newItemTexts = pathItems
    )
}


@Composable
fun BreadcrumbItemFraction(
    content: @Composable () -> Unit,
    clipFraction: Float,
    modifier: Modifier,
    onPathClick: (String) -> Unit,
    text: String,
    newItemTexts: List<String>,
) {

    Row(
        modifier = modifier
            .clipToBounds()
            .clickable {
                onPathClick("/${newItemTexts.joinToString("/")}")
            }
            .drawWithContent {
                val w = size.width

                clipRect(right = w * clipFraction) {
                    this@drawWithContent.drawContent()
                }
            },
    ) {
        content()
    }
}

enum class AnimationState { Appearing, Disappearing, Stable }
