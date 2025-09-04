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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
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

    private val _waitingForItems = MutableStateFlow(false)
    override val waitingForItems = _waitingForItems.asStateFlow()

    override fun setWaitingForItems(value: Boolean) {
        _waitingForItems.update { value }
    }

    val dao = FolderCacheEntryDB.get(context)
    override val currentPath = folderPathHistory.map { it.lastOrNull() }
        .distinctUntilChanged()

    val currentDatabaseFolderCacheEntry: Flow<DbState> =
        currentPath
            .filterNotNull()                 // pas de requête pour path = null
            .distinctUntilChanged()
            .onEach { Log.d("PathCrspd", "READ  path=$it") }
            .flatMapLatest { path ->
                dao.folderCacheEntryRepository()
                    .getFlowByPath(path)
                    .map { it?.let { DbState.Data(it)} ?: DbState.NotFound }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(DbState.Loading) }
                    .distinctUntilChanged()
                    .onEach { Log.d("PathCrspd", "Room emit: $it for $path") }
            }

    val _savedPath = MutableStateFlow<String?>(null)

    //////////////////////////
    // dossier courant trié //
    //////////////////////////
    //on pourrait se dire: si le path n'a pas changé
    //peut-être que le contenu non plus et combinée par flag/manuel/mémo ...
    //d'où ne pas tout recharger, même pas depuis room

    private val paramsFlow: Flow<Params> =
        combineWithSource5(
            scope = scope,
            f1 = folderPathHistory,
            f2 = bottomTools.currentFlagId,
            f3 = currentDatabaseFolderCacheEntry,
            f4 = refreshReloadTrigger,
            f5 = reloadTrigger,
        ) { pathHistory, flagId, savedEntry, _, _ ->
            Triple(pathHistory.lastOrNull(), flagId, savedEntry)
        }.map { (origin, triple) ->
            val (latestPath, flagId, savedEntry) = triple

            Params(
                origin = origin,
                latestPath = latestPath,
                flagId = flagId,
                savedEntry = savedEntry
            )
        }.distinctUntilChanged { a, b ->
            a.latestPath == b.latestPath &&
                    a.flagId == b.flagId &&
                    a.savedEntry == b.savedEntry &&
                    a.origin == b.origin
        }
    //a.savedEntry?.freshness == b.savedEntry?.freshness &&

    private fun folderContentFlow(params: Params): Flow<SigmaFolder?> = flow {
        val (origin, latestPath, flagId, savedEntry) = params
        // 1) Placeholder immédiat (vide l’écran)
        emit(null)

        Log.d("fldDec", "origin:$origin, latestPath:$latestPath, savedEntry:$savedEntry")

        if (latestPath == null) return@flow

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
        val cacheEquality = cachedFolderFreshness?.isSameAs(diskFresh) == true

        val roomOk = savedEntry?.let { it.path == latestPath && it.freshness.isSameAs(diskFresh) } == true

        // 3) Sélection de la source (cache-first si pas plus vieux)
        val decision = when {
            origin != Origin.REFRESH_RELOAD_TRIGGER && roomOk -> ReloadType.Room
            origin != Origin.REFRESH_RELOAD_TRIGGER && cacheInclusion && cacheEquality -> ReloadType.Cache
            else -> ReloadType.Disk
        }

        Log.d("fldDec", "   -> decision:$decision")


        // 4) Émettre la source choisie
        when (decision) {
            ReloadType.Cache -> {
                val oldItems = cacheEntry!!.folder.items
                val newItems = when (sort) {
                    SortingCriterion.ByNameAsc ->
                        oldItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenBy { it.name.lowercase(java.util.Locale.getDefault()) })
                    SortingCriterion.ByDateDesc ->
                        oldItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenByDescending { it.modificationDate })
                }.let { items -> if (params.flagId == null) items else items.filter { it.tag?.id == params.flagId } }

                Log.d("fldDec", "   -> fin calcul du cache et émission du flow")
                emit(cacheEntry.folder.copy(items = newItems))
                setReloadType(ReloadType.Cache)
            }

            ReloadType.Room -> {
                // Exemple : re-trier/mapper à partir de savedEntry (rapide)
                val serviceFolder = savedEntry!!.folder
                val serviceItems = serviceFolder.items
                val newItems = when (sort) {
                    SortingCriterion.ByNameAsc ->
                        serviceItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenBy { it.name.lowercase(java.util.Locale.getDefault()) })
                    SortingCriterion.ByDateDesc ->
                        serviceItems.sortedWith(compareBy<Item> { it.isFile() }
                            .thenByDescending { it.modificationDate })
                }.let { items -> if (params.flagId == null) items else items.filter { it.tag?.id == params.flagId } }

                Log.d("fldDec", "   -> fin calcul de room et émission du flow")
                emit(serviceFolder.copy(items = newItems))
                setReloadType(ReloadType.Room)
            }

            ReloadType.Disk -> {
                setReloadType(ReloadType.Disk)
                emitAll(
                    diskRepository.getFolderItemsLiteFlow(latestPath, sort)
                        .runningFold(emptyList<Item>()) { acc, it -> acc + it }
                        .flowOn(Dispatchers.IO)
                        .onStart {
                            Log.d("fldDec", "   -> début émission flow issu du disque")
                        }
                        .onEach { items ->
                            val realFresh = diskRepository.getFolderFreshness(latestPath)
                            val folder = SigmaFolder.ofItemsAndPersistedSigmaFolder(items, latestPath)
                            val fc = FolderCacheEntry(
                                folder = folder,
                                sort = sort,
                                freshness = realFresh,
                                path = folder.fullPath)
                            _folderCacheFlow.value = folderCache.toMutableMap().apply { put(latestPath, fc) }
                        }
                        .map { items ->
                            val filtered = if (flagId == null) items else items.filter { it.tag?.id == flagId }
                            SigmaFolder.ofItemsAndPersistedSigmaFolder(filtered, latestPath)
                        }
                        .onCompletion {
                            Log.d("fldDec", "   -> fin émission flow issu du disque")
                        }
                )
            }

            else -> {
                Log.d("fldDec", "   -> émission valeurs DUMMY + ReloadType.NONE ???")
                emit(SigmaFolder.DUMMY)
                setReloadType(ReloadType.NONE)
            }
        }

        Log.d("fldDec", "-------------------------------------------")
    }

    val _manualInvalidateFlow = MutableStateFlow(UUID.randomUUID())

    override fun manuallyInvalidateItems(){
        _manualInvalidateFlow.update { UUID.randomUUID() }
    }

    override val currentFolderFlow: StateFlow<SigmaFolder?> =
        paramsFlow
            .flatMapLatest { p ->
                folderContentFlow(p)
            } // placeholder émis dedans
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

