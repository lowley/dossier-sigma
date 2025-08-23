package lorry.folder.items.dossiersigma.domain.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.IMoveProgress
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.MoveEngine
import java.io.File
import java.time.temporal.ChronoUnit

/**
 * Un Worker robuste pour transférer des fichiers vers le NAS.
 * Il remplace l'ancien MoveToNASService pour garantir l'exécution
 * même si l'application est fermée.
 */
@HiltWorker
class MoveToNASWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val engine: MoveEngine,
    // L'injection Hilt fonctionne toujours pour vos dépendances
    private val dsFtp: DSI_FTP
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "move_nas_channel"
        private const val NOTIF_ID = 42

        const val KEY_SOURCES = "sources"  // Array<String>
        const val KEY_TARGET = "target"   // String
        const val P_ITEMS = "p_items"
        const val P_INDEX = "p_index"
        const val P_PCT = "p_pct"

        fun request(sources: List<String>, target: String): OneTimeWorkRequest {

            val data = workDataOf(
                KEY_SOURCES to sources.toTypedArray(),
                KEY_TARGET to target
            )

            return OneTimeWorkRequestBuilder<MoveToNASWorker>()
                .setInputData(data)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL, java.time.Duration.of(
                        10_000,
                        ChronoUnit.MILLIS
                    )
                )
                .addTag("move-to-nas")
                .build()
        }
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    // Le travail principal est effectué ici, dans une coroutine.
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        // Récupérer les données passées lors de la création de la tâche
        val sources =
            inputData.getStringArray(KEY_SOURCES)?.toList().orEmpty()
        val target = inputData.getString(KEY_TARGET) ?: return Result.failure()
        val destination = "/$target"

        setForeground(createForegroundInfo("Copie en cours...", "Préparation"))
        ensureChannel()

        var total = 0
        val callabck = object : IMoveProgress {

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onStart(t: Int) {
                total = t
                setProgress(workDataOf(P_ITEMS to t, P_INDEX to 0, P_PCT to 0))
                updateNotif("Copie en cours", " 0/ $t")
            }

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onItemProgress(index: Int, percent: Int) {
                setProgress(workDataOf(P_ITEMS to total, P_INDEX to index, P_PCT to percent))
            }


            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onItemDone(index: Int) {
                updateNotif("Copie en cours", "${index + 1} / $total")
            }
        }

        return try {
            engine.copyAll(
                sources,
                destination,
                callabck,
                isCancelled = { isStopped }
            )

            updateNotif("Copie terminée", "$total fichier(s) copiés")
            Result.success()
        } catch (e: Exception) {
            updateNotif("Copie interrompue", e.message ?: "Erreur")
            Result.failure()
        }
    }

    private suspend fun createForegroundInfo(title: String, text: String): ForegroundInfo {

        val notif = withContext(Dispatchers.IO) {
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build()
        }

        return ForegroundInfo(NOTIF_ID, notif)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotif(title: String, text: String) {

        val manager = NotificationManagerCompat.from(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(title)
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        manager.notify(NOTIF_ID, notification)
    }

    private fun ensureChannel() {

        val nm =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Copies NAS",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
    }

    private suspend fun copyFile(
        sourcePath: String,
        destination: String,
        index: Int,
        total: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                dsFtp.copy(
                    localFilePath = sourcePath,
                    pathOnNAS = destination,
                ) { percentage ->
                    // Mettre à jour la progression de la notification
                    val progressText = "Copie de ${index + 1}/$total : $percentage%"
                    val notification = createNotification(progressText, percentage)
                    notificationManager.notify(id.hashCode(), notification)

                    // Mettre à jour la progression dans l'UI (si l'app est ouverte)
//                    BottomTools.updateNASProgress(
//                        percentage = percentage,
//                        fileIndex = index,
//                        fileCount = total
//                    )
                }
                true // Succès de la copie
            } catch (e: Exception) {
                e.printStackTrace()
                false // Échec de la copie
            }
        }
    }

    // La logique de vérification et de suppression reste similaire
//    private suspend fun verify(source: String, destination: String): Boolean {
//        return withContext(Dispatchers.IO) {
//            val sourceFile = File(source)
//            val destinationFiles = dsFtp.fetchMP4Files(destination)
//            val file = destinationFiles?.firstOrNull { it.name == sourceFile.name }
//            file?.size == sourceFile.length()
//        }
//    }

    private fun delete(source: String) {
        val sourceFile = File(source)
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
    }


// --- Gestion de la notification de premier plan ---

    private fun createForegroundInfo(notification: android.app.Notification): ForegroundInfo {
        return ForegroundInfo(id.hashCode(), notification)
    }

    private fun createNotification(
        progress: String,
        progressPercent: Int? = null
    ): android.app.Notification {
        val channelId = "MoveToNASChannel"
        val title = "Transfert vers le NAS"

        // Créer le canal de notification si nécessaire (pour Android 8.0+)
        val channel = android.app.NotificationChannel(
            channelId,
            title,
            android.app.NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.deplacer)
            .setOngoing(true)
            .setAutoCancel(false)

        // Ajouter une barre de progression à la notification
        if (progressPercent != null) {
            builder.setProgress(100, progressPercent, false)
        }

        return builder.build()
    }
}

// --- Comment lancer le Worker depuis votre Activity/ViewModel ---

/*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

fun startMoveToNasWorker(context: Context, filesToTransfer: List<String>, nasDirectory: String) {
    val filesJson = Gson().toJson(filesToTransfer)

    val workData = workDataOf(
        MoveToNASWorker.KEY_FILES_TO_TRANSFER to filesJson,
        MoveToNASWorker.KEY_NAS_DIRECTORY to nasDirectory
    )

    val workRequest = OneTimeWorkRequestBuilder<MoveToNASWorker>()
        .setInputData(workData)
        // Vous pouvez ajouter des contraintes, par exemple, ne s'exécuter que si le réseau est présent
        // .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}
*/
