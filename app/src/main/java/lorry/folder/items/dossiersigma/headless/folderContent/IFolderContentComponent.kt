package lorry.folder.items.dossiersigma.headless.folderContent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.UI.sigma.SortingCriterion
import java.util.UUID

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
//    val refreshReloadTrigger: MutableStateFlow<Int>
    fun reloadCurrentFolderByRefresh2()

    /////////////////////////////////////////////////////
    // suivi du type de reload pour le dossier courant //
    /////////////////////////////////////////////////////
    val reloadType: StateFlow<Pair<ReloadType, UUID>>
    fun setReloadType(type: ReloadType)

    fun manuallyInvalidateItems()
    val currentPath: Flow<String?>

    val fastPath: StateFlow<String?>
    fun setFastPath(path: String?)

    val waitingForItems: StateFlow<Boolean>
    fun setWaitingForItems(value: Boolean)


}