package lorry.folder.items.dossiersigma.headless.folderContent

import android.content.Context
import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.Item
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.favoriteObservation.external.FolderCacheEntryDB
import lorry.folder.items.dossiersigma.ui.bottomArea.BottomTools
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.util.UUID
import javax.inject.Singleton

@Singleton
class FolderContentComponent @Inject constructor(
    val diskRepository: IDiskRepository,
    val bottomTools: BottomTools,
    val settingsManager: SettingsManager,
    val context: Context
) : IFolderContentComponent {

    companion object {
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

    private val _fastPath = MutableStateFlow<String?>(null)
    override val fastPath: StateFlow<String?> = _fastPath
    override fun setFastPath(path: String?) {
        _fastPath.value = path
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
    private val refreshReloadTrigger2 = MutableStateFlow(0)

    override fun reloadCurrentFolderByRefresh2() {
        Log.d("badam", "reloadCurrentFolderByRefresh() called", Throwable())
        refreshReloadTrigger2.value = refreshReloadTrigger2.value + 1 // redéclenchement immédiat
    }

    private val _waitingForItems = MutableStateFlow(false)
    override val waitingForItems = _waitingForItems.asStateFlow()

    override fun setWaitingForItems(value: Boolean) {
        _waitingForItems.update { value }
    }

    val dao = FolderCacheEntryDB.get(context)
    override val currentPath = folderPathHistory.map { it.lastOrNull() }
        .distinctUntilChanged()

    private fun currentDatabaseFolderCacheEntryFor(path: String): Flow<DbState> =
        dao.folderCacheEntryRepository()
            .getFlowByPath(path) // Flow<FolderCacheEntry?>
            .map { entry ->
                entry?.let { DbState.Data(it) } ?: DbState.NotFound
            }
            .onStart {
//                Log.d("PathCrspd", "lecture room de path=${path.substringAfterLast("/")}...")
                emit(DbState.Loading)
            }
            .distinctUntilChanged()

    val _savedPath = MutableStateFlow<String?>(null)

    //////////////////////////
    // dossier courant trié //
    //////////////////////////
    private fun folderContentFlow(params: Params): Flow<SigmaFolder?> = flow {
        val (origin, latestPath, flagId, savedEntry, sorting) = params
        // 1) Placeholder immédiat (vide l’écran)
//        emit(null)

        Log.d(
            "fldDec",
            "origin:$origin, latestPath:$latestPath, savedEntry:$savedEntry, flagId:$flagId, sorting:$sorting"
        )

        if (latestPath == null) return@flow

        var decisionMade = false
        var decision: ReloadType = ReloadType.NONE

        if (origin == Origin.SORTING || origin == Origin.CURRENT_FLAG_ID) {
            decisionMade = true
            decision = ReloadType.Cache
        }

        if (origin == Origin.REFRESH_RELOAD_TRIGGER){
            decisionMade = true
            decision = ReloadType.Disk
        }

        if (savedEntry == DbState.Loading) {
            Log.d("fldDec", "   -> savedEntry LOADING -> on passe")
            return@flow
        }

        if (!decisionMade) {

            // 2) Récupérer fraîcheurs APRÈS le placeholder (concurrence structurée)
            val diskFresh = coroutineScope {
                async(Dispatchers.IO) { diskRepository.getFolderFreshness(latestPath) }
                    .await()
            }

            val folderCache = folderCacheFlow.value
            val cacheEntry = folderCache[latestPath]
            val cachedFolderFreshness = cacheEntry?.freshness
            val sort = cacheEntry?.sort ?: SortingCriterion.ByDateDesc

            val cacheInclusion = cacheEntry != null
            val cacheAndDiskEquality = cachedFolderFreshness?.isSameAs(diskFresh) == true

            val roomOk = savedEntry.let {
                it is DbState.Data && it.folderEntry.path == latestPath && it.folderEntry.freshness.isSameAs(
                    diskFresh
                )
            } == true

            val cacheAndRoomEquality = cacheInclusion && savedEntry.let {
                it is DbState.Data && it.folderEntry.path == latestPath && it.folderEntry.freshness.isSameAs(
                    cachedFolderFreshness
                )
            } == true

            Log.d(
                "fldDec",
                "   folderContentFlow -> ELEMENTS DECISION: diskFreshness:${diskFresh.hashCode()}, roomOk:$roomOk, cache pour ce path?:$cacheInclusion, cache Freshness:${cachedFolderFreshness.hashCode()}"
            )

            // 3) Sélection de la source (cache-first si pas plus vieux)
            decision = when {
                cacheAndRoomEquality -> ReloadType.Cache
                roomOk -> ReloadType.Room
                cacheInclusion && cacheAndDiskEquality -> ReloadType.Cache
                else -> ReloadType.Disk
            }
        }

        Log.d("fldDec", "   -> decision:$decision")

        // 4) Émettre la source choisie
        when (decision) {
            ReloadType.Cache -> {
                val folderCache = folderCacheFlow.value
                val cacheEntry = folderCache[latestPath]
                val cachedFolderFreshness = cacheEntry?.freshness
                val sort = cacheEntry?.sort ?: SortingCriterion.ByDateDesc

                val oldItems = cacheEntry!!.folder.items
                val newItems = when (sort) {
                    SortingCriterion.ByNameAsc ->
                        oldItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenBy { it.name.lowercase(java.util.Locale.getDefault()) })

                    SortingCriterion.ByDateDesc ->
                        oldItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenByDescending { it.modificationDate })
                }.let { items -> if (flagId == null) items else items.filter { it.tag?.id == flagId } }

                Log.d(
                    "fldDec",
                    "   folderContentFlow -> fin calcul du cache et émission du flow. items traités(${newItems.size}), flagId:$flagId"
                )
                emit(cacheEntry.folder.copy(items = newItems))
                setReloadType(ReloadType.Cache)
            }

            ReloadType.Room -> {
                val folderCache = folderCacheFlow.value
                val cacheEntry = folderCache[latestPath]
                val cachedFolderFreshness = cacheEntry?.freshness
                val sort = cacheEntry?.sort ?: SortingCriterion.ByDateDesc

                // Exemple : re-trier/mapper à partir de savedEntry (rapide)
                val folderEntry = (savedEntry as DbState.Data).folderEntry
                val serviceFolder = folderEntry.folder
                val serviceItems = serviceFolder.items
                _folderCacheFlow.update { it.toMutableMap().apply { put(latestPath, folderEntry) } }

                val newItems = when (sort) {
                    SortingCriterion.ByNameAsc ->
                        serviceItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenBy { it.name.lowercase(java.util.Locale.getDefault()) })

                    SortingCriterion.ByDateDesc ->
                        serviceItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenByDescending { it.modificationDate })
                }.let { items -> if (flagId == null) items else items.filter { it.tag?.id == flagId } }

                Log.d(
                    "fldDec",
                    "   folderContentFlow -> fin calcul de room et émission du flow. items traités(${newItems.size}), flagId:$flagId, freshness 1:${
                        serviceFolder.computeFreshness().hashCode()
                    }, freshness 2:${folderEntry.freshness.hashCode()}"
                )
                emit(serviceFolder.copy(items = newItems))
                setReloadType(ReloadType.Room)
            }

            ReloadType.Disk -> {
                val folderCache = folderCacheFlow.value
                val cacheEntry = folderCache[latestPath]
                val cachedFolderFreshness = cacheEntry?.freshness
                val sort = cacheEntry?.sort ?: SortingCriterion.ByDateDesc

                setReloadType(ReloadType.Disk)
                emitAll(
                    flow {
                        var lastItems: List<Item> = emptyList()

                        diskRepository.getFolderItemsLiteFlow(latestPath, sort)
                            .runningFold(emptyList<Item>()) { acc, it -> acc + it }
                            .flowOn(Dispatchers.IO)
                            .onStart {
                                Log.d(
                                    "fldDec",
                                    "   folderContentFlow -> début émission flow issu du disque"
                                )
                            }
                            .onEach { items -> lastItems = items }
                            .onCompletion { cause ->
                                val realFresh = diskRepository.getFolderFreshness(latestPath)
                                val folder = SigmaFolder.ofItemsAndPersistedSigmaFolder(
                                    lastItems,
                                    latestPath
                                )
                                val fc = FolderCacheEntry(
                                    folder = folder,
                                    sort = sort,
                                    freshness = realFresh,
                                    path = folder.fullPath
                                )

                                if (cause == null) {
                                    // FIN NORMALE -> MAJ cache ATOMIQUE

                                    val snapshot = folderCacheFlow.value.toMutableMap()
                                        .apply { put(latestPath, fc) }
                                    _folderCacheFlow.value = snapshot
                                } else {
                                    Log.d(
                                        "fldDec",
                                        "   folderContentFlow -> fin disque interrompue (cause=$cause) : cache intact"
                                    )
                                }
                                Log.d(
                                    "fldDec",
                                    "   -> fin émission flow issu du disque. items envoyés: ${lastItems.size}. freshness:${realFresh.hashCode()}"
                                )
                            }
                            .map { items ->
                                val filtered =
                                    if (flagId == null) items else items.filter { it.tag?.id == flagId }
                                SigmaFolder.ofItemsAndPersistedSigmaFolder(filtered, latestPath)
                            }
                            .collect { emit(it) }
                    }
                )
            }

            else -> {
//                Log.d("fldDec", "   -> émission valeurs DUMMY + ReloadType.NONE ???")
//                emit(SigmaFolder.DUMMY)
//                setReloadType(ReloadType.NONE)
            }
        }

        Log.d("fldDec", "-------------------------------------------")
    }

    val _manualInvalidateFlow = MutableStateFlow(UUID.randomUUID())

    override fun manuallyInvalidateItems() {
        _manualInvalidateFlow.update { UUID.randomUUID() }
    }

    override val currentFolderFlow: StateFlow<SigmaFolder?> =
        currentPath
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { path ->
                val dbFlow = currentDatabaseFolderCacheEntryFor(path)
                    .onEach { Log.d("PathCrspd", "Room emit: $it for $path") }

                // reconstruit Params *dans* le scope du path
                val paramsFlowForPath: Flow<Params> =
                    combineWithSource5(
                        scope = scope,
                        currentFlagId = bottomTools.currentFlagId,
                        savedEntry = dbFlow,
                        refreshReloadTrigger = refreshReloadTrigger2,
                        reloadTrigger = reloadTrigger,
                        sorting = sorting
                    ) { flagId, savedEntry, _, _, sorting ->
                        Params(
                            origin = Origin.CURRENT_FLAG_ID, // placeholder, sera écrasé par pair.first
                            latestPath = path,
                            flagId = flagId,
                            savedEntry = savedEntry,
                            sorting = sorting
                        )
                    }
                        .map { (origin, params) -> params.copy(origin = origin) }
                        .distinctUntilChanged { a, b ->
                            a.latestPath == b.latestPath &&
                                    a.flagId == b.flagId &&
                                    a.savedEntry == b.savedEntry &&
                                    a.origin == b.origin &&
                                    a.sorting == b.sorting
                        }

                // pour ce path, ne consomme que les flows du path
                paramsFlowForPath.flatMapLatest { p -> folderContentFlow(p) }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

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

    val _reloadType = MutableStateFlow(ReloadType.NONE to UUID.randomUUID())
    override val reloadType = _reloadType.asStateFlow()

    override fun setReloadType(type: ReloadType) {
        _reloadType.update { type to UUID.randomUUID() }
    }
}

