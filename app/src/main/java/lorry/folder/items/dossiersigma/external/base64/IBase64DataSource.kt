package lorry.folder.items.dossiersigma.external.base64

import android.graphics.Bitmap

interface IBase64DataSource {
    
    suspend fun extractImageFromHtml(html: String): Bitmap?
    suspend fun extractBase64FromHtml(html: String): String?
    
    
}