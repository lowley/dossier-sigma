package lorry.folder.items.dossiersigma.data.clipboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lorry.folder.items.dossiersigma.data.interfaces.IClipboardDataSource
import lorry.folder.items.dossiersigma.data.interfaces.IDiskDataSource
import lorry.folder.items.dossiersigma.domain.Folder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.domain.interfaces.IClipboardRepository
import lorry.folder.items.dossiersigma.domain.interfaces.IDiskRepository
import javax.inject.Inject

class ClipboardRepository @Inject constructor(val datasource: IClipboardDataSource) : IClipboardRepository{
    
    

    
}