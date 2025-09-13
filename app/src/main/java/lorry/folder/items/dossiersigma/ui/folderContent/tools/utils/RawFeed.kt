package lorry.folder.items.dossiersigma.ui.folderContent.tools.utils

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lorry.folder.items.dossiersigma.ui.folderContent.tools.controller.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars.DEFAULT
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.toolbars.Tools
import java.util.UUID
import javax.inject.Singleton

@Singleton
class RawFeed @Inject constructor(

): IRawFeed{

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    private val _currentFlagId = MutableStateFlow<UUID?>(null)

    override val currentFlagId: StateFlow<UUID?> = _currentFlagId

    override fun setCurrentFlagId(flagId: UUID?) {
        _currentFlagId.value = flagId
    }

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    override val defaultContent = BottomToolContent(emptyList(), "DEFAULT_CONTENT")
    override val _bottomToolsContent = MutableStateFlow<BottomToolContent?>(defaultContent)

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////
    //pour SigmaViewModel
    override val _progress = MutableStateFlow(0)
    override val _NASprogress = MutableStateFlow<OverallProgress?>(null)
    override val _movePasteText = MutableStateFlow("Coller")
    override val _copyNASText = MutableStateFlow("1 -> NAS")
    override val _copyAllNASText = MutableStateFlow("Tous -> NAS")

    override val progress: StateFlow<Int> = _progress.asStateFlow()
    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveFileService.copy
     */
    override fun updateProgress(value: Int) {
        _progress.value = value
    }

    override val nasProgress: StateFlow<OverallProgress?> = _NASprogress.asStateFlow()
    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveToNASService.copy
     */
    override fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    ) {
        _NASprogress.value = OverallProgress(
            progress = percentage,
            fileIndex = fileIndex,
            fileSize = fileCount
        )
    }

    override val movePasteText: StateFlow<String> = _movePasteText.asStateFlow()
    override fun updateMovePasteText(value: String) {
        _movePasteText.value = value
    }

    override val copyNASText: StateFlow<String> = _copyNASText.asStateFlow()
    override fun updateNASText(value: String) {
        _copyNASText.value = value
    }

    override val copyAllNASText: StateFlow<String> = _copyAllNASText.asStateFlow()
    override fun updateAllNASText(value: String) {
        _copyAllNASText.value = value
    }


    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////
    override val currentContent: StateFlow<BottomToolContent?> = _bottomToolsContent

    override fun setCurrentContent(tools: Tools) {
        setCurrentFlagId(null)
        _bottomToolsContent.value = when (tools) {
            DEFAULT -> defaultContent
            else -> tools.content()
        }
    }
}