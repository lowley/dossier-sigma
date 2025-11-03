package lorry.folder.items.dossiersigma.headless.folderContentBack

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.basics.domain.SigmaFolder
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.folderContentBack.utils.FolderCacheEntry
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.util.UUID

interface IFolderContentBackComponent{

    val sorting: StateFlow<SortingCriterion>
    fun setSorting(sorting: SortingCriterion)

    val folderPathHistory: StateFlow<List<SigmaPath>>
    fun addFolderPathToHistory(folderPath: SigmaPath)
    fun removeLastFolderPathHistory()
    fun removeNElementsFromHistory(n: Int)
    val folderCacheFlow: StateFlow<Map<SigmaPath, FolderCacheEntry>>
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
    val currentPath: Flow<SigmaPath?>

    val fastPath: StateFlow<SigmaPath?>
    fun setFastPath(path: SigmaPath?)
}