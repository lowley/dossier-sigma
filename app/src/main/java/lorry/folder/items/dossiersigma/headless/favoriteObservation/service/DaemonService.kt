package lorry.folder.items.dossiersigma.headless.favoriteObservation.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.protobuf.LazyStringArrayList
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import androidx.lifecycle.LifecycleService
import androidx.room.Transaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.yield
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.domain.lastSegment
import lorry.folder.items.dossiersigma.headless.domain.mapSigmaPaths
import lorry.folder.items.dossiersigma.headless.domain.str
import lorry.folder.items.dossiersigma.headless.domain.toSigmaPath
import lorry.folder.items.dossiersigma.headless.favoriteObservation.external.FolderCacheEntryDB
import lorry.folder.items.dossiersigma.headless.folderContentBack.utils.FolderCacheEntry
import lorry.folder.items.dossiersigma.headless.folderContentBack.IFolderContentBackComponent
import lorry.folder.items.dossiersigma.headless.folderContentBack.utils.FolderFreshness
import lorry.folder.items.dossiersigma.headless.usecases.homePage.HomeViewModel
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

private const val CHANNEL_ID = "daemon"

@AndroidEntryPoint
class DaemonService : LifecycleService() {

    companion object {
        val TAG = "filos"
    }

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var diskRepository: IDiskRepository

    @Inject
    lateinit var folderContentComponent: IFolderContentBackComponent

    @Inject
    lateinit var filesAccessibleCommunicator: FilesAccessibleChannel

    private var fileObserver: SigmaFileObserver? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var index: Int = 0

    private var currentNotificationColor: Color = Color.Blue
    private var latestNotificationMessage: String? = null

    private lateinit var dao: FolderCacheEntryDB

    override fun onCreate() {
        super.onCreate()

        val i = Intent(this, PermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(i)

        // c'est pour homeViewModel : les settings sont maintenant accessibles
        filesAccessibleCommunicator.activate()

        applicationContext.ensureDaemonChannel()
        dao = FolderCacheEntryDB.get(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        scope.launch(Dispatchers.IO) {

            val waitMs = 5000L  // ex. attente « normale »
            var waitForHomePage = true

            while (waitForHomePage) {
                // Course : soit le timer, soit le signal de Home
                val reason = merge(
                    flow { delay(waitMs); emit("timeout") },
                    HomeViewModel.homeReady.map { "signal" }
                ).first()

                // Reprendre tout de suite si c’est le signal
                if (reason == "signal") {
                    //homeItems déjà chargés, mais on laisse 1s de plus pour l'affichage
                    delay(800L)
                    waitForHomePage = false
                } else {
                    waitForHomePage = false
                }
            }

            settingsManager.saveIsFileObserverEnabled(true)

            // 1) Démarrer au premier plan rapidement
            startForeground(30215, buildOngoingNotification())

            // 2) Démarrer ta boucle “daemon”
            scope.launch {
                runDaemonLoop()
            }
        }
        // Conseil : START_STICKY pour relance automatique après kill système
        return START_STICKY
    }

    private suspend fun runDaemonLoop() {

        val favorites = settingsManager.homeItemsFlow.firstOrNull()
        val currentAppFolder = settingsManager.currentPathFlow.first()

        val favoriteDirs = (favorites?.map { it.path }
            ?: listOf(currentAppFolder)).filterNotNull()
        Log.d(TAG, "envoi initial de ${favoriteDirs.size} dossiers...")

        val dups = favoriteDirs.groupBy { it }.filterValues { it.size > 1 }
        Log.d(
            TAG,
            "paths=${favoriteDirs.size}, distinct=${favoriteDirs.distinct().size}, dups=${dups.keys}"
        )

        recomputeAndSaveAll(favoriteDirs)

//        for (path in favoriteDirs) {
//            computeAndSendFreshness(path, currentAppFolder, timeMeasurement = true)
//        }

        Log.d(TAG, "envoi initial terminé")
        updateNotification(color = Color.Black)

        fileObserver = SigmaFileObserver(
            file = File("/storage/emulated/0/Movies"),
            doOnEvent = { event, path ->
                doOnEvent(event, path)
            }
        )

        fileObserver?.startWatching()

        // Exemple : boucle d’écoute
        while (isActive) {
            //* … ton travail de fond (IO, sync, watch, etc.)
            // Mettre à jour la notif si utile :
            // updateNotification("Progression: 42%")
//            updateNotification("index: $index")

//            index++
            delay(2_000)
        }
    }

    private suspend fun doOnEvent(event: Int, path: SigmaPath?) {

        if (path != null) {
            val favorites = settingsManager.homeItemsFlow.firstOrNull()
            val currentAppFolder = settingsManager.currentPathFlow.first()
            val parent = path.dropLastSegmentOfPath()
            val grandParent = parent.dropLastSegmentOfPath()

            val isFile = path.toFile().isFile
            val isFavorite = favorites?.any { it.path == path } ?: false
            fun SigmaPath.isCurrent() = currentAppFolder == this

            val eventType = convertEvent(event)
            updateNotification("$path : ${eventType.message}")

            var CSRPath = false
            var CSRParent = false
            var CSRGrandParent = false

            //algo
            if (parent.isInRoom() || parent.isCurrent())
                CSRParent = true

            if (!isFile) {
                if (path.isCurrent() || path.isInRoom())
                    CSRPath = true
            }

            if (path.endsWith(".folderPicture.html")) {
                //si modification memo, flag, ou scale
                CSRParent = true

                CSRGrandParent = true
            }

            if (CSRPath) {
                Log.d(
                    TAG,
                    "service envoie dans room: path=${path.lastSegment}, isCurrent=${path.isCurrent()}, isInRoom=${path.isInRoom()}"
                )
                computeAndSendFreshness(path, currentAppFolder)
            }

            if (CSRParent) {
                Log.d(
                    TAG,
                    "service envoie pour ${path.lastSegment} dans room: parent=${
                        parent.lastSegment
                    }, isCurrent=${parent.isCurrent()}, isInRoom=${parent.isInRoom()}"
                )
                computeAndSendFreshness(parent, currentAppFolder)
            }

            if (CSRGrandParent) {
                Log.d(
                    TAG,
                    "service envoie pour ${path.lastSegment} dans room: grandParent=${
                        grandParent.lastSegment
                    }, isCurrent=${grandParent.isCurrent()}, isInRoom=${grandParent.isInRoom()}"
                )
                computeAndSendFreshness(grandParent, currentAppFolder)
            }
        }
    }

