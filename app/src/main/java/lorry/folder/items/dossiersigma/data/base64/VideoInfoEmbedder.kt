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
    private val CHARSET = "UTF-8"

    /**
     * Ajoute une image encodée en base64 à la fin du fichier MP4 sans altérer sa lisibilité.
     */
    override suspend fun appendBase64ToMp4(mp4File: File, base64Image: String, tag: Tags) {
        withContext(Dispatchers.IO) {
            val savedScale = extractContentScaleFromMp4(mp4File)
            removeEmbeddedContentScale(mp4File)

            if (tag == Tags.COVER) {
                val base64Cropped = extractBase64FromMp4(mp4File, 65536, 5, Tags.COVER_CROPPED)
                removeBothEmbeddedBase64(mp4File)

                RandomAccessFile(mp4File, "rw").use { raf ->
                    raf.seek(raf.length())
                    val data = "\n${tag.start}\n$base64Image\n${tag.end}\n"
                    raf.write(data.toByteArray(Charset.forName(CHARSET)))
                }

                if (base64Cropped != null)
                    RandomAccessFile(mp4File, "rw").use { raf ->
                        raf.seek(raf.length())
                        val data = "\n${Tags.COVER_CROPPED.start}\n$base64Cropped\n${Tags.COVER_CROPPED.end}\n"
                        raf.write(data.toByteArray(Charset.forName(CHARSET)))
                    }
                else
                    RandomAccessFile(mp4File, "rw").use { raf ->
                        raf.seek(raf.length())
                        val data = "\n${Tags.COVER_CROPPED.start}\n$base64Image\n{Tags.COVER_CROPPED.end}\n"
                        raf.write(data.toByteArray(Charset.forName(CHARSET)))
                    }
            }

            if (tag == Tags.COVER_CROPPED) {
                RandomAccessFile(mp4File, "rw").use { raf ->
                    raf.seek(raf.length())
                    val data = "\n${Tags.COVER_CROPPED.start}\n$base64Image\n${Tags.COVER_CROPPED.end}\n"
                    raf.write(data.toByteArray(Charset.forName(CHARSET)))
                }
            }

            if (savedScale != null) {
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
        maxAttempts: Int,
        tag: Tags
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
                val startIndex = tail.lastIndexOf(tag.start)
                val endIndex = tail.lastIndexOf(tag.end)

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    return tail.substring(startIndex + tag.start.length, endIndex).trim()
                }
            }

            if (tag == Tags.COVER_CROPPED) {
                val result =
                    extractBase64FromMp4(mp4File, initialLookbackBytes, maxAttempts, Tags.COVER)
                if (result != null) {
                    appendBase64ToMp4(mp4File, result, Tags.COVER_CROPPED)
                }

                return result
            }

            return null
        }
    }

    /**
     * Supprime les données base64 insérées (si présentes).
     */
    override suspend fun removeBothEmbeddedBase64(mp4File: File): Boolean {
        return withContext(Dispatchers.IO) {
            val savedScale = extractContentScaleFromMp4(mp4File)
            //inutile car plus bas le scale est aussi supprimé de facto
            //removeEmbeddedContentScale(mp4File)

            var result = false
            RandomAccessFile(mp4File, "rw").use { raf ->
                val length = raf.length()
                val lookbackBytes = 65536L * 2
                val seekBack = minOf(lookbackBytes, length)
                raf.seek(length - seekBack)

                val bytes = ByteArray(seekBack.toInt())
                raf.readFully(bytes)

                val tail = String(bytes, Charset.forName(CHARSET))
                //START_BASE64_TAG étant la première des 2 images, ont enlève les 2 d'un coup
                val startIndex = tail.indexOf(Tags.COVER.start)

                if (startIndex != -1) {
                    val absoluteStart = length - seekBack + startIndex
                    raf.setLength(absoluteStart)
                    result = true
                } else {
                    result = false
                }
            }

            if (result) {
                if (savedScale != null) {
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

                val scaleAsString = when (contentScale) {
                    ContentScale.Crop -> "Crop"
                    ContentScale.Fit -> "Fit"
                    ContentScale.None -> "None"
                    ContentScale.Inside -> "Inside"
                    ContentScale.FillWidth -> "FillWidth"
                    ContentScale.FillHeight -> "FillHeight"
                    ContentScale.FillBounds -> "FillBounds"
                    else -> "Crop"
                }
                val data = "\n${Tags.SCALE.start}\n$scaleAsString\n${Tags.SCALE.end}\n"
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
            val startIndex = tail.lastIndexOf(Tags.SCALE.start)
            val endIndex = tail.lastIndexOf(Tags.SCALE.end)

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                return contentScaleFromString(
                    tail.substring(startIndex + Tags.SCALE.start.length, endIndex).trim()
                )
            }
            return null
        }
    }

    fun contentScaleFromString(value: String): ContentScale? = when (value) {
        "Crop" -> ContentScale.Crop
        "Fit" -> ContentScale.Fit
        "FillBounds" -> ContentScale.FillBounds
        "FillHeight" -> ContentScale.FillHeight
        "FillWidth" -> ContentScale.FillWidth
        "Inside" -> ContentScale.Inside
        "None" -> ContentScale.None
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
                val startIndex = tail.indexOf(Tags.SCALE.start)

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

sealed class Tags(
    val start: String,
    val end: String
){
    object SCALE: Tags(
        start = "##SIGMA-SCALE-START##",
        end = "##SIGMA-SCALE-END##"
    )

    object COVER: Tags(
        start = "##SIGMA-COVER-START##",
        end = "##SIGMA-COVER-END##"
    )

    object COVER_CROPPED: Tags(
        start = "##SIGMA-COVER_CROPPED-START##",
        end = "##SIGMA-COVER_CROPPED-END##"
    )
}