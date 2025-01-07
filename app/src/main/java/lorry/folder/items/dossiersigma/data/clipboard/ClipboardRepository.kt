package lorry.folder.items.dossiersigma.data.clipboard

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import lorry.folder.items.dossiersigma.data.interfaces.IClipboardDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import javax.inject.Inject

class ClipboardRepository @Inject constructor(val datasource: IClipboardDataSource) : IClipboardRepository{

    /**
     * Récupère l'image dans le clipboard
     * @return l'image dans le clipboard ou null si vide
     */
    override fun getImageFromClipboard(context: Context): Bitmap? {
        if (datasource.hasImageInClipboard(context)) 
            return datasource.getImageFromClipboard(context)
        else return null
    }
}