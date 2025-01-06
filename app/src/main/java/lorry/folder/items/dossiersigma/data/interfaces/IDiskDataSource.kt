package lorry.folder.items.dossiersigma.data.interfaces

interface IDiskDataSource {
    suspend fun getFolderContent(folderPath: String): List<ItemDTO>
    
    
    
}