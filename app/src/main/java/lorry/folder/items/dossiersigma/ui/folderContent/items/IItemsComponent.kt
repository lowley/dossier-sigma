package lorry.folder.items.dossiersigma.ui.folderContent.items

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity

interface IItemsComponent {

    /////////////////
    // drag'n drop //
    /////////////////
    val dragState: StateFlow<DragState?>
    val dragTargetItem: StateFlow<Item?>

    fun setDragTargetItem(item: Item?)
    fun beginDrag(tool: Tool, startOffset: Offset)
    fun addDragOffset(delta: Offset)
    fun terminateDrag()
    val draggableStartPosition: StateFlow<Offset?>
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