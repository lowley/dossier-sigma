package lorry.folder.items.dossiersigma.external.base64

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath
import javax.inject.Inject

class Base64DataSource @Inject constructor() : IBase64DataSource {

    override suspend fun extractImageFromHtml(html: SigmaPath): Bitmap? {

        val htmlFile = html.toFile()
        if (!withContext(Dispatchers.IO) { htmlFile.exists() }) return null

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,([^"]+)"""")
        val match = regex.find(htmlContent) ?: return null

        val base64Image = match.groupValues[1]
        return try {
            withContext(Dispatchers.Default) {
                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }

        } catch (e: Exception) {
            println("Erreur lors du décodage de l'image : ${e.message}")
            null
        }
    }

    override suspend fun extractBase64FromHtml(html: SigmaPath): String? {

        val htmlFile = html.toFile()
        if (!withContext(Dispatchers.IO) { htmlFile.exists() }) return null

        val htmlContent = withContext(Dispatchers.IO) { htmlFile.readText() }

        // Regex pour trouver le contenu de src="data:image/...;base64,..."
        val regex = Regex("""<img\s+[^>]*src\s*=\s*"data:image/[^;]+;base64,([^"]+)"""")
        val match = regex.find(htmlContent) ?: return null

        val base64Image = match.groupValues[1]
        return base64Image
    }
}
