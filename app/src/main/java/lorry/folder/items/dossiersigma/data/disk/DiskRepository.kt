package lorry.folder.items.dossiersigma.data.disk

import kotlinx.coroutines.flow.MutableStateFlow
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository

class DiskRepository : IDiskRepository{
    val currentFolder = MutableStateFlow<Folder>(Folder("", emptyList())) 
    val diskDataSource : IDiskDataSource = DiskDataSource()
    
    
    
}