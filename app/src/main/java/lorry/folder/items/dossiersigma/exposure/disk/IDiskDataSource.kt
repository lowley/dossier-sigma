package lorry.folder.items.dossiersigma.exposure.disk

import lorry.folder.items.dossiersigma.domain.ItemDTO

interface IDiskDataSource {
    suspend fun getFolderContent(folderPath: String): List<ItemDTO>



}