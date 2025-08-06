package lorry.folder.items.dossiersigma.external.playing

interface IPlayingDataSource {

    suspend fun playFile(fullPath: String, type: String)


}