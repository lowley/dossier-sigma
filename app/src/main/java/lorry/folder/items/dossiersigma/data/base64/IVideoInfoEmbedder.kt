package lorry.folder.items.dossiersigma.data.base64

import android.graphics.Bitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

interface IVideoInfoEmbedder {
    //base64
    suspend fun appendBase64ToMp4(mp4File: File, base64Image: String, tagSuffix: String)
    suspend fun extractBase64FromMp4(
        mp4File: File,
        initialLookbackBytes: Int = 65536,
        maxAttempts: Int = 5,
        tagSuffix: String = "BASE64_CROPPED_TAG"
    ): String?
    suspend fun removeBothEmbeddedBase64(mp4File: File): Boolean
    suspend fun base64ToBitmap(base64Str: String): Bitmap?
    suspend fun bitmapToBase64(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): String
    
    //contentScale
    suspend fun appendContentScaleToMp4(mp4File: File, contentScale: ContentScale)
    suspend fun extractContentScaleFromMp4(mp4File: File): ContentScale?
    suspend fun removeEmbeddedContentScale(mp4File: File): Boolean

    
    
}