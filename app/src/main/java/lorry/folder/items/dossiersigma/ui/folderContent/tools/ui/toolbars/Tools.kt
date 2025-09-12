package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomTools
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

sealed class Tools {

    abstract fun content(viewModel: SigmaViewModel? = null): BottomToolContent
    lateinit var bottomTools: BottomTools

}