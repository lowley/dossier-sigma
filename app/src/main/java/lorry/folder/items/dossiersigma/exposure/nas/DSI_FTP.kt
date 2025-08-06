package lorry.folder.items.dossiersigma.exposure.nas

import lorry.folder.items.dossiersigma.domain.SigmaFile

interface DSI_FTP {

    //remplacement de tous les shortcuts
    suspend fun fetchFiles(parent: String): List<SigmaFile>?

    suspend fun fetchDirectories(parent: String): List<String>?

    suspend fun copy(
        localFilePath: String,
        pathOnNAS: String,
        progressCallback: (Int) -> Unit
    ): Boolean

    //suspend fun createPath(path: String): Boolean

}