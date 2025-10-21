package lorry.folder.items.dossiersigma.external.playing

import lorry.folder.items.dossiersigma.headless.domain.SigmaPath

interface IPlayingDataSource {

    suspend fun playFile(fullPath: SigmaPath, type: String)


}