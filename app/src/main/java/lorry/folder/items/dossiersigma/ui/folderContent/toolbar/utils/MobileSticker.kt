package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolBarManager
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import kotlin.math.roundToInt

@Composable
context(ToolBarManager, BoxScope)
fun MobileSticker(
    dragState: DragState,
    activity: SigmaActivity,
    beginDrag: (Tool, Offset) -> Unit,
    terminateDrag: () -> Unit,
    setDragTargetItem: (Item?) -> Unit,
    addDragOffset: (Offset) -> Unit,
    dragTargetItem: StateFlow<Item?>,
) {
    val tool: Tool = dragState.tool
    val offset: Offset = dragState.offset
    val dragTarget by dragTargetItem.collectAsState()

    Box(
        modifier = Modifier.Companion
            .width(85.dp)
            .fillMaxHeight()
            .clickable {
                toolbarComponent.setCurrentTool(tool)
                viewModel.viewModelScope.launch {
                    tool.onClick(tool, viewModel, activity)
                }
            }
    ) {
        StickerIcon(
            modifier = Modifier.Companion
                .offset {
                    IntOffset(
                        offset.x.roundToInt() - 60,
                        offset.y.roundToInt() - 70
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            beginDrag(tool, it)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            addDragOffset(dragAmount)
                        },
                        onDragEnd = {
                            setDragTargetItem(null)
                            terminateDrag()
                        }
                    )
                },
            iconRes = tool.icon,
            iconTint = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringColor = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringWidth = 2.dp,
            ringSize = 85.dp,
            iconSize = 70.dp,
            isRingEnabled = true,
        )

        StickerText(
            tool = tool
        )
    }
}