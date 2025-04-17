package lorry.folder.items.dossiersigma.domain.usecases.pictures

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import lorry.folder.items.dossiersigma.domain.usecases.clipboard.PastingPictureUseCase
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class ChangingPictureUseCase @Inject constructor(
    val pastingPictureUseCase: PastingPictureUseCase,
    val diskRepository: IDiskRepository){
    
    fun changeItemWithClipboardPicture(item: Item): Item {
        val result = when (item){
            is SigmaFile -> item.copy(picture = this@ChangingPictureUseCase.pastingPictureUseCase.getImageFromClipboard())
            is SigmaFolder -> item.copy(picture = this@ChangingPictureUseCase.pastingPictureUseCase.getImageFromClipboard())
            else -> return item
        } 
        return result
    }
    
    suspend fun savePictureOfFolder(item: Item){
        diskRepository.saveFolderPictureToHtmlFile(item)
    }
    
    suspend fun isFolderPopulated(item: Item): Boolean {
        if(item.isFile())
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: Item is not a folder")
        
        val itemAsfolder = item as SigmaFolder
        val folder = File(itemAsfolder.fullPath)
       
        if (!withContext(Dispatchers.IO) {  folder.exists() })
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: folder is empty")
        
        return withContext(Dispatchers.IO) {  folder.listFiles()!!.isNotEmpty()}
    }

    suspend fun urlToBitmap(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


}