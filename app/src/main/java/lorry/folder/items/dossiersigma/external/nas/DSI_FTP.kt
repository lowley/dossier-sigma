package lorry.folder.items.dossiersigma.external.nas

import lorry.folder.items.dossiersigma.basics.domain.SigmaFile
import lorry.folder.items.dossiersigma.basics.domain.SigmaFolder
import lorry.folder.items.dossiersigma.basics.domain.SigmaPath

interface DSI_FTP {
    suspend fun getSigmaFolder(parent: SigmaPath): SigmaFolder?
    suspend fun fetchFiles(parent: SigmaPath): List<SigmaFile>?
    suspend fun fetchDirectories(parent: SigmaPath): List<String>?

    suspend fun copy(
        localFilePath: SigmaPath,
        pathOnNAS: SigmaPath,
        progressCallback: suspend (Int) -> Unit
    ): Boolean
}