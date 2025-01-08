package lorry.folder.items.dossiersigma.domain.interfaces

import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item

interface IDiskRepository {
    
    suspend fun getInitialFolder() : SigmaFolder
    suspend fun getFolderItems(folderPath: String) : List<Item>
    
}