package lorry.folder.items.dossiersigma.ui.folderContentFront

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.BottomTools
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.Tool
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.Tools
import lorry.folder.items.dossiersigma.ui.folderContentFront.utils.frontPage
import javax.inject.Inject
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import java.io.File
import kotlin.getValue

class FolderContentFrontComponent @Inject constructor(
    private val owner: ViewModelStoreOwner,
    private val diskRepository: IDiskRepository,
    private val indexBar: IIndexBar,
    private val folderContentBackComponent: IFolderContentBackComponent,
    private val bottomTools: BottomTools,
) : IFolderContentFrontComponent {


    val frontViewModel: FolderContentFrontViewModel by lazy {
        ViewModelProvider(owner)[FolderContentFrontViewModel::class.java]
    }

    /////////////////
    // drag'n drop //
    /////////////////
    override val dragState: StateFlow<DragState?> = frontViewModel._dragState

    override val dragTargetItem: StateFlow<Item?> = frontViewModel._dragTargetItem


    override fun setDragTargetItem(item: Item?) {
        frontViewModel._dragTargetItem.value = item
    }

    override fun beginDrag(tool: Tool, startOffset: Offset) {
        frontViewModel._dragState.value = DragState(tool, startOffset)
    }

    override fun addDragOffset(delta: Offset) {
        frontViewModel._dragState.value?.let {
            frontViewModel._dragState.value = it.copy(offset = it.offset + delta)
        }
    }

    override fun terminateDrag() {
        frontViewModel._dragState.value = null
    }

    override fun setDraggableStartPosition(position: Offset?) {
        frontViewModel._draggableStartPosition.value = position
    }

    //////////////////
    // bottom tools //
    //////////////////
    @Composable
    override fun BottomToolBar(
        activity: SigmaActivity
    ){
        bottomTools.BottomToolBar(
            activity,
            beginDrag = this::beginDrag,
            terminateDrag = this::terminateDrag,
            setDragTargetItem = this::setDragTargetItem,
            addDragOffset = this::addDragOffset,
            dragTargetItem = dragTargetItem
            )
    }

    override fun observeDefaultContent() = bottomTools.observeDefaultContent()
    override fun setCurrentContent(tools: Tools) = bottomTools.setCurrentContent(tools)
    override val copyAllNASText: StateFlow<String> = bottomTools._copyAllNASText
    override val copyNASText: StateFlow<String> = bottomTools._copyNASText

    override fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    ) = bottomTools.updateNASProgress(percentage, fileIndex, fileCount)

    @Composable
    context(BoxScope)
    override fun MobileSticker(
        dragState: DragState,
        activity: SigmaActivity,
    ) = bottomTools.MobileSticker(
        dragState = dragState,
        activity = activity
    )

    override val currentTool: StateFlow<Tool?> = bottomTools._currentTool

    override var movingItem: Item? = null
    override var itemToMove: Item? = null

    /////////////////////
    // liste des items //
    /////////////////////
    @Composable
    context(SigmaActivity, ColumnScope)
    override fun FolderContentFrontPage(
        onItemTapped: (Item) -> Unit,
        onItemLongPressed: (Item) -> Unit,
        onTopLeftPanelClick: (Item) -> Unit,
    ) = frontPage(
        onHoveredNotHovered = { item ->
            folderContentFrontComponent.setDragTargetItem(item)
        },
        onItemTapped = onItemTapped,
        onItemLongPressed = onItemLongPressed,
        onTopLeftPanelClick = onTopLeftPanelClick,
        getInfoSup = { item ->
            getInfoSup(item)
        },
        getInfoInf = { item ->
            getInfoInf(item)
        },
        onRefresh = {
            folderContentBackComponent.reloadCurrentFolderByRefresh2()
        },
        indexBar = indexBar,
    )

    private suspend fun getInfoSup(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder) diskRepository
                .countFilesAndFolders(File(item.fullPath)).component1().toString() else item.name
                .substringAfterLast(".").toUpperCase(Locale.current)

            infos
        }
    }

    private suspend fun getInfoInf(item: Item): String {
        return withContext(Dispatchers.IO) {
            val infos = if (item is SigmaFolder)
                diskRepository.countFilesAndFolders(File(item.fullPath)).component2()
                    .toString()
            else formatFileSizeShort(diskRepository.getSize(File(item.fullPath)))

            infos
        }
    }

    private fun formatFileSizeShort(bytes: Long): String {
        if (bytes < 1024) return "${bytes}B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
        val value = bytes.toDouble() / (1L shl (z * 10))
        return String.format("%.1f%c", value, " KMGTPE"[z])
    }
}