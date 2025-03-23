package lorry.folder.items.dossiersigma.data.ffmpeg

import android.content.Context
import lorry.folder.items.dossiersigma.data.interfaces.IFfmpegDataSource
import java.nio.file.Paths
import javax.inject.Inject

class FfmpegDataSource @Inject constructor(
    val context: Context
) : IFfmpegDataSource {

    override fun getImagePath(videoFullPath: String): String? {

        val tempImageFullDir = Paths.get(context.cacheDir.path, 
            videoFullPath.substringAfterLast("/").substringBeforeLast(".") + ".jpg").toString()
        
        val command = "-i \"${videoFullPath}\" -map 0:v:1 -c copy \"$tempImageFullDir"
        val session = FFmpegKit.execute(command)

        if (session.ReturnCode.isSuccess(session.getReturnCode())) {
            println("ffmpeg: Cover extraite avec succès !")
        } else {
            println("FFmpeg: Erreur lors de l'extraction de la cover.")
        }
        
        return tempImageFullDir
    }


}