enum class ReloadType {
    NONE,
    Cache,
    Room,
    Disk,
    FlagId,
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class Params(
    val origin: Origin,
    val latestPath: String?,
    val flagId: UUID?,
    val savedEntry: DbState,
    val sorting: SortingCriterion
)

enum class Origin2 {
    MANUAL_INVALIDATE,
    PARAMS
}

enum class Origin {
    FOLDER_PATH_HISTORY,
    CURRENT_FLAG_ID,
    REFRESH_RELOAD_TRIGGER,
    RELOAD_TRIGGER,
    SAVED_ENTRY,
    SORTING
}

fun <A, B, R> combineWithSource2(
    scope: CoroutineScope,
    f1: Flow<A>,
    f2: Flow<B>,
    transform: (A, B) -> R
): Flow<Pair<Origin2, R>> {

    // 1) Partager les sources si besoin (saute ceci si ce sont déjà des StateFlow/SharedFlow)
    val s1 = f1.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
    val s2 = f2.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)

    // 2) Flux des déclencheurs (qui a émis ?)
    val sourceIdFlow = merge(
        s1.map { Origin2.PARAMS },
        s2.map { Origin2.MANUAL_INVALIDATE },
    )

    // 3) Valeur combinée
    val combined = combine(s1, s2) { a, b -> transform(a, b) }

