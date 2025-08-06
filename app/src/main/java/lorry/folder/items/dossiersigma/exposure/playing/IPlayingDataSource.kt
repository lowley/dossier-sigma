package lorry.folder.items.dossiersigma.exposure.playing

interface IPlayingDataSource {

    suspend fun playFile(fullPath: String, type: String)


}