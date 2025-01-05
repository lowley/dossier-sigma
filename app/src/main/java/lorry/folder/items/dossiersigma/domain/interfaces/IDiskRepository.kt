package lorry.folder.items.dossiersigma.domain.interfaces

import lorry.folder.items.dossiersigma.domain.Folder

interface IDiskRepository {
    
    suspend fun getInitialFolder() : Folder
    
}