package lorry.folder.items.dossiersigma.domain.services.pictures

import kotlinx.coroutines.Dispatchers
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.services.clipboard.PastingPictureService
import javax.inject.Inject

class ChangingPictureService @Inject constructor(
    val pastingPictureService: PastingPictureService){
    
    fun changeItemWithClipboardPicture(item: Item): Item {
        return item.copy(picture = pastingPictureService.getImageFromClipboard())
    }
    
    suspend fun isFolderPopulated(item: Item): Boolean {
        if(item.isFile)
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: Item is not a folder")
        
        with(Dispatchers.IO){
            
        }
    }
    
    
}