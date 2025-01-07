package lorry.folder.items.dossiersigma.data.clipboard

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.SigmaApplication
import lorry.folder.items.dossiersigma.data.interfaces.IClipboardDataSource
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.data.interfaces.ItemDTO
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import java.io.File
import javax.inject.Inject
import android.content.ClipDescription
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat.startActivity
import java.io.FileNotFoundException
import java.io.InputStream

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