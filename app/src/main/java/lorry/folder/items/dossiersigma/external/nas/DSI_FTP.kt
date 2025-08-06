package lorry.folder.items.dossiersigma.external.nas

import lorry.folder.items.dossiersigma.headless.domain.SigmaFile

interface DSI_FTP {
    suspend fun fetchFiles(parent: String): List<SigmaFile>?

    suspend fun fetchDirectories(parent: String): List<String>?

    suspend fun copy(
        localFilePath: String,
        pathOnNAS: String,
        progressCallback: (Int) -> Unit
    ): Boolean
}