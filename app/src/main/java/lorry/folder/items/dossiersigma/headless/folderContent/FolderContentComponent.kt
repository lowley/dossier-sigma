package lorry.folder.items.dossiersigma.headless.folderContent

import android.content.Context
import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.favoriteObservation.external.FolderCacheEntryDB
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion

class FolderContentComponent @Inject constructor(
    val diskRepository: IDiskRepository,
    val bottomTools: BottomTools,
    val settingsManager: SettingsManager,
    val context: Context
) : IFolderContentComponent {

    companion object{
        val TAG = "FoldCmp"
    }

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

    override fun reloadCurrentFolder() {
        reloadTrigger.value = reloadTrigger.value + 1 // redéclenchement immédiat
    }

    ////////////////////////
    // reload par refresh //
    ////////////////////////
    override val refreshReloadTrigger = MutableStateFlow(0)

    override fun reloadCurrentFolderByRefresh() {
        refreshReloadTrigger.value = refreshReloadTrigger.value + 1 // redéclenchement immédiat
    }

    enum class Origin {
        FOLDER_PATH_HISTORY,
        CURRENT_FLAG_ID,
        REFRESH_RELOAD_TRIGGER,
        RELOAD_TRIGGER,
        SAVED_ENTRY,
    }

    fun <A, B, C, D, E, R> combineWithSource(
        scope: CoroutineScope,
        f1: Flow<A>,
        f2: Flow<B>,
        f3: Flow<C>,
        f4: Flow<D>,
        f5: Flow<E>,
        transform: (A, B, C, D, E) -> R
    ): Flow<Pair<Origin, R>> {

        // 1) Partager les sources si besoin (saute ceci si ce sont déjà des StateFlow/SharedFlow)
        val s1 = f1.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
        val s2 = f2.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
        val s3 = f3.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
        val s4 = f4.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
        val s5 = f5.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)

        // 2) Flux des déclencheurs (qui a émis ?)
        val sourceIdFlow = merge(
            s1.map { Origin.FOLDER_PATH_HISTORY },
            s2.map { Origin.CURRENT_FLAG_ID },
            s3.filterNotNull().map { Origin.SAVED_ENTRY },
            s4.map { Origin.REFRESH_RELOAD_TRIGGER },
            s5.map { Origin.RELOAD_TRIGGER },
        )

        // 3) Valeur combinée
        val combined = combine(s1, s2, s3, s4, s5) { a, b, c, d, e -> transform(a, b, c, d, e) }

        // 4) Recolle déclencheur + résultat recalculé (une paire par déclenchement)
        return sourceIdFlow.zip(combined) { src, value -> src to value }
    }

    val dao = FolderCacheEntryDB.get(context)
    val currentPath = folderPathHistory.map { it.lastOrNull() }
        .distinctUntilChanged()

    val currentDatabaseFolderCacheEntry: Flow<FolderCacheEntry?> =
        currentPath
            .filterNotNull()                 // pas de requête pour path = null
            .distinctUntilChanged()
            .onEach { Log.d("PathCrspd", "READ  path=$it") }
            .flatMapLatest { path ->
                dao.folderCacheEntryRepository()
                    .getFlowByPath(path)
                    .distinctUntilChanged()
                    .onEach { Log.d("PathCrspd", "Room emit: $it for $path") }
            }
            .shareIn(scope, SharingStarted.Eagerly, replay = 1) // optionnel si tu veux rejouer la dernière

    //////////////////////////
    // dossier courant trié //
    //////////////////////////
    //on pourrait se dire: si le path n'a pas changé
    //peut-être que le contenu non plus et combinée par flag/manuel/mémo ...
    //d'où ne pas tout recharger, même pas depuis room
    override val currentFolderFlow = combineWithSource(
        scope = scope,
        f1 = folderPathHistory,
        f2 = bottomTools.currentFlagId,
        f3 = currentDatabaseFolderCacheEntry,
        f4 = refreshReloadTrigger,
        f5 = reloadTrigger,
    ) { pathHistory, flagId, savedEntry, _, _ ->
        Triple(pathHistory.lastOrNull(), flagId, savedEntry)
    }.flatMapLatest { (origin, triple) ->

        val (latestPath, flagId, savedEntry) = triple

        if (latestPath == null)
            return@flatMapLatest flowOf<SigmaFolder?>(null)

        val cachedFolderFreshness = folderCacheFlow.value[latestPath]?.freshness
        val realFolderFreshness = diskRepository.getFolderFreshness(latestPath)
        val folderCache = folderCacheFlow.value
        //le cache possède le tri actuel
        //le tri si doit être modifié est une modification du cache
        val sort = folderCache[latestPath]?.sort ?: SortingCriterion.ByDateDesc

        //si c'est un refresh du dossier initié par un pullToRefresh
        //on skippe le cache pour forcer un reload à partir du disque

        val cacheInclusion = folderCache.containsKey(latestPath)
        val cacheEquality = cachedFolderFreshness?.isSameAs(realFolderFreshness) == true

        val favorites = settingsManager.homeItemsFlow.first()
        val favoriteInclusion = favorites.any { it.path == latestPath }

        val error = savedEntry != null && savedEntry.path != latestPath
        val roomOk = savedEntry?.let { it.path == latestPath && it.freshness.isSameAs(realFolderFreshness) } == true

        // si favori et non refresh manuel
        // && favoriteInclusion
        if (origin != Origin.REFRESH_RELOAD_TRIGGER && roomOk) {

            val dao = FolderCacheEntryDB.get(context)
//            val serviceEntry = dao.getByPath(latestPath, scope, context)
            val serviceFreshness = savedEntry.freshness
//            val cachedFolderFreshness = folderCacheFlow.value[latestPath]?.freshness

            //on reprend les items du service car freshness identiques
            if (serviceFreshness?.isSameAs(realFolderFreshness) == true
                && savedEntry != null) {
                val serviceFolder = savedEntry.folder
                val serviceItems = serviceFolder.items

                val newItems = when (sort) {
                    SortingCriterion.ByNameAsc ->
                        serviceItems.sortedWith(
                            compareBy<Item> { it.isFile() }
                                .thenBy { it.name.lowercase(java.util.Locale.getDefault()) }
                        )

                    SortingCriterion.ByDateDesc ->
                        serviceItems.sortedWith(
                            compareBy<Item> { it.isFile() }
                                .thenByDescending { it.modificationDate }
                        )
                }

                val newFolder = serviceFolder?.copy(
                    items = newItems ?: emptyList()
                )

                //vient de room
                return@flatMapLatest flowOf(newFolder)
            } else {
                //le déroulement naturel de la méthode
                //entraîne une lecture du disque
            }
        }

        //dans le cache et non refresh manuel
        if (origin != Origin.REFRESH_RELOAD_TRIGGER && cacheInclusion && cacheEquality) {
            val currentCachedFolder = folderCache[latestPath]
            val oldFolder = currentCachedFolder?.folder
            val oldItems = oldFolder?.items ?: emptyList()

            val newItems = when (sort) {
                SortingCriterion.ByNameAsc ->
                    oldItems.sortedWith(
                        compareBy<Item> { it.isFile() }
                            .thenBy { it.name.lowercase(java.util.Locale.getDefault()) }
                    )

                SortingCriterion.ByDateDesc ->
                    oldItems.sortedWith(
                        compareBy<Item> { it.isFile() }
                            .thenByDescending { it.modificationDate }
                    )
            }

            val newFolder = oldFolder?.copy(
                items = newItems ?: emptyList()
            )

            //vient du cache
            return@flatMapLatest flowOf(newFolder)
        }

        //les autres cas: refresh manuel ou pas de cache
        //récupération avec tri

        //vient du disque (rechargement par le disque, lent)
        return@flatMapLatest diskRepository.getFolderItemsLiteFlow(latestPath, sort)
            .runningFold(emptyList<Item>()) { acc, it -> acc + it }
            .flowOn(Dispatchers.IO)
            //stockage dans cache
            .onEach { items ->
                val realFresh = diskRepository.getFolderFreshness(latestPath)
                val folder = SigmaFolder.ofItemsAndPersistedSigmaFolder(
                    items = items,
                    fullPath = latestPath,
                )
                val fc = FolderCacheEntry(folder = folder, sort = sort, freshness = realFresh, path = folder.fullPath)
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
                SigmaFolder.ofItemsAndPersistedSigmaFolder(
                    items = items,
                    fullPath = latestPath,
                )
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