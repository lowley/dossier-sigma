package lorry.folder.items.dossiersigma.domain.services.pictures

import kotlinx.coroutines.Dispatchers
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.services.clipboard.PastingPictureService
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

class ChangingPictureService @Inject constructor(
    val pastingPictureService: PastingPictureService,
    val diskRepository: IDiskRepository){
    
    fun changeItemWithClipboardPicture(item: Item): Item {
        val result = when (item){
            is SigmaFile -> item.copy(picture = pastingPictureService.getImageFromClipboard())
            is SigmaFolder -> item.copy(picture = pastingPictureService.getImageFromClipboard())
            else -> return item
        } 
        return result
    }
    
    fun isFolderPopulated(item: Item): Boolean {
        if(item.isFile())
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: Item is not a folder")
        
        val itemAsfolder = item as SigmaFolder
        val folder = File(itemAsfolder.fullPath)
       
        if (!folder.exists())
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: folder is empty")
        
        return folder.listFiles().isNotEmpty()
    }
    
    
}