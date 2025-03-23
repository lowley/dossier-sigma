package lorry.folder.items.dossiersigma.data.ffmpeg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import lorry.folder.items.dossiersigma.data.interfaces.IFfmpegDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IFfmpegRepository
import java.io.File
import javax.inject.Inject

class FfmpegReporitory @Inject constructor(
    val ffmpegDS: IFfmpegDataSource
) : IFfmpegRepository{
    
    override suspend fun getMp4Cover(filePath: String): Bitmap? {
        
        var imageFullPath = ffmpegDS.getImagePath(filePath)
        if (imageFullPath.isNullOrEmpty())
            return null
        
        val result = BitmapFactory.decodeFile(imageFullPath)

        File(imageFullPath).delete()
        return result
    }
}