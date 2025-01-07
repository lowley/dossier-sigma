package lorry.folder.items.dossiersigma.data.interfaces

import android.content.Context
import android.graphics.Bitmap

interface IClipboardDataSource {
    fun hasImageInClipboard(context: Context): Boolean
    fun getImageFromClipboard(context: Context): Bitmap?
}