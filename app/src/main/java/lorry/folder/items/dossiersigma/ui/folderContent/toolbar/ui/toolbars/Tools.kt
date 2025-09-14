package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars

import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolBarManager

sealed class Tools {

    abstract fun content(): ToolbarContent
    lateinit var toolBarManager: ToolBarManager

}