package lorry.folder.items.dossiersigma.domain.services.pictures

import android.graphics.Bitmap
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.services.clipboard.PastingPictureService
import javax.inject.Inject

class ChangingPictureService @Inject constructor(
    val pastingPictureService: PastingPictureService){
    
    fun changeItemWithClipboardPicture(item: Item): Item {
        return item.copy(content = pastingPictureService.getImageFromClipboard())
    }

    
    
}