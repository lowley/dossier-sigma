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
import java.io.InputStream


class ClipboardDataSource @Inject constructor() : IClipboardDataSource {
    override fun hasImageInClipboard(context: Context): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

//        if (clip != null && clip.itemCount > 0)
//        {
//            val item = clip.getItemAt(0);
//            val uri = item.uri;
//
//            if (uri != null)
//            {
//                var mimeType = context.contentResolver.getType(uri);
//                return mimeType?.startsWith("image/")!!;
//            }
//        }
        
        
        if (clip != null && clip.itemCount > 0) {
            val description = clipboard.primaryClipDescription
            // Vérifiez si le contenu est une URI
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
                    var contentResolver = context.contentResolver

//                    val intent = Intent(Intent.ACTION_VIEW).apply {
//                        data = uri
//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//                    }
//                    try {
//                        startActivity(context, intent, null)
//                    } catch (e: Exception) {
//                        Log.e("Error", "Impossible d'ouvrir l'image : ${e.message}")
//                    }

//                    contentResolver.takePersistableUriPermission(
//                        uri,
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    )
                    
                    inputStream = contentResolver.openInputStream(uri)
                    return BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                finally {
                    inputStream?.close()
                }
            }
        }
        return null
    }


}