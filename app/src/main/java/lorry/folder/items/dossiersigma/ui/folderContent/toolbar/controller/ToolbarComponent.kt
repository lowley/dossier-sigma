package lorry.folder.items.dossiersigma.ui.folderContent.toolbar.controller

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.ui.Tool
import lorry.folder.items.dossiersigma.ui.folderContent.toolbar.utils.ToolsViewModel
import lorry.folder.items.dossiersigma.ui.sigma.SigmaViewModel
import java.util.UUID

/**
 * Nécessite dans l'activity/OnCreate:
 * ```
 * @Inject lateinit var toolsViewModel: ToolsViewModel
 * bottomComponent.attach(toolsViewModel)
 * bottomComponent.sigmaViewModel = this@SigmaActivity.mainViewModel
 * ```
 */

class ToolbarComponent @AssistedInject constructor(
    @Assisted override val toolsViewModel: ToolsViewModel,
    @Assisted override val sigmaViewModel: SigmaViewModel
): IToolbarComponent
{
    @AssistedFactory
    interface Factory{
        fun create(toolsViewModel: ToolsViewModel, sigmaViewModel: SigmaViewModel): ToolbarComponent
    }

    /**
     * Observe différents flux et calcule le contenu de la toolbar<br/>
     *
     * currentFolderFlow: les tools sont une compilation à partir des flags des items<br/>
     *
     * currentFlagID: dit quel tool est activé, donc visible<br/>
     *
     * reloadtrigger: pec du reload des items
     */
    //#[[observeDefaultContent()]]
    override fun observeDefaultContent() {
        toolsViewModel.viewModelScope.launch {
            // On combine les deux sources de données : le cache des tags et l'ID du tag sélectionné.
            // La lambda sera appelée si l'un ou l'autre change.
            combine(
                toolsViewModel.rawFeed.currentFlagId,
                sigmaViewModel.folderContentComponent.currentFolderFlow,
                sigmaViewModel.folderContentComponent.reloadTrigger
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
                                toolsViewModel.rawFeed.setCurrentFlagId(null)
                            } else {
                                toolsViewModel.rawFeed.setCurrentFlagId(this.id)
                            }
                        },
                        // L'état "activé" est dérivé directement de la comparaison des IDs.
                        activated = selectedId != null && tag.id == selectedId
                    )
                }

                // 3. On combine les deux listes et on met à jour le singleton.
                toolsViewModel.rawFeed.defaultContent.updateTools(tagTools)

            }.collect() // Démarre la collecte du Flow combiné.
        }
    }

    ///////////////////////
    // outil sélectionné //
    ///////////////////////
    override val currentTool = toolsViewModel._currentTool.asStateFlow()

    override fun setCurrentTool(tool: Tool?) {
        toolsViewModel._currentTool.value = tool
    }

    ///////////////////////////////////
    // copie/déplacement de fichiers //
    ///////////////////////////////////

    override var movingItem: Item? = toolsViewModel.movingItem
    override var copyingItem: Item? = toolsViewModel.copyingItem
    override var itemToMove: Item? = toolsViewModel.itemToMove
}

data class OverallProgress(
    val progress: Int,
    val fileIndex: Int,
    val fileSize: Int
)