    // 4) Recolle déclencheur + résultat recalculé (une paire par déclenchement)
    return sourceIdFlow.zip(combined) { src, value -> src to value }
}

private fun <T> Flow<T>.shared(scope: CoroutineScope): SharedFlow<T> =
    this.shareIn(scope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

// Utilitaire: lire la dernière valeur disponible (via replay=1)
private suspend fun <T> SharedFlow<T>.latest(): T = first()

// Si tu veux un "latestOrNull" pour éviter le blocage :
private suspend fun <T> SharedFlow<T>.latestOrNull(): T? = firstOrNull()

private fun <A, B, C, D, E, R> combineWithSource5(
    scope: CoroutineScope,
    currentFlagId: Flow<A>,        // f1
    savedEntry: Flow<B>,           // f2 (DbState)
    refreshReloadTrigger: Flow<C>, // f3
    reloadTrigger: Flow<D>,        // f4
    sorting: Flow<E>,
    transform: (A, B, C, D, E) -> R
): Flow<Pair<Origin, R>> {

    // 1) Partager chaque source (replay=1 pour "latest()" immédiat)
    val flagS = currentFlagId.shared(scope)
    val savedS0 = savedEntry.shared(scope)
    val refS = refreshReloadTrigger.shared(scope)
    val relS = reloadTrigger.shared(scope)
    val sortS = sorting.shared(scope)

    // 2) Filtre commun pour SAVED_ENTRY: on écarte Loading
//    val savedIsNotLoading: (Any?) -> Boolean = { b ->
//        (b as? DbState)?.let { it !is DbState.Loading } ?: true
//    }

    // "savedS" servira au DECLENCHEMENT et à la LECTURE (même filtre des deux côtés)
    @Suppress("UNCHECKED_CAST")
//    val savedS = savedS0.filter { savedIsNotLoading(it) } as SharedFlow<B>

    // 3) Évènements d’origine (taggés par enum, pas par position)
    val originEvents: Flow<Origin> = merge(
        flagS.map { Origin.CURRENT_FLAG_ID },
        savedS0.map { Origin.SAVED_ENTRY },
        // On ignore l'impulsion initiale côté ÉVÈNEMENT pour éviter un faux "refresh" au démarrage
        refS.drop(1).map { Origin.REFRESH_RELOAD_TRIGGER },
        relS.drop(1).map { Origin.RELOAD_TRIGGER },
        sortS.drop(1).map { Origin.SORTING },
    )

    // 4) À chaque évènement, on prélève à la demande les dernières valeurs
    //    (pas d'historique; lecture directe via latest()).
    //    NB: pour éviter tout blocage, on NE "droppe" pas côté lecture des triggers.
    return originEvents.flatMapLatest { origin ->
        combine(flagS, savedS0, refS, relS, sortS) { a, b, c, d, e ->
            origin to transform(a, b, c, d, e)
        }.take(1)
    }
}

//fun <A, B, C, D, E, R> combineWithSource5(
//    scope: CoroutineScope,
//    f1: Flow<A>,
//    f2: Flow<B>,
//    f3: Flow<C>,
//    f4: Flow<D>,
//    f5: Flow<E>,
//    transform: (A, B, C, D, E) -> R
//): Flow<Pair<Origin, R>> {
//
//    // 1) Partager les sources si besoin (saute ceci si ce sont déjà des StateFlow/SharedFlow)
//    val s1 = f1.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
//    val s2 = f2.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
//    val s3 = f3.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
//    val s4 = f4.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
//    val s5 = f5.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)
//
//    // 2) Flux des déclencheurs (qui a émis ?)
//    val sourceIdFlow = merge(
//        s1.map { Origin.FOLDER_PATH_HISTORY },
//        s2.map { Origin.CURRENT_FLAG_ID },
//        s3.filterNotNull().map { Origin.SAVED_ENTRY },
//        s4.map { Origin.REFRESH_RELOAD_TRIGGER },
//        s5.map { Origin.RELOAD_TRIGGER },
//    )
//
//    // 3) Valeur combinée
//    val combined = combine(s1, s2, s3, s4, s5) { a, b, c, d, e -> transform(a, b, c, d, e) }
//
//    // 4) Recolle déclencheur + résultat recalculé (une paire par déclenchement)
//    return sourceIdFlow.zip(combined) { src, value -> src to value }
//}

sealed class DbState {
    object Loading : DbState()
    object NotFound : DbState()
    data class Data(val folderEntry: FolderCacheEntry) : DbState()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (javaClass == Loading || javaClass == NotFound) return true
        return (this as Data).folderEntry.freshness == (other as Data).folderEntry.freshness
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String = when (this) {
        Loading -> "Loading"
        NotFound -> "NotFound"
        is Data -> "(Data, path=${folderEntry.path.substringAfterLast("/")})"
    }
}

//val events = merge(
//    s1.map { Origin.CURRENT_FLAG_ID },
//    s2f.map { Origin.SAVED_ENTRY },
//    s3.drop(1).map { Origin.REFRESH_RELOAD_TRIGGER },
//    s4.drop(1).map { Origin.RELOAD_TRIGGER },
//)
//
//// On “échantillonne” les dernières valeurs quand un event arrive
//return events.flatMapLatest { origin ->
//    combine(s1, s2f, s3, s4) { a, b, c, d -> origin to transform(a, b, c, d) }
//        .take(1)  // ← on ne veut que le snapshot au moment de l’event
//}