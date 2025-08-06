package lorry.folder.items.dossiersigma.external.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.InputStream
import javax.inject.Inject

class ClipboardDataSource @Inject constructor() : IClipboardDataSource {
    override fun hasImageInClipboard(context: Context): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        
        if (clip != null && clip.itemCount > 0) {
            val description = clipboard.primaryClipDescription
            return (description?.getMimeType(0)?.startsWith("image") == true)
        }
        return false
    }

    override fun getImageFromClipboard(context: Context): Bitmap? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null && clip.itemCount > 0) {
            val uri = clip.getItemAt(0).uri
            if (uri != null) {
                
                var inputStream: InputStream? = null
                try {
                    inputStream = context.contentResolver.openInputStream(uri)
                    return BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    Log.e("Error", "Cause : ${e.message}")
                }
                finally {
                    inputStream?.close()
                }
            }
        }
        return null
    }
}