package lorry.folder.items.dossiersigma.headless.folderContent

import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

class FolderContentComponent @Inject constructor(
    val diskRepository: IDiskRepository,
    val bottomTools: BottomTools
) : IFolderContentComponent {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    ////////////////////////
    // cache des dossiers //
    ////////////////////////

    //* ce cache est modifié par currentFolderFlow après écriture sur disque
    // si différence de freshness du cache et réelle détectée
    private val _folderCacheFlow = MutableStateFlow<Map<String, FolderCacheEntry>>(emptyMap())
    override val folderCacheFlow = _folderCacheFlow.asStateFlow()

    //////////////////////////
    // historique des paths //
    //////////////////////////
    private val _folderPathHistory = MutableStateFlow<List<String>>(emptyList())
    override val folderPathHistory = _folderPathHistory.asStateFlow()

    override fun addFolderPathToHistory(folderPath: String) {
        val currentHistory = _folderPathHistory.value
        _folderPathHistory.value = currentHistory + folderPath
    }

    override fun removeLastFolderPathHistory() {
        val currentHistory = _folderPathHistory.value
        _folderPathHistory.value = currentHistory.dropLast(1)
    }

    ///////////////////
    // reload manuel //
    ///////////////////
    override val reloadTrigger = MutableStateFlow(0)

    override fun refreshCurrentFolder() {
        reloadTrigger.value = reloadTrigger.value + 1 // redéclenchement immédiat
    }

    //////////////////////////
    // dossier courant trié //
    //////////////////////////
    override val currentFolderFlow = combine(
        folderPathHistory,
        reloadTrigger,
        bottomTools.currentFlagId,
    ) { folderPathHistory, reloadTrigger, flagId ->
        Pair(folderPathHistory.lastOrNull(), flagId)
    }.flatMapLatest { (latestPath, flagId) ->

        if (latestPath == null)
            return@flatMapLatest flowOf<SigmaFolder?>(null)

        val cachedFolderFreshness = folderCacheFlow.value[latestPath]?.freshness
        val realFolderFreshness = diskRepository.getFolderFreshness(latestPath)
        val folderCache = folderCacheFlow.value
        //le cache possède le tri actuel
        //le tri si doit être modifié est une modification du cache
        val sort = folderCache[latestPath]?.sort ?: SortingCriterion.ByDateDesc

        val inclusion = folderCache.containsKey(latestPath)
        val equality = cachedFolderFreshness?.isSameAs(realFolderFreshness) == true


        if (inclusion && equality) {
            val currentCachedFolder = folderCache[latestPath]
            flowOf(currentCachedFolder?.folder)
        } else {
            //récupération avec tri
            diskRepository.getFolderItemsLiteFlow(latestPath, sort)
                .runningFold(emptyList<Item>()) { acc, it -> acc + it }
                .flowOn(Dispatchers.IO)
                //stockage dans cache
                .onEach { items ->
                    val realFresh = diskRepository.getFolderFreshness(latestPath)
                    val folder = SigmaFolder.ofItemsAndPath(
                        items = items,
                        path = latestPath
                    )
                    val fc = FolderCacheEntry(folder, sort, realFresh)
                    _folderCacheFlow.value = folderCache.toMutableMap()
                        .apply { put(latestPath, fc) }
                }
                //filtrage
                .map { items ->
                    val filtered =
                        if (flagId == null) items else items.filter { it.tag?.id == flagId }
                    filtered
                }
                //incorporation dans SigmaFolder
                .map { items ->
                    SigmaFolder.ofItemsAndPath(
                        items = items,
                        path = latestPath
                    )
                }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    ///////////////////
    // tri des items //
    ///////////////////
    override val sorting = combine(
        folderCacheFlow,
        currentFolderFlow
    ) { folderCache, currentFolder ->
        folderCache[currentFolder?.fullPath ?: ""]?.sort ?: SortingCriterion.ByDateDesc
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = SortingCriterion.ByDateDesc
    )

    /**
     * Modifie le cache de tri du dossier courant
     */
    override fun setSorting(sorting: SortingCriterion) {
        val fullPath = currentFolderFlow.value?.fullPath
        if (fullPath == null)
            return

        val currentCache = folderCacheFlow.value[fullPath]
        if (currentCache == null)
            return

        val newCache = currentCache.copy(
            sort = sorting,
            cachedAt = System.currentTimeMillis()
        )

        _folderCacheFlow.value = folderCacheFlow.value.toMutableMap().apply {
            put(fullPath, newCache)
        }
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)