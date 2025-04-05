package lorry.folder.items.dossiersigma.domain.interfaces

import lorry.folder.items.dossiersigma.domain.SigmaFolder
import lorry.folder.items.dossiersigma.domain.Item
import lorry.folder.items.dossiersigma.ui.ITEMS_ORDERING_STRATEGY
import java.io.File

interface IDiskRepository {
    
    suspend fun getInitialFolder() : SigmaFolder
    suspend fun getFolderItems(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY) : List<Item>
    suspend fun saveUrlToTempFile(fileUrl: String) : String?
    suspend fun getSigmaFolder(folderPath: String, sorting: ITEMS_ORDERING_STRATEGY):
            SigmaFolder

    suspend fun saveFolderPictureToHtmlFile(item: Item)
    suspend fun createShortcut(text: String, fullPathAndName: String)

    suspend fun hasPictureFile(folder: Item): Boolean
}