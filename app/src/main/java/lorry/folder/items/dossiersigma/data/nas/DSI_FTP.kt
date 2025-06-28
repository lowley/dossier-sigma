package lorry.folder.items.dossiersigma.data.nas

import lorry.folder.items.copieurtho2.__data.NAS.ThoFile

interface DSI_FTP {

    //remplacement de tous les shortcuts
    suspend fun fetchMP4Files(parent: String): List<ThoFile>?
    suspend fun fetchHtmlFiles(
        parent: String,
        display: (suspend (String) -> Unit)? = null
    ): List<ThoFile>?

    suspend fun delete(fileFullPath: String): Boolean

    //divers
    suspend fun fetchMP4File(parent: String): List<ThoFile>?
    suspend fun fetchDirectories(parent: String): List<String>?

    suspend fun shorten(file: ThoFile, pathOnNAS: String): String?
    suspend fun copy(file: ThoFile, pathOnNAS: String, progressCallback: (Int) -> Unit): Boolean
    suspend fun copy(
        localFilePath: String,
        pathOnNAS: String,
        progressCallback: (Int) -> Unit
    ): Boolean

    suspend fun createShortcut(text: String, pathOnNAS: String): Boolean
    //suspend fun createPath(path: String): Boolean

    //overlay
    suspend fun fetchUniqueLevel2Files(parent: String): List<ThoFile>?
    suspend fun createDirectory(parent: String, child: String): Boolean?

}