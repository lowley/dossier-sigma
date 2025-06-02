package lorry.folder.items.dossiersigma.data.base64

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

object Mp4Base64Embedder : IMp4Base64Embedder{

    private const val START_TAG = "##SIGMA-COVER-START##"
    private const val END_TAG = "##SIGMA-COVER-END##"
    private const val CHARSET = "UTF-8"

    /**
     * Ajoute une image encodée en base64 à la fin du fichier MP4 sans altérer sa lisibilité.
     */
    override suspend fun appendBase64ToMp4(mp4File: File, base64Image: String) {
        withContext(Dispatchers.IO) { 
            RandomAccessFile(mp4File, "rw").use { raf ->
                raf.seek(raf.length())
                val data = "\n$START_TAG\n$base64Image\n$END_TAG\n"
                raf.write(data.toByteArray(Charset.forName(CHARSET)))
            }
        }
    }

    /**
     * Extrait l'image encodée en base64 si elle a été ajoutée via [appendBase64ToMp4].
     */
    override suspend fun extractBase64FromMp4(mp4File: File, lookbackBytes: Int): String? {
        return withContext(Dispatchers.IO) {
            RandomAccessFile(mp4File, "r").use { raf ->
                val length = raf.length()
                val seekBack = minOf(lookbackBytes.toLong(), length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, Charset.forName(CHARSET))
                val startIndex = tail.indexOf(START_TAG)
                val endIndex = tail.indexOf(END_TAG)

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    tail.substring(startIndex + START_TAG.length, endIndex).trim()
                } else {
                    null
                }
            }
        }
    }

    /**
     * Supprime les données base64 insérées (si présentes).
     */
    override suspend fun removeEmbeddedBase64(mp4File: File): Boolean {
        return withContext(Dispatchers.IO) {
            RandomAccessFile(mp4File, "rw").use { raf ->
                val length = raf.length()
                val lookbackBytes = 65536L
                val seekBack = minOf(lookbackBytes, length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, Charset.forName(CHARSET))
                val startIndex = tail.indexOf(START_TAG)

                if (startIndex != -1) {
                    val absoluteStart = length - seekBack + startIndex
                    raf.setLength(absoluteStart)
                    true
                } else {
                    false
                }
            }  
        }
    }
} 
