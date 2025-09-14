package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.IMoveToNASComponent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.controller.IToolbarComponent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.COPY_FILE
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.CROP
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.DEFAULT
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.FILE
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.MOVES
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.MOVE_FILE
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.TAGS_MENU
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.FixedSticker
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.StickerIcon
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.StickerText
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaColors
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import kotlin.math.roundToInt

class ToolBarManager @AssistedInject constructor(
    @Assisted val viewModel: SigmaViewModel,
    @Assisted val bottomComponent: IToolbarComponent,
    var moveToNASComponent: IMoveToNASComponent
) {
    init {
        DEFAULT.toolBarManager = this
        TAGS_MENU.toolBarManager = this
        FILE.toolBarManager = this
        MOVES.toolBarManager = this
        COPY_FILE.toolBarManager = this
        MOVE_FILE.toolBarManager = this
        CROP.toolBarManager = this
    }

    @AssistedFactory
    interface Factory{
        fun create(
            viewModel: SigmaViewModel,
            bottomComponent: IToolbarComponent
        ): ToolBarManager
    }

    @Composable
    fun ToolBar(
        activity: SigmaActivity,
        beginDrag: (Tool, Offset) -> Unit,
        terminateDrag: () -> Unit,
        setDragTargetItem: (Item?) -> Unit,
        addDragOffset: (Offset) -> Unit,
        dragTargetItem: StateFlow<Item?>
    ) {
        val content = bottomComponent.toolsViewModel.rawFeed.currentContent.collectAsState().value
        val toolList = content?.tools?.collectAsState()?.value ?: emptyList()
        val modifier = Modifier.Companion
            .padding(vertical = 0.dp)

        Log.d(SigmaActivity.Companion.TAG, "Content: $content")
        Log.d(SigmaActivity.Companion.TAG, "BottomToolBar: ${toolList.size}")

        /*
//        @startuml
//        (*) -up-> "First Action"
//        -right-> "Second Action"
//        --> "Third Action"
//        -left-> (*)
        @enduml
         */

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(SigmaColors.current.primary),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {

            toolList.forEach { tool ->
                //icône statique, toujours existante
                FixedSticker(
                    tool = tool,
                    activity = activity,
                    beginDrag = beginDrag,
                    terminateDrag = terminateDrag,
                    setDragTargetItem = setDragTargetItem,
                    addDragOffset = addDragOffset,
                    dragTargetItem = dragTargetItem
                )
            }
        }
    }



    @Composable
    context(BoxScope)
    fun MobileSticker(
        dragState: DragState,
        activity: SigmaActivity,
    ) {
        val tool: Tool = dragState.tool
        val offset: Offset = dragState.offset

        Box(
            modifier = Modifier.Companion
                .width(85.dp)
                .fillMaxHeight()
                .clickable {
                    bottomComponent.setCurrentTool(tool)
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
                            onDrag = { change, dragAmount ->
                            },
                            onDragEnd = {}
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
}







