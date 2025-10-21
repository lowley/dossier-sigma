package lorry.folder.items.dossiersigma.external.base64

import android.graphics.Bitmap
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath

interface IBase64DataSource {
    
    suspend fun extractImageFromHtml(html: SigmaPath): Bitmap?
    suspend fun extractBase64FromHtml(html: SigmaPath): String?
    
    
}