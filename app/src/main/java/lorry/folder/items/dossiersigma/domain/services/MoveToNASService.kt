package lorry.folder.items.dossiersigma.domain.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import lorry.folder.items.copieurtho2.__data.NAS.DS_FTP
import lorry.folder.items.dossiersigma.R
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Appelé par @see[lorry.folder.items.dossiersigma.ui.MainActivity.onCreate]
 * , déclaration de CustomMoveFileExistingDestinationDialog
 */
class MoveToNASService : Service(), CoroutineScope by MainScope() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "move_nas_channel"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("dans onStartCommand")
        val filesTotransferString = intent?.getStringExtra("filesToTransfer") ?: return START_NOT_STICKY

        val type = object : TypeToken<List<String>>() {}.type
        val filesTotransfer = Gson().fromJson<List<String>>(filesTotransferString, type)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Copie de fichiers en cours"))

        println("copie de ${filesTotransfer.size} fichiers vers le NAS")
        val destination = "/videos"

        serviceScope.launch {
            println("MoveToNASService: dans launch")
            for (source in filesTotransfer) {
                println("MoveToNASService: copie de ${source}")
                try {
                    copy(source, destination)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if ((verify(source, destination)))
                    delete(source)

                SigmaViewModel.requestRefresh()
            }

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun copy(source: String, destination: String) {
        if (source == null || destination == null)
            return

        val sourceFile = File(source)
        val nasDS = DS_FTP()

        if (sourceFile.isDirectory)
            return

        println("début de la copie")
//        val duration = measureTimeMillis {

        val copy = async {
            nasDS.copy(
                localFilePath = source,
                pathOnNAS = destination,
            ) { p ->
                println("progression: $p%")
                BottomTools.updateNASProgress(p)
            }
        }

        val copyResult = copy.await()

        println("la copie de ${sourceFile.name} est terminée en ${if (copyResult) "succès" else "échec"}")
//        }
//        println("fin de la copie en ${millisToHMS(duration)}")
    }

    fun millisToHMS(millis: Long): String {
        val seconds = millis / 1000
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60

        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun copyFileWithProgress(source: File, dest: File, onProgress: (percent: Int) -> Unit) {
        val input = FileInputStream(source)
        val output = FileOutputStream(dest)
        val totalBytes = source.length()
        val buffer = ByteArray(8 * 1024)
        var bytesCopied = 0L
        var lastProgress = -1

        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
            bytesCopied += read

            val progress = (bytesCopied * 100 / totalBytes).toInt()
            if (progress != lastProgress) {
                onProgress(progress)
                updateProgress(progress)
                lastProgress = progress
            }
        }

        input.close()
        output.flush()
        output.close()
    }

    private suspend fun verify(source: String, destination: String): Boolean {
        val sourceFile = File(source)

        val destinationFiles = DS_FTP().fetchMP4Files(destination)

        var file = destinationFiles
            ?.first { it.name == source.substringAfterLast("/") }

        if (file == null)
            return false

        return file.size == sourceFile.length()
    }

    private fun delete(source: String) {
        val source = File(source)
        if (source.exists()) {
            if (source.isFile)
                source.delete()
            else
                source.deleteRecursively()
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Copie de fichiers",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    private fun buildNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dossier Sigma")
            .setContentText(message)
            .setSmallIcon(R.drawable.deplacer)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateProgress(progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Copie en cours")
            .setContentText("Progression : $progress%")
            .setSmallIcon(R.drawable.deplacer)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    val success = deleteRecursively(child)
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return file.delete()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}