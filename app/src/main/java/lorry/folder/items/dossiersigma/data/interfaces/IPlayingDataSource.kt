package lorry.folder.items.dossiersigma.data.interfaces

interface IPlayingDataSource {

    suspend fun playFile(fullPath: String, type: String)
    
    
}