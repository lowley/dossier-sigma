package lorry.folder.items.dossiersigma.data.disk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.SigmaFile
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import javax.inject.Inject

class DiskRepository @Inject constructor(val datasource: IDiskDataSource) : IDiskRepository{
    
    override suspend fun getInitialFolder() : SigmaFolder {
        return SigmaFolder(
            fullPath = "C:/Users/olivier/Desktop",
            items = List<Item>(80, init = { SigmaFile("/", "fichier ${it}", null)}),
            picture = null
        )
    }

    suspend override fun getFolderItems(folderPath: String): List<Item>{
        return withContext(Dispatchers.IO){
            return@withContext datasource.getFolderContent(folderPath).map { itemDTO ->
                if (itemDTO.isFile){
                    SigmaFile(
                        path = itemDTO.name.substringBeforeLast("/"),
                        name = itemDTO.name.substringAfterLast("/"),
                        picture = null
                    ) 
                }
                else SigmaFolder(
                    path = itemDTO.path,
                    name = itemDTO.name, 
                    picture = null, 
                    items = listOf())
            }
        }
    }
}