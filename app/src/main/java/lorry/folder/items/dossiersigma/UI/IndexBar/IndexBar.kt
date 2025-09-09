package lorry.folder.items.dossiersigma.UI.IndexBar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.folderContent.IFolderContentComponent
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.UI.IndexBar.utilities.Content
import lorry.folder.items.dossiersigma.UI.IndexBar.utilities.InfoType
import lorry.folder.items.dossiersigma.UI.IndexBar.utilities.toIndexBarItemInfoList
import lorry.folder.items.dossiersigma.UI.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.UI.sigma.SigmaColors
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class IndexBar @Inject constructor(
    val folderContentComponent: IFolderContentComponent
) : IIndexBar {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val modelFlow = combine(
        folderContentComponent.currentFolderFlow,
        folderContentComponent.sorting
    ) { currentFolder, sorting ->
        currentFolder?.items?.toIndexBarItemInfoList(sorting)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    context(BoxScope, SigmaActivity)
    override fun display(currentScrollState: LazyGridState) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            val model by modelFlow.collectAsState(emptyList())

            model?.forEach { info ->
                when (val c = info.content) {
                    is Content.Text -> {
                        var tooltipVisible = remember { mutableStateOf(false) }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(info.contextualHelp)
                                }
                            },
                            state = rememberTooltipState(isPersistent = false)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            val currentFolderItems = remember {
                                derivedStateOf {
                                    mainViewModel.folderContentComponent
                                        .currentFolderFlow?.value?.items ?: emptyList()
                                }
                            }

//                                mainViewModel.displayedItemsFlow.collectAsState()

                            Text(
                                modifier = Modifier
                                    .clickable {
                                        tooltipVisible.value = !tooltipVisible.value

                                        val items = currentFolderItems.value
                                            .sortedBy { it.name }

                                        val zone = ZoneId.systemDefault()

                                        items.forEachIndexed { index, item ->
                                            val itemFirstCharacter = item.name.first().uppercase()

                                            if (itemFirstCharacter.equals(info.content.text)) {
                                                coroutineScope.launch {
                                                    currentScrollState.animateScrollToItem(
                                                        index
                                                    )
                                                }

                                                return@clickable
                                            }
                                        }
                                    },
                                text = info.content.text,
                                color = if (info.infoType == InfoType.MAJOR) SigmaColors.current.tertiary else SigmaColors.current.secondary
                            )

                        }
                    }

                    is Content.Icon -> {
                        var tooltipVisible = remember { mutableStateOf(false) }

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(info.contextualHelp)
                                }
                            },
                            state = rememberTooltipState(isPersistent = false)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            val currentFolderItems = remember {
                                derivedStateOf {
                                    mainViewModel.folderContentComponent
                                        .currentFolderFlow.value?.items ?: emptyList()
                                }
                            }

                            Icon(
                                modifier = Modifier
                                    .size(if (info.infoType == InfoType.MAJOR) 20.dp else 15.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                tooltipVisible.value = !tooltipVisible.value

                                                require(info.endDate != null)
                                                val items = currentFolderItems.value
                                                    .sortedBy { it.modificationDate }
                                                    .reversed()

                                                val zone = ZoneId.systemDefault()

                                                items.forEachIndexed { index, item ->
                                                    val itemDate =
                                                        Instant.ofEpochMilli(item.modificationDate)
                                                            .atZone(ZoneId.systemDefault())
                                                            .toLocalDate()
                                                    if (itemDate.equals(info.endDate) ||
                                                        itemDate.isBefore(info.endDate)
                                                    ) {
                                                        coroutineScope.launch {
                                                            currentScrollState.animateScrollToItem(
                                                                index
                                                            )
                                                        }
                                                        return@detectTapGestures
                                                    }
                                                }
                                            })
                                    },
                                painter = painterResource(id = R.drawable.rond),
                                tint = if (info.infoType == InfoType.MAJOR) SigmaColors.current.tertiary else SigmaColors.current.secondary,
                                contentDescription = null
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}