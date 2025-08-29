package lorry.folder.items.dossiersigma.headless.folderContent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

interface IFolderContentComponent{

    val sorting: StateFlow<SortingCriterion>
    fun setSorting(sorting: SortingCriterion)
    fun refreshCurrentFolder()

    val folderPathHistory: StateFlow<List<String>>

    val folderCacheFlow: StateFlow<Map<String, FolderCacheEntry>>
    val reloadTrigger: MutableStateFlow<Int>
    val currentFolderFlow: StateFlow<SigmaFolder?>

    fun addFolderPathToHistory(folderPath: String)
    fun removeLastFolderPathHistory()


}