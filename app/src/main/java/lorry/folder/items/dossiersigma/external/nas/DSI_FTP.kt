package lorry.folder.items.dossiersigma.external.nas

import lorry.folder.items.dossiersigma.headless.domain.SigmaFile
import lorry.folder.items.dossiersigma.headless.domain.SigmaFolder

interface DSI_FTP {
    suspend fun getSigmaFolder(parent: String): SigmaFolder?
    suspend fun fetchFiles(parent: String): List<SigmaFile>?
    suspend fun fetchDirectories(parent: String): List<String>?

    suspend fun copy(
        localFilePath: String,
        pathOnNAS: String,
        progressCallback: suspend (Int) -> Unit
    ): Boolean
}