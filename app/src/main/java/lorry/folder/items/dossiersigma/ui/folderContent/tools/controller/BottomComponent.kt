package lorry.folder.items.dossiersigma.ui.folderContent.tools.controller

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.ComponentWithViewModel
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.BottomToolContent
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.OverallProgress
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.tools.ui.Tools
import lorry.folder.items.dossiersigma.ui.folderContent.tools.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.util.UUID
import javax.inject.Inject

/**
 * Nécessite dans l'activity/OnCreate:
 * ```
 * @Inject lateinit var toolsViewModel: ToolsViewModel
 * bottomComponent.attach(toolsViewModel)
 * bottomComponent.sigmaViewModel = this@SigmaActivity.mainViewModel
 * ```
 */
@ActivityRetainedScoped
class BottomComponent @Inject constructor(

): IBottomComponent, ComponentWithViewModel<ToolsViewModel>()
{
    var _sigmaViewModel: SigmaViewModel? = null
    fun sigmaViewModel(): SigmaViewModel = _sigmaViewModel
        ?: throw IllegalStateException("BottomComponent avec sigmaViewModel null")

    ////////////////////////
    // étiquette courante //
    ////////////////////////
    override val currentFlagId: StateFlow<UUID?> = viewModel._currentFlagId

    override fun setCurrentFlagId(flagId: UUID?) {
        viewModel._currentFlagId.value = flagId
    }

    /////////////////////////////////
    // différentes barres d'outils //
    /////////////////////////////////

    override val currentContent: StateFlow<BottomToolContent?> = viewModel._bottomToolsContent
    val defaultContent = viewModel.defaultContent

    override fun setCurrentContent(tools: Tools) {
        setCurrentFlagId(null)
        viewModel._bottomToolsContent.value = when (tools) {
            Tools.DEFAULT -> defaultContent
            else -> tools.content(sigmaViewModel())
        }
    }

    override fun observeDefaultContent() {
        viewModel.viewModelScope.launch {
            // On combine les deux sources de données : le cache des tags et l'ID du tag sélectionné.
            // La lambda sera appelée si l'un ou l'autre change.
            combine(
                currentFlagId,
                sigmaViewModel().folderContentComponent.currentFolderFlow,
                sigmaViewModel().folderContentComponent.reloadTrigger
            ) { selectedId, currentFolder, _ ->
                val tags = currentFolder
                    ?.items
                    ?.mapNotNull { it.tag }
                    ?.distinctBy { it.id }
                    ?: emptyList()

                val tagTools = tags.map { tag ->
                    Tool(
                        text = { tag.title },
                        icon = R.drawable.etiquette,
                        tint = tag.color,
                        id = tag.id ?: UUID.randomUUID(),
                        onClick = { _, _ ->
                            // La logique est simplifiée : on change juste l'ID sélectionné.
                            // La recomposition se chargera de mettre à jour l'état "activated".
                            if (this.activated) {
                                setCurrentFlagId(null)
                            } else {
                                setCurrentFlagId(this.id)
                            }
                        },
                        // L'état "activé" est dérivé directement de la comparaison des IDs.
                        activated = selectedId != null && tag.id == selectedId
                    )
                }

                // 3. On combine les deux listes et on met à jour le singleton.
                defaultContent.updateTools(tagTools)

            }.collect() // Démarre la collecte du Flow combiné.
        }
    }

    ///////////////////////
    // outil sélectionné //
    ///////////////////////
    override val currentTool = viewModel._currentTool.asStateFlow()

    override fun setCurrentTool(tool: Tool?) {
        viewModel._currentTool.value = tool
    }

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////
    override val progress: StateFlow<Int> = viewModel._progress.asStateFlow()

    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveFileService.copy
     */
    override fun updateProgress(value: Int) {
        viewModel._progress.value = value
    }

    override val nasProgress: StateFlow<OverallProgress?> = viewModel._NASprogress.asStateFlow()
    /**
     * utilisé par
     * @see lorry.folder.items.dossiersigma.headless.services.MoveToNASService.copy
     */
    override fun updateNASProgress(
        percentage: Int,
        fileIndex: Int,
        fileCount: Int
    ) {
        viewModel._NASprogress.value = OverallProgress(
            progress = percentage,
            fileIndex = fileIndex,
            fileSize = fileCount
        )
    }

    override val movePasteText: StateFlow<String> = viewModel._movePasteText.asStateFlow()
    override fun updateMovePasteText(value: String) {
        viewModel._movePasteText.value = value
    }

    override val copyNASText: StateFlow<String> = viewModel._copyNASText.asStateFlow()
    override fun updateNASText(value: String) {
        viewModel._copyNASText.value = value
    }

    override val copyAllNASText: StateFlow<String> = viewModel._copyAllNASText.asStateFlow()
    override fun updateAllNASText(value: String) {
        viewModel._copyAllNASText.value = value
    }


}

data class OverallProgress(
    val progress: Int,
    val fileIndex: Int,
    val fileSize: Int
)