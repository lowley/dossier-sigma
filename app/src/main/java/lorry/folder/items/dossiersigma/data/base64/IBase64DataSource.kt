package lorry.folder.items.dossiersigma.data.base64

import android.graphics.Bitmap
import java.io.File

interface IBase64DataSource {
    
    suspend fun extractImageFromHtml(html: String): Bitmap?
    
    
    
}