    suspend fun SigmaPath.isInRoom(): Boolean {
        return dao.getByPath(
            path = this,
            ctx = this@DaemonService,
            scope = scope
        ) != null
    }

    private suspend fun computeAndSendFreshness(
        path: SigmaPath,
        currentAppFolder: SigmaPath?,
        timeMeasurement: Boolean = false
    ) {
        var fc: FolderCacheEntry? = null
        var newFreshness: FolderFreshness? = null

        measureTimeMillis {
            fc = generateFolderCacheEntry(path)
            newFreshness = fc!!.freshness
        }.let {
            if (timeMeasurement)
                Log.d(TAG, "favori:$path, calcul freshness: $it ms")
        }

        updateNotification(color = Color.Red)

        var oldFreshness: FolderFreshness? = null

        measureTimeMillis {
            oldFreshness = dao.getByPath(
                path,
                scope,
                this@DaemonService
            )?.freshness
        }.let {
            if (timeMeasurement)
                Log.d(TAG, "favori:$path, recup freshness de base: $it ms")
        }

        if (newFreshness == null)
            return

        var same = false
        measureTimeMillis {
            same = newFreshness!!.isSameAs(oldFreshness)
        }.let {
            if (timeMeasurement)
                Log.d(TAG, "favori:$path, comparaison des 2: $it ms")
        }

        if (!same) {
            Log.d(TAG, "   sauvegarde de FolderCacheEntry pour $path")
            Log.d("PathCrspd", "      fc: path=${fc!!.path}, id=${fc!!.id}")

            measureTimeMillis {
                dao.saveFolderCacheEntry(fc!!, this@DaemonService)
            }.let {
                if (timeMeasurement)
                    Log.d(TAG, "favori:$path, écriture en base car <>: $it ms")
            }

            if (path == currentAppFolder) {
                Log.d(
                    TAG,
                    "   envoi de Freshness de currentAppFolder: ${fc!!.freshness.hashCode()} pour $currentAppFolder"
                )

                measureTimeMillis {
                    settingsManager.saveTestFreshness(fc!!.freshness)
                }.let {
                    if (timeMeasurement)
                        Log.d(TAG, "favori:$path, dossier courant -> datastore: $it ms")
                }
            }

            Log.d(TAG, "   sauvegarde achevée")
        }

        updateNotification(color = Color.White)
    }

