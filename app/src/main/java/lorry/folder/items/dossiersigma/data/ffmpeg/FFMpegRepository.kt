package lorry.folder.items.dossiersigma.data.ffmpeg

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.interfaces.IFFMpegDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IFFMpegRepository
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

class FFMpegRepository @Inject constructor(
    val ffmpegDatasource: IFFMpegDataSource,
    val diskRepository: IDiskRepository) : IFFMpegRepository {
    
    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun addPictureToMP4Metadata(pictureUrl: String, videoPath: String): Boolean {
        
        val inputFile = File(videoPath)
        if (pictureUrl.isEmpty() 
            || videoPath.isEmpty()
            || !videoPath.endsWith(".mp4")
            || !inputFile.exists())
            return false
        
        var tempPicturePath: String? = null
        withContext(Dispatchers.IO){
            tempPicturePath = diskRepository.saveUrlToTempFile(pictureUrl)
        }
        
        if (tempPicturePath.isNullOrEmpty())
            return false

        withContext(Dispatchers.IO) {
            ffmpegDatasource.executeAsync(
                "-i \"$videoPath\" -i  \"$tempPicturePath\" -map 0 -map 1 -c copy -disposition:v:1 attached_pic \"$videoPath\"",
                null
            )
        }
        
        val tempPictureFile = File(tempPicturePath)
        tempPictureFile.delete()
        
        return true
    }
}