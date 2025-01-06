package lorry.folder.items.dossiersigma.domain.interfaces

import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.Item

interface IDiskRepository {
    
    suspend fun getInitialFolder() : Folder
    suspend fun getFolderItems(folderPath: String) : List<Item>
    
}