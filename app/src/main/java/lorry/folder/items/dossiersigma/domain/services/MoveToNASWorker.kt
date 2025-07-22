//package lorry.folder.items.dossiersigma.domain.services
//
//import android.content.Context
//import androidx.core.app.NotificationCompat
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.ForegroundInfo
//import androidx.work.WorkerParameters
//import androidx.work.workDataOf
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import lorry.folder.items.copieurtho2.__data.NAS.DS_FTP
//import lorry.folder.items.dossiersigma.R
//import lorry.folder.items.dossiersigma.ui.components.BottomTools
//import java.io.File
//import java.util.UUID
//
///**
// * Un Worker robuste pour transférer des fichiers vers le NAS.
// * Il remplace l'ancien MoveToNASService pour garantir l'exécution
// * même si l'application est fermée.
// */
//@HiltWorker
//class MoveToNASWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted workerParams: WorkerParameters,
//    // L'injection Hilt fonctionne toujours pour vos dépendances
//    private val dsFtp: DS_FTP
//) : CoroutineWorker(appContext, workerParams) {
//
//    companion object {
//        // Clés pour passer les données au Worker
//        const val KEY_FILES_TO_TRANSFER = "filesToTransfer"
//        const val KEY_NAS_DIRECTORY = "nasDirectory"
//        const val KEY_NOTIFICATION_TITLE = "notificationTitle"
//    }
//
//    private val notificationManager =
//        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
//
//    // Le travail principal est effectué ici, dans une coroutine.
//    override suspend fun doWork(): Result {
//        // Récupérer les données passées lors de la création de la tâche
//        val filesToTransferString = inputData.getString(KEY_FILES_TO_TRANSFER) ?: return Result.failure()
//        val nasDirectory = inputData.getString(KEY_NAS_DIRECTORY) ?: return Result.failure()
//
//        val type = object : TypeToken<List<String>>() {}.type
//        val filesToTransfer = Gson().fromJson<List<String>>(filesToTransferString, type)
//
//        val destination = "/$nasDirectory"
//
//        // Boucle sur chaque fichier à transférer
//        filesToTransfer.forEachIndexed { index, sourcePath ->
//            try {
//                // Créer et afficher la notification de premier plan pour ce fichier
//                val progress = "Copie de ${index + 1}/${filesToTransfer.size}"
//                val notification = createNotification(progress)
//                setForeground(createForegroundInfo(notification))
//
//                // Logique de copie
//                val copySuccess = copyFile(sourcePath, destination, index, filesToTransfer.size)
//
//                // Si la copie réussit et que la vérification est bonne, on supprime le fichier source
//                if (copySuccess && verify(sourcePath, destination)) {
//                    delete(sourcePath)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // En cas d'erreur, on peut dire à WorkManager de réessayer plus tard
//                return Result.retry()
//            }
//        }
//
//        // Une fois terminé, on peut notifier l'UI si nécessaire
//        // SigmaViewModel.requestRefresh() // Attention : à gérer via un Flow ou une DB
//
//        return Result.success()
//    }
//
//    private suspend fun copyFile(sourcePath: String, destination: String, index: Int, total: Int): Boolean {
//        return withContext(Dispatchers.IO) {
//            try {
//                dsFtp.copy(
//                    localFilePath = sourcePath,
//                    pathOnNAS = destination,
//                ) { percentage ->
//                    // Mettre à jour la progression de la notification
//                    val progressText = "Copie de ${index + 1}/$total : $percentage%"
//                    val notification = createNotification(progressText, percentage)
//                    notificationManager.notify(id.hashCode(), notification)
//
//                    // Mettre à jour la progression dans l'UI (si l'app est ouverte)
//                    BottomTools.updateNASProgress(
//                        percentage = percentage,
//                        fileIndex = index,
//                        fileCount = total
//                    )
//                }
//                true // Succès de la copie
//            } catch (e: Exception) {
//                e.printStackTrace()
//                false // Échec de la copie
//            }
//        }
//    }
//
//    // La logique de vérification et de suppression reste similaire
//    private suspend fun verify(source: String, destination: String): Boolean {
//        return withContext(Dispatchers.IO) {
//            val sourceFile = File(source)
//            val destinationFiles = dsFtp.fetchMP4Files(destination)
//            val file = destinationFiles?.firstOrNull { it.name == sourceFile.name }
//            file?.size == sourceFile.length()
//        }
//    }
//
//    private fun delete(source: String) {
//        val sourceFile = File(source)
//        if (sourceFile.exists()) {
//            sourceFile.delete()
//        }
//    }
//
//
//    // --- Gestion de la notification de premier plan ---
//
//    private fun createForegroundInfo(notification: android.app.Notification): ForegroundInfo {
//        return ForegroundInfo(id.hashCode(), notification)
//    }
//
//    private fun createNotification(progress: String, progressPercent: Int? = null): android.app.Notification {
//        val channelId = "MoveToNASChannel"
//        val title = "Transfert vers le NAS"
//
//        // Créer le canal de notification si nécessaire (pour Android 8.0+)
//        val channel = android.app.NotificationChannel(
//            channelId,
//            title,
//            android.app.NotificationManager.IMPORTANCE_LOW
//        )
//        notificationManager.createNotificationChannel(channel)
//
//        val builder = NotificationCompat.Builder(applicationContext, channelId)
//            .setContentTitle(title)
//            .setContentText(progress)
//            .setSmallIcon(R.drawable.deplacer)
//            .setOngoing(true)
//            .setAutoCancel(false)
//
//        // Ajouter une barre de progression à la notification
//        if (progressPercent != null) {
//            builder.setProgress(100, progressPercent, false)
//        }
//
//        return builder.build()
//    }
//}
//
//// --- Comment lancer le Worker depuis votre Activity/ViewModel ---
//
///*
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.workDataOf
//
//fun startMoveToNasWorker(context: Context, filesToTransfer: List<String>, nasDirectory: String) {
//    val filesJson = Gson().toJson(filesToTransfer)
//
//    val workData = workDataOf(
//        MoveToNASWorker.KEY_FILES_TO_TRANSFER to filesJson,
//        MoveToNASWorker.KEY_NAS_DIRECTORY to nasDirectory
//    )
//
//    val workRequest = OneTimeWorkRequestBuilder<MoveToNASWorker>()
//        .setInputData(workData)
//        // Vous pouvez ajouter des contraintes, par exemple, ne s'exécuter que si le réseau est présent
//        // .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
//        .build()
//
//    WorkManager.getInstance(context).enqueue(workRequest)
//}
//*/