//    override val currentFolderFlow: StateFlow<SigmaFolder?> =
//        combineWithSource2(
//            scope,
//            paramsFlow,
//            _manualInvalidateFlow
//        ){ params, _ ->
//            params
//        }.flatMapLatest { (origin, p) ->
//            if (origin == Origin2.MANUAL_INVALIDATE)
//                flowOf(SigmaFolder.DUMMY)
//            else
//                folderContentFlow(p)
//        } // placeholder émis dedans
//            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

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

    val _reloadType = MutableStateFlow(ReloadType.NONE)
    override val reloadType = _reloadType.asStateFlow()

    override fun setReloadType(type: ReloadType) {
        _reloadType.update { type }
    }
}

enum class ReloadType {
    NONE,
    Cache,
    Room,
    Disk,
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
    val savedEntry: DbState
)

enum class Origin2{
    MANUAL_INVALIDATE,
    PARAMS
}

enum class Origin {
    FOLDER_PATH_HISTORY,
    CURRENT_FLAG_ID,
    REFRESH_RELOAD_TRIGGER,
    RELOAD_TRIGGER,
    SAVED_ENTRY,
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

fun <A, B, C, D, E, R> combineWithSource5(
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

sealed class DbState {
    object Loading: DbState()
    object NotFound: DbState()
    data class Data(val folderEntry: FolderCacheEntry): DbState()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (javaClass == Loading || javaClass == NotFound) return true
        return (this as Data).folderEntry.freshness == (other as Data).folderEntry.freshness
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String = when(this) {
        Loading -> "Loading"
        NotFound -> "NotFound"
        is Data -> "(Data, path=${folderEntry.path.substringAfterLast("/")})"
    }
}