package lorry.folder.items.dossiersigma.ui.folderContent.tools.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomTools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.StickerIcon
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.StickerText
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toColoredTag
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

@Composable
context(BottomTools, RowScope)
fun FixedSticker(
    modifier: Modifier = Modifier.Companion,
    tool: Tool,
    activity: SigmaActivity,
    beginDrag: (Tool, Offset) -> Unit,
    terminateDrag: () -> Unit,
    setDragTargetItem: (Item?) -> Unit,
    addDragOffset: (Offset) -> Unit,
    dragTargetItem: StateFlow<Item?>,
) {
    Box(
        modifier = modifier
            .width(85.dp)
            .fillMaxHeight()
            .clickable {
                setCurrentTool(tool)
                viewModel.viewModelScope.launch {
                    tool.onClick(tool, viewModel, activity)
                }
            }
    ) {
        var globalOffset: Offset = Offset.Companion.Zero
        //icône statique, toujours existante
        StickerIcon(
            modifier = Modifier.Companion
                .padding(top = 0.dp)
                .align(Alignment.Companion.TopCenter)
                .onGloballyPositioned { layoutCoordinates ->
                    val localOffset = layoutCoordinates.positionInRoot()
                    globalOffset = layoutCoordinates.localToRoot(Offset.Companion.Zero)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            beginDrag(tool, globalOffset)
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            addDragOffset(dragAmount)
                        },
                        onDragEnd = {
                            val target = dragTargetItem.value

                            if (target != null) {
                                viewModel.assignColoredTagToItem(
                                    target,
                                    tool.toColoredTag()
                                )
                            }

                            terminateDrag()
                        },
                        onDragCancel = {},
                    )
                },
            iconRes = tool.icon,
            iconTint = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringColor = if (tool.isColoredIcon) Color.Companion.Unspecified else
                (tool.tint ?: Color(0xFFe9c46a)),
            ringWidth = 2.dp,
            iconSize = 28.dp,
            ringSize = 33.dp,
            isRingEnabled = tool.activated
        )

        StickerText(
            tool = tool
        )
    }

}
