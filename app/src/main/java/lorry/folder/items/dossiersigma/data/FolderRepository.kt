package lorry.folder.items.dossiersigma.data

import kotlinx.coroutines.flow.MutableStateFlow
import lorry.folder.items.dossiersigma.domain.Folder

class FolderRepository {
    val currentFolder = MutableStateFlow<Folder>(Folder("", emptyList())) 
    
    
    
    
}