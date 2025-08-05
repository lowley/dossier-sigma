package lorry.folder.items.dossiersigma.exposure.interfaces

interface IDiskDataSource {
    suspend fun getFolderContent(folderPath: String): List<ItemDTO>
    
    
    
}