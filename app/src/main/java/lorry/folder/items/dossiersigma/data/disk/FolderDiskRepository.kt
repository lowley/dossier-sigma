package lorry.folder.items.dossiersigma.data.disk

import kotlinx.coroutines.flow.MutableStateFlow
import lorry.folder.items.dossiersigma.domain.Folder

class FolderDiskRepository {
    val currentFolder = MutableStateFlow<Folder>(Folder("", emptyList())) 
    
    
    
    
}