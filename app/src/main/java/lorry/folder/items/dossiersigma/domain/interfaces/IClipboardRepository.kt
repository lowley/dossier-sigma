package lorry.folder.items.dossiersigma.domain.interfaces

import android.content.Context
import android.graphics.Bitmap

interface IClipboardRepository {
    fun getImageFromClipboard(context: Context): Bitmap?
    fun hasImageInClipboard(context: Context): Boolean
}