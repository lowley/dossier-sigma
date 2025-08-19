package lorry.folder.items.dossiersigma.external.capsule.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.base64.VideoInfoEmbedder
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


class FileCapsuleManager @Inject constructor(
    private val targetPath: String,
    private val useOld: Boolean = false
) {
    suspend fun save(element: IElementInCapsule, forFolder: Boolean) {

        val eliminate = forFolder && (element is InitialPicture || element is CroppedPicture)

        if (!eliminate) {
            val capsuleIO = if (useOld)
                FileCapsuleIO()
            else FileMetadataManager()

            val target = File(targetPath)
            val existingCapsule = if (target.exists()) {
                capsuleIO.getCapsule(targetPath) ?: CapsuleData()
            } else {
                CapsuleData()
            }

            val updatedCapsule = element.update(existingCapsule)
            capsuleIO.replaceCapsule(targetPath, updatedCapsule)
        }

        //branchement enregistrement en fichier pour dossiers
        val pictureDir = File(targetPath.replaceAfterLast("/", ".sigma"))
        if(!pictureDir.exists())
            pictureDir.mkdirs()

        if (forFolder && element is InitialPicture && element.initialPicture != null){
            val file = File(targetPath.replaceAfterLast("/", ".sigma/initialPicture.webp"))

            FileOutputStream(file).use<FileOutputStream, Unit> { out ->
                (element.initialPicture as Bitmap).compress(
                        Bitmap.CompressFormat.WEBP_LOSSLESS, // or WEBP_LOSSY
                        100,out)
            }
        }

        if (forFolder && element is CroppedPicture && element.croppedPicture != null){
            val file = File(targetPath.replaceAfterLast("/", ".sigma/croppedPicture.webp"))

            FileOutputStream(file).use<FileOutputStream, Unit> { out ->
                (element.croppedPicture as Bitmap).compress(
                    Bitmap.CompressFormat.WEBP_LOSSLESS, // or WEBP_LOSSY
                    100,out)
            }
        }
    }

    suspend fun getCapsule(): CapsuleData {
        val compositeIO = FileMetadataManager()

        val target = File(targetPath)
        val firstMelt = withContext(Dispatchers.IO) {
            if (target.exists()) {
                compositeIO.getCapsule(targetPath) ?: CapsuleData()
            } else
                CapsuleData()
        }

        val folderSuffix = ".folderPicture.html"
        if (!targetPath.endsWith(folderSuffix))
            return firstMelt

        val initialWebp = File(targetPath.replaceAfterLast("/", ".sigma/initialPicture.webp"))
        val secondMeltInitial = try {val inputStream: InputStream = FileInputStream(initialWebp)
            val bufferedInputStream = BufferedInputStream(inputStream)
            val bmp = BitmapFactory.decodeStream(bufferedInputStream)
            if (bmp != null) {
                val videoInfoEmbedder = VideoInfoEmbedder()
                val b64 = videoInfoEmbedder.bitmapToBase64(bmp)
                b64
            }
            else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        val croppedWebp = File(targetPath.replaceAfterLast("/", ".sigma/croppedPicture.webp"))
        val secondMeltCropped = try {
            val inputStream: InputStream = FileInputStream(croppedWebp)
            val bufferedInputStream = BufferedInputStream(inputStream)
            val bmp = BitmapFactory.decodeStream(bufferedInputStream)
            if (bmp != null) {
                val videoInfoEmbedder = VideoInfoEmbedder()
                val b64 = videoInfoEmbedder.bitmapToBase64(bmp)
                b64
            }
            else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        if (secondMeltInitial != null || secondMeltCropped != null) {
            return firstMelt.copy(
                initialPicture = secondMeltInitial,
                croppedPicture = secondMeltCropped

            )
        }

        return firstMelt
    }

    suspend fun <T> getElement(reader: IElementReader<T>): T? {
        return withContext(Dispatchers.IO) {
            reader.fileGet(targetPath)
        }
    }
}