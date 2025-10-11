package lorry.folder.items.dossiersigma.headless.moveToNasWorker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.petertackage.kotlinoptions.Option
import com.petertackage.kotlinoptions.optionOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ServiceLocator
import lorry.folder.items.dossiersigma.headless.moveToNasWorker.utilities.IMoveProgress
import lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS.ManifestEntry
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Un Worker robuste pour transférer des fichiers vers le NAS.
 * Il remplace l'ancien MoveToNASService pour garantir l'exécution
 * même si l'application est fermée.
 */
class MoveToNASWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "move_nas_channel"
        private const val NOTIF_ID = 42

        const val KEY_MANIFEST_PATH = "manifest_path"
        const val KEY_MANIFEST_URI = "manifest_uri"// Array<String>
        const val KEY_TARGET = "target"   // String
        const val P_ITEMS = "p_items"
        const val P_INDEX = "p_index"
        const val P_PCT = "p_pct"

        var sourceFolderInPath: Option<String> = optionOf(null)

        fun request(
            target: String,
            manifestPath: String,
            manifestUri: String
        ): OneTimeWorkRequest {

            val data = workDataOf(
                KEY_MANIFEST_PATH to manifestPath,
                KEY_MANIFEST_URI to manifestUri,
                KEY_TARGET to target
            )

            //! exécute [[doWork]]
            return OneTimeWorkRequestBuilder<MoveToNASWorker>()
                .setInputData(data)
                .addTag("move-to-nas-active")
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL, Duration.of(
                        10_000,
                        ChronoUnit.MILLIS
                    )
                )
                .addTag("move-to-nas")
                .build()
        }
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    //#[[doWork]]
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        val engine = ServiceLocator.moveEngine(applicationContext)

        val uri = inputData.getString(KEY_MANIFEST_URI) ?: return Result.failure()
        val path = inputData.getString(KEY_MANIFEST_PATH) ?: return Result.failure()
        val json = File(path).readText()
        val type = object : TypeToken<List<ManifestEntry>>() {}.type
        val entries: List<ManifestEntry> = Gson().fromJson(json, type)

        val target = inputData.getString(KEY_TARGET) ?: return Result.failure()
        val destination = "/$target"

        sourceFolderInPath = optionOf(
            entries
                .firstOrNull()
                ?.fullPath
                ?.substringBeforeLast("/") //répertoire contenant
        )
        
        setForeground(createForegroundInfo("Copie en cours...", "Préparation"))
        ensureChannel()

        var total = 0
        var currentPercent = -1
        val callback = object : IMoveProgress {

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onStart(t: Int) {
                total = t
                setProgress(workDataOf(P_ITEMS to t, P_INDEX to 0, P_PCT to 0))
                withContext(Dispatchers.Main) {
                    updateNotif("Copie en cours", " 0/ $t")
                }
            }

            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onItemProgress(index: Int, percent: Int) {
                if (currentPercent != percent) {
                    setProgress(workDataOf(P_ITEMS to total, P_INDEX to index, P_PCT to percent))
                    withContext(Dispatchers.Main) {
                        updateNotif("Copie en cours", "${index + 1} / $total: $percent%")
                    }
                    currentPercent = percent
                }
            }


            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override suspend fun onItemDone(index: Int) {
                withContext(Dispatchers.Main) {
                    updateNotif("Copie en cours", "${index + 1} / $total")
                }
            }
        }

        return try {
            engine.copyAll(
                entries,
                destination,
                callback,
                isCancelled = { isStopped },
                path = path,
                uri = uri
            )

            updateNotif("Copie terminée", "$total fichier(s) copiés")
            Result.success()
        } catch (e: Exception) {
            updateNotif("Copie interrompue", e.message ?: "Erreur")
            Result.failure()
        }finally {
            sourceFolderInPath = optionOf(null)
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

        return ForegroundInfo(
            NOTIF_ID,
            notif,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
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
        val dsFtp = ServiceLocator.dsFtp(applicationContext)

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

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return ForegroundInfo(
            id.hashCode(),
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun createNotification(
        progress: String,
        progressPercent: Int? = null
    ): Notification {
        val channelId = "MoveToNASChannel"
        val title = "Transfert vers le NAS"

        // Créer le canal de notification si nécessaire (pour Android 8.0+)
        val channel = NotificationChannel(
            channelId,
            title,
            NotificationManager.IMPORTANCE_LOW
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
