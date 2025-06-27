package lorry.folder.items.dossiersigma.domain.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import lorry.folder.items.dossiersigma.ui.components.BottomTools
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

/**
 * Appelé par @see[lorry.folder.items.dossiersigma.ui.MainActivity.onCreate]
 * , déclaration de CustomMoveFileExistingDestinationDialog
 */
class MoveFileService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "move_file_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val source = intent?.getStringExtra("source") ?: return START_NOT_STICKY
        var destination = intent.getStringExtra("destination") ?: return START_NOT_STICKY

        val addSuffix = intent.getStringExtra("addSuffix")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Copie de fichiers en cours"))

        println("copie de $source vers $destination")

        Thread {
            try {
                copy(source, destination, suffix = addSuffix)
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }

            if ((verify(source, destination) &&
                        (source.substringAfterLast("/") == destination.substringAfterLast("/")))
                || File(source).isDirectory
            ) {
                delete(source)
            }

            SigmaViewModel.requestRefresh()
            stopSelf()
        }.start()

        return START_NOT_STICKY
    }

    private fun copy(source: String, destination: String, suffix: String?) {
        if (source == null || destination == null)
            return

        val sourceFile = File(source)
        val destinationItem = File(destination)

        if (destinationItem.isDirectory) {
//            val sourceExtension = sourceFile.extension
            val destinationFile =
                File("$destination/${source.substringAfterLast("/").substringBeforeLast(".")}$suffix")

            println("début de la copie")
            val duration = measureTimeMillis {
                if (sourceFile.isFile)
                    copyFileWithProgress(sourceFile, destinationFile) { p ->
                        println("progression: $p%")
                        BottomTools.updateProgress(p)
                    }
//                    sourceFile.copyTo(destinationFile, overwrite = true)
                else
                    sourceFile.copyRecursively(destinationFile, overwrite = true)
            }
            println("fin de la copie en ${millisToHMS(duration)}")
        }

        if (destinationItem.isFile) {
            println("début de la copie")
            val duration = measureTimeMillis {
                val sourceExtension = sourceFile.extension
                val destinationFile = File(
                    "$destination/${
                        source.substringAfterLast("/").substringBeforeLast(".")
                    }$suffix.$sourceExtension"
                )
                if (sourceFile.isFile)
                    copyFileWithProgress(sourceFile, destinationFile) { p ->
                        println("progression: $p%")
                    }
                else
                    sourceFile.copyRecursively(destinationFile, overwrite = true)
            }
            println("fin de la copie en ${millisToHMS(duration)}")

        }

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

    private fun verify(source: String, destination: String): Boolean {
        val sourceFile = File(source)
        val destinationFile = File(destination)

        if (!sourceFile.exists() || !destinationFile.exists())
            return false

        val sourceSize = try {
            sourceFile.length()
        } catch (e: Exception) {
            return false
        }

        val destinationSize = try {
            destinationFile.length()
            sourceFile.length()
        } catch (e: Exception) {
            return false
        }

        return sourceSize == destinationSize
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
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateProgress(progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Copie en cours")
            .setContentText("Progression : $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
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