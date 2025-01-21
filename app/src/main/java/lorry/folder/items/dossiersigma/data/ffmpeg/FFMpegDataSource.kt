package lorry.folder.items.dossiersigma.data.ffmpeg

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.interfaces.IFFMpegDataSource
import javax.inject.Inject

class FFMpegDataSource @Inject constructor() : IFFMpegDataSource {
    
    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun executeAsync(arguments: String, sessionCompleteCallback: FFmpegSessionCompleteCallback?) {
        withContext(Dispatchers.IO){
            FFmpegKit.executeAsync(arguments, sessionCompleteCallback)
        }
    }
}