    private fun buildOngoingNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, SigmaActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Service actif")
            .setContentText("Surveillance en cours…")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String? = null, color: Color? = null) {
        text?.let { latestNotificationMessage = it }
        color?.let { currentNotificationColor = it }

        val smallIcon = when (color) {
            Color.Blue -> R.drawable.oeil  //lecture disque
            Color.Red -> R.drawable.stylo //écriture en base
            Color.Green -> R.drawable.coche //ok
            Color.Black -> R.drawable.coche //ok
            Color.White -> R.drawable.encrier
            else -> R.drawable.coche
        }


        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle("Observateur de fichiers@Dossier σ")
            .setContentText(latestNotificationMessage)
            .setColor(currentNotificationColor.toArgb())
            .setColorized(true)
            .setOngoing(true)
            .build()
        nm.notify(30215, notif)
    }

    override fun onDestroy() {
        fileObserver?.stopWatching()
        fileObserver = null

        scope.launch(Dispatchers.IO) {
            settingsManager.saveIsFileObserverEnabled(false)
            job.cancel() // Arrête proprement les coroutines
            stopForeground(STOP_FOREGROUND_DETACH) // détache la notif (ou STOP_FOREGROUND_REMOVE pour la retirer)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    fun convertEvent(event: Int): EventType {
        when {
            (event and FileObserver.CREATE) != 0 ->
                return EventType.CREATE

            (event and FileObserver.DELETE) != 0 ->
                return EventType.DELETE

            (event and FileObserver.MODIFY) != 0 ->
                return EventType.MODIFY

            (event and FileObserver.MOVED_FROM) != 0 ->
                return EventType.MOVED_FROM

            (event and FileObserver.MOVED_TO) != 0 ->
                return EventType.MOVED_TO

            (event and FileObserver.ATTRIB) != 0 ->
                return EventType.ATTRIB

            (event and FileObserver.CLOSE_WRITE) != 0 ->
                return EventType.CLOSE_WRITE

            else ->
                return EventType.UNKNOWN
        }
    }

    private suspend fun generateFolderCacheEntry(fullPath: SigmaPath): FolderCacheEntry {

        updateNotification(color = Color.Blue)

        val items = diskRepository.getFolderItems(fullPath, SortingCriterion.ByDateDesc)
        val realFresh = diskRepository.getFolderFreshness(fullPath)
        val folder = SigmaFolder.ofItemsAndPersistedSigmaFolder(
            items = items,
            fullPath = fullPath,
        )
        val fc = FolderCacheEntry(
            folder = folder,
            path = folder.fullPath,
            sort = SortingCriterion.ByDateDesc,
            freshness = realFresh
        )

        updateNotification(color = Color.White)
        return fc
    }

    @Transaction
    suspend fun recomputeAndSaveAll(favorites: List<SigmaPath>) {
        // 1) compute freshness en parallèle limitée (voir §3)
        updateNotification(color = Color.Blue)
        val computed: Map<SigmaPath, FolderCacheEntry> = computeAllFreshness(favorites)

        // 2) lecture unique
        updateNotification(color = Color.White)
        val existing =
            dao.folderCacheEntryRepository().getAllByPaths(favorites.mapSigmaPaths{ it.str }).associateBy { it.path }

        // 3) diff en mémoire
        val toWrite = buildList {
            for ((path, new) in computed) {
                val old = existing[path]
                if (old == null || !new.freshness.isSameAs(old.freshness)) add(new)
            }
        }
        updateNotification(color = Color.Red)
        if (toWrite.isNotEmpty()) dao.folderCacheEntryRepository().upsertAll(toWrite)

        updateNotification(color = Color.Black)
    }

    suspend fun computeAllFreshness(paths: List<SigmaPath>): Map<SigmaPath, FolderCacheEntry> =
        coroutineScope {
            // limite de parallélisme
            val sem = Semaphore(permits = 6)
            paths.map { path ->
                async(Dispatchers.IO) {
                    sem.withPermit {
                        // ← ta fonction actuelle, pure disque
                        path to generateFolderCacheEntry(path)
                    }
                }
            }.awaitAll().toMap()
        }
}

fun Context.ensureDaemonChannel() {
    val mgr = getSystemService(NotificationManager::class.java)
    val ch = NotificationChannel(
        CHANNEL_ID, "Service en cours", NotificationManager.IMPORTANCE_LOW
    ).apply { description = "Activité de fond en cours" }
    mgr.createNotificationChannel(ch)
}

fun Context.isServiceRunning(serviceClass: Class<out Service>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Context.startDaemon() {

    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var exists = false

    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (DaemonService::class.java.name == service.service.className) {
            exists = true
        }
    }

    Log.d("FldSig", "at startup, daemon service exists?: $exists")

    if (!exists) {
        val i = Intent(this, DaemonService::class.java)
        startForegroundService(i)
    }
}

fun Context.stopDaemon() {
    stopService(Intent(this, DaemonService::class.java))
}

fun Context.isDaemonNotifVisible(id: Int = 30215, channelId: String = "daemon"): Boolean {
    val nm = getSystemService(NotificationManager::class.java)
    // API 23+ : notifications de TON app uniquement (pas besoin de permission spéciale)
    return nm.activeNotifications.any { it.id == id && it.notification.channelId == channelId }
}

sealed class EventType(val message: String) {
    object CREATE : EventType("fichier/dossier créé")
    object DELETE : EventType("supprimé ")
    object MODIFY : EventType("contenu modifié")
    object MOVED_FROM : EventType("déplacé/renommé ce fichier/depuis ce dossier")
    object MOVED_TO : EventType("déplacé/renommé vers ce dossier")
    object ATTRIB : EventType("métadonnées changées")
    object CLOSE_WRITE : EventType("fermé après écriture")
    object UNKNOWN : EventType("???")
}

@Singleton
class FilesAccessibleChannel @Inject constructor() {
    // sera complété une seule fois
//    val deferred = CompletableDeferred<Unit>()

    private val _ready = MutableStateFlow(false)
    val isActivated = _ready.asStateFlow()

    fun activate() {
        _ready.update { true }
    }
}