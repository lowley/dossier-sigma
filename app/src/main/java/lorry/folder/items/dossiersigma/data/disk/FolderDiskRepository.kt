package lorry.folder.items.dossiersigma.data.disk

import kotlinx.coroutines.flow.MutableStateFlow
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.interfaces.IFolderDiskRepository

class FolderDiskRepository : IFolderDiskRepository{
    val currentFolder = MutableStateFlow<Folder>(Folder("", emptyList())) 
    
    
    
    
}