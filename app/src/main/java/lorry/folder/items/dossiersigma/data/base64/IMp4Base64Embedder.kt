package lorry.folder.items.dossiersigma.data.base64

import android.graphics.Bitmap
import java.io.File

interface IMp4Base64Embedder {
    suspend fun appendBase64ToMp4(mp4File: File, base64Image: String)
    suspend fun extractBase64FromMp4(mp4File: File, initialLookbackBytes: Int = 65536, maxAttempts: Int = 5): String?
    suspend fun removeEmbeddedBase64(mp4File: File): Boolean
    suspend fun base64ToBitmap(base64Str: String): Bitmap?
    suspend fun bitmapToBase64(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): String
}