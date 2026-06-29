package lorry.folder.items.dossiersigma.ui.folderContent.breadcrumb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.basics.domain.toSigmaPath

@Composable
fun BreadcrumbComponent.UI(
    state: BreadcrumbState?,
    onClick: (SigmaPath) -> Unit,
    animDuration: Int
) {
    if (state == null || state is BreadcrumbState.LOADING)
        return

    val stateData = state as BreadcrumbState.DATA
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        if (stateData.animation == Animation.APPEAR) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Box(
       modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val segs = stateData.currentPath?.split("/") ?: emptyList()

            // éléments non animés
            segs.dropLast(1).forEachIndexed { i, seg ->
                BreadcrumbChip(
                    text = seg,
                ) { onClick(("/" + segs.slice(0..i).joinToString("/")).toSigmaPath()) }

                Separator()
            }

            // dernier élément animé
            val lastSeg = segs.lastOrNull() ?: ""
            val lastSegIndex = segs.size - 1
            val show = stateData.animation == Animation.APPEAR
            
            val visibleState = remember(lastSeg, lastSegIndex) {
                MutableTransitionState(!show).apply {
                    targetState = show
                }
            }
            visibleState.targetState = show

            key("$lastSeg-$lastSegIndex") {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = expandHorizontally(
                        expandFrom = Alignment.Start,
                        animationSpec = tween(durationMillis = animDuration)
                    ),
                    exit = shrinkHorizontally(
                        shrinkTowards = Alignment.End,
                        animationSpec = tween(durationMillis = animDuration)
                    )
                ) {
                    Row(
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        BreadcrumbChip(
                            text = lastSeg,
                        ) { }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbChip(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text, modifier = modifier.clickable {
            onClick()
        })
}

@Composable
fun Separator() {
    Text(text = "/")
}
