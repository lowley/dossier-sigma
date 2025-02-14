package lorry.folder.items.dossiersigma.domain.interfaces

import android.graphics.Bitmap

interface IFFMpegRepository {
    
    suspend fun addPictureToMP4Metadata(pictureUrl: String, filePath: String) : Boolean
    suspend fun getBitmapFromMP4(filePath: String) : Bitmap?
    
}