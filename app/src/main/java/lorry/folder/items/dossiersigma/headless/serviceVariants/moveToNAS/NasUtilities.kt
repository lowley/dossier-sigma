package lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.nas.DSI_FTP
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import lorry.folder.items.dossiersigma.headless.domain.lastSegment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class NasUtilities @Inject constructor(
    val nasDS: DSI_FTP,
) {
    companion object {
        val TAG = "NasUtls"
    }


    suspend fun copy(
        source: SigmaPath,
        destination: SigmaPath,
        index: Int,
        total: Int,
        changeBottomTools: (percentage: Int, index: Int, total: Int) -> Unit
    ) {
        if (source == null || destination == null)
            return

        val sourceFile = source.toFile()

        if (sourceFile.isDirectory)
            return

        println("début de la copie")
//        val duration = measureTimeMillis {

        val copyResult = withContext(Dispatchers.IO) {
            val copy = async {
                nasDS.copy(
                    localFilePath = source,
                    pathOnNAS = destination,
                ) { p ->
//                    println("progression: $p%")
                    changeBottomTools(p, index, total)
                }
            }

            copy.await()
        }

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
//                updateProgress(progress)
                lastProgress = progress
            }
        }

        input.close()
        output.flush()
        output.close()
    }

    suspend fun verify(source: SigmaPath, destination: SigmaPath): Boolean {
        Log.d(TAG, "début de verify")
        Log.d(TAG, "source=$source, destination=$destination")

        val sourceFile = source.toFile()
        val destinationFiles = nasDS.fetchFiles(destination)

        var file = destinationFiles
            ?.firstOrNull { it.name == source.lastSegment }

        if (file == null)
            return false

        val result = file.size == sourceFile.length()
        Log.d(TAG, "début de verify: resultat=$result")

        return result
//        return true
    }

    fun delete(source: SigmaPath) {
        val source = source.toFile()
        if (source.exists()) {
            if (source.isFile)
                source.delete()
            else
                source.deleteRecursively()
        }
    }
}