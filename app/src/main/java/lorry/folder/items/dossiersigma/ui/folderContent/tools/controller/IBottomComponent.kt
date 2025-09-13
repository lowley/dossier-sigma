package lorry.folder.items.dossiersigma.ui.folderContent.tools.controller

import dagger.assisted.Assisted
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars.Tools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.util.UUID

interface IBottomComponent {

    ////////////////
    // viewmodels //
    ////////////////
    val sigmaViewModel: SigmaViewModel

    val toolsViewModel: ToolsViewModel

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    fun observeDefaultContent()

    /////////////////////////////////////////////////////////////////////////////
    // outil sélectionné, destiné à l'affichage par remontée dans MainActivity //
    /////////////////////////////////////////////////////////////////////////////
    val currentTool: StateFlow<Tool?>
    fun setCurrentTool(tool: Tool?)

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////

    var movingItem: Item?
    var copyingItem: Item?
    var itemToMove: Item?



}