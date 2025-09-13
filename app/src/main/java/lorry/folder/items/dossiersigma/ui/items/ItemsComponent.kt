package lorry.folder.items.dossiersigma.ui.items

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.ComponentWithViewModel
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.ui.IndexBar.IIndexBar
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.BottomComponent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomTools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.items.utils.ItemsPage
import lorry.folder.items.dossiersigma.ui.items.utils.ItemsViewModel
import javax.inject.Inject
import lorry.folder.items.dossiersigma.ui.sigma.DragState
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.io.File

class ItemsComponent @Inject constructor(
    private val owner: ViewModelStoreOwner,
    private val diskRepository: IDiskRepository,
    private val indexBar: IIndexBar,
    private val folderContentBackComponent: IFolderContentBackComponent,
    val bottomToolsFactory: BottomTools.Factory,
    val bottomComponentFactory: BottomComponent.Factory,
    val toolsViewModel: ToolsViewModel,
    val sigmaViewModel: SigmaViewModel
) : IItemsComponent, ComponentWithViewModel<ItemsViewModel>() {

    val bottomComponent = bottomComponentFactory.create(
        viewModel = toolsViewModel,
        sigmaViewModel = sigmaViewModel
    )

    //ici c'est #[[BottomTools]]
    val bottomTools = bottomToolsFactory.create(
        viewModel = sigmaViewModel,
        bottomComponent = bottomComponent
    )

//    val itemsViewModel: ItemsViewModel by lazy {
//        ViewModelProvider(owner)[ItemsViewModel::class.java]
//    }

    /////////////////
    // drag'n drop //
    /////////////////
    override val dragState: StateFlow<DragState?> = viewModel._dragState

    override val dragTargetItem: StateFlow<Item?> = viewModel._dragTargetItem


    override fun setDragTargetItem(item: Item?) {
        viewModel._dragTargetItem.value = item
    }

    override fun beginDrag(tool: Tool, startOffset: Offset) {
        viewModel._dragState.value = DragState(tool, startOffset)
    }

    override fun addDragOffset(delta: Offset) {
        viewModel._dragState.value?.let {
            viewModel._dragState.value = it.copy(offset = it.offset + delta)
        }
    }

    override fun terminateDrag() {
        viewModel._dragState.value = null
    }

    override fun setDraggableStartPosition(position: Offset?) {
        viewModel._draggableStartPosition.value = position
    }

    /////////////////////
    // liste des items //
    /////////////////////
    @Composable
    context(SigmaActivity, ColumnScope)
    override fun FolderContentFrontPage(
        onItemTapped: (Item) -> Unit,
        onItemLongPressed: (Item) -> Unit,
        onTopLeftPanelClick: (Item) -> Unit,
    ) = ItemsPage(
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

    override val copyAllNASText: StateFlow<String> = toolsViewModel.rawFeed.copyAllNASText
    override val copyNASText: StateFlow<String> = toolsViewModel.rawFeed.copyAllNASText

    override fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    ) = toolsViewModel.rawFeed.updateNASProgress(
        percentage,
        fileIndex,
        fileCount
    )

    override var movingItem: Item? = toolsViewModel.movingItem
    override var itemToMove: Item? = toolsViewModel.movingItem

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