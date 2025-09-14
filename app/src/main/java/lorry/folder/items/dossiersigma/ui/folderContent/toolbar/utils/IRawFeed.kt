package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.controller.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.ToolbarContent
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.toolbars.Tools
import java.util.UUID

interface IRawFeed {

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    val currentFlagId: StateFlow<UUID?>
    fun setCurrentFlagId(flagId: UUID?)

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    val defaultContent: ToolbarContent
    val _bottomToolsContent: MutableStateFlow<ToolbarContent?>

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////

    val _progress: MutableStateFlow<Int>
    val _NASprogress: MutableStateFlow<OverallProgress?>
    val _movePasteText: MutableStateFlow<String>
    val _copyNASText: MutableStateFlow<String>
    val _copyAllNASText: MutableStateFlow<String>

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


    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////

    val currentContent: StateFlow<ToolbarContent?>

    fun setCurrentContent(tools: Tools)
}