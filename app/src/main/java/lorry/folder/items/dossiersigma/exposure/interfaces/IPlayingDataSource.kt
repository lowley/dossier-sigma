package lorry.folder.items.dossiersigma.exposure.interfaces

interface IPlayingDataSource {

    suspend fun playFile(fullPath: String, type: String)
    
    
}