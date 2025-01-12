package lorry.folder.items.dossiersigma.domain.usecases.clipboard

import android.content.Context
import android.graphics.Bitmap
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import javax.inject.Inject

class PastingPictureService @Inject constructor(
    val context: Context,
    val clipboardRepository: IClipboardRepository){
    
    fun getImageFromClipboard() : Bitmap? {
        return clipboardRepository.getImageFromClipboard(context)
    }
}