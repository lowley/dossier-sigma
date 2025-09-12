package lorry.folder.items.dossiersigma.ui.folderContent.tools.controller

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

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    val currentFlagId: StateFlow<UUID?>
    fun setCurrentFlagId(flagId: UUID?)

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////

    val currentContent: StateFlow<BottomToolContent?>
    val defaultContent: BottomToolContent
    fun setCurrentContent(tools: Tools)
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
    val progress: StateFlow<Int>
    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveFileService.copy
     */
    fun updateProgress(value: Int)

    val nasProgress: StateFlow<OverallProgress?>
    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveToNASService.copy
     */
    fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    )

    val copyNASText: StateFlow<String>
    fun updateNASText(value: String)

    val copyAllNASText: StateFlow<String>
    fun updateAllNASText(value: String)

    val movePasteText: StateFlow<String>
    fun updateMovePasteText(value: String)


}