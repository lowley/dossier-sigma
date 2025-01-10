package lorry.folder.items.dossiersigma.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb2( //2
    items: List<String>, // Liste observable
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    arrowColor: Color = Color.Gray,
    transitionDuration: Int = 600,
    arrowTransitionSpeed: Int = 300
) {
    // Trace les items visibles pour gérer leur transition
    val visibleItems = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    var visible by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(items) {

    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(transitionDuration)) +
                slideInHorizontally(animationSpec = tween(arrowTransitionSpeed)),
        exit = fadeOut(animationSpec = tween(transitionDuration)) +
                slideOutHorizontally(animationSpec = tween(arrowTransitionSpeed))
    ) {
        Text(
            modifier = Modifier
                .clickable(onClick = { visible = !visible }),
            text = "youkoulélé"
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb1( //1
    items: List<String>, // Liste observable
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    arrowColor: Color = Color.Gray,
    transitionDuration: Int = 600,
    arrowTransitionSpeed: Int = 300
) {
    // Trace les items visibles pour gérer leur transition
    val visibleItems = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(items) {
        // Synchronise les items visibles avec les items actuels
        val addedItems = items - visibleItems
        val removedItems = visibleItems - items

        // Supprime les éléments disparus avec une animation
        removedItems.forEach { item ->
            scope.launch {
                visibleItems.remove(item)
            }
        }

        // Ajoute les nouveaux éléments avec une animation
        addedItems.forEach { item ->
            scope.launch {
                visibleItems.add(item)
            }
        }
    }

    Row(modifier = modifier) {
        visibleItems.forEachIndexed { index, item ->
            AnimatedVisibility(
                visible = item in visibleItems,
                enter = fadeIn(animationSpec = tween(transitionDuration)) +
                        slideInHorizontally(animationSpec = tween(arrowTransitionSpeed)),
                exit = fadeOut(animationSpec = tween(transitionDuration)) +
                        slideOutHorizontally(animationSpec = tween(arrowTransitionSpeed))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .clickable {
                                val path = items
                                    .take(index + 1)
                                    .joinToString("/")
                                onPathClick(path)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 16.sp,
                        fontWeight = if (index == visibleItems.lastIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == visibleItems.lastIndex) activeColor else inactiveColor
                    )

                    if (index != visibleItems.lastIndex) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedContent(
                            targetState = ">",
                            transitionSpec = {
                                slideInHorizontally(
                                    animationSpec = tween(arrowTransitionSpeed)
                                ).with(
                                    slideOutHorizontally(
                                        animationSpec = tween(arrowTransitionSpeed)
                                    )
                                ).using(SizeTransform(clip = false))
                            }
                        ) { it ->
                            Text(
                                text = ">",
                                color = arrowColor,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb00( //00
    items: List<String>, // Liste observable
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    arrowColor: Color = Color.Gray,
    transitionDuration: Int = 600,
    arrowTransitionSpeed: Int = 300
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(items) {


    }

    Row(modifier = modifier) {
        items.forEachIndexed { index, item ->

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item,
                    modifier = Modifier
                        .clickable {
                            val path = items
                                .take(index + 1)
                                .joinToString("/")
                            onPathClick(path)
                        }
                        .padding(horizontal = 4.dp),
                    fontSize = 16.sp,
                    fontWeight = if (index == items.lastIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == items.lastIndex) activeColor else inactiveColor
                )

                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = ">",
                        color = arrowColor,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb( //3
    items: List<String>, // Liste observable
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Blue,
    inactiveColor: Color = Color.Gray,
    arrowColor: Color = Color.Gray,
    transitionDuration: Int = 600,
    arrowTransitionSpeed: Int = 300
) {
    val scope = rememberCoroutineScope()
    val visible = remember { mutableStateOf(true) }
    val visibleItems = remember { mutableStateListOf<String>() }

    LaunchedEffect(items) {
        scope.launch {
            visible.value = false
        }

        visibleItems.removeRange(0, visibleItems.size)
        visibleItems.addAll(items)

        scope.launch {
            visible.value = true
        }
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(modifier = modifier) {
            items.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item,
                        modifier = Modifier
                            .clickable {
                                val path = items
                                    .take(index + 1)
                                    .joinToString("/")
                                onPathClick(path)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 16.sp,
                        fontWeight = if (index == items.lastIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == items.lastIndex) activeColor else inactiveColor
                    )

                    if (index != items.lastIndex) {
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = ">",
                            color = arrowColor,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }


}