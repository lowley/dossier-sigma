package lorry.folder.items.dossiersigma.ui.folderContentFront

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.Tool
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.Tools
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

interface IFolderContentFrontComponent {

    /////////////////
    // drag'n drop //
    /////////////////
    val dragState: StateFlow<DragState?>
    val dragTargetItem: StateFlow<Item?>

    fun setDragTargetItem(item: Item?)
    fun beginDrag(tool: Tool, startOffset: Offset)
    fun addDragOffset(delta: Offset)
    fun terminateDrag()
    fun setDraggableStartPosition(position: Offset?)

    ////////////////////
    // folder content //
    ////////////////////
    @Composable
    context(SigmaActivity, ColumnScope)
    fun FolderContentFrontPage(
        onItemTapped: (Item) -> Unit,
        onItemLongPressed: (Item) -> Unit,
        onTopLeftPanelClick: (Item) -> Unit,
        )

    //////////////////
    // bottom tools //
    //////////////////
    @Composable
    fun BottomToolBar(
        activity: SigmaActivity
    )

    fun observeDefaultContent()
    fun setCurrentContent(tools: Tools)
    val currentTool: StateFlow<Tool?>

    @Composable
    context(BoxScope)
    fun MobileSticker(
        dragState: DragState,
        activity: SigmaActivity,
    )

    /////////////////////////
    // copies/déplacements //
    /////////////////////////
    val copyAllNASText: StateFlow<String>
    val copyNASText: StateFlow<String>

    fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    )

    var movingItem: Item?
    var itemToMove: Item?








}