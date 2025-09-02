package lorry.folder.items.dossiersigma.headless.favoriteObservation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.protobuf.LazyStringArrayList
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.favoriteObservation.external.FolderCacheEntryDB
import lorry.folder.items.dossiersigma.headless.folderContent.FolderCacheEntry
import lorry.folder.items.dossiersigma.ui.settings.SettingsManager
import lorry.folder.items.dossiersigma.ui.sigma.SigmaActivity
import lorry.folder.items.dossiersigma.ui.sigma.SortingCriterion
import java.io.File
import javax.inject.Inject

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

    private var fileObserver: SigmaFileObserver? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var index: Int = 0

    override fun onCreate() {
        super.onCreate()
        applicationContext.ensureDaemonChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        scope.launch(Dispatchers.IO) {
            settingsManager.saveIsFileObserverEnabled(true)
        }

        // 1) Démarrer au premier plan rapidement
        startForeground(30215, buildOngoingNotification())

        // 2) Démarrer ta boucle “daemon”
        scope.launch {
            runDaemonLoop()
        }

        // Conseil : START_STICKY pour relance automatique après kill système
        return START_STICKY
    }

    private suspend fun runDaemonLoop() {

        val favorites = settingsManager.homeItemsFlow.firstOrNull()
        val currentAppFolder = settingsManager.currentPathFlow.first()

        val files = favorites
            ?.map {
                it.path
//                    ?.substringAfterLast("/")
//                    ?.substringBeforeLast(".")
            }
            ?.joinToString(",")
            ?: "rien"

//            updateNotification("$index - $files")

        val parent = favorites?.let {
            it[0]
                .path
                ?.substringBeforeLast("/")
        }


        val directories = (favorites?.map { it.path }
            ?: LazyStringArrayList.emptyList() + currentAppFolder).filterNotNull()
        Log.d(TAG, "envoi initial de ${directories.size} dossiers...")

        val dups = directories.groupBy { it }.filterValues { it.size > 1 }
        Log.d(
            TAG,
            "paths=${directories.size}, distinct=${directories.distinct().size}, dups=${dups.keys}"
        )

        for (path in directories) {
            computeAndSendFreshness(path, currentAppFolder)
        }
        Log.d(TAG, "envoi initial terminé")

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

    private suspend fun doOnEvent(event: Int, path: String?) {

        if (path != null) {
            val favorites = settingsManager.homeItemsFlow.firstOrNull()
            val currentAppFolder = settingsManager.currentPathFlow.first()

            val eventType = convertEvent(event)
            updateNotification("$path : ${eventType.message}")

//            for (path in (favorites?.map { it.path }
//                ?: emptyList() + currentAppFolder)
//                .filterNotNull()) {
            computeAndSendFreshness(path, currentAppFolder)
//            }
        }
    }

    private suspend fun computeAndSendFreshness(path: String, currentAppFolder: String?) {
        var fc: FolderCacheEntry? = null
        fc = generateFolderCacheEntry(path)
        if (fc == null) {
            updateNotification("fc null")
            Log.d(TAG, "envoi de Freshness: ${fc!!.freshness.hashCode()}")
            return
        }

        val dao = FolderCacheEntryDB.get(this@DaemonService)

        val oldFreshness = dao.getByPath(
            path, scope,
            this@DaemonService
        )?.freshness

        val newFreshness = fc!!.freshness

        if (!newFreshness.isSameAs(oldFreshness)) {
            Log.d(TAG, "   sauvegarde de FolderCacheEntry pour $path")
            Log.d(TAG, "      fc: path=${fc!!.path}, id=${fc!!.id}")
            dao.saveFolderCacheEntry(fc!!, this@DaemonService)

            if (path == currentAppFolder) {
                Log.d(
                    TAG,
                    "   envoi de Freshness de currentAppFolder: ${fc!!.freshness.hashCode()} pour $currentAppFolder"
                )
                settingsManager.saveTestFreshness(fc!!.freshness)
            }

            Log.d(TAG, "   sauvegarde achevée")
        }
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

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Service actif")
            .setContentText(text)
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

    private suspend fun generateFolderCacheEntry(path: String): FolderCacheEntry {

        val items = diskRepository.getFolderItems(path, SortingCriterion.ByDateDesc)
        val realFresh = diskRepository.getFolderFreshness(path)
        val folder = SigmaFolder.ofItemsAndPersistedSigmaFolder(
            items = items,
            fullPath = path.substringBeforeLast("/"),
//            name = path.substringAfterLast("/")
        )
        val fc = FolderCacheEntry(
            folder = folder,
            path = folder.fullPath,
            sort = SortingCriterion.ByDateDesc,
            freshness = realFresh
        )
        return fc
    }
}


fun Context.ensureDaemonChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mgr = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_ID, "Service en cours", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Activité de fond en cours" }
        mgr.createNotificationChannel(ch)
    }
}

fun Context.startDaemon() {
    val i = Intent(this, DaemonService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
    else startService(i)
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
    object MOVED_FROM : EventType("déplacé/renommé depuis ce dossier")
    object MOVED_TO : EventType("déplacé/renommé vers ce dossier")
    object ATTRIB : EventType("métadonnées changées")
    object CLOSE_WRITE : EventType("fermé après écriture")
    object UNKNOWN : EventType("???")
}