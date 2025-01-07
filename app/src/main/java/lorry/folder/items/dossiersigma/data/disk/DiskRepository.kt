package lorry.folder.items.dossiersigma.data.disk

import android.graphics.Bitmap
import android.util.Range
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import javax.inject.Inject

class DiskRepository @Inject constructor(val datasource: IDiskDataSource) : IDiskRepository{
    
    override suspend fun getInitialFolder() : Folder {
        return Folder(
            path = "C:/Users/olivier/Desktop",
            items = List<Item>(80, init = { Item("fichier ${it}", true, null)})
        )
    }

    suspend override fun getFolderItems(folderPath: String): List<Item>{
        return withContext(Dispatchers.IO){
            return@withContext datasource.getFolderContent(folderPath).map { itemDTO ->
                Item(
                    name = itemDTO.name,
                    isFile = itemDTO.isFile,
                    content = null
                )
            }
        }
    }

    
}