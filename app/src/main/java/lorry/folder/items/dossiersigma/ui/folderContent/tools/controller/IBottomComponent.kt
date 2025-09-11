package lorry.folder.items.dossiersigma.ui.folderContent.tools.controller

import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tools
import java.util.UUID

interface IBottomComponent {

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    val currentFlagId: StateFlow<UUID?>
    fun setCurrentFlagId(flagId: UUID?)

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    val currentContent: StateFlow<BottomToolContent?>
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