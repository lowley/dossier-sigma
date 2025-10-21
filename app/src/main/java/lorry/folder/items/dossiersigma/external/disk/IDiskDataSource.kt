package lorry.folder.items.dossiersigma.external.disk

import lorry.folder.items.dossiersigma.headless.domain.ItemDTO
import lorry.folder.items.dossiersigma.headless.domain.SigmaPath
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface IDiskDataSource {
    suspend fun getFolderContent(folderPath: SigmaPath): List<ItemDTO>

    //* retourne paire d'instants: dernière modification du dossier, et max des fichiers
    @OptIn(ExperimentalTime::class)
    suspend fun getFolderLiteContent(folderPath: SigmaPath): Triple<Instant, Instant, Instant>

}