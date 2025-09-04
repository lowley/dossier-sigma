package lorry.folder.items.dossiersigma.headless.folderContent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

interface IFolderContentComponent{

    val sorting: StateFlow<SortingCriterion>
    fun setSorting(sorting: SortingCriterion)

    val folderPathHistory: StateFlow<List<String>>
    fun addFolderPathToHistory(folderPath: String)
    fun removeLastFolderPathHistory()
    val folderCacheFlow: StateFlow<Map<String, FolderCacheEntry>>
    val currentFolderFlow: StateFlow<SigmaFolder?>

    ///////////////////
    // reload simple //
    ///////////////////
    val reloadTrigger: MutableStateFlow<Int>
    fun reloadCurrentFolder()

    /////////////////////////
    // reload pour refresh //
    /////////////////////////
    val refreshReloadTrigger: MutableStateFlow<Int>
    fun reloadCurrentFolderByRefresh()

    /////////////////////////////////////////////////////
    // suivi du type de reload pour le dossier courant //
    /////////////////////////////////////////////////////
    val reloadType: StateFlow<ReloadType>
    fun setReloadType(type: ReloadType)

    fun manuallyInvalidateItems()
    val currentPath: Flow<String?>

    val waitingForItems: StateFlow<Boolean>
    fun setWaitingForItems(value: Boolean)


}