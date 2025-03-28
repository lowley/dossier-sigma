package lorry.folder.items.dossiersigma.data.playing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import lorry.folder.items.dossiersigma.data.interfaces.IPlayingDataSource
import java.io.File
import javax.inject.Inject

class PlayingDataSource @Inject constructor(
    val context: Context
) : IPlayingDataSource {
    
    override suspend fun playMP4File(fullPath: String) {
        val file = File(fullPath)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // définie dans ton manifeste
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent) 
        
        
        
        
    }


}