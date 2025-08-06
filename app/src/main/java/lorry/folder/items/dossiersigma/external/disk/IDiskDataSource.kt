package lorry.folder.items.dossiersigma.external.disk

import lorry.folder.items.dossiersigma.headless.domain.ItemDTO

interface IDiskDataSource {
    suspend fun getFolderContent(folderPath: String): List<ItemDTO>



}