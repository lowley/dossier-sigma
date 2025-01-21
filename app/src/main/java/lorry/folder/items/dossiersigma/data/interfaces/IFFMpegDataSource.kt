package lorry.folder.items.dossiersigma.data.interfaces

import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback

interface IFFMpegDataSource {
    
    suspend fun executeAsync(arguments: String, sessionCompleteCallback: FFmpegSessionCompleteCallback?)
    
    
}