package lorry.folder.items.dossiersigma.data.base64

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import javax.inject.Inject

class Mp4Base64Embedder @Inject constructor() : IMp4Base64Embedder {

    private val START_TAG = "##SIGMA-COVER-START##"
    private val END_TAG = "##SIGMA-COVER-END##"
    private val CHARSET = "UTF-8"

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
     * Recherche progressivement dans des blocs de taille croissante si besoin.
     */
    override suspend fun extractBase64FromMp4(mp4File: File, initialLookbackBytes: Int, maxAttempts: Int): String? {
        val charset = Charset.forName(CHARSET)
        RandomAccessFile(mp4File, "r").use { raf ->
            val length = raf.length()
            for (attempt in 1..maxAttempts) {
                val lookbackBytes = (initialLookbackBytes * attempt).toLong()
                val seekBack = minOf(lookbackBytes, length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, charset)
                val startIndex = tail.lastIndexOf(START_TAG)
                val endIndex = tail.lastIndexOf(END_TAG)

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    return tail.substring(startIndex + START_TAG.length, endIndex).trim()
                }
            }
            return null
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

    override suspend fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun bitmapToBase64(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int
    ): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
} 
