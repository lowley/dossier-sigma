package lorry.folder.items.dossiersigma.data.bento

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.disk.ITempFileDataSource
import lorry.folder.items.dossiersigma.data.interfaces.IBentoDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IBentoRepository
import java.io.File
import javax.inject.Inject
import lorry.folder.items.dossiersigma.BentoJNI

class BentoRepository @Inject constructor(
    val bentoDatasource: IBentoDataSource,
    val tempFileDataSource: ITempFileDataSource,
    private val context: Context
) : IBentoRepository {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun addPictureToMP4Metadata(pictureUrl: String, videoPath: String): Boolean {

        val inputFile = File(videoPath)
        if (pictureUrl.isEmpty()
            || videoPath.isEmpty()
            || !videoPath.endsWith(".mp4")
            || !inputFile.exists()
        )
            return false

        var tempPicturePath: String? = null
        withContext(Dispatchers.IO) {
            tempPicturePath = tempFileDataSource.saveUrlToTempFile(pictureUrl)
        }

        if (tempPicturePath.isNullOrEmpty())
            return false

        if (!inputFile.exists()) {
            Log.e("Bento4", "Le fichier n'existe pas : $inputFile")
        } else if (!inputFile.canRead()) {
            Log.e("Bento4", "Le fichier ne peut pas être lu : permissions insuffisantes")
        } else {
            Log.d("Bento4", "Le fichier est accessible, appel à Bento4...")
        }
        
        try {
            val videopath2 = "/storage/emulated/0/Download/a.mp4"
            val result = BentoJNI.AddTagCC(videopath2, "COVR:JPEG:$tempPicturePath", 0)
            println("Résultat : $result")
            
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val tempPictureFile = File(tempPicturePath)
            tempPictureFile.delete()
        }

        return false
    }

    override suspend fun getBitmapFromMP4(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.embeddedPicture?.let { byteArray ->
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }
}