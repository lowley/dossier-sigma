package lorry.folder.items.dossiersigma.headless.usecases.pictures

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.external.disk.IDiskRepository
import lorry.folder.items.dossiersigma.basics.domain.Item
import lorry.folder.items.dossiersigma.basics.domain.SigmaFile
import lorry.folder.items.dossiersigma.basics.domain.SigmaFolder
import lorry.folder.items.dossiersigma.headless.usecases.clipboard.PastingPictureUseCase
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
    
    suspend fun savePictureOfFolder(item: Item, onlyCropped: Boolean){
        diskRepository.saveFolderPictureToHtmlFile(item, onlyCropped = onlyCropped)
    }


    suspend fun isFolderPopulated(item: Item): Boolean {
        if(item.isFile())
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: Item is not a folder")
        
        val itemAsfolder = item as SigmaFolder
        val folder = itemAsfolder.fullPath.toFile()
       
        if (!withContext(Dispatchers.IO) {  folder.exists() })
            throw IllegalArgumentException("ChangingPictureService/isFolderPopulated: folder is empty")
        
        return withContext(Dispatchers.IO) {  folder.listFiles()!!.isNotEmpty()}
    }

    suspend fun urlToBitmap(data: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            return@withContext if (data.startsWith("data:image")) {
                // 🟢 C'est une image encodée en base64
                val base64Data = data.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                // 🌐 C'est une URL réseau
                val connection = URL(data).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}