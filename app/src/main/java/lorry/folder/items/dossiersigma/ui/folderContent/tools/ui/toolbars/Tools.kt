package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomTools

sealed class Tools {

    abstract fun content(): BottomToolContent
    lateinit var bottomTools: BottomTools

}