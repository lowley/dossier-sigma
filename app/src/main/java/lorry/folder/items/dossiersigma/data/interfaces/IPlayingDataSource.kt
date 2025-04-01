package lorry.folder.items.dossiersigma.data.interfaces

interface IPlayingDataSource {

    suspend fun playMP4File(fullPath: String, type: String)
    
    
}