package lorry.folder.items.dossiersigma.data.ffmpeg

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lorry.folder.items.dossiersigma.data.interfaces.IFFMpegDataSource
import javax.inject.Inject

class FFMpegDataSource @Inject constructor() : IFFMpegDataSource {
    
    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(arguments: String, sessionCompleteCallback: FFmpegSessionCompleteCallback) {
        GlobalScope.launch{
            FFmpegKit.executeAsync(arguments, sessionCompleteCallback)
        }
    }
}