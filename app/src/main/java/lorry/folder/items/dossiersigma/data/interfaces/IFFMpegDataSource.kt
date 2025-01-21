package lorry.folder.items.dossiersigma.data.interfaces

import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback

interface IFFMpegDataSource {
    
    fun execute(arguments: String, sessionCompleteCallback: FFmpegSessionCompleteCallback)
    
    
}