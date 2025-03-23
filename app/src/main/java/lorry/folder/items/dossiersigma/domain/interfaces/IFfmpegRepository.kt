package lorry.folder.items.dossiersigma.domain.interfaces

import android.graphics.Bitmap

interface IFfmpegRepository {
    
    suspend fun getMp4Cover(filePath: String): Bitmap?
    
    
}