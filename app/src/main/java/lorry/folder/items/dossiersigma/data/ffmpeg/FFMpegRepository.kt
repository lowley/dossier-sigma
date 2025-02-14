package lorry.folder.items.dossiersigma.data.ffmpeg

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore.Files
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.disk.ITempFileDataSource
import lorry.folder.items.dossiersigma.data.interfaces.IFFMpegDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IFFMpegRepository
import lorry.folder.items.dossiersigma.ui.SigmaViewModel
import org.mp4parser.boxes.iso14496.part12.MetaBox
import org.mp4parser.boxes.iso14496.part12.MovieBox
import org.mp4parser.boxes.iso14496.part12.UserDataBox
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import lorry.folder.items.dossiersigma.BentoJNI
import java.util.HashMap

class FFMpegRepository @Inject constructor(
    val ffmpegDatasource: IFFMpegDataSource,
    val tempFileDataSource: ITempFileDataSource,
    private val context: Context
) : IFFMpegRepository {

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

        try {
            val result = BentoJNI.AddTag_C(videoPath, "key:JPEG:abc", 1)
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