package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.controller

import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel

interface IToolbarComponent {

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