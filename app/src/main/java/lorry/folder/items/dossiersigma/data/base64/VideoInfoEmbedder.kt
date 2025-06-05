package lorry.folder.items.dossiersigma.data.base64

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import javax.inject.Inject

class VideoInfoEmbedder @Inject constructor() : IVideoInfoEmbedder {

    private val START_BASE64_TAG = "##SIGMA-COVER-START##"
    private val END_BASE64_TAG = "##SIGMA-COVER-END##"

    private val START_SCALE_TAG = "##SIGMA-SCALE-START##"
    private val END_SCALE_TAG = "##SIGMA-SCALE-END##"

    private val CHARSET = "UTF-8"

    /**
     * Ajoute une image encodée en base64 à la fin du fichier MP4 sans altérer sa lisibilité.
     */
    override suspend fun appendBase64ToMp4(mp4File: File, base64Image: String) {
        withContext(Dispatchers.IO) {
            val savedScale = extractContentScaleFromMp4(mp4File)
            removeEmbeddedContentScale(mp4File)
            
            RandomAccessFile(mp4File, "rw").use { raf ->
                raf.seek(raf.length())
                val data = "\n$START_BASE64_TAG\n$base64Image\n$END_BASE64_TAG\n"
                raf.write(data.toByteArray(Charset.forName(CHARSET)))
            }
            
            if (savedScale != null){
                appendContentScaleToMp4(mp4File, savedScale)
            }
        }
    }

    /**
     * Extrait l'image encodée en base64 si elle a été ajoutée via [appendBase64ToMp4].
     * Recherche progressivement dans des blocs de taille croissante si besoin.
     */
    override suspend fun extractBase64FromMp4(
        mp4File: File,
        initialLookbackBytes: Int,
        maxAttempts: Int
    ): String? {
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
                val startIndex = tail.lastIndexOf(START_BASE64_TAG)
                val endIndex = tail.lastIndexOf(END_BASE64_TAG)

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    return tail.substring(startIndex + START_BASE64_TAG.length, endIndex).trim()
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
            val savedScale = extractContentScaleFromMp4(mp4File)
            //inutile car plus bas le scale est aussi supprimé de facto
            //removeEmbeddedContentScale(mp4File)
            
            var result = false
            RandomAccessFile(mp4File, "rw").use { raf ->
                val length = raf.length()
                val lookbackBytes = 65536L
                val seekBack = minOf(lookbackBytes, length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, Charset.forName(CHARSET))
                val startIndex = tail.indexOf(START_BASE64_TAG)

                if (startIndex != -1) {
                    val absoluteStart = length - seekBack + startIndex
                    raf.setLength(absoluteStart)
                    result = true
                } else {
                    result = false
                }
            }
            
            if (result) {
                if (savedScale != null){
                    appendContentScaleToMp4(mp4File, savedScale)
                }
            }
            
            result
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

    override suspend fun appendContentScaleToMp4(mp4File: File, contentScale: ContentScale) {
        withContext(Dispatchers.IO) {
            RandomAccessFile(mp4File, "rw").use { raf ->
                raf.seek(raf.length())
                val scaleAsString = contentScale.toString()
                val data = "\n$START_SCALE_TAG\n$scaleAsString\n$END_SCALE_TAG\n"
                raf.write(data.toByteArray(Charset.forName(CHARSET)))
            }
        }
    }

    override suspend fun extractContentScaleFromMp4(mp4File: File): ContentScale? {
        val charset = Charset.forName(CHARSET)
        RandomAccessFile(mp4File, "r").use { raf ->
            val length = raf.length()
            val lookbackBytes = 500L
            val seekBack = minOf(lookbackBytes, length)
            raf.seek(length - seekBack)

            val bytes = ByteArray(seekBack.toInt())
            raf.readFully(bytes)

            val tail = String(bytes, charset)
            val startIndex = tail.lastIndexOf(START_SCALE_TAG)
            val endIndex = tail.lastIndexOf(END_SCALE_TAG)

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                return contentScaleFromString(
                    tail.substring(startIndex + START_BASE64_TAG.length, endIndex).trim()
                )
            }
            return null
        }
    }

    fun contentScaleFromString(value: String): ContentScale? = when (value) {
        "ContentScale.Crop" -> ContentScale.Crop
        "ContentScale.Fit" -> ContentScale.Fit
        "ContentScale.FillBounds" -> ContentScale.FillBounds
        "ContentScale.FillHeight" -> ContentScale.FillHeight
        "ContentScale.FillWidth" -> ContentScale.FillWidth
        "ContentScale.Inside" -> ContentScale.Inside
        "ContentScale.None" -> ContentScale.None
        else -> null // ou exception
    }

    override suspend fun removeEmbeddedContentScale(mp4File: File): Boolean {
        return withContext(Dispatchers.IO) {
            RandomAccessFile(mp4File, "rw").use { raf ->
                val length = raf.length()
                val lookbackBytes = 500L
                val seekBack = minOf(lookbackBytes, length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, Charset.forName(CHARSET))
                val startIndex = tail.indexOf(START_SCALE_TAG)

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
