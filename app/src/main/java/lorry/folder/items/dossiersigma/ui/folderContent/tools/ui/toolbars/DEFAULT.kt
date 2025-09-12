package lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars

import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tools
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

object DEFAULT : Tools() {
    override fun content(viewModel: SigmaViewModel?) = component.defaultContent
}